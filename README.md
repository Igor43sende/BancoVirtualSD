# BancoVirtual

Sistema Distribuído de Banco Virtual desenvolvido como trabalho prático para a disciplina de **Sistemas Distribuídos I** (IFMG - Ciência da Computação).

## 📚 Descrição

Este sistema simula um serviço de e-banking com múltiplos servidores replicados usando **JGroups** para comunicação entre réplicas e interface CLI para interação com o usuário.

## ⚙️ Funcionalidades (Etapa 1)

- Criação de contas bancárias únicas (sem duplicação de cliente).
- Saldo inicial de R$ 1.000,00 por conta.
- Autenticação via identificador e senha.
- Transferência entre contas.
- Consulta de saldo e extrato.
- Persistência em disco do estado do sistema.
- Comunicação entre servidores com JGroups.

## 🏗️ Tecnologias

- Java
- JGroups 3.6.4.Final
- Terminal CLI
- Sistema de persistência por serialização de objetos

## 🚀 Execução

Veja o arquivo [`LEIAME.txt`](LEIAME.txt) para instruções de compilação e execução via terminal.

## 📌 Próximos passos (Etapa 2)

- Separação por camadas usando múltiplos canais JGroups
- Comunicação RPC (Java RMI ou gRPC) com gateway
- Reintegração de estado em caso de falha
- Segurança: autenticação reforçada e criptografia
- Auditoria e visualização do montante total
