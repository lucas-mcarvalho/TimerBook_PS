<div align="center">
  <img src="front-end/src/assets/Home/TimerbookLogo.svg" alt="Logo TimerBook" width="140" />

  <h1>TimerBook</h1>

  <p>
    Uma plataforma para organizar livros, acompanhar metas de leitura e transformar o progresso do usuário em estatísticas claras.
  </p>
</div>

## Sobre o Projeto

O TimerBook é um sistema web para gerenciamento de leituras. A aplicação permite cadastrar livros em PDF, acessar uma biblioteca pessoal, ler os arquivos em um leitor próprio, definir metas diárias, acompanhar sessões de leitura e visualizar estatísticas de evolução.

O projeto também conta com recursos de autenticação, recuperação de senha, verificação de e-mail, conquistas, lembretes e um assistente inteligente para apoiar o usuário durante a leitura.

## Links

| Recurso | Acesso |
|---------|--------|
| Deploy da aplicação | [timerbook.com.br](http://timerbook.com.br) |
| Landing page | [TimerBook Landing Page](https://lucas-mcarvalho.github.io/TimerBook_PS/) |
| Propaganda do projeto | [Assistir no YouTube](https://www.youtube.com/watch?v=rk8HtUo2tHo) |
| Vídeo do projeto | [Assistir no YouTube](https://www.youtube.com/watch?v=RutJxCjv6bo) |
| Planejamento no Trello | [Trello TimerBook](https://trello.com/b/HtVptYfz/timerbook) |
| User Stories e protótipo | [Figma TimerBook](https://www.figma.com/design/dgX72w2shMIYEC9jvOFopp/TimerBook?node-id=1-5&p=f&t=HIWQvvAN6rVbJlLf-0) |
| Repositório | [GitHub](https://github.com/lucas-mcarvalho/TimerBook_PS) |
| Repositório mobile | [TimerBookMobile](https://github.com/lucas-mcarvalho/TimerBookMobile) |

## Tecnologias

<table>
  <tr>
    <td align="center" width="110">
      <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/java/java-original-wordmark.svg" width="64" height="64" alt="Java" />
      <br />
      <strong></strong>
    </td>
    <td align="center" width="110">
      <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/spring/spring-original.svg" width="56" height="56" alt="Spring Boot" />
      <br />
      <strong>Spring Boot</strong>
    </td>
    <td align="center" width="110">
      <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/postgresql/postgresql-original-wordmark.svg" width="64" height="64" alt="PostgreSQL" />
      <br />
      <strong></strong>
    </td>
    <td align="center" width="110">
      <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/docker/docker-plain-wordmark.svg" width="64" height="64" alt="Docker" />
      <br />
      <strong></strong>
    </td>
  </tr>
  <tr>
    <td align="center" width="110">
      <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/javascript/javascript-original.svg" width="56" height="56" alt="JavaScript" />
      <br />
      <strong></strong>
    </td>
    <td align="center" width="110">
      <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/react/react-original-wordmark.svg" width="64" height="64" alt="React" />
      <br />
      <strong></strong>
    </td>
    <td align="center" width="110">
      <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/python/python-original-wordmark.svg" width="64" height="64" alt="Python" />
      <br />
      <strong></strong>
    </td>
    <td align="center" width="110">
      <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/amazonwebservices/amazonwebservices-original-wordmark.svg" width="72" height="64" alt="AWS" />
      <br />
      <strong></strong>
    </td>
  </tr>
</table>

## Principais Funcionalidades

- Cadastro e autenticação de usuários.
- Biblioteca pessoal para organização dos livros.
- Upload e leitura de arquivos PDF.
- Leitor integrado com suporte a progresso de leitura.
- Metas diárias e acompanhamento de sequência de leitura.
- Estatísticas de páginas lidas, tempo de leitura e evolução.
- Conquistas e lembretes para incentivar a constância.
- Assistente inteligente para auxiliar durante a leitura.

## Equipe

| Nome | Perfil GitHub |
|------|---------------|
| Tiago Barbosa de Castro Souza | [TiagoBrs](https://github.com/TiagoBrs) |
| Vitor Kawan Barbosa Borges | [KawanVitor1](https://github.com/KawanVitor1) |
| Lucas Monteiro de Carvalho | [lucas-mcarvalho](https://github.com/lucas-mcarvalho) |
| Matheus Silva Pontes | [matheuspontes01](https://github.com/matheuspontes01) |
| Bruno Henrique Frota Sobral | [Bruno-uft](https://github.com/Bruno-uft) |
| Kayk Zago Pinheiro | [kayke002](https://github.com/kayke002) |

## Informações Acadêmicas

**Curso:** Ciência da Computação

**Professor:** Dr. Edeílson Milhomem

## Estrutura do Projeto

```text
TimerBook_PS/
├── BackEnd/
│   └── TimerBook_API/
├── front-end/
│   └── src/
├── ia-service/
├── landing_page/
├── uploads/
├── docker-compose.yml
├── index.html
└── README.md
```

## Configuração do `.env`

O arquivo `.env` deve ficar na raiz do projeto. Ele é carregado pelo serviço `backend` no `docker-compose.yml` e concentra as configurações sensíveis ou dependentes do ambiente, como credenciais de e-mail, autenticação Google, URLs públicas, CORS, pagamentos e armazenamento.

Não publique valores reais de senha, tokens ou chaves de API no repositório. Para compartilhar a estrutura do arquivo, use valores fictícios ou crie um `.env.example`.

| Grupo | Variáveis | Para que serve |
|-------|-----------|----------------|
| E-mail SMTP | `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | Envio de confirmação de conta, recuperação de senha e lembretes de leitura. |
| Login com Google | `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` | Configuração do OAuth2 com Google. |
| URLs do front-end | `FRONTEND_RESET_PASSWORD_URL`, `FRONTEND_VERIFY_EMAIL_URL`, `FRONTEND_OAUTH2_REDIRECT_URL` | Links usados em fluxos de senha, verificação de e-mail e login social. |
| CORS e API | `CORS_ORIGIN_PATTERNS`, `VITE_API_BASE_URL` | Origens permitidas no backend e endereço base da API para configurações Vite/front-end. |
| Pagamentos | `PAYMENT_PROVIDER`, `MP_ACCESS_TOKEN`, `MP_PUBLIC_KEY`, `PAYMENT_*` | Configuração de checkout, retorno e webhook de pagamentos. |
| Upload e armazenamento | `APP_STORAGE_TYPE`, `APP_MAX_FILE_SIZE`, `APP_MAX_REQUEST_SIZE`, `AWS_*` | Define limites de upload e credenciais para armazenamento em S3, quando usado. |
| Admin de desenvolvimento | `DEV_ADMIN_*` | Seed opcional de usuário administrador em ambiente de desenvolvimento. |

Modelo seguro para referência:

```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=seu-email
MAIL_PASSWORD=sua-senha-ou-app-password

GOOGLE_CLIENT_ID=seu-client-id
GOOGLE_CLIENT_SECRET=seu-client-secret

PAYMENT_PROVIDER=mercado_pago
MP_ACCESS_TOKEN=seu-token
MP_PUBLIC_KEY=sua-chave-publica
PAYMENT_CHECKOUT_TITLE=TimerBook Premium
PAYMENT_CHECKOUT_AMOUNT=0.00
PAYMENT_CHECKOUT_CURRENCY=BRL
PAYMENT_SUCCESS_URL=http://localhost:5173/perfil?payment=success
PAYMENT_CANCEL_URL=http://localhost:5173/perfil?payment=cancel
PAYMENT_NOTIFICATION_URL=http://localhost:8080/billing/webhook/mercado_pago
PAYMENT_WEBHOOK_SECRET=seu-webhook-secret
PAYMENT_CUSTOMER_PORTAL_URL=http://localhost:5173/perfil

FRONTEND_RESET_PASSWORD_URL=http://localhost:5173/redefinir-senha
FRONTEND_VERIFY_EMAIL_URL=http://localhost:5173/verify-email
FRONTEND_OAUTH2_REDIRECT_URL=http://localhost:5173/oauth2/redirect
CORS_ORIGIN_PATTERNS=http://localhost:5173,http://localhost:8080
VITE_API_BASE_URL=http://localhost:8080

APP_STORAGE_TYPE=local
APP_MAX_FILE_SIZE=100MB
APP_MAX_REQUEST_SIZE=100MB
AWS_ACCESS_KEY_ID=sua-access-key
AWS_SECRET_ACCESS_KEY=sua-secret-key
AWS_REGION=sa-east-1
AWS_S3_BUCKET=nome-do-bucket

DEV_ADMIN_SEED_ENABLED=false
DEV_ADMIN_EMAIL=admin@email.com
DEV_ADMIN_USERNAME=admin
DEV_ADMIN_PASSWORD=senha-temporaria
DEV_ADMIN_RESET_PASSWORD=false
```

## Como Rodar o Projeto

### Pré-requisitos

- Git
- Docker
- Docker Compose

### Passos

1. Clone o repositório:

```bash
git clone https://github.com/lucas-mcarvalho/TimerBook_PS.git
```

2. Acesse a pasta do projeto:

```bash
cd TimerBook_PS
```

3. Configure o arquivo `.env` na raiz do projeto seguindo a seção anterior.

4. Suba os containers:

```bash
docker compose up --build
```

5. Acesse a aplicação no navegador:

```text
http://localhost:5173
```

Na primeira execução, aguarde alguns instantes para que os serviços sejam criados e o banco de dados seja configurado.
