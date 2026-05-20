package com.lab3.moeda.dto.request;

public record AlunoRequestDTO(
        String nome,
        String cpf,
        String rg,
        String endereco,
        Integer instituicaoId,
        String curso,
        String email,
        String senha
) {}
