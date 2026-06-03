package com.lab3.moeda.dto.response;

public record TrocaResponseDTO(
        int id,
        int solicitanteId,
        String solicitanteNome,
        int destinatarioId,
        String destinatarioNome,
        int resgateOferecidoId,
        String vantagemOferecidaNome,
        String empresaOferecidaNome,
        int resgateDesejadoId,
        String vantagemDesejadaNome,
        String empresaDesejadaNome,
        String data,
        String expiraEm,
        String status
) {}
