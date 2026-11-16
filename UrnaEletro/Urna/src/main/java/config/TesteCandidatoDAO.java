package config;

import dao.CandidatoDAO;
import model.Vereador;

public class TesteCandidatoDAO {
    
    public static void main(String[] args) {

        try {
            FirebaseConfig.conectar();
            CandidatoDAO dao = new CandidatoDAO(
            FirebaseConfig.getFirestore()
            );

            Vereador vereador = new Vereador(
                12345,
                "João Teste",
                "Partido Azul"
            );

            dao.salvar(vereador);

            System.out.println("Candidato salvo com sucesso!");

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