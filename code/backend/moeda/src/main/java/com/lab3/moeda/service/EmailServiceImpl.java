package com.lab3.moeda.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String emailRemetente;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
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

    private void enviarEmail(String para, String assunto, String corpo) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setFrom(emailRemetente);
        helper.setTo(para);
        helper.setSubject(assunto);
        helper.setText(corpo, true);
        mailSender.send(message);
        log.info("[EMAIL] Enviado para: {} | Assunto: {}", para, assunto);
    }
}
