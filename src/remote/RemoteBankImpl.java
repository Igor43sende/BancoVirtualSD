package remote;

import controller.Group;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.List;

public class RemoteBankImpl extends UnicastRemoteObject implements RemoteBank {
    private final Group group;

    public RemoteBankImpl(Group group) throws RemoteException {
        super();
        this.group = group;
    }

    @Override
    public boolean requisitarCriacaoConta(String nome, String cpf, String senha) throws RemoteException {
        System.out.println("[CLIENTE] Requisição de criação de conta - Nome: " + nome + ", CPF: " + cpf);
        try {
            boolean sucesso = "Conta criada com sucesso!".equals(group.requisitarCriacaoConta(nome, cpf, senha));
            System.out.println("[CLIENTE] Resultado da criação de conta: " + (sucesso ? "SUCESSO" : "FALHA"));
            return sucesso;
        } catch (Exception e) {
            System.err.println("Erro ao criar conta remotamente: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean requisitarLogin(String cpf, String senha) throws RemoteException {
        System.out.println("[CLIENTE] Requisição de login - CPF: " + cpf);
        try {
            boolean sucesso = "Login realizado com sucesso!".equals(group.requisitarLogin(cpf, senha));
            System.out.println("[CLIENTE] Resultado do login: " + (sucesso ? "SUCESSO" : "FALHA"));
            return sucesso;
        } catch (Exception e) {
            System.err.println("Erro ao realizar login remotamente: " + e.getMessage());
            return false;
        }
    }

    @Override
    public double requisitarSaldo(String cpf) throws RemoteException {
        System.out.println("[CLIENTE] Requisição de saldo - CPF: " + cpf);
        try {
            double saldo = group.requisitarSaldo(cpf);
            System.out.println("[CLIENTE] Saldo retornado: " + saldo);
            return saldo;
        } catch (Exception e) {
            System.err.println("Erro ao consultar saldo remotamente: " + e.getMessage());
            return 0.0;
        }
    }

    @Override
    public boolean requisitarTransferencia(String origem, String destino, double valor) throws RemoteException {
        System.out.println("[CLIENTE] Requisição de transferência - Origem: " + origem + ", Destino: " + destino + ", Valor: " + valor);
        try {
            boolean sucesso = group.requisitarTransferencia(origem, destino, valor);
            System.out.println("[CLIENTE] Resultado da transferência: " + (sucesso ? "SUCESSO" : "FALHA"));
            return sucesso;
        } catch (Exception e) {
            System.err.println("Erro ao realizar transferência remota: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> requisitarExtrato(String cpf) throws RemoteException {
        System.out.println("[CLIENTE] Requisição de extrato - CPF: " + cpf);
        try {
            List<String> extrato = group.requisitarExtrato(cpf);
            System.out.println("[CLIENTE] Extrato retornado com " + extrato.size() + " linhas.");
            return extrato;
        } catch (Exception e) {
            System.err.println("Erro ao consultar extrato remotamente: " + e.getMessage());
            return List.of("Erro ao obter extrato.");
        }
    }

    @Override
    public double requisitarMontanteBanco() throws RemoteException {
        System.out.println("[CLIENTE] Requisição do montante total do banco.");
        try {
            double montante = group.requisitarMontanteBanco();
            System.out.println("[CLIENTE] Montante total retornado: " + montante);
            return montante;
        } catch (Exception e) {
            System.err.println("Erro ao consultar montante do banco remotamente: " + e.getMessage());
            return 0.0;
        }
    }

    @Override
    public void encerrar() throws RemoteException {
        System.out.println("[CLIENTE] Requisição para encerrar o servidor.");
        group.encerrar();
    }
}
