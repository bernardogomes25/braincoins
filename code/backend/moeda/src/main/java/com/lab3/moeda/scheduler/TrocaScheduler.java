package com.lab3.moeda.scheduler;

import com.lab3.moeda.service.TrocaService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TrocaScheduler {

    private final TrocaService trocaService;

    public TrocaScheduler(TrocaService trocaService) {
        this.trocaService = trocaService;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void expirarTrocasPendentes() {
        System.out.println("[Scheduler] Iniciando verificação de trocas expiradas...");
        trocaService.expirarPendentes();
    }
}
