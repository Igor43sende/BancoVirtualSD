package controller;

import model.BankAccount;
import org.jgroups.*;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.util.RspList;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;


public class Group extends ReceiverAdapter {

    private JChannel channel;
    private RpcDispatcher dispatcher;
    private final Database database = new Database();

    public void iniciar() throws Exception {
        channel = new JChannel("config.xml");
        dispatcher = new RpcDispatcher(channel, this);
        channel.setReceiver(this);
        channel.connect("BancoVirtual");

        // Solicita estado do cluster se não for o primeiro
        channel.getState(null, 5000);
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        System.out.println("Enviando estado...");
        database.salvarEstado();
        try (ObjectOutputStream out = new ObjectOutputStream(output)) {
            out.writeObject(database.getContas());
        }
    }

    @Override
    public void setState(InputStream input) throws Exception {
        System.out.println("Recebendo estado...");
        try (ObjectInputStream in = new ObjectInputStream(input)) {
            Map<String, BankAccount> estado = (Map<String, BankAccount>) in.readObject();
            database.setContas(estado);
            System.out.println("Estado sincronizado com o cluster.");
        }
    }

    // =====================
    // MÉTODOS REMOTOS (RPC)
    // =====================
    public String criarContaRemota(String nome, String cpf, String senha) {
        if (database.criarConta(nome, cpf, senha)) {
            return "Conta criada com sucesso!";
        } else {
            return "CPF já cadastrado.";
        }
    }

    public boolean autenticarRemoto(String cpf, String senha) {
        return database.autenticar(cpf, senha);
    }

    public double consultarSaldoRemoto(String cpf) {
        return database.getSaldo(cpf);
    }

    public boolean transferirRemoto(String origem, String destino, double valor) {
        return database.transferir(origem, destino, valor);
    }

    public java.util.List<String> extratoRemoto(String cpf) {
        return database.getExtrato(cpf);
    }

    public double calcularMontanteRemoto() {
        return database.calcularMontanteTotal();
    }

    // =========================================
    // MÉTODOS QUE DISPARAM OS RPCs REMOTAMENTE
    // =========================================
    public void requisitarCriacaoConta(String nome, String cpf, String senha) throws Exception {
        Method m = Group.class.getMethod("criarContaRemota", String.class, String.class, String.class);
        MethodCall call = new MethodCall(m, nome, cpf, senha);
        RspList<Object> respostas = dispatcher.callRemoteMethods(null, call, RequestOptions.SYNC());
        respostas.entrySet().forEach(System.out::println);
    }

    public boolean requisitarLogin(String cpf, String senha) throws Exception {
        Method m = Group.class.getMethod("autenticarRemoto", String.class, String.class);
        MethodCall call = new MethodCall(m, cpf, senha);
        RspList<Object> respostas = dispatcher.callRemoteMethods(null, call, RequestOptions.SYNC());

        return respostas.entrySet().stream()
                .map(e -> e.getValue().getValue())
                .anyMatch(o -> Boolean.TRUE.equals(o));
    }

    public double requisitarSaldo(String cpf) throws Exception {
        Method m = Group.class.getMethod("consultarSaldoRemoto", String.class);
        MethodCall call = new MethodCall(m, cpf);
        RspList<Object> respostas = dispatcher.callRemoteMethods(null, call, RequestOptions.SYNC());

        return respostas.entrySet().stream()
                .map(e -> e.getValue().getValue())
                .filter(o -> o instanceof Double)
                .mapToDouble(o -> (Double) o)
                .max().orElse(0.0);
    }


    public boolean requisitarTransferencia(String origem, String destino, double valor) throws Exception {
        Method m = Group.class.getMethod("transferirRemoto", String.class, String.class, double.class);
        MethodCall call = new MethodCall(m, origem, destino, valor);
        RspList<Object> resp = dispatcher.callRemoteMethods(null, call, RequestOptions.SYNC());

        return resp.entrySet().stream()
                .map(e -> e.getValue().getValue())
                .anyMatch(o -> Boolean.TRUE.equals(o));
    }

    public List<String> requisitarExtrato(String cpf) throws Exception {
        Method m = Group.class.getMethod("extratoRemoto", String.class);
        MethodCall call = new MethodCall(m, cpf);
        RspList<Object> resp = dispatcher.callRemoteMethods(null, call, RequestOptions.SYNC());

        for (var e : resp.entrySet()) {
            Object o = e.getValue().getValue();
            if (o instanceof List) {
                return (List<String>) o;
            }
        }
        return new ArrayList<>();
    }

    public double requisitarMontanteBanco() throws Exception {
        Method m = Group.class.getMethod("calcularMontanteRemoto");
        MethodCall call = new MethodCall(m);
        RspList<Object> respostas = dispatcher.callRemoteMethods(null, call, RequestOptions.SYNC());

        return respostas.entrySet().stream()
                .map(e -> e.getValue().getValue())
                .filter(o -> o instanceof Double)
                .mapToDouble(o -> (Double) o)
                .max().orElse(0.0);
    }


    // Encerra o canal JGroups
    public void encerrar() {
        if (channel != null && channel.isConnected()) {
            channel.close();
        }
    }
}
