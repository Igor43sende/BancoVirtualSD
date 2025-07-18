
# Guia Git - Trabalhando com Branches

Este guia contém as principais instruções para trabalhar com branches no Git usando o terminal. Aqui você vai aprender como clonar um repositório, criar uma nova branch a partir de uma branch existente, atualizar sua branch original e enviar suas alterações para o GitHub.

---

## Passo 0: Clone o repositório (caso ainda não tenha)

Se você ainda não clonou o repositório, use o comando abaixo:

```bash
git clone https://github.com/Igor43sende/BancoVirtualSD.git
cd BancoVirtual
```

---

## Passo 1: Acesse a pasta do seu projeto

Abra o terminal e navegue até o diretório local do seu repositório:

```bash
cd caminho/para/seu/projeto
```

---

## Passo 2: Verifique a branch atual

Confira em qual branch você está trabalhando com o comando:

```bash
git branch
```

A branch atual estará marcada com um `*`. Caso não esteja na branch `PlajadorFinanceiroMatComp`, altere para ela usando:

```bash
git checkout main
```

---

## Passo 3: Atualize a branch original (opcional, mas recomendado)

Para garantir que sua branch está sincronizada com o repositório remoto, rode:

```bash
git pull origin main
```

---

## Passo 4: Crie e mude para a nova branch

Para criar uma nova branch (exemplo: `nova-feature`) e já mudar para ela, execute:

```bash
git checkout -b nova-feature
```

> **Nota:** Substitua `nova-feature` pelo nome desejado para sua branch.

---

## Passo 5: Confirme a branch atual

Verifique se está na branch correta com:

```bash
git branch
```

A branch atual estará marcada com `*`.

---

## Passo 6: Faça alterações, commit e envie para o GitHub

Depois de modificar seus arquivos, execute os comandos:

```bash
git add .
git commit -m "Mensagem do commit"
git push -u origin nova-feature
```

---

## Resumo dos comandos principais

```bash
git clone https://github.com/Igor43sende/BancoVirtual.git
cd BancoVirtual
git checkout main
git pull origin main
git checkout -b nome-da-nova-branch
git push -u origin nome-da-nova-branch
```

---

Agora você já sabe como criar e trabalhar com branches no Git pelo terminal!

Se precisar de ajuda com mais comandos ou informações sobre Git, é só pedir!