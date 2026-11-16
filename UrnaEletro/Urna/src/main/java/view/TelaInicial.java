package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import config.FirebaseConfig;
import dao.CandidatoDAO;
import dao.VotacaoDAO;
import model.Urna;
import model.Politico;
import model.Vereador;
import model.Presidente;

public class TelaInicial extends JFrame {

    private Urna urna;

    // Códigos provisórios de acesso
    private final String CODIGO_MESARIO = "2";
    private final String CODIGO_VOTACAO = "2026";

   public TelaInicial()  {

        urna = new Urna();

        try {
    CandidatoDAO dao = new CandidatoDAO(
            FirebaseConfig.getFirestore());

    List<Politico> vereadores = dao.listar("Vereador");
    List<Politico> presidentes = dao.listar("Presidente");

    // Primeiro, carrega os candidatos
    for (Politico candidato : vereadores) {
        urna.adicionarVereador((Vereador) candidato);
    }

    for (Politico candidato : presidentes) {
        urna.adicionarPresidente((Presidente) candidato);
    }

    // Depois, restaura o estado da votação
    VotacaoDAO votacaoDAO = new VotacaoDAO(
            FirebaseConfig.getFirestore());

    votacaoDAO.carregarEstado(urna);

} catch (Exception e) {
    JOptionPane.showMessageDialog(
            this,
            "Erro ao carregar candidatos do Firebase: " + e.getMessage());
}

        configurarTela();
    }

    private void configurarTela() {

        setTitle("Urna Cidadã");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel principal = new JPanel();
        principal.setLayout(new BoxLayout(principal, BoxLayout.Y_AXIS));
        principal.setBackground(new Color(235, 235, 235));
        principal.setBorder(
                BorderFactory.createEmptyBorder(40, 60, 40, 60));

        JLabel titulo = new JLabel("URNA CIDADÃ");
        titulo.setFont(new Font("Arial", Font.BOLD, 30));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel("Sistema de votação eletrônica");
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 15));
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel instrucao = new JLabel("Digite seu código de acesso:");
        instrucao.setFont(new Font("Arial", Font.BOLD, 16));
        instrucao.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPasswordField campoCodigo = new JPasswordField();
        campoCodigo.setMaximumSize(new Dimension(250, 40));
        campoCodigo.setFont(new Font("Arial", Font.BOLD, 22));
        campoCodigo.setHorizontalAlignment(JTextField.CENTER);

        JButton confirmar = new JButton("CONFIRMAR");
        confirmar.setFont(new Font("Arial", Font.BOLD, 16));
        confirmar.setBackground(new Color(40, 100, 170));
        confirmar.setForeground(Color.WHITE);
        confirmar.setFocusPainted(false);
        confirmar.setAlignmentX(Component.CENTER_ALIGNMENT);

        confirmar.addActionListener(e -> {

            String codigo = new String(campoCodigo.getPassword());

            if (codigo.equals(CODIGO_MESARIO)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Acesso autorizado ao mesário!");

                PainelMesario painel = new PainelMesario(urna, this);
                painel.setVisible(true);

                setVisible(false);

            } else if (codigo.equals(CODIGO_VOTACAO)) {
                if (urna.isVotacaoEncerrada()) {
                    JOptionPane.showMessageDialog(this, "A votação já foi encerrada!");
                    return;
                }

                JOptionPane.showMessageDialog(this, "Acesso autorizado à votação!");
                TelaVotacao.iniciar(urna, this);
                setVisible(false);
            }
        });

        campoCodigo.addActionListener(e -> confirmar.doClick());

        principal.add(titulo);
        principal.add(Box.createVerticalStrut(10));
        principal.add(subtitulo);
        principal.add(Box.createVerticalStrut(50));
        principal.add(instrucao);
        principal.add(Box.createVerticalStrut(15));
        principal.add(campoCodigo);
        principal.add(Box.createVerticalStrut(25));
        principal.add(confirmar);

        add(principal);
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {
                FirebaseConfig.conectar();

                TelaInicial tela = new TelaInicial();

                tela.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        try {
                            FirebaseConfig.encerrar();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                tela.setVisible(true);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Não foi possível conectar ao Firebase: " + e.getMessage());
            }
        });
    }
}
