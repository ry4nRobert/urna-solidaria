package model;
public abstract class Politico {
    // informações dos politicos

    private int numero;
    private int votos = 0;
    private String nome;
    private String partido;
    private String foto;

    public Politico(int numero, String nome, String partido){
        this.numero = numero;
        this.nome = nome;
        this.partido = partido;
    }
    
    public int getNumero() {
        return numero;
    }

    public String getNome() {
        return nome;
    }

    public String getPartido() {
        return partido;
    }

    public int getVotos() {
    return votos;
    }

    public String getFoto() {
        return foto;
    }


public void registrarVoto() {
    votos++;
}
public void restaurarVotos(int votos) {
    this.votos = votos;
}

public void setFoto(String foto) {
    this.foto = foto;
}

public abstract String getCargo();

}
/* Classe politico feita, vereador e presidente herdam de politico */