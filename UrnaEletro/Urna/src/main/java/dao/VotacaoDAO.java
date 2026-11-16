package dao;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentSnapshot;
import model.Urna;
import java.util.HashMap;
import java.util.Map;

public class VotacaoDAO {

    private final Firestore db;

    public VotacaoDAO(Firestore db) {
        this.db = db;
    }

    public void salvarEstado(Urna urna) throws Exception {

        Map<String, Object> dados = new HashMap<>();

        dados.put("votosBrancosVereador", urna.getVotosBrancosVereador());
        dados.put("votosNulosVereador", urna.getVotosNulosVereador());
        dados.put("votosBrancosPresidente", urna.getVotosBrancosPresidente());
        dados.put("votosNulosPresidente", urna.getVotosNulosPresidente());
        dados.put("votacaoEncerrada", urna.isVotacaoEncerrada());

        db.collection("votacao")
          .document("geral")
          .collection("estado")
          .document("atual")
          .set(dados)
          .get();
    }

    public void carregarEstado(Urna urna) throws Exception {

        DocumentSnapshot documento = db.collection("votacao")
            .document("geral")
            .collection("estado")
            .document("atual")
            .get()
            .get();

        if (!documento.exists()) {
            return;
        }

        Long bv = documento.getLong("votosBrancosVereador");
        Long nv = documento.getLong("votosNulosVereador");
        Long bp = documento.getLong("votosBrancosPresidente");
        Long np = documento.getLong("votosNulosPresidente");

        boolean encerrada = Boolean.TRUE.equals(
            documento.getBoolean("votacaoEncerrada")
        );

        urna.restaurarVotosBrancosENulos(
                bv == null ? 0 : bv.intValue(),
                nv == null ? 0 : nv.intValue(),
                bp == null ? 0 : bp.intValue(),
                np == null ? 0 : np.intValue()
        );

        urna.restaurarEstadoVotacao(encerrada);
    }
}
