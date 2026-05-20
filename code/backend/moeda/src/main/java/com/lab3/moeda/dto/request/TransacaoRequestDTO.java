package com.lab3.moeda.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TransacaoRequestDTO(
        int professorId,
        int alunoId,
        @Positive(message = "Valor deve ser maior que zero") short valor,
        @NotBlank(message = "Motivo é obrigatório") @Size(min = 3, max = 500, message = "Motivo deve ter entre 3 e 500 caracteres") String motivo
) {}
