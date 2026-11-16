package model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Urna {

    private ArrayList<Vereador> vereadores;
    private ArrayList<Presidente> presidentes;
    private int votosBrancosVereador;
    private int votosNulosVereador;
    private int votosBrancosPresidente;
    private int votosNulosPresidente;
    private boolean cadastrosBloqueados = false;
    private boolean votacaoEncerrada = false;

    public Urna() {
        vereadores = new ArrayList<>();
        presidentes = new ArrayList<>();
    }

    public boolean adicionarVereador(Vereador vereador) {

        if (cadastrosBloqueados) {
            return false;
        }

        if (buscarVereador(vereador.getNumero()) != null) {
            return false;
        }

        vereadores.add(vereador);
        return true;
    }

    public boolean adicionarPresidente(Presidente presidente) {

        if (cadastrosBloqueados) {
            return false;
        }

        if (buscarPresidente(presidente.getNumero()) != null) {
            return false;
        }

        presidentes.add(presidente);
        return true;
    }

    public boolean removerVereador(Vereador vereador) {

        if (cadastrosBloqueados) {
            return false;
        }

        return vereadores.remove(vereador);
    }

    public boolean removerPresidente(Presidente presidente) {

        if (cadastrosBloqueados) {
            return false;
        }

        return presidentes.remove(presidente);
    }

    public ArrayList<Vereador> getVereadores() {
        return vereadores;
    }

    public ArrayList<Presidente> getPresidentes() {
        return presidentes;
    }

    public int getVotosBrancosVereador() {
        return votosBrancosVereador;
    }

    public int getVotosNulosVereador() {
        return votosNulosVereador;
    }

    public int getVotosBrancosPresidente() {
        return votosBrancosPresidente;
    }

    public int getVotosNulosPresidente() {
        return votosNulosPresidente;
    }

    public Vereador buscarVereador(int numero) {

        for (Vereador vereador : vereadores) {

            if (vereador.getNumero() == numero) {
                return vereador;
            }

        }

        return null;
    }

    public Presidente buscarPresidente(int numero) {

        for (Presidente presidente : presidentes) {

            if (presidente.getNumero() == numero) {
                return presidente;
            }

        }

        return null;
    }

    public boolean votarVereador(Eleitor eleitor, int numero) {

        if (votacaoEncerrada) {
            return false;
        }

        // Verifica se já votou para vereador
        if (eleitor.jaVotouVereador()) {
            return false;
        }

        Vereador vereador = buscarVereador(numero);

        if (vereador != null) {
            vereador.registrarVoto();
            eleitor.registrarVotoVereador();
            return true;
        }

        return false;
    }

    public boolean votarPresidente(Eleitor eleitor, int numero) {

        if (votacaoEncerrada) {
            return false;
        }

        if (!eleitor.jaVotouVereador()) {
            return false;
        }

        // Impede votar novamente para presidente
        if (eleitor.jaVotouPresidente()) {
            return false;
        }

        Presidente presidente = buscarPresidente(numero);

        if (presidente != null) {
            presidente.registrarVoto();
            eleitor.registrarVotoPresidente();
            return true;
        }

        return false;
    }

    public boolean votarBrancoVereador(Eleitor eleitor) {

        if (votacaoEncerrada) {
            return false;
        }

        if (eleitor.jaVotouVereador()) {
            return false;
        }

        votosBrancosVereador++;
        eleitor.registrarVotoVereador();

        return true;
    }

    public boolean votarBrancoPresidente(Eleitor eleitor) {

        if (votacaoEncerrada) {
            return false;
        }

        if (!eleitor.jaVotouVereador()) {
            return false;
        }

        if (eleitor.jaVotouPresidente()) {
            return false;
        }

        votosBrancosPresidente++;
        eleitor.registrarVotoPresidente();

        return true;
    }

    public boolean votarNuloVereador(Eleitor eleitor, int numero) {

        if (votacaoEncerrada) {
            return false;
        }

        if (eleitor.jaVotouVereador()) {
            return false;
        }

        Vereador vereador = buscarVereador(numero);

        if (vereador != null) {
            return false;
        }

        votosNulosVereador++;
        eleitor.registrarVotoVereador();

        return true;
    }

    public boolean votarNuloPresidente(Eleitor eleitor, int numero) {

        if (votacaoEncerrada) {
            return false;
        }

        if (!eleitor.jaVotouVereador()) {
            return false;
        }

        if (eleitor.jaVotouPresidente()) {
            return false;
        }

        Presidente presidente = buscarPresidente(numero);

        if (presidente != null) {
            return false;
        }

        votosNulosPresidente++;
        eleitor.registrarVotoPresidente();

        return true;
    }

    public void bloquearCadastros() {
        cadastrosBloqueados = true;
    }

    public void desbloquearCadastros() {
        cadastrosBloqueados = false;
    }

    public boolean isCadastrosBloqueados() {
        return cadastrosBloqueados;
    }

    public String apurarVereador() {
    return apurar("VEREADOR", vereadores, votosBrancosVereador, votosNulosVereador);
}

public String apurarPresidente() {
    return apurar("PRESIDENTE", presidentes, votosBrancosPresidente, votosNulosPresidente);
}

// Método único que serve para qualquer cargo (polimorfismo)
private String apurar(String cargo, List<? extends Politico> candidatos, int brancos, int nulos) {

    StringBuilder resultado = new StringBuilder();

    ArrayList<Politico> ranking = new ArrayList<>(candidatos);
    ranking.sort(Comparator.comparingInt(Politico::getVotos).reversed());

    int validos = 0;

    for (Politico p : ranking) {
        validos += p.getVotos();
    }

    int total = validos + brancos + nulos;

    resultado.append("===== APURAÇÃO: ").append(cargo).append(" =====\n\n");

    if (total == 0) {
        resultado.append("Nenhum voto registrado.\n");
        return resultado.toString();
    }

    int posicao = 1;

    for (Politico p : ranking) {
        double percentual = p.getVotos() * 100.0 / total;

        resultado.append(String.format(
            "%dº %s (%d) - %s: %d votos (%.1f%%)%n",
            posicao++, p.getNome(), p.getNumero(), p.getPartido(),
            p.getVotos(), percentual
        ));
    }

    resultado.append(String.format(
        "Brancos: %d (%.1f%%)%n", brancos, brancos * 100.0 / total
    ));

    resultado.append(String.format(
        "Nulos: %d (%.1f%%)%n", nulos, nulos * 100.0 / total
    ));

    resultado.append("Total de votos: ").append(total).append("\n");

    if (validos == 0) {
        resultado.append("Resultado: sem votos válidos.\n");

    } else if (ranking.size() > 1 &&
            ranking.get(0).getVotos() == ranking.get(1).getVotos()) {

        resultado.append("Resultado: EMPATE entre os mais votados.\n");

    } else {
        Politico vencedor = ranking.get(0);

        resultado.append("Vencedor: ")
                 .append(vencedor.getNome())
                 .append(" (")
                 .append(vencedor.getCargo())
                 .append(")\n");
    }

    return resultado.toString();
}

public void restaurarVotosBrancosENulos(
        int brancosVereador,
        int nulosVereador,
        int brancosPresidente,
        int nulosPresidente) {

    this.votosBrancosVereador = brancosVereador;
    this.votosNulosVereador = nulosVereador;
    this.votosBrancosPresidente = brancosPresidente;
    this.votosNulosPresidente = nulosPresidente;
}

public void restaurarEstadoVotacao(boolean encerrada) {
    this.votacaoEncerrada = encerrada;
    this.cadastrosBloqueados = encerrada;
}


public void resetarVotacao() {

    // Zera votos dos candidatos
    for (Vereador vereador : vereadores) {
        vereador.restaurarVotos(0);
    }

    for (Presidente presidente : presidentes) {
        presidente.restaurarVotos(0);
    }

    // Zera votos brancos e nulos
    votosBrancosVereador = 0;
    votosNulosVereador = 0;
    votosBrancosPresidente = 0;
    votosNulosPresidente = 0;

    // Reabre a votação
    votacaoEncerrada = false;
    cadastrosBloqueados = false;
}

    public void encerrarVotacao() {
        votacaoEncerrada = true;
        bloquearCadastros();
    }

    public boolean isVotacaoEncerrada() {
        return votacaoEncerrada;
    }

}

/* Ryan, já fiz o update da grande parte das coisas, da uma polida no código*/