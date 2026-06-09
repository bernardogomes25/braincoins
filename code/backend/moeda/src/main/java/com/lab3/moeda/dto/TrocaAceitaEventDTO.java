package com.lab3.moeda.dto;

public class TrocaAceitaEventDTO {

    private int trocaId;

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
