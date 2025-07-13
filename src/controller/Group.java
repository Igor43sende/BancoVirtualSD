package controller; // Pacote onde está localizada esta classe

import model.BankAccount; // Importa a classe que representa contas bancárias
import org.jgroups.*; // Importa classes principais do JGroups (biblioteca de cluster)
import org.jgroups.blocks.MethodCall; // Importa classe para chamadas de métodos remotos
import org.jgroups.blocks.RpcDispatcher; // Importa classe que gerencia RPC
import org.jgroups.blocks.RequestOptions; // Opções para chamadas remotas
import org.jgroups.util.RspList; // Lista de respostas de múltiplos nós

import java.io.*; // Importa classes para entrada/saída
import java.lang.reflect.Method; // Importa classe para usar reflexão
import java.util.Map; // Importa interface Map
import java.util.ArrayList; // Importa ArrayList
import java.util.List; // Importa List

public class Group extends ReceiverAdapter { // Classe que gerencia o cluster e extende ReceiverAdapter do JGroups

    private JChannel channel; // Canal de comunicação do JGroups
    private RpcDispatcher dispatcher; // Responsável por gerenciar chamadas remotas (RPC)
    private final Database database = new Database(); // Banco de dados local com as contas

    public void iniciar() throws Exception { // Método para iniciar o canal e conectar ao cluster
        channel = new JChannel("config.xml"); // Cria canal usando arquivo de configuração
        dispatcher = new RpcDispatcher(channel, this); // Inicializa o dispatcher para lidar com RPCs
        channel.setReceiver(this); // Define que esta classe irá receber mensagens e eventos
        channel.connect("BancoVirtual"); // Conecta ao cluster com nome "BancoVirtual"

        // Se não for o primeiro nó, solicita o estado atual do cluster
        channel.getState(null, 5000); // timeout de 5 segundos
    }

    @Override
    public void getState(OutputStream output) throws Exception { // Envia estado atual para novos nós
        System.out.println("Enviando estado...");
        database.salvarEstado(); // Salva estado localmente
        try (ObjectOutputStream out = new ObjectOutputStream(output)) {
            out.writeObject(database.getContas()); // Serializa e envia o mapa de contas
        }
    }

    @Override
    public void setState(InputStream input) throws Exception { // Recebe o estado de outro nó do cluster
        System.out.println("Recebendo estado...");
        try (ObjectInputStream in = new ObjectInputStream(input)) {
            Map<String, BankAccount> estado = (Map<String, BankAccount>) in.readObject(); // Lê o estado recebido
            database.setContas(estado); // Atualiza o banco de dados local com o novo estado
            System.out.println("Estado sincronizado com o cluster.");
        }
    }

    // =====================
    // MÉTODOS REMOTOS (RPC)
    // =====================
    public String criarContaRemota(String nome, String cpf, String senha) { // RPC para criar conta
        if (database.criarConta(nome, cpf, senha)) {
            return "Conta criada com sucesso!";
        } else {
            return "CPF já cadastrado.";
        }
    }

    public boolean autenticarRemoto(String cpf, String senha) { // RPC para autenticação
        return database.autenticar(cpf, senha);
    }

    public double consultarSaldoRemoto(String cpf) { // RPC para consultar saldo
        return database.getSaldo(cpf);
    }

    public boolean transferirRemoto(String origem, String destino, double valor) { // RPC para transferência
        return database.transferir(origem, destino, valor);
    }

    public java.util.List<String> extratoRemoto(String cpf) { // RPC para obter extrato
        return database.getExtrato(cpf);
    }

    public double calcularMontanteRemoto() { // RPC para calcular montante total do banco
        return database.calcularMontanteTotal();
    }

    // =========================================
    // MÉTODOS QUE DISPARAM OS RPCs REMOTAMENTE
    // =========================================
    // Exemplo: requisitarCriacaoConta
    public void requisitarCriacaoConta(String nome, String cpf, String senha) throws Exception {
        // Pega uma referência ao método remoto que será chamado nos outros nós do cluster.
        // Aqui usamos reflexão para obter o método "criarContaRemota" que recebe 3 Strings.
        Method m = Group.class.getMethod("criarContaRemota", String.class, String.class, String.class);

        // Cria um objeto MethodCall que encapsula qual método remoto será chamado
        // e quais argumentos serão passados para ele.
        MethodCall call = new MethodCall(m, nome, cpf, senha);

        // Envia a chamada RPC para todos os nós do cluster usando o dispatcher.
        // RequestOptions.SYNC() indica que queremos esperar todas as respostas antes de continuar.
        // RspList<Object> irá armazenar todas as respostas recebidas de cada nó.
        RspList<Object> respostas = dispatcher.callRemoteMethods(null, call, RequestOptions.SYNC());

        // Imprime no console todas as respostas recebidas para fins de depuração
        respostas.entrySet().forEach(System.out::println);
    }
    // Exemplo: requisitarLogin
    public boolean requisitarLogin(String cpf, String senha) throws Exception {
        // Obtém a referência ao método remoto "autenticarRemoto" que recebe dois Strings
        Method m = Group.class.getMethod("autenticarRemoto", String.class, String.class);

        // Cria a chamada do método remoto com os argumentos cpf e senha
        MethodCall call = new MethodCall(m, cpf, senha);

        // Executa a chamada RPC em todos os nós e guarda as respostas.
        // O dispatcher envia para todos e aguarda retorno.
        RspList<Object> respostas = dispatcher.callRemoteMethods(null, call, RequestOptions.SYNC());

        // Percorre todas as respostas, pega o valor retornado por cada nó,
        // e verifica se algum nó retornou true (login bem-sucedido).
        return respostas.entrySet().stream()
                .map(e -> e.getValue().getValue()) // pega o valor retornado (true ou false)
                .anyMatch(o -> Boolean.TRUE.equals(o)); // verifica se algum é true
    }
    // Exemplo: requisitarSaldo
    public double requisitarSaldo(String cpf) throws Exception {
        // Usa reflexão para pegar o método remoto "consultarSaldoRemoto" que recebe um String
        Method m = Group.class.getMethod("consultarSaldoRemoto", String.class);

        // Cria o MethodCall com o CPF como argumento
        MethodCall call = new MethodCall(m, cpf);

        // Envia para todos os nós do cluster e coleta todas as respostas.
        RspList<Object> respostas = dispatcher.callRemoteMethods(null, call, RequestOptions.SYNC());

        // Das respostas, pegamos apenas valores do tipo Double e calculamos o maior saldo recebido.
        // Isso é útil para garantir que pegamos o dado mais atual em um sistema distribuído.
        return respostas.entrySet().stream()
                .map(e -> e.getValue().getValue()) // valor retornado por cada nó
                .filter(o -> o instanceof Double) // filtra apenas valores Double
                .mapToDouble(o -> (Double) o) // converte para double
                .max().orElse(0.0); // retorna o maior saldo ou 0.0 se não houver resposta
    }
    // Exemplo: requisitarTransferencia
    public boolean requisitarTransferencia(String origem, String destino, double valor) throws Exception {
        // Pega referência ao método remoto transferirRemoto que recebe String, String, double
        Method m = Group.class.getMethod("transferirRemoto", String.class, String.class, double.class);

        // Cria a chamada remota
        MethodCall call = new MethodCall(m, origem, destino, valor);

        // Envia para todos os nós do cluster e coleta respostas.
        RspList<Object> resp = dispatcher.callRemoteMethods(null, call, RequestOptions.SYNC());

        // Verifica se algum nó confirmou a transferência (retornou true)
        return resp.entrySet().stream()
                .map(e -> e.getValue().getValue())
                .anyMatch(o -> Boolean.TRUE.equals(o));
    }
    // Exemplo: requisitarExtrato
    public List<String> requisitarExtrato(String cpf) throws Exception {
        // Pega o método remoto "extratoRemoto"
        Method m = Group.class.getMethod("extratoRemoto", String.class);

        // Cria a chamada com argumento CPF
        MethodCall call = new MethodCall(m, cpf);

        // Envia a chamada para todos os nós e coleta respostas
        RspList<Object> resp = dispatcher.callRemoteMethods(null, call, RequestOptions.SYNC());

        // Percorre as respostas e, se encontrar uma lista, retorna como extrato
        for (var e : resp.entrySet()) {
            Object o = e.getValue().getValue();
            if (o instanceof List) {
                return (List<String>) o;
            }
        }
        // Se não houver resposta válida, retorna lista vazia
        return new ArrayList<>();
    }
    // Exemplo: requisitarMontanteBanco
    public double requisitarMontanteBanco() throws Exception {
        // Pega referência ao método remoto "calcularMontanteRemoto" sem parâmetros
        Method m = Group.class.getMethod("calcularMontanteRemoto");

        // Cria a chamada
        MethodCall call = new MethodCall(m);

        // Envia para todos os nós e guarda as respostas
        RspList<Object> respostas = dispatcher.callRemoteMethods(null, call, RequestOptions.SYNC());

        // Retorna o maior valor (montante total) retornado pelos nós
        return respostas.entrySet().stream()
                .map(e -> e.getValue().getValue()) // pega o valor retornado
                .filter(o -> o instanceof Double) // garante que é double
                .mapToDouble(o -> (Double) o) // converte
                .max().orElse(0.0); // retorna maior ou 0.0
    }
    // Encerra o canal JGroups
    public void encerrar() {
        if (channel != null && channel.isConnected()) {
            channel.close();
        }
    }
}
