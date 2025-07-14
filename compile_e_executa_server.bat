@echo off
echo =====================================
echo Compilando o servidor BancoVirtualSD
echo =====================================

REM Cria o diretório de saída, se não existir
if not exist out (
    mkdir out
)

REM Compila os arquivos .java com o JGroups no classpath
javac -cp ".;lib/jgroups-3.6.4.Final.jar" ^
    -d out ^
    src/controller/*.java ^
    src/model/*.java ^
    src/remote/*.java ^
    src/view/*.java

if %ERRORLEVEL% NEQ 0 (
    echo =====================================
    echo ERRO na compilação!
    echo =====================================
    pause
    exit /b %ERRORLEVEL%
)

echo =====================================
echo Compilação concluída com sucesso!
echo =====================================

REM Executa o servidor
echo Iniciando servidor...
java -cp ".;out;lib/jgroups-3.6.4.Final.jar" remote.BankServer

pause
