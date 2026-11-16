package config;

public class TesteFirebase {

    public static void main(String[] args) {
        try {
            FirebaseConfig.conectar();

            System.out.println("Conexão com Firebase realizada!");
            
        } catch (Exception e) {
            System.out.println("Erro ao conectar com Firebase:");
            e.printStackTrace();
        }
    }
}