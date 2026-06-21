package com.lab3.moeda.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final RestClient restClient;

    @Value("${mail.api.key}")
    private String apiKey;

    @Value("${app.mail.from}")
    private String emailRemetente;

    public EmailServiceImpl(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Async
    @Override
    public void enviarEmailAssincrono(String para, String assunto, String corpo) {
        try {
            enviarEmail(para, assunto, corpo);
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail assincronamente para {}: {}", para, e.getMessage(), e);
        }
    }

    @Override
    public void enviarEmailSincrono(String para, String assunto, String corpo) {
        try {
            enviarEmail(para, assunto, corpo);
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail sincronamente para {}: {}", para, e.getMessage(), e);
            throw new RuntimeException("Falha ao enviar e-mail", e);
        }
    }

    private void enviarEmail(String para, String assunto, String corpo) {
        Map<String, Object> body = Map.of(
            "from", emailRemetente,
            "to", List.of(para),
            "subject", assunto,
            "html", corpo
        );

        restClient.post()
            .uri(RESEND_API_URL)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .toBodilessEntity();

        log.info("[EMAIL] Enviado para: {} | Assunto: {}", para, assunto);
    }
}
