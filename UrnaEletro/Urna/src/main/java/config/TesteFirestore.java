package config;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.api.core.ApiFuture;
import java.util.HashMap;
import java.util.Map;

public class TesteFirestore {

    public static void main(String[] args) {

        try {
            FirebaseConfig.conectar();

            Firestore db = FirebaseConfig.getFirestore();

            Map<String, Object> dados = new HashMap<>();
            dados.put("mensagem", "Conexão funcionando!");
            dados.put("projeto", "Urna Cidadã");

            DocumentReference documento = db.collection("teste_conexao")
                    .document("documento1");
            documento.set(dados).get();

            System.out.println("Documento gravado com sucesso!");

            ApiFuture<DocumentSnapshot> resultado = documento.get();
            DocumentSnapshot snapshot = resultado.get();

            if (snapshot.exists()) {
                System.out.println("Dados recuperados:");
                System.out.println(snapshot.getData());
            }

        } catch (Exception e) {
            System.out.println("Erro ao testar o Firestore:");
            e.printStackTrace();

        } finally {
            try {
                FirebaseConfig.encerrar();
                System.out.println("Conexão encerrada!");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}