package controller;

import model.BankAccount;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Database {

    private static final String ARQUIVO_ESTADO = "banco_estado.dat";
    private final Map<String, BankAccount> contas = new ConcurrentHashMap<>();

    public Database() {
        carregarEstado();
    }

    public boolean criarConta(String nome, String cpf, String senha) {
        if (contas.containsKey(cpf)) return false;
        contas.put(cpf, new BankAccount(cpf, nome, senha));
        salvarEstado();
        return true;
    }

    public boolean autenticar(String cpf, String senha) {
        BankAccount conta = contas.get(cpf);
        return conta != null && conta.getSenha().equals(senha);
    }

    public double getSaldo(String cpf) {
        BankAccount conta = contas.get(cpf);
        return conta != null ? conta.getSaldo() : 0.0;
    }

    public boolean transferir(String origemCpf, String destinoCpf, double valor) {
        BankAccount origem = contas.get(origemCpf);
        BankAccount destino = contas.get(destinoCpf);

        if (origem == null || destino == null || origemCpf.equals(destinoCpf)) return false;

        synchronized (origemCpf.compareTo(destinoCpf) < 0 ? origem : destino) {
            synchronized (origemCpf.compareTo(destinoCpf) < 0 ? destino : origem) {
                if (origem.sacar(valor, "Transferência para " + destinoCpf)) {
                    destino.depositar(valor, "Transferência de " + origemCpf);
                    salvarEstado();
                    return true;
                }
                return false;
            }
        }
    }

    public List<String> getExtrato(String cpf) {
        BankAccount conta = contas.get(cpf);
        return conta != null ? conta.getExtrato() : new ArrayList<>();
    }

    public double calcularMontanteTotal() {
        return contas.values().stream().mapToDouble(BankAccount::getSaldo).sum();
    }

    // ✅ Agora públicos para acesso pelo Group.java
    public void salvarEstado() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARQUIVO_ESTADO))) {
            oos.writeObject(contas);
        } catch (IOException e) {
            System.err.println("Erro ao salvar estado: " + e.getMessage());
        }
    }

    public void carregarEstado() {
        File f = new File(ARQUIVO_ESTADO);
        if (!f.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                contas.putAll((Map<String, BankAccount>) obj);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao carregar estado: " + e.getMessage());
        }
    }

    // ✅ Métodos auxiliares usados por Group.java
    public Map<String, BankAccount> getContas() {
        return contas;
    }

    public void setContas(Map<String, BankAccount> novasContas) {
        contas.clear();
        contas.putAll(novasContas);
        salvarEstado();
    }
}
