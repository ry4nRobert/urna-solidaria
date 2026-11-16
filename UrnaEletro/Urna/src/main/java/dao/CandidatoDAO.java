package dao;

import com.google.cloud.firestore.Firestore;
import model.Politico;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import model.Vereador;
import model.Presidente;
import java.util.HashMap;
import java.util.Map;

public class CandidatoDAO {

    private final Firestore db;

    public CandidatoDAO(Firestore db) {
        this.db = db;
    }

    public void salvar(Politico politico) throws Exception {

        String colecao;

        if (politico.getCargo().equals("Vereador")) {
            colecao = "vereadores";
        } else if (politico.getCargo().equals("Presidente")) {
            colecao = "presidentes";
        } else {
            throw new IllegalArgumentException("Cargo inválido.");
        }

        Map<String, Object> dados = new HashMap<>();

        dados.put("numero", politico.getNumero());
        dados.put("nome", politico.getNome());
        dados.put("partido", politico.getPartido());
        dados.put("cargo", politico.getCargo());
        dados.put("votos", politico.getVotos());

        if (politico.getFoto() != null) {
            dados.put("foto", politico.getFoto());
        }

        db.collection(colecao)
          .document("cadastro")
          .collection("candidatos")
          .document(String.valueOf(politico.getNumero()))
          .set(dados)
          .get();
    }

    public List<Politico> listar(String cargo) throws Exception {

        String colecao = cargo.equalsIgnoreCase("Vereador")
            ? "vereadores" : "presidentes";

        List<Politico> candidatos = new ArrayList<>();

        for (QueryDocumentSnapshot documento : db.collection(colecao)
                .document("cadastro")
                .collection("candidatos")
                .get().get().getDocuments()) {

            int numero = documento.getLong("numero").intValue();
            String nome = documento.getString("nome");
            String partido = documento.getString("partido");

            Long votosSalvos = documento.getLong("votos");
            int votos = votosSalvos == null ? 0 : votosSalvos.intValue();

            Politico candidato;

            if (cargo.equalsIgnoreCase("Vereador")) {
                candidato = new Vereador(numero, nome, partido);
            } else {
                candidato = new Presidente(numero, nome, partido);
            }

            candidato.restaurarVotos(votos);
            candidato.setFoto(documento.getString("foto"));
            candidatos.add(candidato);
        }

        return candidatos;
    }

    public void remover(Politico politico) throws Exception {

        String colecao;

        if (politico.getCargo().equals("Vereador")) {
            colecao = "vereadores";
        } else if (politico.getCargo().equals("Presidente")) {
            colecao = "presidentes";
        } else {
            throw new IllegalArgumentException("Cargo inválido.");
        }

        db.collection(colecao)
        .document("cadastro")
        .collection("candidatos")
        .document(String.valueOf(politico.getNumero()))
        .delete()
        .get();
    }


    public void atualizar(Politico antigo, Politico atualizado) throws Exception {

        if (!antigo.getCargo().equals(atualizado.getCargo())) {
            throw new IllegalArgumentException("Não é possível alterar o cargo.");
        }

        // Salva os novos dados primeiro.
        salvar(atualizado);

        // Se o número mudou, exclui o documento antigo.
        if (antigo.getNumero() != atualizado.getNumero()) {
            remover(antigo);
        }
    }
}
