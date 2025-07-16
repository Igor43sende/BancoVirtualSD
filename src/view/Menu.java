package view;

import remote.RemoteBank;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

public class Menu {
    private static final Scanner scanner = new Scanner(System.in);
    private static RemoteBank grupo; // Agora usando RMI
    private static String clienteLogado = null;

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("172.22.70.26", 1099);
            grupo = (RemoteBank) registry.lookup("BancoVirtual");
            System.out.println("Conectado ao servidor RMI.");
        } catch (Exception e) {
            System.err.println("Erro ao iniciar o sistema distribuído: " + e.getMessage());
            return;
        }

        int opcao; // Variável para armazenar a opção escolhida pelo usuário
        do {
            // Exibe o menu principal
            System.out.println("\n=== Banco Virtual Distribuído ===");
            System.out.println("1 - Criar conta");
            System.out.println("2 - Login");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opção: ");
            opcao = Integer.parseInt(scanner.nextLine()); // Lê a opção digitada e converte para inteiro

            switch (opcao) {
                case 1 -> criarConta(); // Chama o método para criar conta
                case 2 -> login(); // Chama o método para login
                case 0 -> System.out.println("Encerrando o programa..."); // Encerra
                default -> System.out.println("Opção inválida."); // Caso digite algo diferente das opções acima
            }

        } while (opcao != 0); // Continua exibindo o menu até o usuário escolher sair
        try {
            grupo.encerrar();
        } catch (java.rmi.RemoteException e) {
            System.err.println("Erro ao encerrar conexão: " + e.getMessage());
        }
    }

    private static void criarConta() { // Método para criação de conta
        try {
            System.out.print("Nome: ");
            String nome = scanner.nextLine(); // Lê o nome digitado
            System.out.print("CPF: ");
            String cpf = scanner.nextLine(); // Lê o CPF digitado
            System.out.print("Senha: ");
            String senha = scanner.nextLine(); // Lê a senha digitada

            grupo.requisitarCriacaoConta(nome, cpf, senha); // Solicita ao Group a criação da conta
        } catch (Exception e) {
            System.err.println("Erro ao criar conta: " + e.getMessage()); // Exibe erro caso algo dê errado
        }
    }

    private static void login() { // Método para autenticação do cliente
        System.out.print("CPF: ");
        String cpf = scanner.nextLine(); // Lê o CPF digitado
        System.out.print("Senha: ");
        String senha = scanner.nextLine(); // Lê a senha digitada

        try {
            boolean sucesso = grupo.requisitarLogin(cpf, senha); // Solicita login ao Group
            if (sucesso) {
                clienteLogado = cpf; // Armazena o CPF do cliente logado
                System.out.println("Login bem-sucedido!");
                menuLogado(); // Chama o menu exclusivo para cliente logado
            } else {
                System.out.println("Login falhou. Verifique CPF e senha."); // Caso login falhe
            }
        } catch (Exception e) {
            System.err.println("Erro ao autenticar: " + e.getMessage()); // Exibe erro de exceção
        }
    }

    private static void menuLogado() { // Menu exibido somente para clientes logados
        int opcao;
        do {
            // Exibe o menu do cliente logado
            System.out.println("\n=== Menu do Cliente ===");
            System.out.println("1 - Consultar saldo");
            System.out.println("2 - Transferência");
            System.out.println("3 - Ver extrato");
            System.out.println("4 - Consultar montante total do banco");
            System.out.println("0 - Logout");
            System.out.print("Escolha uma opção: ");
            opcao = Integer.parseInt(scanner.nextLine()); // Lê a opção

            switch (opcao) {
                case 1 -> consultarSaldo(); // Consulta saldo
                case 2 -> transferencia(); // Realiza transferência
                case 3 -> verExtrato(); // Visualiza extrato
                case 4 -> consultarMontante(); // Consulta o montante total do banco
                case 0 -> {
                    clienteLogado = null; // Faz logout
                    System.out.println("Logout realizado.");
                }
                default -> System.out.println("Opção inválida."); // Opção incorreta
            }

        } while (clienteLogado != null); // Continua no menu até o usuário deslogar
    }

    private static void consultarSaldo() { // Método para consultar saldo do cliente
        try {
            double saldo = grupo.requisitarSaldo(clienteLogado); // Pede o saldo ao Group
            System.out.printf("Saldo atual: R$ %.2f\n", saldo); // Exibe saldo formatado
        } catch (Exception e) {
            System.err.println("Erro ao consultar saldo: " + e.getMessage());
        }
    }

    private static void transferencia() { // Método para realizar transferência
        try {
            System.out.print("CPF do destinatário: ");
            String destino = scanner.nextLine(); // Lê o CPF do destinatário
            System.out.print("Valor a transferir: ");
            double valor = Double.parseDouble(scanner.nextLine()); // Lê e converte o valor digitado

            boolean ok = grupo.requisitarTransferencia(clienteLogado, destino, valor); // Solicita transferência ao Group
            if (ok) {
                System.out.println("Transferência realizada com sucesso!");
            } else {
                System.out.println("Falha na transferência (verifique saldo ou CPF).");
            }

        } catch (Exception e) {
            System.err.println("Erro na transferência: " + e.getMessage());
        }
    }

    private static void verExtrato() { // Método para exibir extrato do cliente
        try {
            List<String> extrato = grupo.requisitarExtrato(clienteLogado); // Pede extrato ao Group
            System.out.println("\n=== Extrato ===");
            if (extrato.isEmpty()) {
                System.out.println("Nenhuma movimentação."); // Se não houver movimentações
            } else {
                extrato.forEach(System.out::println); // Exibe cada linha do extrato
            }
        } catch (Exception e) {
            System.err.println("Erro ao consultar extrato: " + e.getMessage());
        }
    }

    private static void consultarMontante() { // Método para consultar montante total do banco
        try {
            double total = grupo.requisitarMontanteBanco(); // Solicita montante ao Group
            System.out.printf("Montante total do banco: R$ %.2f\n", total); // Exibe valor formatado
        } catch (Exception e) {
            System.err.println("Erro ao consultar montante: " + e.getMessage());
        }
    }
}
