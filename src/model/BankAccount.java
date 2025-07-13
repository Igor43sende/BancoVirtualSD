package model;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;


public class BankAccount implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String cpf;
    private final String nome;
    private final String senha;
    private double saldo;
    private final List<String> extrato;

    public BankAccount(String cpf, String nome, String senha) {
        this.cpf = cpf;
        this.nome = nome;
        this.senha = senha;
        this.saldo = 1000.0; //Saldo inicial fixo
        this.extrato = new ArrayList<>();
        extrato.add("Conta criada com saldo inicial de R$1000.00");
    }

    public String getCpf() {
        return cpf;
    }

    public String getNome() {
        return nome;
    }

    public String getSenha() {
        return senha;
    }

    public double getSaldo() {
        return saldo;
    }

    public boolean depositar(double valor, String descricao) {
        if (valor <= 0) return false;
        saldo += valor;
        extrato.add("Depósito: R$" + valor + " | " +descricao);
        return true;
    }

    public boolean sacar(double valor, String descricao) {
        if (valor <= 0 || valor > saldo) {
            extrato.add("Tentativa de saque negada R$: " + valor + " | " + descricao);
            return false;
        }
        saldo -= valor;
        extrato.add("Saque: R$" + valor + " | " + descricao);
        return true;
    }

    public List<String> getExtrato() {
        return new ArrayList<>(extrato); //Protege a lista original
    }
}