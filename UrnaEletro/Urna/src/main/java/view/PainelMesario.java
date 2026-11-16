package view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.io.File;

import model.Urna;
import model.Politico;
import model.Vereador;
import model.Presidente;
import dao.CandidatoDAO;
import config.FirebaseConfig;
import dao.VotacaoDAO;
import util.FotoUtil;

public class PainelMesario extends JFrame {

    private Urna urna;
    private TelaInicial telaInicial;

    private DefaultListModel<String> modeloLista;
    private JList<String> listaCandidatos;

    private ArrayList<Politico> candidatosExibidos;

    private boolean cadastroBloqueado = false;

    private boolean textoValido(String texto) {
        return texto != null && texto.matches("[\\p{L} ]+");
    }

    public PainelMesario(Urna urna, TelaInicial telaInicial) {

        this.urna = urna;
        this.telaInicial = telaInicial;
        this.candidatosExibidos = new ArrayList<>();

        setTitle("Urna Cidadã - Painel do Mesário");
        setSize(750, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel principal = new JPanel(new BorderLayout(15, 15));
        principal.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel(
                "Gerenciamento de Candidatos",
                SwingConstants.CENTER);

        titulo.setFont(new Font("Arial", Font.BOLD, 24));

        modeloLista = new DefaultListModel<>();
        listaCandidatos = new JList<>(modeloLista);

        listaCandidatos.setFont(new Font("Arial", Font.PLAIN, 16));

        JScrollPane rolagem = new JScrollPane(listaCandidatos);

        JPanel botoes = new JPanel(new GridLayout(9, 1, 10, 10));

        JButton adicionar = new JButton("Adicionar candidato");
        JButton alterar = new JButton("Alterar candidato");
        JButton remover = new JButton("Remover candidato");
        JButton bloquear = new JButton("Bloquear cadastros");
        JButton fechar = new JButton("Fechar painel");
        JButton voltar = new JButton("Voltar à tela inicial");
        JButton encerrar = new JButton("Encerrar votação");
        JButton apurar = new JButton("Apurar votos");
        JButton resetar = new JButton("Resetar votação");

        adicionar.addActionListener(e -> adicionarCandidato());
        alterar.addActionListener(e -> alterarCandidato());
        remover.addActionListener(e -> removerCandidato());
        bloquear.addActionListener(e -> bloquearCadastros(bloquear));
        fechar.addActionListener(e -> dispose());
        apurar.addActionListener(e -> apurarVotos());
        resetar.addActionListener(e -> resetarVotacao(bloquear));
        voltar.addActionListener(e -> {
            telaInicial.setVisible(true);
            dispose();
        });

        encerrar.addActionListener(e -> {
            int resposta = JOptionPane.showConfirmDialog(
                    this,
                    "Tem certeza que deseja encerrar a votação?",
                    "Encerrar votação",
                    JOptionPane.YES_NO_OPTION);

            if (resposta == JOptionPane.YES_OPTION) {
                try {
                    urna.encerrarVotacao();

                    VotacaoDAO dao = new VotacaoDAO(
                            FirebaseConfig.getFirestore());

                    dao.salvarEstado(urna);

                    mostrarMensagem("Votação encerrada e salva no Firebase!");

                } catch (Exception ex) {
                    ex.printStackTrace();
                    mostrarMensagem(
                            "A votação foi encerrada localmente, mas houve erro ao salvar no Firebase: "
                                    + ex.getMessage());
                }
            }
        });

        botoes.add(adicionar);
        botoes.add(alterar);
        botoes.add(remover);
        botoes.add(bloquear);
        botoes.add(fechar);
        botoes.add(voltar);
        botoes.add(encerrar);
        botoes.add(apurar);
        botoes.add(resetar);

        principal.add(titulo, BorderLayout.NORTH);
        principal.add(rolagem, BorderLayout.CENTER);
        principal.add(botoes, BorderLayout.EAST);

        add(principal);

        atualizarLista();
    }

    // Atualiza a lista de candidatos
    private void atualizarLista() {

        modeloLista.clear();
        candidatosExibidos.clear();

        for (Vereador vereador : urna.getVereadores()) {

            candidatosExibidos.add(vereador);

            modeloLista.addElement(
                    "Vereador | " +
                            vereador.getNumero() + " | " +
                            vereador.getNome() + " | " +
                            vereador.getPartido());
        }

        for (Presidente presidente : urna.getPresidentes()) {

            candidatosExibidos.add(presidente);

            modeloLista.addElement(
                    "Presidente | " +
                            presidente.getNumero() + " | " +
                            presidente.getNome() + " | " +
                            presidente.getPartido());
        }
    }

    // Adicionar candidato
    private void adicionarCandidato() {

        if (cadastroBloqueado || urna.isCadastrosBloqueados()) {
            mostrarMensagem("Os cadastros estão bloqueados.");
            return;
        }

        String[] cargos = { "Vereador", "Presidente" };

        String cargo = (String) JOptionPane.showInputDialog(
                this, "Selecione o cargo:", "Novo candidato",
                JOptionPane.QUESTION_MESSAGE, null, cargos, cargos[0]);

        if (cargo == null)
            return;

        String nome = JOptionPane.showInputDialog(this, "Nome do candidato:");
        if (nome == null)
            return;

        if (!textoValido(nome)) {
            mostrarMensagem("O nome deve conter apenas letras e espaços.");
            return;
        }

        String partido = JOptionPane.showInputDialog(this, "Partido:");
        if (partido == null)
            return;

        if (!textoValido(partido)) {
            mostrarMensagem("O partido deve conter apenas letras e espaços.");
            return;
        }

        String numeroTexto = JOptionPane.showInputDialog(
                this, "Número do candidato:");

        if (numeroTexto == null)
            return;

        Politico novoCandidato;

        try {
            int numero = Integer.parseInt(numeroTexto.trim());

            if (cargo.equals("Vereador")) {

                if (numeroTexto.trim().length() != 5) {
                    mostrarMensagem("O número do vereador deve ter 5 dígitos.");
                    return;
                }

                novoCandidato = new Vereador(numero, nome.trim(), partido.trim());

            } else {

                if (numeroTexto.trim().length() != 2) {
                    mostrarMensagem("O número do presidente deve ter 2 dígitos.");
                    return;
                }

                novoCandidato = new Presidente(numero, nome.trim(), partido.trim());
            }

            int querFoto = JOptionPane.showConfirmDialog(
                    this, "Deseja adicionar uma foto ao candidato?",
                    "Foto", JOptionPane.YES_NO_OPTION);

            if (querFoto == JOptionPane.YES_OPTION) {
                novoCandidato.setFoto(selecionarFoto());
            }

            boolean sucesso;

            if (novoCandidato instanceof Vereador) {
                sucesso = urna.adicionarVereador((Vereador) novoCandidato);
            } else {
                sucesso = urna.adicionarPresidente((Presidente) novoCandidato);
            }

            if (!sucesso) {
                mostrarMensagem("Já existe um candidato com esse número.");
                return;
            }

            try {
                CandidatoDAO dao = new CandidatoDAO(
                        FirebaseConfig.getFirestore());

                dao.salvar(novoCandidato);

            } catch (Exception e) {

                if (novoCandidato instanceof Vereador) {
                    urna.removerVereador((Vereador) novoCandidato);
                } else {
                    urna.removerPresidente((Presidente) novoCandidato);
                }

                e.printStackTrace();

                mostrarMensagem("Erro ao salvar no Firebase. O cadastro foi cancelado.");
                return;
            }

            mostrarMensagem("Candidato cadastrado e salvo no Firebase!");
            atualizarLista();

        } catch (NumberFormatException e) {
            mostrarMensagem("Digite um número válido.");
        }
    }

    // Alterar candidato

    private void alterarCandidato() {

        if (cadastroBloqueado || urna.isCadastrosBloqueados()) {
            mostrarMensagem("Os cadastros estão bloqueados.");
            return;
        }

        int indice = listaCandidatos.getSelectedIndex();

        if (indice == -1) {
            mostrarMensagem("Selecione um candidato.");
            return;
        }

        Politico candidato = candidatosExibidos.get(indice);

        if (candidato.getVotos() > 0) {
            mostrarMensagem("Não é possível alterar um candidato que já recebeu votos.");
            return;
        }

        String nome = JOptionPane.showInputDialog(
                this, "Novo nome:", candidato.getNome());

        if (nome == null)
            return;

        if (!textoValido(nome)) {
            mostrarMensagem("O nome deve conter apenas letras e espaços.");
            return;
        }

        String partido = JOptionPane.showInputDialog(
                this, "Novo partido:", candidato.getPartido());

        if (partido == null)
            return;

        if (!textoValido(partido)) {
            mostrarMensagem("O partido deve conter apenas letras e espaços.");
            return;
        }

        String numeroTexto = JOptionPane.showInputDialog(
                this, "Novo número:", candidato.getNumero());

        if (numeroTexto == null)
            return;

        try {

            int numero = Integer.parseInt(numeroTexto.trim());
            boolean vereador = candidato instanceof Vereador;

            if (numeroTexto.trim().length() != (vereador ? 5 : 2)) {
                mostrarMensagem(vereador
                        ? "O número do vereador deve ter 5 dígitos."
                        : "O número do presidente deve ter 2 dígitos.");
                return;
            }

            Politico novoCandidato;

            if (vereador) {
                Vereador existente = urna.buscarVereador(numero);

                if (existente != null && existente != candidato) {
                    mostrarMensagem("Esse número já está cadastrado.");
                    return;
                }

                novoCandidato = new Vereador(numero, nome.trim(), partido.trim());

            } else {
                Presidente existente = urna.buscarPresidente(numero);

                if (existente != null && existente != candidato) {
                    mostrarMensagem("Esse número já está cadastrado.");
                    return;
                }

                novoCandidato = new Presidente(numero, nome.trim(), partido.trim());
            }

            // Mantém a foto atual (sem isso, alterar o candidato apagaria a foto)
            novoCandidato.setFoto(candidato.getFoto());

            int trocar = JOptionPane.showConfirmDialog(
                    this, "Deseja trocar a foto?",
                    "Foto", JOptionPane.YES_NO_OPTION);

            if (trocar == JOptionPane.YES_OPTION) {
                String nova = selecionarFoto();
                if (nova != null) {
                    novoCandidato.setFoto(nova);
                }
            }

            CandidatoDAO dao = new CandidatoDAO(
                    FirebaseConfig.getFirestore());

            dao.atualizar(candidato, novoCandidato);

            boolean removido;

            if (vereador) {
                removido = urna.removerVereador((Vereador) candidato);
            } else {
                removido = urna.removerPresidente((Presidente) candidato);
            }

            if (!removido) {
                mostrarMensagem("O Firebase foi atualizado, mas houve erro na lista local.");
                return;
            }

            if (vereador) {
                urna.adicionarVereador((Vereador) novoCandidato);
            } else {
                urna.adicionarPresidente((Presidente) novoCandidato);
            }

            mostrarMensagem("Candidato alterado e salvo no Firebase!");
            atualizarLista();

        } catch (NumberFormatException e) {
            mostrarMensagem("Digite um número válido.");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarMensagem("Erro ao atualizar candidato no Firebase: "
                    + e.getMessage());
        }
    }

    // Remover o candidato

    private void removerCandidato() {

        if (cadastroBloqueado || urna.isCadastrosBloqueados()) {
            mostrarMensagem("Os cadastros estão bloqueados.");
            return;
        }

        int indice = listaCandidatos.getSelectedIndex();

        if (indice == -1) {
            mostrarMensagem("Selecione um candidato.");
            return;
        }

        Politico candidato = candidatosExibidos.get(indice);

        if (candidato.getVotos() > 0) {
            mostrarMensagem("Não é possível remover um candidato que já recebeu votos.");
            return;
        }

        int resposta = JOptionPane.showConfirmDialog(
                this,
                "Deseja realmente remover " + candidato.getNome() + "?",
                "Confirmar remoção",
                JOptionPane.YES_NO_OPTION);

        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            CandidatoDAO dao = new CandidatoDAO(
                    FirebaseConfig.getFirestore());

            // Primeiro remove do Firebase.
            dao.remover(candidato);

            // Depois remove da urna local.
            boolean removido;

            if (candidato instanceof Vereador) {
                removido = urna.removerVereador((Vereador) candidato);
            } else {
                removido = urna.removerPresidente((Presidente) candidato);
            }

            if (!removido) {
                mostrarMensagem("O candidato foi removido do Firebase, mas não da lista local. Atualize os dados.");
                return;
            }

            mostrarMensagem("Candidato removido com sucesso!");
            atualizarLista();

        } catch (Exception e) {

            e.printStackTrace();

            mostrarMensagem("Erro ao remover candidato do Firebase: "
                    + e.getMessage());
        }
    }

    // Bloquear as alterações
    private void bloquearCadastros(JButton botao) {

        if (urna.isCadastrosBloqueados()) {

            urna.desbloquearCadastros();

        } else {

            urna.bloquearCadastros();
        }

        boolean bloqueado = urna.isCadastrosBloqueados();

        botao.setText(
                bloqueado
                        ? "Desbloquear cadastros"
                        : "Bloquear cadastros");

        mostrarMensagem(
                bloqueado
                        ? "Cadastros bloqueados!"
                        : "Cadastros liberados!");
    }

    private void resetarVotacao(JButton botaoBloquear) {

    int resposta = JOptionPane.showConfirmDialog(
            this,
            "Deseja realmente resetar a votação?\n\n"
                    + "Todos os votos serão apagados.\n"
                    + "Os candidatos serão mantidos.\n"
                    + "A votação será reaberta.",
            "Confirmar reset",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

    if (resposta != JOptionPane.YES_OPTION) {
        return;
    }

    try {
        // Reseta os dados na memória
        urna.resetarVotacao();

        // Zera os votos dos candidatos no Firebase
        CandidatoDAO candidatoDAO = new CandidatoDAO(
                FirebaseConfig.getFirestore());

        for (Vereador vereador : urna.getVereadores()) {
            candidatoDAO.salvar(vereador);
        }

        for (Presidente presidente : urna.getPresidentes()) {
            candidatoDAO.salvar(presidente);
        }

        // Salva o novo estado da votação
        VotacaoDAO votacaoDAO = new VotacaoDAO(
                FirebaseConfig.getFirestore());

        votacaoDAO.salvarEstado(urna);

        cadastroBloqueado = false;
        botaoBloquear.setText("Bloquear cadastros");

        atualizarLista();

        mostrarMensagem(
                "Votação resetada com sucesso!\n"
                        + "Os candidatos foram preservados.\n"
                        + "A votação está aberta novamente.");

    } catch (Exception e) {
        e.printStackTrace();

        mostrarMensagem(
                "Erro ao resetar a votação: " + e.getMessage());
    }
}

    private void apurarVotos() {

        if (!urna.isVotacaoEncerrada()) {
            mostrarMensagem("Encerre a votação antes de realizar a apuração.");
            return;
        }

        String resultado = urna.apurarVereador() + "\n\n"
                + urna.apurarPresidente();

        JTextArea area = new JTextArea(resultado);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(600, 400));

        JOptionPane.showMessageDialog(
                this,
                scroll,
                "Apuração dos votos",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Escolhe a imagem e devolve em Base64 (null se cancelar ou der erro)
    private String selecionarFoto() {

        File arquivo = FotoUtil.escolherArquivo(this);

        if (arquivo == null) {
            return null;
        }

        try {
            return FotoUtil.arquivoParaBase64(arquivo);

        } catch (Exception e) {
            mostrarMensagem("Não foi possível carregar a imagem: " + e.getMessage());
            return null;
        }
    }

    private void mostrarMensagem(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem);
    }
}