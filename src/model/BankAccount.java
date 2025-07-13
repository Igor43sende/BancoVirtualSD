package model; // Pacote onde está localizada esta classe

import java.util.ArrayList; // Importa ArrayList para armazenar o extrato
import java.util.List; // Importa List (interface)
import java.io.Serializable; // Importa Serializable para permitir que objetos sejam serializados

public class BankAccount implements Serializable { // Classe que representa uma conta bancária e pode ser serializada

    private static final long serialVersionUID = 1L; // ID de versão para garantir compatibilidade na serialização
    private final String cpf; // CPF do titular da conta
    private final String nome; // Nome do titular
    private final String senha; // Senha da conta
    private double saldo; // Saldo atual da conta
    private final List<String> extrato; // Lista de movimentações (extrato)

    public BankAccount(String cpf, String nome, String senha) { // Construtor da conta
        this.cpf = cpf; // Define o CPF
        this.nome = nome; // Define o nome
        this.senha = senha; // Define a senha
        this.saldo = 1000.0; // Saldo inicial fixo de R$1000,00
        this.extrato = new ArrayList<>(); // Cria a lista para registrar extrato
        extrato.add("Conta criada com saldo inicial de R$1000.00"); // Adiciona registro inicial ao extrato
    }

    public String getCpf() { // Retorna o CPF do titular
        return cpf;
    }

    public String getNome() { // Retorna o nome do titular
        return nome;
    }

    public String getSenha() { // Retorna a senha da conta
        return senha;
    }

    public double getSaldo() { // Retorna o saldo atual
        return saldo;
    }

    public boolean depositar(double valor, String descricao) { // Realiza depósito
        if (valor <= 0) return false; // Não permite depósito de valores negativos ou zero
        saldo += valor; // Soma o valor ao saldo
        extrato.add("Depósito: R$" + valor + " | " + descricao); // Registra operação no extrato
        return true; // Retorna true para indicar sucesso
    }

    public boolean sacar(double valor, String descricao) { // Realiza saque
        if (valor <= 0 || valor > saldo) { // Verifica valor inválido ou saldo insuficiente
            extrato.add("Tentativa de saque negada R$: " + valor + " | " + descricao); // Registra tentativa negada
            return false; // Retorna false para indicar falha
        }
        saldo -= valor; // Subtrai valor do saldo
        extrato.add("Saque: R$" + valor + " | " + descricao); // Registra saque no extrato
        return true; // Retorna true para indicar sucesso
    }

    public List<String> getExtrato() { // Retorna uma cópia do extrato
        return new ArrayList<>(extrato); // Protege a lista original contra modificações externas
    }
}
