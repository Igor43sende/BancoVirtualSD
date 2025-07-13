#!/bin/bash

# Compila os arquivos .java com JGroups no classpath
mkdir -p out

javac -cp "lib/jgroups-3.6.4.Final.jar" -d out \
    src/model/BankAccount.java \
    src/controller/Database.java \
    src/controller/Group.java \
    src/view/Menu.java

echo "Compilação concluída com sucesso."
