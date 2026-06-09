# 🐰 Plano de Implementação: Integração RabbitMQ nas Trocas entre Alunos

## 📋 Contexto

O sistema de trocas (TrocaEntity) atualmente funciona de forma **síncrona**: quando um aluno aceita uma troca, o `TrocaService.aceitar()` executa toda a lógica de transferência de titularidade dos resgates e dispara e-mails na mesma thread da request HTTP.

**Objetivo**: Introduzir uma fila **RabbitMQ** no fluxo de **aceitação de troca**, desacoplando a intenção de aceitar (request HTTP) do processamento efetivo (transferência de `aluno_id` nos resgates + envio de e-mails).

O padrão seguido é **idêntico** ao exemplo fornecido em `/home/bernardo/braincoins/RabbitMQ/`:
- Fila direta
- `GsonMessageConverter` para serialização JSON
- Producer via `RabbitTemplate`
- Consumer via `@RabbitListener`

---

## 🔄 Fluxo Proposto

```
PATCH /trocas/{id}/aceitar (HTTP Request)
        │
        ▼
TrocaService.aceitar()
  ├── ✓ valida status PENDENTE
  ├── ✓ seta status → PROCESSANDO
  └── ✓ publica TrocaAceitaEventDTO → fila.aceite.troca
                                              │
                                              ▼ (Assíncrono)
                                    TrocaConsumerService
                                      (@RabbitListener)
                                      ├── ✓ transfere aluno_id (resgates)
                                      ├── ✓ seta status → ACEITA
                                      └── ✓ envia e-mails async
```

---

## 📝 Resumo das Mudanças

| Tipo | Arquivo | Ação |
|------|---------|------|
| **Dependência** | `pom.xml` | Adicionar `spring-boot-starter-amqp` + `gson` |
| **Config** | `application.properties` | Adicionar credenciais RabbitMQ |
| **Infra** | `docker-compose.yml` | Adicionar serviço `rabbitmq:3-management` |
| **Novo (Config)** | `config/GsonMessageConverter.java` | Copiar do exemplo |
| **Novo (Config)** | `config/RabbitConfig.java` | Definir fila + beans |
| **Novo (DTO)** | `dto/TrocaAceitaEventDTO.java` | DTO com `trocaId` |
| **Novo (Service)** | `service/TrocaConsumerService.java` | Lógica de processamento |
| **Modificado (Enum)** | `model/StatusTroca.java` | Adicionar `PROCESSANDO` |
| **Modificado (Service)** | `service/TrocaService.aceitar()` | Publicar evento ao invés de processar |

---

## ✏️ Detalhes de Implementação

### 1️⃣ Dependências — `code/backend/moeda/pom.xml`

Adicionar dentro de `<dependencies>`:

```xml
<!-- RabbitMQ -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>

<!-- JSON Serialization (igual ao exemplo) -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
</dependency>
```

---

### 2️⃣ Configuração — `code/backend/moeda/src/main/resources/application.properties`

Adicionar:

```properties
# RabbitMQ Connection
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

---

### 3️⃣ Docker Compose — `docker-compose.yml`

Adicionar o serviço RabbitMQ (com Management UI):

```yaml
rabbitmq:
  image: rabbitmq:3-management
  ports:
    - "5672:5672"        # AMQP port
    - "15672:15672"      # Management UI: http://localhost:15672
  environment:
    RABBITMQ_DEFAULT_USER: guest
    RABBITMQ_DEFAULT_PASS: guest
  healthcheck:
    test: rabbitmq-diagnostics -q ping
    interval: 30s
    timeout: 10s
    retries: 5
```

---

### 4️⃣ Config — `config/GsonMessageConverter.java`

📍 `code/backend/moeda/src/main/java/com/lab3/moeda/config/GsonMessageConverter.java`

**Cópia direta** do exemplo (`/RabbitMQ/src/main/java/com/example/RabbitMQ/config/GsonMessageConverter.java`).

Responsabilidades:
- Serializar objetos Java → JSON (usando Gson)
- Deserializar JSON → objetos Java (usando o header `__TypeId__`)
- Gerenciar metadados de tipo nas mensagens RabbitMQ

---

### 5️⃣ Config — `config/RabbitConfig.java`

📍 `code/backend/moeda/src/main/java/com/lab3/moeda/config/RabbitConfig.java`

Adaptado do exemplo. Template:

```java
package com.lab3.moeda.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String FILA_ACEITE_TROCA = "fila.aceite.troca";

    @Bean
    public Queue filaAceiteTroca() {
        return new Queue(FILA_ACEITE_TROCA, true);  // durable = true
    }

    @Bean
    public MessageConverter messageConverter() {
        return new GsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
```

---

### 6️⃣ DTO — `dto/TrocaAceitaEventDTO.java`

📍 `code/backend/moeda/src/main/java/com/lab3/moeda/dto/TrocaAceitaEventDTO.java`

DTO simples (sem Lombok, como o exemplo):

```java
package com.lab3.moeda.dto;

public class TrocaAceitaEventDTO {
    private int trocaId;

    // Construtor vazio (necessário para Gson)
    public TrocaAceitaEventDTO() {
    }

    public TrocaAceitaEventDTO(int trocaId) {
        this.trocaId = trocaId;
    }

    public int getTrocaId() {
        return trocaId;
    }

    public void setTrocaId(int trocaId) {
        this.trocaId = trocaId;
    }
}
```

---

### 7️⃣ Service — `service/TrocaConsumerService.java`

📍 `code/backend/moeda/src/main/java/com/lab3/moeda/service/TrocaConsumerService.java`

Novo serviço com `@RabbitListener`. Processa a aceitação de troca de forma **assíncrona**:

```java
package com.lab3.moeda.service;

import com.lab3.moeda.config.RabbitConfig;
import com.lab3.moeda.dto.TrocaAceitaEventDTO;
import com.lab3.moeda.model.ResgateEntity;
import com.lab3.moeda.model.TrocaEntity;
import com.lab3.moeda.model.StatusTroca;
import com.lab3.moeda.repository.TrocaRepository;
import com.lab3.moeda.repository.ResgateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrocaConsumerService {

    private final TrocaRepository trocaRepository;
    private final ResgateRepository resgateRepository;
    private final EmailService emailService;

    @RabbitListener(queues = RabbitConfig.FILA_ACEITE_TROCA)
    @Transactional
    public void processarAceitacaoTroca(TrocaAceitaEventDTO event) {
        try {
            log.info("Processando aceitação de troca: {}", event.getTrocaId());

            // Busca a troca
            TrocaEntity troca = trocaRepository.findById(event.getTrocaId())
                .orElseThrow(() -> new IllegalArgumentException("Troca não encontrada"));

            // Re-valida status PROCESSANDO
            if (troca.getStatus() != StatusTroca.PROCESSANDO) {
                throw new IllegalStateException("Troca não está em estado PROCESSANDO");
            }

            // Transfere posse dos resgates
            ResgateEntity resgateOferecido = troca.getResgateOferecido();
            ResgateEntity resgateDesejado = troca.getResgateDesejado();

            resgateOferecido.setAluno(troca.getAlunoDestinatario());
            resgateDesejado.setAluno(troca.getAlunoSolicitante());

            resgateRepository.save(resgateOferecido);
            resgateRepository.save(resgateDesejado);

            // Atualiza status da troca
            troca.setStatus(StatusTroca.ACEITA);
            trocaRepository.save(troca);

            // Envia e-mails assincronamente
            emailService.enviarEmailAssincrono(
                "Troca aceita!",
                "Sua troca foi aceita.",
                troca.getAlunoSolicitante().getEmail()
            );

            emailService.enviarEmailAssincrono(
                "Troca aceita!",
                "Você aceitou uma troca.",
                troca.getAlunoDestinatario().getEmail()
            );

            log.info("Troca {} processada com sucesso", event.getTrocaId());
        } catch (Exception e) {
            log.error("Erro ao processar aceitação de troca: {}", event.getTrocaId(), e);
            throw e;  // Requeue the message on failure
        }
    }
}
```

---

### 8️⃣ Enum — `model/StatusTroca.java`

📍 Adicionar o estado `PROCESSANDO` ao enum (localizar dentro de `TrocaEntity` ou arquivo separado):

**Antes:**
```java
PENDENTE, ACEITA, RECUSADA, EXPIRADA, CANCELADA
```

**Depois:**
```java
PENDENTE, PROCESSANDO, ACEITA, RECUSADA, EXPIRADA, CANCELADA
```

---

### 9️⃣ Service — `service/TrocaService.aceitar()`

📍 `code/backend/moeda/src/main/java/com/lab3/moeda/service/TrocaService.java`

Modificar apenas o método `aceitar()`:

**Antes** (síncrono):
```java
public void aceitar(int trocaId) {
    TrocaEntity troca = trocaRepository.findById(trocaId).orElseThrow();
    
    // ... validações ...
    
    // Transfere posse (toda lógica aqui)
    resgateOferecido.setAluno(destinatario);
    resgateDesejado.setAluno(solicitante);
    // ... envio de e-mails ...
}
```

**Depois** (assíncrono via RabbitMQ):
```java
public void aceitar(int trocaId) {
    TrocaEntity troca = trocaRepository.findById(trocaId).orElseThrow();
    
    // Valida status PENDENTE
    if (troca.getStatus() != StatusTroca.PENDENTE) {
        throw new IllegalStateException("Troca não está PENDENTE");
    }
    
    // Muda status para PROCESSANDO
    troca.setStatus(StatusTroca.PROCESSANDO);
    trocaRepository.save(troca);
    
    // Publica evento na fila
    TrocaAceitaEventDTO event = new TrocaAceitaEventDTO(trocaId);
    rabbitTemplate.convertAndSend(RabbitConfig.FILA_ACEITE_TROCA, event);
}
```

**Importante**: Injetar `RabbitTemplate` via construtor usando `@RequiredArgsConstructor` (já em uso no projeto):

```java
@Service
@RequiredArgsConstructor
@Transactional
public class TrocaService {
    private final TrocaRepository trocaRepository;
    private final ResgateRepository resgateRepository;
    private final EmailService emailService;
    private final RabbitTemplate rabbitTemplate;  // ← Adicionar
    
    // ... resto da classe ...
}
```

---

## ✅ Verificação End-to-End

### Passo 1: Iniciar Infraestrutura

```bash
# Terminal 1 - Start RabbitMQ + PostgreSQL
cd /home/bernardo/braincoins
docker-compose up -d
```

Aguardar RabbitMQ estar pronto (healthcheck):
```bash
docker-compose logs rabbitmq
```

### Passo 2: Iniciar Backend

```bash
# Terminal 2 - Start Spring Boot
cd /home/bernardo/braincoins/code/backend/moeda
./mvnw clean install
./mvnw spring-boot:run -Dmaven.test.skip=true
```

Aguardar log: `Started moeda in XX seconds`

### Passo 3: Verificar Fila no RabbitMQ

Abrir no navegador: **http://localhost:15672**
- Login: `guest` / `guest`
- Ir para **Queues** tab
- Confirmar que existe fila `fila.aceite.troca` (durable)

### Passo 4: Teste Manual via Postman

Usar collection: `BrainCoins_API.postman_collection.json`

1. **Criar Aluno A** — `POST /alunos`
   ```json
   {
     "nome": "Aluno A",
     "email": "alunoa@example.com",
     "cpf": "12345678901",
     "curso": "Engenharia",
     "saldo": 1000
   }
   ```
   → Guardar `idAlunoA`

2. **Criar Aluno B** — `POST /alunos`
   ```json
   {
     "nome": "Aluno B",
     "email": "alunob@example.com",
     "cpf": "98765432109",
     "curso": "Medicina",
     "saldo": 1000
   }
   ```
   → Guardar `idAlunoB`

3. **Criar Empresa** — `POST /empresas`
   ```json
   {
     "nome": "Empresa X",
     "email": "empresa@example.com",
     "cnpj": "12345678000190"
   }
   ```
   → Guardar `idEmpresa`

4. **Criar Vantagem** — `POST /vantagens`
   ```json
   {
     "empresaId": <idEmpresa>,
     "nome": "Desconto 20%",
     "descricao": "Desconto em compras",
     "custo": 100,
     "estoque": 10
   }
   ```
   → Guardar `idVantagem`

5. **Resgatar Vantagem (Aluno A)** — `POST /resgates`
   ```json
   {
     "alunoId": <idAlunoA>,
     "vantagemId": <idVantagem>
   }
   ```
   → Guardar `idResgateA` + anote cupom

6. **Resgatar Vantagem (Aluno B)** — `POST /resgates`
   ```json
   {
     "alunoId": <idAlunoB>,
     "vantagemId": <idVantagem>
   }
   ```
   → Guardar `idResgateB` + anote cupom

7. **Criar Troca** — `POST /trocas`
   ```json
   {
     "alunoSolicitanteId": <idAlunoA>,
     "alunoDestinatarioId": <idAlunoB>,
     "resgateOferecidoId": <idResgateA>,
     "resgateDesejadoId": <idResgateB>
   }
   ```
   → Guardar `idTroca`
   → Resposta: status = `PENDENTE`

8. **Verificar Fila** — RabbitMQ dashboard
   - Message count em `fila.aceite.troca` = 0 (ainda não foi aceita)

9. **Aceitar Troca** — `PATCH /trocas/<idTroca>/aceitar`
   - Resposta: status = `PROCESSANDO` ✓
   - **Importante**: Resposta retorna imediatamente, processamento é assíncrono

10. **Verificar Fila** — RabbitMQ dashboard
    - Message count em `fila.aceite.troca` deve ir de 1 → 0 em ~100ms

11. **Aguardar Processamento**
    - Esperar ~1 segundo (tempo para consumer processar)

12. **Verificar Status da Troca** — `GET /trocas/<idTroca>`
    - Status deve ser `ACEITA` ✓

13. **Verificar Resgates Trocados**
    - `GET /resgates/<idResgateA>`
      - `aluno_id` deve ser `<idAlunoB>` ✓
    - `GET /resgates/<idResgateB>`
      - `aluno_id` deve ser `<idAlunoA>` ✓

14. **Verificar E-mails** (se SMTP configurado)
    - Aluno A deve receber: "Sua troca foi aceita"
    - Aluno B deve receber: "Você aceitou uma troca"

---

## 🎯 Comportamento Esperado

| Etapa | Antes | Depois |
|-------|-------|--------|
| `PATCH /trocas/{id}/aceitar` enviado | Aguarda 2-3s | Retorna em 50-100ms |
| Status da troca imediatamente após | `ACEITA` | `PROCESSANDO` |
| Resgates transferidos | Sim (síncrono) | Sim (assíncrono, ~100ms depois) |
| E-mails enviados | Sim (síncrono) | Sim (assíncrono, via `@Async`) |
| Fila RabbitMQ | N/A | Mensagem visível por ~100ms |

---

## 📊 Diagrama de Sequência

```
Aluno B                    HTTP API           RabbitMQ            Consumer
   │                          │                   │                  │
   ├──── PATCH /aceitar ─────>│                   │                  │
   │                          │                   │                  │
   │                          ├─ Seta PROCESSANDO │                  │
   │                          │                   │                  │
   │                          ├─ Publica Evento ─>│                  │
   │<──── 202 Accepted ───────┤                   │                  │
   │     (status=PROCESSANDO) │                   │                  │
   │                          │                   │                  │
   │                          │                   ├─ Consome msg ───>│
   │                          │                   │                  │
   │                          │                   │                  ├─ Transf. aluno_id
   │                          │                   │                  │
   │                          │                   │                  ├─ Seta ACEITA
   │                          │                   │                  │
   │                          │                   │                  ├─ Envia e-mails
   │                          │                   │                  │
   │    GET /trocas/... ────>│                   │                  │
   │<──── status=ACEITA ──────┤                   │                  │
   │                          │                   │                  │
```

---

## ⚠️ Observações Importantes

1. **Mudança de Comportamento**: A resposta do endpoint `PATCH /trocas/{id}/aceitar` agora é **imediata** (202 Accepted com status PROCESSANDO), não aguardando a conclusão do processamento.

2. **Integridade**: Se o consumer falhar, a mensagem será **re-enfileirada** automaticamente (retry padrão do RabbitMQ).

3. **Email**: O envio de e-mails continua assíncrono (via `@Async`), ocorrendo dentro do consumer.

4. **Tests Atualizados**: Os testes do `TrocaService` que chamavam `aceitar()` e esperavam a transferência imediata precisarão ser ajustados para validar apenas a mudança para `PROCESSANDO`.

---

## 📦 Arquivos da Implementação

- ✅ Criar `config/GsonMessageConverter.java` (copiar do exemplo)
- ✅ Criar `config/RabbitConfig.java` (novo, baseado no exemplo)
- ✅ Criar `dto/TrocaAceitaEventDTO.java` (novo, simples)
- ✅ Criar `service/TrocaConsumerService.java` (novo, lógica de processamento)
- ✅ Modificar `model/StatusTroca.java` (adicionar `PROCESSANDO`)
- ✅ Modificar `service/TrocaService.aceitar()` (publicar evento)
- ✅ Modificar `pom.xml` (adicionar dependências)
- ✅ Modificar `application.properties` (configurar RabbitMQ)
- ✅ Modificar `docker-compose.yml` (adicionar RabbitMQ)

---

**Status**: ✏️ Pronto para implementação
