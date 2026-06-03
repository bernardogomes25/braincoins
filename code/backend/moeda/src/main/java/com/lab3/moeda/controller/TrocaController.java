package com.lab3.moeda.controller;

import com.lab3.moeda.dto.request.TrocaRequestDTO;
import com.lab3.moeda.dto.response.AlunoDisponivelResponseDTO;
import com.lab3.moeda.dto.response.TrocaResponseDTO;
import com.lab3.moeda.service.TrocaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trocas")
public class TrocaController {

    private final TrocaService trocaService;

    public TrocaController(TrocaService trocaService) {
        this.trocaService = trocaService;
    }

    @PostMapping
    public ResponseEntity<TrocaResponseDTO> criar(@RequestBody TrocaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trocaService.criar(dto));
    }

    @GetMapping("/alunos-disponiveis/{alunoId}")
    public List<AlunoDisponivelResponseDTO> listarAlunosDisponiveis(@PathVariable int alunoId) {
        return trocaService.listarAlunosDisponiveis(alunoId);
    }

    @GetMapping("/recebidas/{alunoId}")
    public List<TrocaResponseDTO> listarRecebidas(@PathVariable int alunoId) {
        return trocaService.listarRecebidas(alunoId);
    }

    @GetMapping("/enviadas/{alunoId}")
    public List<TrocaResponseDTO> listarEnviadas(@PathVariable int alunoId) {
        return trocaService.listarEnviadas(alunoId);
    }

    @PatchMapping("/{id}/aceitar")
    public ResponseEntity<TrocaResponseDTO> aceitar(@PathVariable int id) {
        return ResponseEntity.ok(trocaService.aceitar(id));
    }

    @PatchMapping("/{id}/recusar")
    public ResponseEntity<TrocaResponseDTO> recusar(@PathVariable int id) {
        return ResponseEntity.ok(trocaService.recusar(id));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<TrocaResponseDTO> cancelar(@PathVariable int id) {
        return ResponseEntity.ok(trocaService.cancelar(id));
    }
}
