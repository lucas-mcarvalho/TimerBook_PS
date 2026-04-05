# TimerBook

<img src="./TimerbookLogo.png" width="300" />

**Curso: Ciência da Computação**

**Professor: Dr. Edeílson Milhomem**



**Equipe:**

| Nome                           | Perfil GitHub                                 |
|--------------------------------|----------------------------------------------|
| Tiago Barbosa de Castro Souza  | [TiagoBrs](https://github.com/TiagoBrs)     |
| Vitor Kawan Barbosa Borges     | [KawanVitor1](https://github.com/KawanVitor1)|
| Lucas Monteiro de Carvalho     | [lucas-mcarvalho](https://github.com/lucas-mcarvalho)|
| Matheus Silva Pontes           | [matheuspontes01](https://github.com/matheuspontes01)|
| Bruno Henrique Frota Sobral    | [Bruno-uft](https://github.com/Bruno-uft)  |
| Kayk Zago Pinheiro             | [kayke002](https://github.com/kayke002)    |





# DESCRIÇÃO DO PROJETO

O projeto TimerBook tem como objetivo ser um software para gerenciamento de suas leituras. Inclui funcionalidades como o cadastro de livros que o usuário esteja lendo, acesso a banco de dados de livros já cadastrados ,sistemas de métricas e lembrentes para auxiliar o usuário na leitura de seus diversos livros e também possui um leitor de pdf própio para auxiliar na leitura.


# O projeto foi feito usando as seguintes tecnologias:
<p align="left">

<p align="left">
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg" width="50" title="Java" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original-wordmark.svg" width="50" title="Spring Boot" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/postgresql/postgresql-original-wordmark.svg" width="50" title="PostgreSQL" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original-wordmark.svg" width="50" title="Docker" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/javascript/javascript-original.svg" width="50" title="JavaScript" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/react/react-original-wordmark.svg" width="50" title="React JS" />
</p>                               
          
## 🔗 Links Úteis


* **Planejamento no Trello:**[Trello](https://trello.com/b/WXQL66Tj/timerbook-sistema-de-gerenciamento-de-leitura)
* **User Stories & Protótipo (Figma):** [Ver Telas no Figma](https://www.figma.com/proto/IihsrG5vSCSRwtLC64WnLd/User-Stories?node-id=0-1&t=epDzeNh9tx9MlHN4-1)


## 🖥️ Estrutura do Projeto

```
TimerBook/
Backend/
├── Timerbook/
 └── src/main/
 └── target/classes/
 └── pomxml
 └── Dockerfile
 └── mvnw.cmd
 └── mvnw

FrontEnd/
    └── node_modules/
    └──public/
    └── src/
    └──Dockerfile
 

├──.gitignore
├──README.MD
├──docker-compose.yml



```

---

## 🛠️ Como rodar o projeto


  **Pré-requisito: Instalar o Git e Docker **
⚠️ Antes de prosseguir, certifique-se de que você tem o **Git e Docker** instalado na sua máquina.
* **Windows/Linux/Mac:** [Baixe e instale o Git aqui](https://git-scm.com/downloads).

1.  **Clone o repositório**

    ```bash
    git clone [https://github.com/lucas-mcarvalho/TimerBook.git](https://github.com/lucas-mcarvalho/TimerBook.git)
    ```


🐳 Como rodar com Docker 
2.  **Subir os Containers**
    Na raiz do projeto, abra o terminal, troque para a branch correta e execute:

    ```bash
    git checkout docker
    docker-compose up -d --build
    ```
    *Aguarde alguns instantes. O banco de dados será criado e configurado automaticamente na primeira execução.*

3.  **Para Acessar o Projeto ,**
    Abra seu navegador e acesse:
    ```
    http://localhost:5173
    ```


