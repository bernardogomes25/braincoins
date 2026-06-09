package com.lab3.moeda.service;

import com.lab3.moeda.config.RabbitConfig;
import com.lab3.moeda.dto.TrocaAceitaEventDTO;
import com.lab3.moeda.model.ResgateEntity;
import com.lab3.moeda.model.StatusTroca;
import com.lab3.moeda.model.TrocaEntity;
import com.lab3.moeda.repository.ResgateRepository;
import com.lab3.moeda.repository.TrocaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrocaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(TrocaConsumerService.class);

    private final TrocaRepository trocaRepository;
    private final ResgateRepository resgateRepository;
    private final EmailService emailService;

    public TrocaConsumerService(TrocaRepository trocaRepository,
                                ResgateRepository resgateRepository,
                                EmailService emailService) {
        this.trocaRepository = trocaRepository;
        this.resgateRepository = resgateRepository;
        this.emailService = emailService;
    }

    @RabbitListener(queues = RabbitConfig.FILA_ACEITE_TROCA)
    @Transactional
    public void processarAceitacaoTroca(TrocaAceitaEventDTO event) {
        try {
            log.info("Processando aceitação de troca: {}", event.getTrocaId());

            TrocaEntity troca = trocaRepository.findById(event.getTrocaId())
                    .orElseThrow(() -> new IllegalArgumentException("Troca não encontrada: " + event.getTrocaId()));

            if (troca.getStatus() != StatusTroca.PROCESSANDO) {
                throw new IllegalStateException("Troca não está em estado PROCESSANDO: " + event.getTrocaId());
            }

            ResgateEntity resgateOferecido = troca.getResgateOferecido();
            ResgateEntity resgateDesejado = troca.getResgateDesejado();

            resgateOferecido.setAluno(troca.getAlunoDestinatario());
            resgateDesejado.setAluno(troca.getAlunoSolicitante());

            resgateRepository.save(resgateOferecido);
            resgateRepository.save(resgateDesejado);

            troca.setStatus(StatusTroca.ACEITA);
            trocaRepository.save(troca);

            enviarEmailAceite(troca);

            log.info("Troca {} processada com sucesso", event.getTrocaId());
        } catch (Exception e) {
            log.error("Erro ao processar aceitação de troca {}: {}", event.getTrocaId(), e.getMessage(), e);
            throw e;
        }
    }

    private void enviarEmailAceite(TrocaEntity troca) {
        try {
            String assunto = "✅ Troca aceita — BrainCoins";

            String corpoPara = """
                    Olá %s,

                    Sua solicitação de troca foi aceita por %s!

                    Você recebeu: %s (%s)
                    Você enviou: %s (%s)

                    ---
                    BrainCoins - Sistema de Moeda Estudantil
                    """.formatted(
                    troca.getAlunoSolicitante().getNome(),
                    troca.getAlunoDestinatario().getNome(),
                    troca.getResgateDesejado().getVantagem().getNome(),
                    troca.getResgateDesejado().getVantagem().getEmpresa().getNome(),
                    troca.getResgateOferecido().getVantagem().getNome(),
                    troca.getResgateOferecido().getVantagem().getEmpresa().getNome()
            );
            emailService.enviarEmailAssincrono(troca.getAlunoSolicitante().getEmail(), assunto, corpoPara);

            String corpoAceitante = """
                    Olá %s,

                    Você aceitou a troca com %s.

                    Você recebeu: %s (%s)
                    Você enviou: %s (%s)

                    ---
                    BrainCoins - Sistema de Moeda Estudantil
                    """.formatted(
                    troca.getAlunoDestinatario().getNome(),
                    troca.getAlunoSolicitante().getNome(),
                    troca.getResgateOferecido().getVantagem().getNome(),
                    troca.getResgateOferecido().getVantagem().getEmpresa().getNome(),
                    troca.getResgateDesejado().getVantagem().getNome(),
                    troca.getResgateDesejado().getVantagem().getEmpresa().getNome()
            );
            emailService.enviarEmailAssincrono(troca.getAlunoDestinatario().getEmail(), assunto, corpoAceitante);
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail de aceite de troca {}: {}", troca.getId(), e.getMessage(), e);
        }
    }
}
