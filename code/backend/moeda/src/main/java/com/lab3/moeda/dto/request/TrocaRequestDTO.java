package com.lab3.moeda.dto.request;

import jakarta.validation.constraints.Positive;

public record TrocaRequestDTO(
        @Positive int solicitanteId,
        @Positive int destinatarioId,
        @Positive int resgateOferecidoId,
        @Positive int resgateDesejadoId
) {}
