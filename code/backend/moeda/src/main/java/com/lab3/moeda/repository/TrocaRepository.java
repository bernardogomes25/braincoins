package com.lab3.moeda.repository;

import com.lab3.moeda.model.StatusTroca;
import com.lab3.moeda.model.TrocaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface TrocaRepository extends JpaRepository<TrocaEntity, Integer> {
    List<TrocaEntity> findByAlunoDestinatarioIdOrderByDataSolicitacaoDesc(int alunoId);
    List<TrocaEntity> findByAlunoSolicitanteIdOrderByDataSolicitacaoDesc(int alunoId);
    List<TrocaEntity> findByStatusAndDataSolicitacaoBefore(StatusTroca status, LocalDateTime limite);
}
