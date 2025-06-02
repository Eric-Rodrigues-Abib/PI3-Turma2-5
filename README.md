# SuperID - Gerenciador Seguro de Acesso com Autenticação via QR Code e AES-256

O SuperID é um aplicativo Android desenvolvido com o objetivo de ser uma plataforma segura de autenticação e gerenciamento de credenciais. 
Com integração ao **Firebase Authentication**, o app oferece autenticação via email/senha tradicional e também um sistema moderno de login sem senha,
utilizando **leitura de QR Code** gerado por sites parceiros.

Além do login, o SuperID também permite aos usuários **gerenciar suas senhas pessoais** de forma segura, com funcionalidades para **salvar,
editar e excluir credenciais**, criptografadas localmente com **AES-256**, uma das tecnologias mais confiáveis e robustas de criptografia atualmente utilizadas no mercado.

---

## Funcionalidades Principais

- Login com email e senha
- Cadastro de novo usuário com nome, email e senha
- Recuperação de senha via email
- Login via leitura de QR Code (sem uso de senha)
- Armazenamento local de senhas pessoais
- Edição e exclusão de senhas salvas
- Criptografia de dados sensíveis com AES-256
- Navegação fluida com Fragments
- Integração com Firebase Authentication

---

## Criptografia: Segurança com AES-256

Todos os dados sensíveis armazenados no dispositivo, como senhas pessoais, são protegidos com o padrão de criptografia **AES-256** (Advanced Encryption Standard de 256 bits). 
Esse algoritmo é amplamente utilizado em sistemas bancários, militares e aplicações corporativas devido à sua robustez.

### Por que utilizamos AES-256?

- **Alto nível de segurança**: chave de 256 bits dificulta ataques por força bruta
- **Velocidade**: eficiente mesmo em dispositivos móveis
- **Confiabilidade**: aprovado por órgãos de segurança internacionais

### Como funciona a proteção?

- Cada senha é criptografada com **uma chave única derivada de uma base segura**
- Utilizamos **IV (Initialization Vector)** aleatório para cada operação, o que evita padrões na criptografia
- As operações de criptografia e descriptografia seguem o padrão **AES/CBC/PKCS5Padding**

---

## Tecnologias Utilizadas

- **Linguagem:** Kotlin
- **Interface:** Fragments, Navigation Component, Material Components
- **Autenticação:** Firebase Authentication
- **Criptografia:** AES-256 com IV e chave secreta
- **Persistência local:** SharedPreferences (criptografado) ou Room (dependendo da evolução)
- **Layout:** XML
- **Controle de versão:** Git

---

## Instalação e Execução

### Instalar via APK (release)

1. Acesse a aba "Releases" deste repositório no GitHub
2. Baixe o arquivo `app-release.apk`
3. Envie para o seu celular Android
4. Ative a opção de instalar apps de fontes desconhecidas
5. Instale e execute o aplicativo

### Clonar e rodar via Android Studio

1. Clone este repositório:
```bash
git clone https://github.com/seu-usuario/seu-repo.git
Abra o projeto no Android Studio

Configure o Firebase:

Crie um projeto em https://console.firebase.google.com

Ative o método de autenticação "Email/Senha"

Baixe o arquivo google-services.json e coloque na pasta app/

Sincronize o projeto com o Gradle e execute em um dispositivo/emulador

Como funciona o login por QR Code
O usuário acessa um site parceiro que exibe um QR Code na tela

O app SuperID, ao clicar em “Logar com SuperID”, ativa a câmera para escanear o QR Code

O QR Code contém um token de autenticação gerado pelo site parceiro

Ao ler o token, o app valida e autentica o usuário, sem necessidade de digitar senha

É uma abordagem moderna, rápida e segura para login

