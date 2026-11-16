# 🗳️ Urna Cidadã

Sistema de **votação eletrônica** em Java com interface gráfica (Swing), que simula o funcionamento de uma urna: cadastro de candidatos com foto, votação para **Vereador** e **Presidente**, apuração dos resultados e persistência dos dados na nuvem com **Firebase Firestore**.

Projeto desenvolvido para a disciplina **Projeto de Extensão III**.

---

## 📋 Sumário

- [Funcionalidades](#-funcionalidades)
- [Tecnologias utilizadas](#-tecnologias-utilizadas)
- [Estrutura do projeto](#-estrutura-do-projeto)
- [Pré-requisitos](#-pré-requisitos)
- [Configurando o Firebase](#-configurando-o-firebase)
- [Como executar](#-como-executar)
- [Como usar](#-como-usar)
- [Estrutura do banco de dados](#-estrutura-do-banco-de-dados)
- [Fotos dos candidatos](#-fotos-dos-candidatos)
- [Regras de negócio](#-regras-de-negócio)
- [Segurança](#-segurança)
- [Problemas comuns](#-problemas-comuns)
- [Melhorias futuras](#-melhorias-futuras)
- [Autores](#-autores)

---

## ✨ Funcionalidades

**Para o eleitor**
- Acesso à votação por código de acesso.
- Votação em duas etapas: primeiro **Vereador** (5 dígitos), depois **Presidente** (2 dígitos).
- Teclado numérico com os botões **BRANCO**, **CORRIGE** e **CONFIRMA**.
- Exibição da **foto do candidato** assim que o número completo é digitado.
- Confirmação do voto mostrando nome e partido do candidato.
- Voto **branco** e voto **nulo** (número inexistente).

**Para o mesário**
- Acesso ao painel por código de acesso próprio.
- Adicionar, alterar e remover candidatos (com foto opcional).
- Bloquear e desbloquear cadastros.
- Encerrar a votação.
- Apurar os votos (ranking, percentuais, brancos, nulos, vencedor ou empate).
- Resetar a votação (apaga os votos, mantém os candidatos).

**Persistência**
- Candidatos, votos e estado da votação são salvos no **Cloud Firestore**.
- Ao reabrir o programa, tudo é recarregado do banco automaticamente.

---

## 🛠️ Tecnologias utilizadas

| Tecnologia | Versão | Para que é usada |
|---|---|---|
| **Java** | 21 ou superior | Linguagem principal do projeto |
| **Java Swing / AWT** | (incluído no JDK) | Interface gráfica (telas, botões, teclado da urna) |
| **Apache Maven** | 3.x | Gerenciamento de dependências e compilação |
| **Firebase Admin SDK** | 9.7.0 | Comunicação do Java com o Firebase |
| **Cloud Firestore** | — | Banco de dados NoSQL na nuvem |
| **TwelveMonkeys ImageIO (WebP)** | 3.12.0 | Leitura de imagens no formato WebP |
| **javax.imageio** | (incluído no JDK) | Leitura, redimensionamento e conversão das fotos |
| **Base64 (`java.util.Base64`)** | (incluído no JDK) | Armazenar a foto como texto no Firestore |
| **Visual Studio Code** | — | Ambiente de desenvolvimento utilizado |
| **Git / GitHub** | — | Controle de versão |

### Conceitos de Programação Orientada a Objetos aplicados

- **Herança:** `Vereador` e `Presidente` herdam da classe abstrata `Politico`.
- **Polimorfismo:** o método de apuração e o `getCargo()` funcionam para qualquer tipo de candidato.
- **Encapsulamento:** atributos privados com getters e setters.
- **Padrão DAO:** as classes `CandidatoDAO` e `VotacaoDAO` isolam o acesso ao banco de dados.
- **Separação em camadas:** `model` (regras), `view` (telas), `dao` (banco), `config` (conexão) e `util` (utilitários).

---

## 📁 Estrutura do projeto

```
UrnaEletro/Urna/
├── pom.xml
├── README.md
└── src/main/java/
    ├── config/
    │   ├── FirebaseConfig.java        # Conexão com o Firebase/Firestore
    │   ├── TesteFirebase.java         # Testa a conexão
    │   ├── TesteFirestore.java        # Testa gravação e leitura
    │   ├── TesteCandidatoDAO.java     # Testa o DAO de candidatos
    │   └── TesteListarCandidatos.java # Testa a listagem
    ├── dao/
    │   ├── CandidatoDAO.java          # Salvar, listar, atualizar e remover candidatos
    │   └── VotacaoDAO.java            # Salvar e carregar o estado da votação
    ├── model/
    │   ├── Politico.java              # Classe abstrata (nome, número, partido, votos, foto)
    │   ├── Vereador.java
    │   ├── Presidente.java
    │   ├── Eleitor.java
    │   ├── Urna.java                  # Regras da votação e apuração
    │   ├── MenuConsole.java           # Versão antiga, em console
    │   └── App.java                   # Executa a versão em console (sem Firebase)
    ├── util/
    │   └── FotoUtil.java              # Escolher, redimensionar e converter fotos
    └── view/
        ├── TelaInicial.java           # Tela de login + main da aplicação
        ├── TelaVotacao.java           # Tela da urna (eleitor)
        └── PainelMesario.java         # Painel de gerenciamento (mesário)
```

---

## ✅ Pré-requisitos

- **JDK 21 ou superior** ([Adoptium](https://adoptium.net/))
- **Apache Maven** ([maven.apache.org](https://maven.apache.org/download.cgi))
- Uma **conta Google** (para usar o Firebase)
- Conexão com a internet (o banco fica na nuvem)

Para conferir as instalações:

```bash
java -version
mvn -version
```

---

## 🔥 Configurando o Firebase

O projeto **não inclui as credenciais** do banco. Cada pessoa precisa criar o seu próprio projeto no Firebase.

### 1. Criar o projeto
1. Acesse [console.firebase.google.com](https://console.firebase.google.com) e clique em **Criar um projeto**.
2. Dê um nome ao projeto (ex.: `urna-eletro`). Pode desativar o Google Analytics.
3. Anote o **ID do projeto**, que aparece em **Configurações do projeto → Geral**. Ele pode ser diferente do nome.

### 2. Criar o banco Firestore
1. No menu lateral: **Criação → Firestore Database → Criar banco de dados**.
2. Edição **Standard**, ID `(default)`.
3. Local: `southamerica-east1` (São Paulo). Essa escolha não pode ser alterada depois.
4. Pode iniciar em **modo de teste**. O projeto usa o Admin SDK, que não depende das regras de segurança.

Não é necessário criar coleções manualmente: o programa as cria sozinho.

### 3. Gerar a chave de acesso
1. **Configurações do projeto → Contas de serviço → Gerar nova chave privada**.
2. Será baixado um arquivo `.json`. Guarde-o **fora da pasta do projeto**, por exemplo `C:\chaves\urna-eletro.json`.

### 4. Informar o ID do projeto no código
Em `src/main/java/config/FirebaseConfig.java`, ajuste o ID para o do seu projeto:

```java
.setProjectId("SEU-ID-DO-PROJETO")
```

### 5. Apontar para a chave

O programa procura a chave pela variável de ambiente `GOOGLE_APPLICATION_CREDENTIALS`.

**Windows (PowerShell):**
```powershell
setx GOOGLE_APPLICATION_CREDENTIALS "C:\chaves\urna-eletro.json"
```
Depois, **feche e reabra** o VS Code (ou terminal), senão a variável não é reconhecida.

**Linux / macOS:**
```bash
export GOOGLE_APPLICATION_CREDENTIALS="/caminho/para/urna-eletro.json"
```

### 6. Testar a conexão
Execute a classe `config.TesteFirestore`. Se tudo estiver certo, aparece:

```
Documento gravado com sucesso!
Dados recuperados:
{projeto=Urna Cidadã, mensagem=Conexão funcionando!}
```

> Os avisos `WARNING` do Java sobre `sun.misc.Unsafe` e `SLF4J` são inofensivos e podem ser ignorados.

---

## ▶️ Como executar

Na pasta `UrnaEletro/Urna` (onde está o `pom.xml`):

### 1. Baixar as dependências e compilar
```bash
mvn clean compile
```

### 2. Executar a aplicação

**Pelo VS Code (recomendado):** abra `src/main/java/view/TelaInicial.java` e clique em **Run** acima do método `main`.

**Pelo terminal:**
```bash
mvn compile exec:java -Dexec.mainClass=view.TelaInicial
```

> A classe principal da versão com interface gráfica e Firebase é `view.TelaInicial`.
> A classe `model.App` é uma versão antiga em **console**, que não usa o Firebase.

---

## 🧭 Como usar

### Códigos de acesso (provisórios)

| Perfil | Código |
|---|---|
| Mesário | `2` |
| Votação (eleitor) | `2026` |

Digite o código na tela inicial e clique em **CONFIRMAR** (ou pressione Enter).

### Fluxo recomendado

1. **Entre como mesário** (`2`) e cadastre os candidatos.
2. Ainda no painel, clique em **Bloquear cadastros** para impedir alterações durante a votação.
3. Volte à tela inicial (**Voltar à tela inicial**).
4. **Entre na votação** (`2026`) e vote.
5. Ao final, entre novamente como mesário, clique em **Encerrar votação** e depois em **Apurar votos**.

### Cadastrando um candidato

No painel do mesário, clique em **Adicionar candidato** e informe, em sequência:

1. **Cargo** (Vereador ou Presidente)
2. **Nome** (apenas letras e espaços)
3. **Partido** (apenas letras e espaços)
4. **Número** (5 dígitos para vereador, 2 para presidente)
5. **Foto** (opcional): escolha uma imagem JPG, PNG ou WebP

Para corrigir um cadastro, selecione o candidato na lista e clique em **Alterar candidato**. Para excluir, use **Remover candidato**.

### Votando

1. A tela abre em **Vereador**. Digite os 5 dígitos do número do candidato.
2. Quando o número estiver completo, a **foto** do candidato aparece no quadrado à direita.
3. Clique em **CONFIRMA** e confirme o voto na janela que aparece.
   - **CORRIGE** apaga o número digitado.
   - **BRANCO** registra voto em branco.
   - Número que não existe gera a opção de **voto nulo**.
4. Em seguida a tela passa para **Presidente** (2 dígitos), com o mesmo funcionamento.
5. Ao finalizar, aparece "Votação finalizada!" e o programa volta à tela inicial, pronto para o próximo eleitor.

### Botões do painel do mesário

| Botão | O que faz |
|---|---|
| Adicionar candidato | Cadastra um novo candidato (com foto opcional) |
| Alterar candidato | Edita nome, partido, número e foto |
| Remover candidato | Exclui o candidato selecionado |
| Bloquear / Desbloquear cadastros | Impede ou libera alterações nos candidatos |
| Encerrar votação | Fecha a votação e bloqueia os cadastros |
| Apurar votos | Mostra o resultado (só depois de encerrar a votação) |
| Resetar votação | Zera todos os votos, mantém os candidatos e reabre a votação |
| Voltar à tela inicial | Retorna ao login |
| Fechar painel | Fecha o painel do mesário |

---

## 🗄️ Estrutura do banco de dados

Os dados ficam no Cloud Firestore, organizados assim:

```
vereadores/
  └── cadastro/
      └── candidatos/
          └── {numero}        → numero, nome, partido, cargo, votos, foto

presidentes/
  └── cadastro/
      └── candidatos/
          └── {numero}        → numero, nome, partido, cargo, votos, foto

votacao/
  └── geral/
      └── estado/
          └── atual           → votosBrancosVereador, votosNulosVereador,
                                votosBrancosPresidente, votosNulosPresidente,
                                votacaoEncerrada
```

A coleção `teste_conexao` é criada apenas pelo `TesteFirestore` e pode ser apagada.

---

## 🖼️ Fotos dos candidatos

- **Formatos aceitos:** JPG, PNG e WebP.
- Ao escolher a imagem, ela é **redimensionada para 170×200 px** (preenchendo o quadrado, sem deformar) e convertida para **JPEG**.
- A imagem é salva como texto **Base64** no campo `foto` do documento do candidato, sem precisar do Firebase Storage.
- Como cada foto fica com poucas dezenas de KB, ela cabe com folga no limite de 1 MiB por documento do Firestore.
- A foto é opcional. Candidatos sem foto continuam funcionando normalmente.
- Imagens WebP **animadas** podem não ser lidas. Use uma imagem estática.

---

## 📏 Regras de negócio

- O número do **vereador** tem **5 dígitos**; o do **presidente**, **2 dígitos**.
- Não podem existir dois candidatos com o mesmo número no mesmo cargo.
- O eleitor **precisa votar em Vereador antes de Presidente**.
- Cada eleitor vota **uma única vez** em cada cargo.
- Candidato que **já recebeu votos não pode ser alterado nem removido**.
- Com os cadastros **bloqueados**, não é possível adicionar, alterar nem remover candidatos.
- Depois de **encerrada**, a votação não aceita mais votos e não pode ser acessada pela tela inicial.
- A **apuração** só está disponível após o encerramento da votação.
- Em caso de **empate** entre os mais votados, o resultado informa o empate em vez de um vencedor.
- Percentuais são calculados sobre o total de votos (válidos + brancos + nulos).

---

## 🔒 Segurança

- **Nunca publique o arquivo `.json` da conta de serviço** no GitHub. Quem o possuir tem controle total do seu banco.
- Crie um `.gitignore` na raiz do repositório:
  ```
  *.json
  target/
  bin/
  ```
- Se a chave for exposta, revogue-a em **Configurações do projeto → Contas de serviço** e gere outra.
- Os códigos de acesso (`2` e `2026`) estão fixos em `TelaInicial.java` e são **provisórios**. Para uso real, troque por um mecanismo de autenticação.
- Se o Firestore foi criado em **modo de teste**, o banco fica aberto a acessos externos por 30 dias. Depois de testar, troque as regras (aba **Regras**) por:
  ```
  rules_version = '2';
  service cloud.firestore {
    match /databases/{database}/documents {
      match /{document=**} {
        allow read, write: if false;
      }
    }
  }
  ```
  O programa continua funcionando, pois o Admin SDK ignora essas regras.

---

## 🩺 Problemas comuns

| Mensagem / sintoma | Causa provável | Solução |
|---|---|---|
| `Your default credentials were not found` | Variável `GOOGLE_APPLICATION_CREDENTIALS` não definida | Defina a variável e **reinicie** o VS Code/terminal |
| `NOT_FOUND` / `database (default) does not exist` | Firestore não foi criado | Crie o banco no console do Firebase |
| `PERMISSION_DENIED` | ID do projeto no código diferente do projeto da chave | Ajuste o `setProjectId` no `FirebaseConfig.java` |
| "Não foi possível conectar ao Firebase" ao abrir | Sem internet ou credenciais inválidas | Verifique a conexão e a chave |
| "O arquivo não é uma imagem válida" ao escolher `.webp` | Dependência WebP não foi baixada | Rode `mvn clean compile` e reinicie a IDE |
| `class file has wrong version` / erro de compilação | JDK anterior ao 21 | Instale o JDK 21 ou superior |
| Fotos não aparecem na votação | Candidato sem foto ou número incompleto | Cadastre a foto e digite todos os dígitos do número |

---

## 🚀 Melhorias futuras

- Autenticação real para mesário e eleitores (em vez de códigos fixos).
- Mover os códigos de acesso para o banco ou para um arquivo de configuração.
- Exibir a foto também na janela de confirmação do voto e na lista do mesário.
- Suporte a mais cargos (governador, deputado etc.).
- Relatório de apuração em PDF.
- Registro (log) de ações do mesário.

---

## 👥 Autores

- João Pedro Leite
- Ryan Robert Barreto de Almeida

**Instituição:** UniJorge
**Disciplina:** Projeto de Extensão III
