package com.lab3.moeda.util;

public final class EmailTemplates {

    private EmailTemplates() {}

    // ─── Layout wrapper ───────────────────────────────────────────────────────

    private static String layout(String content) {
        return "<!DOCTYPE html>" +
               "<html lang='pt-BR'>" +
               "<head>" +
               "<meta charset='UTF-8'>" +
               "<meta name='viewport' content='width=device-width,initial-scale=1.0'>" +
               "<title>BrainCoins</title>" +
               "</head>" +
               "<body style='margin:0;padding:0;background-color:#111827;font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Helvetica,Arial,sans-serif;'>" +
               "<table width='100%' cellpadding='0' cellspacing='0'>" +
               "<tr><td align='center' style='padding:32px 16px;background-color:#111827;'>" +
               "<table cellpadding='0' cellspacing='0' style='width:100%;max-width:580px;background-color:#1f2937;border-radius:16px;overflow:hidden;border:1px solid #374151;'>" +
               "<tr><td style='background:linear-gradient(135deg,#78350f 0%,#92400e 60%,#b45309 100%);padding:28px 32px;text-align:center;'>" +
               "<p style='font-size:36px;margin:0 0 8px;'>&#129689;</p>" +
               "<h1 style='color:#fbbf24;margin:0;font-size:22px;font-weight:700;letter-spacing:-0.5px;'>BrainCoins</h1>" +
               "<p style='color:#fde68a;margin:4px 0 0;font-size:11px;letter-spacing:1px;text-transform:uppercase;'>Sistema de Moeda Estudantil</p>" +
               "</td></tr>" +
               "<tr><td style='padding:32px;'>" +
               content +
               "</td></tr>" +
               "<tr><td style='padding:14px 32px 18px;border-top:1px solid #374151;text-align:center;'>" +
               "<p style='color:#6b7280;font-size:11px;margin:0;'>&#169; 2025 BrainCoins &mdash; Este &eacute; um e-mail autom&aacute;tico. N&atilde;o responda.</p>" +
               "</td></tr>" +
               "</table>" +
               "</td></tr>" +
               "</table>" +
               "</body>" +
               "</html>";
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static String detailRow(String label, String value) {
        return "<tr><td style='padding:10px 14px;border-bottom:1px solid #374151;'>" +
               "<span style='color:#9ca3af;font-size:11px;display:block;margin-bottom:3px;text-transform:uppercase;letter-spacing:0.5px;'>" + label + "</span>" +
               "<span style='color:#f3f4f6;font-size:14px;font-weight:600;'>" + value + "</span>" +
               "</td></tr>";
    }

    private static String detailTable(String rows) {
        return "<table width='100%' cellpadding='0' cellspacing='0' " +
               "style='background-color:#111827;border-radius:10px;border:1px solid #374151;overflow:hidden;margin:16px 0;'>" +
               "<tbody>" + rows + "</tbody>" +
               "</table>";
    }

    // ─── 1. Cupom do aluno (com QR Code) ─────────────────────────────────────

    public static String cupomAluno(String nome, String vantagemNome, String empresaNome,
                                     String codigo, String validoAte) {
        String qrBase64 = QrCodeUtil.gerarQrCodeBase64(codigo);
        String qrSection = qrBase64 != null
            ? "<div style='text-align:center;margin:16px 0 8px;'>" +
              "<img src='data:image/png;base64," + qrBase64 + "' width='180' height='180' " +
              "alt='QR Code do cupom' style='border-radius:8px;background:#ffffff;padding:8px;display:inline-block;' />" +
              "</div>"
            : "";

        String rows = detailRow("Vantagem", vantagemNome) +
                      detailRow("Empresa", empresaNome) +
                      detailRow("V&aacute;lido at&eacute;", validoAte);

        String content =
            "<h2 style='color:#f3f4f6;margin:0 0 8px;font-size:20px;font-weight:700;'>&#127873; Seu cupom est&aacute; pronto!</h2>" +
            "<p style='color:#d1d5db;margin:0 0 20px;font-size:15px;line-height:1.6;'>Ol&aacute;, <strong style='color:#fbbf24;'>" + nome + "</strong>! Seu resgate foi confirmado com sucesso.</p>" +
            detailTable(rows) +
            "<div style='background-color:#92400e;border:2px dashed #fbbf24;border-radius:10px;padding:20px;text-align:center;margin:20px 0 8px;'>" +
            "<p style='color:#fde68a;font-size:11px;margin:0 0 10px;letter-spacing:1px;text-transform:uppercase;'>C&oacute;digo do Cupom</p>" +
            "<span style='color:#fbbf24;font-size:32px;font-weight:700;letter-spacing:8px;font-family:Courier New,Courier,monospace;'>" + codigo + "</span>" +
            "</div>" +
            qrSection +
            "<p style='color:#9ca3af;font-size:13px;text-align:center;margin:16px 0 0;line-height:1.5;'>Apresente este cupom (c&oacute;digo ou QR Code) ao retirar seu benef&iacute;cio.</p>";

        return layout(content);
    }

    // ─── 2. Novo resgate para empresa ─────────────────────────────────────────

    public static String novoResgateEmpresa(String empresaNome, String vantagemNome,
                                             String alunoNome, String codigo) {
        String rows = detailRow("Vantagem", vantagemNome) +
                      detailRow("Aluno", alunoNome) +
                      detailRow("C&oacute;digo de valida&ccedil;&atilde;o",
                                "<span style='font-family:Courier New,Courier,monospace;letter-spacing:4px;color:#fbbf24;font-size:15px;'>" + codigo + "</span>");

        String content =
            "<h2 style='color:#f3f4f6;margin:0 0 8px;font-size:20px;font-weight:700;'>&#128230; Novo resgate recebido!</h2>" +
            "<p style='color:#d1d5db;margin:0 0 20px;font-size:15px;line-height:1.6;'>Ol&aacute;, <strong style='color:#fbbf24;'>" + empresaNome + "</strong>! Um aluno resgatou uma de suas vantagens.</p>" +
            detailTable(rows) +
            "<div style='background-color:#1c3a2e;border-left:3px solid #10b981;border-radius:0 8px 8px 0;padding:12px 16px;margin:16px 0 0;'>" +
            "<p style='color:#6ee7b7;font-size:13px;margin:0;'>&#128337; O aluno tem at&eacute; <strong>15 dias</strong> para retirar o benef&iacute;cio. Valide o c&oacute;digo na retirada.</p>" +
            "</div>";

        return layout(content);
    }

    // ─── 3. Resgate expirado — aluno ─────────────────────────────────────────

    public static String resgateExpirado(String nome, String vantagemNome, int valorMoedas) {
        String content =
            "<h2 style='color:#f3f4f6;margin:0 0 8px;font-size:20px;font-weight:700;'>&#9888;&#65039; Resgate expirado</h2>" +
            "<p style='color:#d1d5db;margin:0 0 20px;font-size:15px;line-height:1.6;'>Ol&aacute;, <strong style='color:#fbbf24;'>" + nome + "</strong>!</p>" +
            "<div style='background-color:#2c1810;border-left:3px solid #f97316;border-radius:0 8px 8px 0;padding:16px;margin:0 0 20px;'>" +
            "<p style='color:#fdba74;font-size:14px;margin:0;'>Seu resgate de <strong>&ldquo;" + vantagemNome + "&rdquo;</strong> expirou porque n&atilde;o foi retirado em 15 dias.</p>" +
            "</div>" +
            "<div style='background-color:#111827;border:1px solid #374151;border-radius:10px;padding:16px;text-align:center;margin:0 0 16px;'>" +
            "<p style='color:#9ca3af;font-size:11px;margin:0 0 6px;text-transform:uppercase;letter-spacing:0.5px;'>Moedas reembolsadas</p>" +
            "<p style='color:#34d399;font-size:30px;font-weight:700;margin:0;'>+" + valorMoedas + " &#129689;</p>" +
            "</div>" +
            "<p style='color:#9ca3af;font-size:13px;text-align:center;margin:0;'>As moedas j&aacute; foram devolvidas ao seu saldo automaticamente.</p>";

        return layout(content);
    }

    // ─── 4. Moedas recebidas — aluno ─────────────────────────────────────────

    public static String moedasRecebidas(String nomeAluno, int valor, String nomeProfessor,
                                          String motivo, int saldo) {
        String rows = detailRow("Professor", nomeProfessor) +
                      detailRow("Motivo", "&ldquo;" + motivo + "&rdquo;");

        String content =
            "<h2 style='color:#f3f4f6;margin:0 0 8px;font-size:20px;font-weight:700;'>&#129689; Voc&ecirc; recebeu moedas!</h2>" +
            "<p style='color:#d1d5db;margin:0 0 20px;font-size:15px;line-height:1.6;'>Ol&aacute;, <strong style='color:#fbbf24;'>" + nomeAluno + "</strong>! Um professor reconheceu seu m&eacute;rito.</p>" +
            "<div style='background-color:#111827;border:1px solid #374151;border-radius:10px;padding:16px;text-align:center;margin:0 0 20px;'>" +
            "<p style='color:#9ca3af;font-size:11px;margin:0 0 6px;text-transform:uppercase;letter-spacing:0.5px;'>Moedas recebidas</p>" +
            "<p style='color:#fbbf24;font-size:36px;font-weight:700;margin:0;'>+" + valor + " &#129689;</p>" +
            "</div>" +
            detailTable(rows) +
            "<div style='background-color:#111827;border:1px solid #374151;border-radius:10px;padding:12px 16px;margin:16px 0 0;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0'><tr>" +
            "<td style='color:#9ca3af;font-size:13px;'>Seu saldo atual</td>" +
            "<td align='right' style='color:#fbbf24;font-size:16px;font-weight:700;'>" + saldo + " &#129689;</td>" +
            "</tr></table>" +
            "</div>" +
            "<p style='color:#9ca3af;font-size:13px;text-align:center;margin:16px 0 0;'>Continue assim! &#128640;</p>";

        return layout(content);
    }

    // ─── 5. Confirmação de envio — professor ─────────────────────────────────

    public static String confirmacaoEnvio(String nomeProfessor, String nomeAluno,
                                           int valor, String motivo, int saldo) {
        String rows = detailRow("Aluno", nomeAluno) +
                      detailRow("Moedas enviadas", valor + " &#129689;") +
                      detailRow("Justificativa", "&ldquo;" + motivo + "&rdquo;");

        String content =
            "<h2 style='color:#f3f4f6;margin:0 0 8px;font-size:20px;font-weight:700;'>&#10003;&#65039; Transa&ccedil;&atilde;o confirmada</h2>" +
            "<p style='color:#d1d5db;margin:0 0 20px;font-size:15px;line-height:1.6;'>Ol&aacute;, Prof. <strong style='color:#fbbf24;'>" + nomeProfessor + "</strong>! Sua transa&ccedil;&atilde;o foi registrada com sucesso.</p>" +
            detailTable(rows) +
            "<div style='background-color:#111827;border:1px solid #374151;border-radius:10px;padding:12px 16px;margin:16px 0 0;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0'><tr>" +
            "<td style='color:#9ca3af;font-size:13px;'>Seu saldo restante</td>" +
            "<td align='right' style='color:#fbbf24;font-size:16px;font-weight:700;'>" + saldo + " &#129689;</td>" +
            "</tr></table>" +
            "</div>";

        return layout(content);
    }

    // ─── 6. Nova solicitação de troca — destinatário ──────────────────────────

    public static String novaSolicitacaoTroca(String nomeDestinatario, String nomeSolicitante,
                                               String vantagemOferecida, String empresaOferecida,
                                               String vantagemDesejada, String empresaDesejada,
                                               int diasValidade) {
        String content =
            "<h2 style='color:#f3f4f6;margin:0 0 8px;font-size:20px;font-weight:700;'>&#128260; Nova solicita&ccedil;&atilde;o de troca</h2>" +
            "<p style='color:#d1d5db;margin:0 0 20px;font-size:15px;line-height:1.6;'>Ol&aacute;, <strong style='color:#fbbf24;'>" + nomeDestinatario + "</strong>! <strong style='color:#f3f4f6;'>" + nomeSolicitante + "</strong> quer trocar resgates com voc&ecirc;.</p>" +
            "<table width='100%' cellpadding='0' cellspacing='0' style='margin:0 0 20px;'><tr>" +
            "<td style='width:47%;background-color:#111827;border-radius:10px;padding:14px;border:1px solid #374151;vertical-align:top;'>" +
            "<p style='color:#10b981;font-size:11px;margin:0 0 6px;text-transform:uppercase;letter-spacing:0.5px;font-weight:700;'>Ele oferece</p>" +
            "<p style='color:#f3f4f6;font-size:14px;font-weight:700;margin:0 0 3px;'>" + vantagemOferecida + "</p>" +
            "<p style='color:#9ca3af;font-size:12px;margin:0;'>" + empresaOferecida + "</p>" +
            "</td>" +
            "<td style='width:6%;text-align:center;vertical-align:middle;padding:0 4px;'>" +
            "<span style='color:#6b7280;font-size:20px;'>&#8644;</span>" +
            "</td>" +
            "<td style='width:47%;background-color:#111827;border-radius:10px;padding:14px;border:1px solid #374151;vertical-align:top;'>" +
            "<p style='color:#f97316;font-size:11px;margin:0 0 6px;text-transform:uppercase;letter-spacing:0.5px;font-weight:700;'>Ele quer</p>" +
            "<p style='color:#f3f4f6;font-size:14px;font-weight:700;margin:0 0 3px;'>" + vantagemDesejada + "</p>" +
            "<p style='color:#9ca3af;font-size:12px;margin:0;'>" + empresaDesejada + "</p>" +
            "</td>" +
            "</tr></table>" +
            "<div style='background-color:#1a2030;border-left:3px solid #6366f1;border-radius:0 8px 8px 0;padding:12px 16px;'>" +
            "<p style='color:#a5b4fc;font-size:13px;margin:0;'>&#128337; Esta solicita&ccedil;&atilde;o expira em <strong>" + diasValidade + " dias</strong>. Acesse o sistema para aceitar ou recusar.</p>" +
            "</div>";

        return layout(content);
    }

    // ─── 7. Troca aceita — para o solicitante ────────────────────────────────

    public static String trocaAceitaSolicitante(String nomeSolicitante, String nomeDestinatario,
                                                  String vantagemRecebida, String empresaRecebida,
                                                  String vantagemEnviada, String empresaEnviada) {
        String rows = detailRow("Voc&ecirc; recebeu",
                                vantagemRecebida + " <span style='color:#9ca3af;font-size:12px;font-weight:400;'>(" + empresaRecebida + ")</span>") +
                      detailRow("Voc&ecirc; enviou",
                                vantagemEnviada + " <span style='color:#9ca3af;font-size:12px;font-weight:400;'>(" + empresaEnviada + ")</span>");

        String content =
            "<h2 style='color:#f3f4f6;margin:0 0 8px;font-size:20px;font-weight:700;'>&#10003;&#65039; Troca aceita!</h2>" +
            "<p style='color:#d1d5db;margin:0 0 20px;font-size:15px;line-height:1.6;'>Ol&aacute;, <strong style='color:#fbbf24;'>" + nomeSolicitante + "</strong>! <strong style='color:#f3f4f6;'>" + nomeDestinatario + "</strong> aceitou sua solicita&ccedil;&atilde;o de troca.</p>" +
            detailTable(rows) +
            "<div style='background-color:#1c2a10;border-left:3px solid #10b981;border-radius:0 8px 8px 0;padding:12px 16px;margin:16px 0 0;'>" +
            "<p style='color:#6ee7b7;font-size:13px;margin:0;'>Os resgates j&aacute; foram transferidos. Aproveite! &#127881;</p>" +
            "</div>";

        return layout(content);
    }

    // ─── 8. Troca aceita — para o destinatário (aceitante) ───────────────────

    public static String trocaAceitaDestinatario(String nomeDestinatario, String nomeSolicitante,
                                                   String vantagemRecebida, String empresaRecebida,
                                                   String vantagemEnviada, String empresaEnviada) {
        String rows = detailRow("Voc&ecirc; recebeu",
                                vantagemRecebida + " <span style='color:#9ca3af;font-size:12px;font-weight:400;'>(" + empresaRecebida + ")</span>") +
                      detailRow("Voc&ecirc; enviou",
                                vantagemEnviada + " <span style='color:#9ca3af;font-size:12px;font-weight:400;'>(" + empresaEnviada + ")</span>");

        String content =
            "<h2 style='color:#f3f4f6;margin:0 0 8px;font-size:20px;font-weight:700;'>&#128260; Troca conclu&iacute;da!</h2>" +
            "<p style='color:#d1d5db;margin:0 0 20px;font-size:15px;line-height:1.6;'>Ol&aacute;, <strong style='color:#fbbf24;'>" + nomeDestinatario + "</strong>! Voc&ecirc; aceitou a troca com <strong style='color:#f3f4f6;'>" + nomeSolicitante + "</strong>.</p>" +
            detailTable(rows) +
            "<div style='background-color:#1c2a10;border-left:3px solid #10b981;border-radius:0 8px 8px 0;padding:12px 16px;margin:16px 0 0;'>" +
            "<p style='color:#6ee7b7;font-size:13px;margin:0;'>Os resgates j&aacute; foram transferidos. Aproveite! &#127881;</p>" +
            "</div>";

        return layout(content);
    }

    // ─── 9. Troca recusada — solicitante ─────────────────────────────────────

    public static String trocaRecusada(String nomeSolicitante, String nomeDestinatario) {
        String content =
            "<h2 style='color:#f3f4f6;margin:0 0 8px;font-size:20px;font-weight:700;'>&#10060; Troca recusada</h2>" +
            "<p style='color:#d1d5db;margin:0 0 20px;font-size:15px;line-height:1.6;'>Ol&aacute;, <strong style='color:#fbbf24;'>" + nomeSolicitante + "</strong>!</p>" +
            "<div style='background-color:#2c1010;border-left:3px solid #ef4444;border-radius:0 8px 8px 0;padding:16px;margin:0 0 16px;'>" +
            "<p style='color:#fca5a5;font-size:14px;margin:0;'><strong style='color:#f87171;'>" + nomeDestinatario + "</strong> recusou sua solicita&ccedil;&atilde;o de troca.</p>" +
            "</div>" +
            "<p style='color:#9ca3af;font-size:13px;margin:0;'>Voc&ecirc; pode fazer uma nova solicita&ccedil;&atilde;o com outro aluno a qualquer momento.</p>";

        return layout(content);
    }

    // ─── 10. Troca expirada — solicitante ────────────────────────────────────

    public static String trocaExpirada(String nomeSolicitante, String nomeDestinatario,
                                        int diasValidade) {
        String content =
            "<h2 style='color:#f3f4f6;margin:0 0 8px;font-size:20px;font-weight:700;'>&#9888;&#65039; Solicita&ccedil;&atilde;o expirada</h2>" +
            "<p style='color:#d1d5db;margin:0 0 20px;font-size:15px;line-height:1.6;'>Ol&aacute;, <strong style='color:#fbbf24;'>" + nomeSolicitante + "</strong>!</p>" +
            "<div style='background-color:#2c1810;border-left:3px solid #f97316;border-radius:0 8px 8px 0;padding:16px;margin:0 0 16px;'>" +
            "<p style='color:#fdba74;font-size:14px;margin:0;'>Sua solicita&ccedil;&atilde;o de troca com <strong style='color:#fb923c;'>" + nomeDestinatario + "</strong> expirou ap&oacute;s " + diasValidade + " dias sem resposta.</p>" +
            "</div>" +
            "<p style='color:#9ca3af;font-size:13px;margin:0;'>Os resgates originais n&atilde;o foram alterados. Voc&ecirc; pode fazer uma nova solicita&ccedil;&atilde;o a qualquer momento.</p>";

        return layout(content);
    }

    // ─── 11. Troca cancelada — destinatário ──────────────────────────────────

    public static String trocaCancelada(String nomeDestinatario, String nomeSolicitante,
                                         String vantagemOferecida, String empresaOferecida,
                                         String vantagemDesejada, String empresaDesejada) {
        String rows = detailRow("Ele oferecia",
                                vantagemOferecida + " <span style='color:#9ca3af;font-size:12px;font-weight:400;'>(" + empresaOferecida + ")</span>") +
                      detailRow("Ele queria",
                                vantagemDesejada + " <span style='color:#9ca3af;font-size:12px;font-weight:400;'>(" + empresaDesejada + ")</span>");

        String content =
            "<h2 style='color:#f3f4f6;margin:0 0 8px;font-size:20px;font-weight:700;'>&#128683; Troca cancelada</h2>" +
            "<p style='color:#d1d5db;margin:0 0 20px;font-size:15px;line-height:1.6;'>Ol&aacute;, <strong style='color:#fbbf24;'>" + nomeDestinatario + "</strong>! <strong style='color:#f3f4f6;'>" + nomeSolicitante + "</strong> cancelou a solicita&ccedil;&atilde;o de troca enviada para voc&ecirc;.</p>" +
            detailTable(rows) +
            "<p style='color:#9ca3af;font-size:13px;margin:16px 0 0;'>Nenhuma a&ccedil;&atilde;o &eacute; necess&aacute;ria da sua parte.</p>";

        return layout(content);
    }
}
