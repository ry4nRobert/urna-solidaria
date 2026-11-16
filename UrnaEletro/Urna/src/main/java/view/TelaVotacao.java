package view;

import javax.swing.*;

import dao.CandidatoDAO;
import dao.VotacaoDAO;

import java.awt.*;
import model.Urna;
import model.Eleitor;
import model.Vereador;
import model.Politico;
import util.FotoUtil;
import model.Presidente;

public class TelaVotacao {

    public static void iniciar(Urna urna, TelaInicial telaInicial) {

        try {
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame janela = new JFrame("Urna Cidadã");

        // Criando eleitor
        Eleitor eleitor = new Eleitor("Eleitor");

        VotacaoDAO votacaoDAO = new VotacaoDAO(
                config.FirebaseConfig.getFirestore());

        CandidatoDAO candidatoDAO = new CandidatoDAO(
                config.FirebaseConfig.getFirestore());

        // Painel principal
        JPanel tela = new JPanel(new BorderLayout(25, 10));
        tela.setBackground(new Color(235, 235, 235));
        tela.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Área de votação, atualizada pela 2 vez
        JPanel display = new JPanel(new BorderLayout(15, 20));
        display.setBackground(new Color(216, 230, 230));
        display.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 195, 195), 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        JLabel cargo = new JLabel("Vereador", SwingConstants.CENTER);
        cargo.setFont(new Font("Arial", Font.BOLD, 24));
        cargo.setForeground(new Color(40, 40, 40));

        JLabel numero = new JLabel("* *", SwingConstants.CENTER);
        numero.setFont(new Font("Arial", Font.BOLD, 48));
        numero.setForeground(new Color(40, 40, 40));

        StringBuilder digitado = new StringBuilder();

        JLabel foto = new JLabel("Foto do candidato", SwingConstants.CENTER);
        foto.setPreferredSize(new Dimension(170, 200));
        foto.setBackground(new Color(235, 240, 240));
        foto.setOpaque(true);
        foto.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Mostra a foto do candidato quando o número digitado está completo
        Runnable atualizarFoto = () -> {

            boolean ehVereador = cargo.getText().equals("Vereador");
            int tamanho = ehVereador ? 5 : 2;

            Politico achado = null;

            if (digitado.length() == tamanho) {
                int n = Integer.parseInt(digitado.toString());
                achado = ehVereador
                        ? urna.buscarVereador(n)
                        : urna.buscarPresidente(n);
            }

            ImageIcon icone = achado == null
                    ? null
                    : FotoUtil.base64ParaIcon(achado.getFoto());

            foto.setIcon(icone);
            foto.setText(icone == null ? "Foto do candidato" : "");
        };

        display.add(cargo, BorderLayout.NORTH);
        display.add(numero, BorderLayout.CENTER);
        display.add(foto, BorderLayout.EAST);

        // Área do teclado que também está sendo atualizada pela 2 vez
        JPanel teclado = new JPanel(new BorderLayout(10, 18));
        teclado.setBackground(new Color(95, 98, 100));
        teclado.setPreferredSize(new Dimension(420, 0));
        teclado.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel numeros = new JPanel(new GridLayout(4, 3, 18, 14));
        numeros.setOpaque(false);
        for (int i = 1; i <= 9; i++) {
            JButton botao = criarBotaoNumerico(String.valueOf(i));

            botao.addActionListener(e -> {
                int limite = cargo.getText().equals("Vereador") ? 5 : 2;

                if (digitado.length() >= limite) {
                    return;
                }

                digitado.append(botao.getText());
                numero.setText(digitado.toString());
                atualizarFoto.run();
            });

            numeros.add(botao);
        }

        numeros.add(new JLabel(""));

        JButton botao0 = criarBotaoNumerico("0");

        botao0.addActionListener(e -> {
            int limite = cargo.getText().equals("Vereador") ? 5 : 2;

            if (digitado.length() >= limite) {
                return;
            }

            digitado.append("0");
            numero.setText(digitado.toString());
            atualizarFoto.run();
        });

        numeros.add(botao0);

        numeros.add(new JLabel(""));

        // Botões de ação
        JPanel acoes = new JPanel(new GridLayout(1, 3, 12, 0));
        acoes.setOpaque(false);
        acoes.setPreferredSize(new Dimension(0, 45)); // Altura fixa para os botões de ação

        JButton branco = criarBotaoAcao("BRANCO", Color.WHITE, Color.BLACK);

        branco.addActionListener(e -> {

            int resposta = JOptionPane.showConfirmDialog(
                    janela,
                    "Deseja confirmar seu voto em branco para " + cargo.getText() + "?",
                    "Confirmar voto",
                    JOptionPane.YES_NO_OPTION);

            if (resposta != JOptionPane.YES_OPTION) {
                return;
            }

            boolean sucesso;

            if (cargo.getText().equals("Vereador")) {
                sucesso = urna.votarBrancoVereador(eleitor);
            } else {
                sucesso = urna.votarBrancoPresidente(eleitor);
            }

            if (sucesso) {

                try {
                    votacaoDAO.salvarEstado(urna);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            janela,
                            "Erro ao salvar voto no Firebase: " + ex.getMessage());
                    return;
                }

                digitado.setLength(0);
                numero.setText("* *");

                if (cargo.getText().equals("Vereador")) {
                    cargo.setText("Presidente");
                    atualizarFoto.run();
                } else {
                    JOptionPane.showMessageDialog(janela, "Votação finalizada!");
                    janela.dispose();
                    telaInicial.setVisible(true);
                }

            } else {
                JOptionPane.showMessageDialog(
                        janela,
                        "Não foi possível registrar o voto.");
            }
        });
        JButton corrige = criarBotaoAcao("CORRIGE", new Color(230, 138, 0), Color.WHITE);
        corrige.addActionListener(e -> {
            digitado.setLength(0);
            numero.setText("* *");
            atualizarFoto.run();
        });

        JButton confirma = criarBotaoAcao("CONFIRMA", new Color(46, 155, 46), Color.WHITE);
        confirma.addActionListener(e -> {

            if (digitado.length() == 0) {
                JOptionPane.showMessageDialog(
                        janela,
                        "Digite um número antes de confirmar.");
                return;
            }

            int numeroVotado = Integer.parseInt(digitado.toString());

            boolean sucesso = false;

            if (cargo.getText().equals("Vereador")) {

                Vereador candidato = urna.buscarVereador(numeroVotado);

                if (candidato != null) {

                    int resposta = JOptionPane.showConfirmDialog(
                            janela,
                            "Candidato: " + candidato.getNome()
                                    + "\nPartido: " + candidato.getPartido()
                                    + "\n\nConfirma seu voto?",
                            "Confirmar vereador",
                            JOptionPane.YES_NO_OPTION);

                    if (resposta == JOptionPane.YES_OPTION) {
                        sucesso = urna.votarVereador(eleitor, numeroVotado);
                    }

                } else {

                    int resposta = JOptionPane.showConfirmDialog(
                            janela,
                            "Número não encontrado. Deseja confirmar voto nulo?",
                            "Voto nulo",
                            JOptionPane.YES_NO_OPTION);

                    if (resposta == JOptionPane.YES_OPTION) {
                        sucesso = urna.votarNuloVereador(eleitor, numeroVotado);
                    }
                }

            } else {

                Presidente candidato = urna.buscarPresidente(numeroVotado);

                if (candidato != null) {

                    int resposta = JOptionPane.showConfirmDialog(
                            janela,
                            "Candidato: " + candidato.getNome()
                                    + "\nPartido: " + candidato.getPartido()
                                    + "\n\nConfirma seu voto?",
                            "Confirmar presidente",
                            JOptionPane.YES_NO_OPTION);

                    if (resposta == JOptionPane.YES_OPTION) {
                        sucesso = urna.votarPresidente(eleitor, numeroVotado);
                    }

                } else {

                    int resposta = JOptionPane.showConfirmDialog(
                            janela,
                            "Número não encontrado. Deseja confirmar voto nulo?",
                            "Voto nulo",
                            JOptionPane.YES_NO_OPTION);

                    if (resposta == JOptionPane.YES_OPTION) {
                        sucesso = urna.votarNuloPresidente(eleitor, numeroVotado);
                    }
                }
            }

            if (sucesso) {

    try {

        if (cargo.getText().equals("Vereador")) {
            Vereador candidato = urna.buscarVereador(numeroVotado);

            if (candidato != null) {
                candidatoDAO.salvar(candidato);
            }

        } else {
            Presidente candidato = urna.buscarPresidente(numeroVotado);

            if (candidato != null) {
                candidatoDAO.salvar(candidato);
            }
        }

        votacaoDAO.salvarEstado(urna);

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
                janela,
                "Erro ao salvar voto no Firebase: " + ex.getMessage()
        );
        return;
    }

    digitado.setLength(0);
    numero.setText("* *");

    if (cargo.getText().equals("Vereador")) {
        cargo.setText("Presidente");
        atualizarFoto.run();
    } else {
        JOptionPane.showMessageDialog(janela, "Votação finalizada!");
        janela.dispose();
        telaInicial.setVisible(true);
    }

} else {
    JOptionPane.showMessageDialog(
            janela,
            "Não foi possível registrar o voto."
    );
}});

        acoes.add(branco);
        acoes.add(corrige);
        acoes.add(confirma);

        teclado.add(numeros, BorderLayout.CENTER);
        teclado.add(acoes, BorderLayout.SOUTH);

        // Montagem da tela
        tela.add(display, BorderLayout.CENTER);
        tela.add(teclado, BorderLayout.EAST);

        janela.add(tela);

        janela.setSize(950, 460);
        janela.setLocationRelativeTo(null);
        janela.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        janela.setVisible(true);
    }

    private static JButton criarBotaoNumerico(String texto) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Arial", Font.BOLD, 18));
        botao.setBackground(new Color(55, 58, 60));
        botao.setForeground(Color.WHITE);
        botao.setFocusPainted(false);
        botao.setBorder(BorderFactory.createLineBorder(new Color(40, 42, 44), 1));
        botao.setOpaque(true);
        return botao;
    }

    private static JButton criarBotaoAcao(String texto, Color fundo, Color textoCor) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Arial", Font.BOLD, 13));
        botao.setBackground(fundo);
        botao.setForeground(textoCor);
        botao.setFocusPainted(false);
        botao.setBorderPainted(false);
        botao.setOpaque(true);
        return botao;

    }

}