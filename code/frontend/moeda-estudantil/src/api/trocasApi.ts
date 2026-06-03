import { api } from './vantagensApi';

export const listarAlunosDisponiveis = (alunoId: number) =>
  api.get(`/trocas/alunos-disponiveis/${alunoId}`);

export const criarTroca = (payload: {
  solicitanteId: number;
  destinatarioId: number;
  resgateOferecidoId: number;
  resgateDesejadoId: number;
}) => api.post('/trocas', payload);

export const listarTrocasRecebidas = (alunoId: number) =>
  api.get(`/trocas/recebidas/${alunoId}`);

export const listarTrocasEnviadas = (alunoId: number) =>
  api.get(`/trocas/enviadas/${alunoId}`);

export const aceitarTroca = (id: number) =>
  api.patch(`/trocas/${id}/aceitar`);

export const recusarTroca = (id: number) =>
  api.patch(`/trocas/${id}/recusar`);

export const cancelarTroca = (id: number) =>
  api.patch(`/trocas/${id}/cancelar`);
