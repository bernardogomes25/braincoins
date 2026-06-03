package com.lab3.moeda.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusTroca {
    PENDENTE, ACEITA, RECUSADA, EXPIRADA, CANCELADA;

    @JsonValue
    public String getValor() {
        return name().toLowerCase();
    }
}
