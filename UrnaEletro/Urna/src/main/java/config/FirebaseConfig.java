package config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import java.io.IOException;

public class FirebaseConfig {

    private static Firestore firestore;

    public static void conectar() throws IOException {

            if (FirebaseApp.getApps().isEmpty()) {

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .setProjectId("urna-ele")
                        .build();

                FirebaseApp.initializeApp(options);
            }

        firestore = FirestoreClient.getFirestore();
    }

    public static Firestore getFirestore() {
        return firestore;
    }

    public static void encerrar() throws Exception {

        if (firestore != null) {
            firestore.close();
            firestore = null;
        }
    }
}