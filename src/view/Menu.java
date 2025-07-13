package view;

import controller.Group;
import java.util.List;
import java.util.Scanner;

public class Menu {

    private static final Scanner scanner = new Scanner(System.in);
    private static Group grupo;
    private static String clienteLogado = null;

    public static void main(String[] args) {
        try {
            grupo = new Group();
            grupo.iniciar();
        } catch (Exception e) {
            System.err.println("Erro ao iniciar o sistema distribuído: " + e.getMessage());
            return;
        }

        int opcao;
        do {
            System.out.println("\n=== Banco Virtual Distribuído ===");
            System.out.println("1 - Criar conta");
            System.out.println("2 - Login");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opção: ");
            opcao = Integer.parseInt(scanner.nextLine());

            switch (opcao) {
                case 1 -> criarConta();
                case 2 -> login();
                case 0 -> System.out.println("Encerrando o programa...");
                default -> System.out.println("Opção inválida.");
            }

        } while (opcao != 0);

        grupo.encerrar();
    }

    private static void criarConta() {
        try {
            System.out.print("Nome: ");
            String nome = scanner.nextLine();
            System.out.print("CPF: ");
            String cpf = scanner.nextLine();
            System.out.print("Senha: ");
            String senha = scanner.nextLine();

            grupo.requisitarCriacaoConta(nome, cpf, senha);
        } catch (Exception e) {
            System.err.println("Erro ao criar conta: " + e.getMessage());
        }
    }

    private static void login() {
        System.out.print("CPF: ");
        String cpf = scanner.nextLine();
        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        try {
            boolean sucesso = grupo.requisitarLogin(cpf, senha);
            if (sucesso) {
                clienteLogado = cpf;
                System.out.println("Login bem-sucedido!");
                menuLogado();
            } else {
                System.out.println("Login falhou. Verifique CPF e senha.");
            }
        } catch (Exception e) {
            System.err.println("Erro ao autenticar: " + e.getMessage());
        }
    }

    private static void menuLogado() {
        int opcao;
        do {
            System.out.println("\n=== Menu do Cliente ===");
            System.out.println("1 - Consultar saldo");
            System.out.println("2 - Transferência");
            System.out.println("3 - Ver extrato");
            System.out.println("4 - Consultar montante total do banco");
            System.out.println("0 - Logout");
            System.out.print("Escolha uma opção: ");
            opcao = Integer.parseInt(scanner.nextLine());

            switch (opcao) {
                case 1 -> consultarSaldo();
                case 2 -> transferencia();
                case 3 -> verExtrato();
                case 4 -> consultarMontante();
                case 0 -> {
                    clienteLogado = null;
                    System.out.println("Logout realizado.");
                }
                default -> System.out.println("Opção inválida.");
            }

        } while (clienteLogado != null);
    }

    private static void consultarSaldo() {
        try {
            double saldo = grupo.requisitarSaldo(clienteLogado);
            System.out.printf("Saldo atual: R$ %.2f\n", saldo);
        } catch (Exception e) {
            System.err.println("Erro ao consultar saldo: " + e.getMessage());
        }
    }

    private static void transferencia() {
        try {
            System.out.print("CPF do destinatário: ");
            String destino = scanner.nextLine();
            System.out.print("Valor a transferir: ");
            double valor = Double.parseDouble(scanner.nextLine());

            boolean ok = grupo.requisitarTransferencia(clienteLogado, destino, valor);
            if (ok) {
                System.out.println("Transferência realizada com sucesso!");
            } else {
                System.out.println("Falha na transferência (verifique saldo ou CPF).");
            }

        } catch (Exception e) {
            System.err.println("Erro na transferência: " + e.getMessage());
        }
    }

    private static void verExtrato() {
        try {
            List<String> extrato = grupo.requisitarExtrato(clienteLogado);
            System.out.println("\n=== Extrato ===");
            if (extrato.isEmpty()) {
                System.out.println("Nenhuma movimentação.");
            } else {
                extrato.forEach(System.out::println);
            }
        } catch (Exception e) {
            System.err.println("Erro ao consultar extrato: " + e.getMessage());
        }
    }

    private static void consultarMontante() {
        try {
            double total = grupo.requisitarMontanteBanco();
            System.out.printf("Montante total do banco: R$ %.2f\n", total);
        } catch (Exception e) {
            System.err.println("Erro ao consultar montante: " + e.getMessage());
        }
    }
}
