package controller; // Pacote onde está localizada esta classe

import model.BankAccount; // Importa a classe que representa contas bancárias

import java.io.*; // Importa classes para entrada/saída (serialização)
import java.util.*; // Importa classes utilitárias (List, Map etc.)
import java.util.concurrent.ConcurrentHashMap; // Importa implementação thread-safe de Map

public class Database { // Classe que gerencia contas bancárias e persistência de dados

    private static final String ARQUIVO_ESTADO = "banco_estado.dat"; // Nome do arquivo onde será salvo o estado
    private final Map<String, BankAccount> contas = new ConcurrentHashMap<>(); // Mapa de contas, chaveado pelo CPF

    public Database() {
        carregarEstado(); // Ao iniciar, tenta carregar o estado salvo anteriormente
    }

    public boolean criarConta(String nome, String cpf, String senha) {
        // Verifica se já existe uma conta com o mesmo CPF
        if (contas.containsKey(cpf)) return false;

        // Se não existe, cria nova conta e adiciona ao mapa
        contas.put(cpf, new BankAccount(cpf, nome, senha));

        salvarEstado(); // Persiste as alterações
        return true; // Retorna sucesso
    }

    public boolean autenticar(String cpf, String senha) {
        BankAccount conta = contas.get(cpf); // Busca conta pelo CPF
        // Retorna true se a conta existir e a senha estiver correta
        return conta != null && conta.getSenha().equals(senha);
    }

    public double getSaldo(String cpf) {
        BankAccount conta = contas.get(cpf); // Busca conta
        // Retorna saldo se existir, ou 0.0 caso não exista
        return conta != null ? conta.getSaldo() : 0.0;
    }

    public boolean transferir(String origemCpf, String destinoCpf, double valor) {
        BankAccount origem = contas.get(origemCpf); // Conta de origem
        BankAccount destino = contas.get(destinoCpf); // Conta de destino

        // Verifica se ambas existem e não são a mesma conta
        if (origem == null || destino == null || origemCpf.equals(destinoCpf)) return false;

        // Ordena locks para evitar deadlock:
        // quem tem CPF "menor" trava primeiro
        synchronized (origemCpf.compareTo(destinoCpf) < 0 ? origem : destino) {
            synchronized (origemCpf.compareTo(destinoCpf) < 0 ? destino : origem) {
                // Tenta sacar o valor da conta de origem
                if (origem.sacar(valor, "Transferência para " + destinoCpf)) {
                    // Se conseguiu sacar, deposita na conta destino
                    destino.depositar(valor, "Transferência de " + origemCpf);
                    salvarEstado(); // Persiste as alterações
                    return true;
                }
                return false; // Não tinha saldo suficiente
            }
        }
    }

    public List<String> getExtrato(String cpf) {
        BankAccount conta = contas.get(cpf); // Busca conta
        // Retorna extrato ou lista vazia se não existir
        return conta != null ? conta.getExtrato() : new ArrayList<>();
    }

    public double calcularMontanteTotal() {
        // Soma todos os saldos de todas as contas
        return contas.values().stream().mapToDouble(BankAccount::getSaldo).sum();
    }

    // ================================
    // Métodos para salvar e carregar estado do banco
    // ================================

    public void salvarEstado() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARQUIVO_ESTADO))) {
            oos.writeObject(contas); // Serializa o mapa de contas e salva no arquivo
        } catch (IOException e) {
            System.err.println("Erro ao salvar estado: " + e.getMessage());
        }
    }

    public void carregarEstado() {
        File f = new File(ARQUIVO_ESTADO); // Cria objeto File
        if (!f.exists()) return; // Se arquivo não existe, nada a fazer

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Object obj = ois.readObject(); // Lê objeto serializado
            if (obj instanceof Map) {
                // Se for um Map, faz cast e adiciona ao mapa local
                contas.putAll((Map<String, BankAccount>) obj);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao carregar estado: " + e.getMessage());
        }
    }

    // ================================
    // Métodos auxiliares usados pelo Group.java
    // ================================

    public Map<String, BankAccount> getContas() {
        return contas; // Retorna o mapa de contas (referência)
    }

    public void setContas(Map<String, BankAccount> novasContas) {
        contas.clear(); // Limpa mapa atual
        contas.putAll(novasContas); // Copia todas as contas recebidas
        salvarEstado(); // Persiste as alterações
    }
}
