package com.lab3.moeda.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trocas")
public class TrocaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "aluno_solicitante_id", nullable = false)
    private AlunoEntity alunoSolicitante;

    @ManyToOne
    @JoinColumn(name = "aluno_destinatario_id", nullable = false)
    private AlunoEntity alunoDestinatario;

    @ManyToOne
    @JoinColumn(name = "resgate_oferecido_id", nullable = false)
    private ResgateEntity resgateOferecido;

    @ManyToOne
    @JoinColumn(name = "resgate_desejado_id", nullable = false)
    private ResgateEntity resgateDesejado;

    @Column(nullable = false)
    private LocalDateTime dataSolicitacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusTroca status = StatusTroca.PENDENTE;

    public TrocaEntity() {}

    public TrocaEntity(AlunoEntity alunoSolicitante, AlunoEntity alunoDestinatario,
                       ResgateEntity resgateOferecido, ResgateEntity resgateDesejado,
                       LocalDateTime dataSolicitacao) {
        this.alunoSolicitante = alunoSolicitante;
        this.alunoDestinatario = alunoDestinatario;
        this.resgateOferecido = resgateOferecido;
        this.resgateDesejado = resgateDesejado;
        this.dataSolicitacao = dataSolicitacao;
    }

    public int getId() { return id; }

    public AlunoEntity getAlunoSolicitante() { return alunoSolicitante; }

    public AlunoEntity getAlunoDestinatario() { return alunoDestinatario; }

    public ResgateEntity getResgateOferecido() { return resgateOferecido; }

    public ResgateEntity getResgateDesejado() { return resgateDesejado; }

    public LocalDateTime getDataSolicitacao() { return dataSolicitacao; }

    public StatusTroca getStatus() { return status; }

    public void setStatus(StatusTroca status) { this.status = status; }
}
