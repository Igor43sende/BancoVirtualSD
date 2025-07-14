package remote;

import controller.Group; // <-- IMPORT NECESSÁRIO
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class BankServer {
    public static void main(String[] args) {
        try {
            Group group = new Group();     // instancia Group corretamente
            group.iniciar();               // inicia o cluster JGroups
            RemoteBank banco = new RemoteBankImpl(group);  // injeta o Group
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("BancoVirtual", banco);
            System.out.println("Servidor BancoVirtualSD rodando via RMI.");
        } catch (Exception e) {
            System.err.println("Erro no servidor RMI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}