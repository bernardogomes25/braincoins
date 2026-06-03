package com.lab3.moeda.dto.response;

import java.util.List;

public record AlunoDisponivelResponseDTO(
        int alunoId,
        String nome,
        String curso,
        List<ResgateResumoDTO> resgatesAtivos
) {}
