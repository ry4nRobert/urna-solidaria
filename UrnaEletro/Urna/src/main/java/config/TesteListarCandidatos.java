package config;

import dao.CandidatoDAO;
import model.Politico;
import java.util.List;

public class TesteListarCandidatos {

    public static void main(String[] args) {

        try {
            FirebaseConfig.conectar();

            CandidatoDAO dao = new CandidatoDAO(
                FirebaseConfig.getFirestore()
            );

            List<Politico> candidatos = dao.listar("Vereador");

            for (Politico candidato : candidatos) {
                System.out.println(
                    candidato.getNumero() + " - " +
                    candidato.getNome() + " - " +
                    candidato.getPartido()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                FirebaseConfig.encerrar();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}