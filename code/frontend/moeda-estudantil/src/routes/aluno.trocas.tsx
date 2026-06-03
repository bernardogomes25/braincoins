import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { useStore } from "@/lib/mock-data";
import { GlassCard, PageHeader, StatusBadge, EmptyState } from "@/components/ui-bits";
import { ArrowLeftRight } from "lucide-react";
import { toast } from "sonner";
import {
  listarAlunosDisponiveis,
  criarTroca,
  listarTrocasRecebidas,
  listarTrocasEnviadas,
  aceitarTroca,
  recusarTroca,
  cancelarTroca,
} from "@/api/trocasApi";
import { buscarResgatesAluno } from "@/api/vantagensApi";

type ResgateResumo = { resgateId: number; vantagemNome: string; empresaNome: string };
type AlunoDisponivel = { alunoId: number; nome: string; curso: string; resgatesAtivos: ResgateResumo[] };
type TrocaAPI = {
  id: number;
  solicitanteId: number;
  solicitanteNome: string;
  destinatarioId: number;
  destinatarioNome: string;
  resgateOferecidoId: number;
  vantagemOferecidaNome: string;
  empresaOferecidaNome: string;
  resgateDesejadoId: number;
  vantagemDesejadaNome: string;
  empresaDesejadaNome: string;
  data: string;
  expiraEm: string;
  status: string;
};
type ResgateAtivo = { id: number; vantagemNome: string; empresaNome: string; status: string };

export const Route = createFileRoute("/aluno/trocas")({
  component: Trocas,
});

type Tab = "procurar" | "recebidas" | "enviadas";

function Trocas() {
  const { currentUserId, currentUser } = useStore();
  const alunoId = Number(currentUserId || (currentUser as any)?.id);

  const [tab, setTab] = useState<Tab>("procurar");

  // procurar
  const [disponiveis, setDisponiveis] = useState<AlunoDisponivel[]>([]);
  const [meusResgates, setMeusResgates] = useState<ResgateAtivo[]>([]);
  const [destinatarioSel, setDestinatarioSel] = useState<AlunoDisponivel | null>(null);
  const [resgateDesejadoId, setResgateDesejadoId] = useState<number | "">("");
  const [resgateOferecidoId, setResgateOferecidoId] = useState<number | "">("");
  const [enviando, setEnviando] = useState(false);

  // recebidas / enviadas
  const [recebidas, setRecebidas] = useState<TrocaAPI[]>([]);
  const [enviadas, setEnviadas] = useState<TrocaAPI[]>([]);
  const [loadingListas, setLoadingListas] = useState(false);

  useEffect(() => {
    if (!alunoId) return;
    listarAlunosDisponiveis(alunoId)
      .then((r) => setDisponiveis(r.data ?? []))
      .catch(() => setDisponiveis([]));

    buscarResgatesAluno(alunoId)
      .then((r) => {
        const ativos = (r.data ?? []).filter((res: any) => res.status === "ativo");
        setMeusResgates(ativos.map((res: any) => ({
          id: res.id,
          vantagemNome: res.vantagemNome,
          empresaNome: res.empresaNome,
          status: res.status,
        })));
      })
      .catch(() => setMeusResgates([]));
  }, [alunoId]);

  useEffect(() => {
    if (tab === "recebidas" || tab === "enviadas") {
      if (!alunoId) return;
      setLoadingListas(true);
      Promise.all([
        listarTrocasRecebidas(alunoId),
        listarTrocasEnviadas(alunoId),
      ])
        .then(([r, e]) => {
          setRecebidas(r.data ?? []);
          setEnviadas(e.data ?? []);
        })
        .catch(() => {})
        .finally(() => setLoadingListas(false));
    }
  }, [tab, alunoId]);

  function selecionarDestinatario(aluno: AlunoDisponivel) {
    setDestinatarioSel(aluno);
    setResgateDesejadoId("");
    setResgateOferecidoId("");
  }

  async function enviarSolicitacao() {
    if (!destinatarioSel || !resgateDesejadoId || !resgateOferecidoId) {
      toast.error("Selecione o destinatário, o resgate desejado e o resgate oferecido.");
      return;
    }
    setEnviando(true);
    try {
      await criarTroca({
        solicitanteId: alunoId,
        destinatarioId: destinatarioSel.alunoId,
        resgateOferecidoId: Number(resgateOferecidoId),
        resgateDesejadoId: Number(resgateDesejadoId),
      });
      toast.success("Solicitação de troca enviada!");
      setDestinatarioSel(null);
      setResgateDesejadoId("");
      setResgateOferecidoId("");
    } catch (err: any) {
      toast.error(err?.response?.data?.message ?? "Erro ao enviar solicitação.");
    } finally {
      setEnviando(false);
    }
  }

  async function handleAceitar(id: number) {
    try {
      await aceitarTroca(id);
      toast.success("Troca aceita!");
      setRecebidas((prev) => prev.map((t) => t.id === id ? { ...t, status: "aceita" } : t));
    } catch (err: any) {
      toast.error(err?.response?.data?.message ?? "Erro ao aceitar troca.");
    }
  }

  async function handleRecusar(id: number) {
    try {
      await recusarTroca(id);
      toast.success("Troca recusada.");
      setRecebidas((prev) => prev.map((t) => t.id === id ? { ...t, status: "recusada" } : t));
    } catch (err: any) {
      toast.error(err?.response?.data?.message ?? "Erro ao recusar troca.");
    }
  }

  async function handleCancelar(id: number) {
    try {
      await cancelarTroca(id);
      toast.success("Troca cancelada.");
      setEnviadas((prev) => prev.map((t) => t.id === id ? { ...t, status: "cancelada" } : t));
    } catch (err: any) {
      toast.error(err?.response?.data?.message ?? "Erro ao cancelar troca.");
    }
  }

  return (
    <div>
      <PageHeader title="Trocas" subtitle="Troque resgates ativos com outros alunos." />

      {/* Tabs */}
      <div className="flex gap-2 mb-6">
        {(["procurar", "recebidas", "enviadas"] as Tab[]).map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
              tab === t
                ? "bg-coin text-coin-foreground"
                : "bg-secondary text-muted-foreground hover:bg-secondary/80"
            }`}
          >
            {t === "procurar" ? "Procurar" : t === "recebidas" ? "Recebidas" : "Enviadas"}
          </button>
        ))}
      </div>

      {/* ── Procurar ─────────────────────────────────────── */}
      {tab === "procurar" && (
        <div className="space-y-6">
          {disponiveis.length === 0 ? (
            <GlassCard>
              <EmptyState
                icon={<ArrowLeftRight className="h-7 w-7 text-muted-foreground" />}
                title="Nenhum aluno disponível"
                description="Outros alunos precisam ter resgates ativos para aparecer aqui."
              />
            </GlassCard>
          ) : (
            <div className="grid sm:grid-cols-2 gap-4">
              {disponiveis.map((aluno) => (
                <div
                  key={aluno.alunoId}
                  className={`vault-card rounded-xl p-5 cursor-pointer border-2 transition-colors ${
                    destinatarioSel?.alunoId === aluno.alunoId
                      ? "border-coin/60"
                      : "border-transparent"
                  }`}
                  onClick={() => selecionarDestinatario(aluno)}
                >
                  <p className="font-bold text-foreground">{aluno.nome}</p>
                  <p className="text-xs text-muted-foreground mb-3">{aluno.curso}</p>
                  <ul className="space-y-1">
                    {aluno.resgatesAtivos.map((r) => (
                      <li key={r.resgateId} className="text-xs text-foreground/80">
                        • {r.vantagemNome} <span className="text-muted-foreground">({r.empresaNome})</span>
                      </li>
                    ))}
                  </ul>
                </div>
              ))}
            </div>
          )}

          {destinatarioSel && (
            <GlassCard>
              <h3 className="font-semibold text-foreground mb-4">
                Configurar troca com {destinatarioSel.nome}
              </h3>

              <div className="space-y-4">
                <div>
                  <label className="text-xs text-muted-foreground uppercase tracking-wider">
                    Resgate que você quer (de {destinatarioSel.nome})
                  </label>
                  <select
                    className="mt-1 w-full bg-secondary border border-border rounded-lg px-3 py-2 text-sm text-foreground"
                    value={resgateDesejadoId}
                    onChange={(e) => setResgateDesejadoId(Number(e.target.value))}
                  >
                    <option value="">Selecionar...</option>
                    {destinatarioSel.resgatesAtivos.map((r) => (
                      <option key={r.resgateId} value={r.resgateId}>
                        {r.vantagemNome} — {r.empresaNome}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="text-xs text-muted-foreground uppercase tracking-wider">
                    Resgate que você oferece (seu)
                  </label>
                  {meusResgates.length === 0 ? (
                    <p className="mt-1 text-sm text-muted-foreground">
                      Você não tem resgates ativos para oferecer.
                    </p>
                  ) : (
                    <select
                      className="mt-1 w-full bg-secondary border border-border rounded-lg px-3 py-2 text-sm text-foreground"
                      value={resgateOferecidoId}
                      onChange={(e) => setResgateOferecidoId(Number(e.target.value))}
                    >
                      <option value="">Selecionar...</option>
                      {meusResgates.map((r) => (
                        <option key={r.id} value={r.id}>
                          {r.vantagemNome} — {r.empresaNome}
                        </option>
                      ))}
                    </select>
                  )}
                </div>

                <button
                  onClick={enviarSolicitacao}
                  disabled={enviando || !resgateDesejadoId || !resgateOferecidoId}
                  className="w-full py-2 rounded-lg text-sm font-semibold bg-coin text-coin-foreground disabled:opacity-50 transition-opacity"
                >
                  {enviando ? "Enviando..." : "Enviar solicitação"}
                </button>
              </div>
            </GlassCard>
          )}
        </div>
      )}

      {/* ── Recebidas ─────────────────────────────────────── */}
      {tab === "recebidas" && (
        loadingListas ? (
          <GlassCard><p className="text-muted-foreground text-sm">Carregando...</p></GlassCard>
        ) : recebidas.length === 0 ? (
          <GlassCard>
            <EmptyState
              icon={<ArrowLeftRight className="h-7 w-7 text-muted-foreground" />}
              title="Nenhuma troca recebida"
              description="Quando outro aluno te enviar uma solicitação, ela aparecerá aqui."
            />
          </GlassCard>
        ) : (
          <div className="space-y-4">
            {recebidas.map((t) => (
              <div key={t.id} className="vault-card rounded-xl p-5">
                <div className="flex items-start justify-between gap-3 mb-3">
                  <div>
                    <p className="font-semibold text-foreground">
                      Troca de <span className="text-coin">{t.solicitanteNome}</span>
                    </p>
                    <p className="text-xs text-muted-foreground mt-0.5">
                      Ele oferece: {t.vantagemOferecidaNome} ({t.empresaOferecidaNome})
                    </p>
                    <p className="text-xs text-muted-foreground">
                      Ele quer: {t.vantagemDesejadaNome} ({t.empresaDesejadaNome})
                    </p>
                  </div>
                  <StatusBadge status={t.status as any} />
                </div>
                {t.status === "pendente" && (
                  <div className="flex gap-2">
                    <button
                      onClick={() => handleAceitar(t.id)}
                      className="flex-1 py-1.5 rounded-lg text-xs font-semibold bg-emerald/10 text-emerald border border-emerald/30 hover:bg-emerald/20 transition-colors"
                    >
                      Aceitar
                    </button>
                    <button
                      onClick={() => handleRecusar(t.id)}
                      className="flex-1 py-1.5 rounded-lg text-xs font-semibold bg-coral/10 text-coral border border-coral/30 hover:bg-coral/20 transition-colors"
                    >
                      Recusar
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )
      )}

      {/* ── Enviadas ─────────────────────────────────────── */}
      {tab === "enviadas" && (
        loadingListas ? (
          <GlassCard><p className="text-muted-foreground text-sm">Carregando...</p></GlassCard>
        ) : enviadas.length === 0 ? (
          <GlassCard>
            <EmptyState
              icon={<ArrowLeftRight className="h-7 w-7 text-muted-foreground" />}
              title="Nenhuma troca enviada"
              description="Suas solicitações de troca aparecerão aqui."
            />
          </GlassCard>
        ) : (
          <div className="space-y-4">
            {enviadas.map((t) => (
              <div key={t.id} className="vault-card rounded-xl p-5">
                <div className="flex items-start justify-between gap-3 mb-3">
                  <div>
                    <p className="font-semibold text-foreground">
                      Para <span className="text-coin">{t.destinatarioNome}</span>
                    </p>
                    <p className="text-xs text-muted-foreground mt-0.5">
                      Você ofereceu: {t.vantagemOferecidaNome} ({t.empresaOferecidaNome})
                    </p>
                    <p className="text-xs text-muted-foreground">
                      Você quer: {t.vantagemDesejadaNome} ({t.empresaDesejadaNome})
                    </p>
                  </div>
                  <StatusBadge status={t.status as any} />
                </div>
                {t.status === "pendente" && (
                  <button
                    onClick={() => handleCancelar(t.id)}
                    className="w-full py-1.5 rounded-lg text-xs font-semibold bg-secondary text-muted-foreground border border-border hover:bg-secondary/80 transition-colors"
                  >
                    Cancelar
                  </button>
                )}
              </div>
            ))}
          </div>
        )
      )}
    </div>
  );
}
