package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteBank extends Remote {
    boolean requisitarCriacaoConta(String nome, String cpf, String senha) throws RemoteException;
    boolean requisitarLogin(String cpf, String senha) throws RemoteException;
    double requisitarSaldo(String cpf) throws RemoteException;
    boolean requisitarTransferencia(String origem, String destino, double valor) throws RemoteException;
    List<String> requisitarExtrato(String cpf) throws RemoteException;
    double requisitarMontanteBanco() throws RemoteException;
    void encerrar() throws RemoteException;
}
