### ## Membro 1: Backend, Domínio e Filtros (O "Arquiteto de Dados")

O teu trabalho focar-se-á nos pacotes `domain`, `dto`, `repository` e `service`.

Aqui está a reestruturação detalhada e passo a passo para as tarefas do **Membro 1 (Backend, Domínio e Filtros)**. 

Dividi as responsabilidades em etapas lógicas e sequenciais, mencionando exatamente os ficheiros da vossa estrutura (`tree.txt`) que precisam de ser alterados ou criados, para que possas começar a programar imediatamente.

---

### 🛠️ Membro 1: Backend, Domínio e Filtros (O "Arquiteto de Dados")

#### Passo 1: Atualização do Modelo de Domínio (Entidades JPA)
A tua primeira tarefa é adaptar as classes da pasta `src/main/java/.../domain` para suportarem as novas regras exigidas pelo cliente.

* [x] **1.1. Refatorar Moradas do Cliente:**
  * **Ficheiros:** `domain/utilizador/Cliente.java` e `domain/utilizador/Morada.java`.
  * **Ação:** Alterar a relação de `1:1` para `1:N`. Um cliente passa a ter uma lista de moradas associadas, podendo escolher uma existente ou usar uma nova num pedido
* [x] **1.2. Refatorar Entidade Pedido:**
  * **Ficheiros:** `domain/pedido/Pedido.java`.
  * **Ação:** Modificar a estrutura do pedido para garantir que pode conter produtos de diferentes restaurantes em simultâneo
* [x] **1.3. Especificar Pagamentos:**
  * **Ficheiros:** `PagamentoMultibanco.java`, `PagamentoMBWay.java` e `PagamentoDinheiro.java` (em `domain/pagamento/`).
  * **Ação:** Adicionar os atributos específicos de cada tipo: guardar a bandeira do cartão no Multibanco, o número de telemóvel no MBWay, e a informação do troco no pagamento em Numerário
  Não precisas de implementar a lógica de processamento real
* [x] **1.4. Criar a Entidade Avaliação:**
  * **Ficheiros:** Criar `Avaliacao.java` na pasta `domain/avaliacao`.
  * **Ação:** Implementar a entidade que permite ao cliente avaliar o restaurante após realizar uma compra
  . Ligar esta entidade a `Restaurante` e `Pedido`/`Cliente`.
* [x] **1.5. Criar a Entidade Menu Partilhado:**
  * **Ficheiros:** Criar `Menu.java` na pasta `domain/menu`.
  * **Ação:** Criar a estrutura que permite partilhar um menu entre diversos restaurantes
  Garantir no mapeamento JPA que uma alteração num item deste menu se reflete em todos os restaurantes que o partilham

---

#### Passo 2: Atualização da Transferência de Dados (DTOs)
Como o domínio mudou, os dados que entram e saem da API também mudam.

* [ ] **2.1. Atualizar Pedido DTOs:**
  * **Ficheiros:** `dto/pedidoDTO/CriarPedidoDTO.java`.
  * **Ação:** Ajustar para suportar a seleção da morada de entrega (nova ou existente) e a lista de produtos de múltiplos restaurantes.
* [ ] **2.2. Atualizar Pagamento DTOs:**
  * **Ficheiros:** `dto/pagamentoDTO/ProcessarPagamentoDTO.java`.
  * **Ação:** Incluir campos opcionais para receber a bandeira do cartão, número do MBWay e valor para troco.
* [ ] **2.3. Criar DTOs para Menu e Avaliação:**
  * **Ficheiros:** Criar pastas `menuDTO` e `avaliacaoDTO` em `dto/` e adicionar os respetivos DTOs de criação e resposta.

---

#### Passo 3: Implementação da Lógica de Negócio (Services)
Agora tens de dar vida às regras de negócio na pasta `src/main/java/.../service`.

* [ ] **3.1. Automatizar Atribuição de Entregador:**
  * **Ficheiros:** `EntregaService.java` e/ou `PedidoService.java`.
  * **Ação:** Desenvolver o algoritmo que encontra o entregador válido/disponível mais adequado e o associa automaticamente a um pedido quando este fica pronto, sem intervenção humana
* [ ] **3.2. Lógica de Menus:**
  * **Ficheiros:** Criar `MenuService.java`.
  * **Ação:** Permitir a gestão de menus (criar, atualizar) e a associação explícita destes menus aos restaurantes pretendidos

---

#### Passo 4: Implementação do Sistema de Buscas e Filtros (Repositories)
O cliente pediu procuras mais avançadas
Terás de usar Spring Data JPA (Specification API, Criteria API ou anotações `@Query`) na pasta `src/main/java/.../repository`.

* [ ] **4.1. Filtros de Utilizador (`utilizadorRepository/`):**
  * **Ação:** Implementar buscas por nome, tipo de utilizador 
  , número de pedidos realizados (só para clientes) 
  e número de entregas realizadas (só para entregadores)
* [ ] **4.2. Filtros de Restaurante (`IRestauranteRepository.java`):**
  * **Ação:** Implementar buscas por nome 
  , número de pedidos realizados 
  , número de avaliações recebidas 
  , morada 
  , tipo de cozinha 
  , horário de funcionamento (aberto/fechado) 
   e preço médio dos pratos
  .
* [ ] **4.3. Filtros de Produtos (`IProdutoRepository.java`):**
  * **Ação:** Implementar buscas por nome 
  , preço 
  , categoria 
  , disponibilidade (acessível apenas a admin/entregadores) 
   e popularidade (número de vezes pedido num intervalo de tempo)
  .
* [ ] **4.4. Filtros de Menu (Criar `IMenuRepository.java`):**
  * **Ação:** Implementar buscas por nome 
  , número de produtos associados 
   e preço médio dos produtos associados
  .

---

#### Passo 5: Validação de Queries Complexas (Perguntas de Negócio)
Para garantir que a base de dados ficou bem modelada, o sistema tem de ser capaz de responder às seguintes questões analíticas

* [ ] **5.1.** Calcular a média do troco nos pagamentos em numerário
* [ ] **5.2.** Descobrir qual é o item mais pedido de um determinado restaurante
* [ ] **5.3.** Identificar o entregador com mais entregas realizadas para um restaurante específico
* [ ] **5.4.** Descobrir qual o restaurante mais popular de uma franquia específica
* [ ] **5.5.** Identificar o cliente que fez mais pedidos num intervalo de tempo específico
* **Ação:** Traduzir estas questões para métodos nos Repositórios (usando agregações como `COUNT`, `AVG`, `GROUP BY`) e validar o seu funcionamento via testes unitários em `src/test/java/.../repositories/`.

---

# membro 1 extra (se der tempo senao quem conseguir)

O Trabalho de Casa para o Membro 1 (Backend)

O projeto vai correr no Docker, mas o vosso grupo não se pode esquecer de consertar os testes localmente antes de submeter o projeto, pois os testes fazem parte da nota (ver enunciado: "O repositório deve aceitar apenas código com o nível de qualidade aceite pela equipa (ex.: pre-commit, testes).").  

A pessoa responsável pelo backend deverá abrir o VS Code / IntelliJ e corrigir os testes guiando-se pelos mesmos erros que resolvemos há pouco. Aqui está a lista do que ele tem de alterar nos testes:

    Testes do Cliente: Substituir client.setAddress(morada) por client.addAdresses(morada) e client.getAddress() por client.getAddresses().get(0).

    Testes do PagamentoMultibanco: Onde diz new PagamentoMultibanco(id, valor, data, ref, entidade, ref_entidade), passar a incluir a bandeira no final (ex: "VISA").

    Remover "PagamentoCartao": Apagar qualquer referência a PagamentoCartao nos testes da Factory.

    Testes do Pedido: O Pedido e o CriarPedidoDTO mudaram de construtor (porque agora suportam vários restaurantes). Os testes têm de ser atualizados para passar os novos argumentos corretos.

Faz a alteração do -DskipTests no backend/Dockerfile e diz-me se a aplicação finalmente arrancou no Docker!

---

---

Aqui está o plano de ação detalhado e passo a passo para o **Membro 2 (Interface Web com Thymeleaf)**, mapeado diretamente para os requisitos do projeto e para a vossa atual estrutura de pastas.

---

### 🌐 Membro 2: Interface Web com Thymeleaf (O "Web Developer")

A tua missão principal é construir a aplicação de *browser* com renderização *server-side* utilizando Thymeleaf
Esta interface deve ser capaz de realizar as consultas à informação do sistema e comunicar com o backend via *endpoints* REST

#### Passo 1: Configuração Base e Estrutura Visual
Antes de ligar dados, precisas de ter a "cara" da aplicação pronta nas pastas do Spring Boot.

* [x] **1.1. Validar Dependências:**
  * **Ficheiro:** `pom.xml`.
  * **Ação:** Confirmar se as dependências do `spring-boot-starter-thymeleaf` e `spring-boot-starter-web` estão corretamente configuradas para compilar o HTML.
* [x] **1.2. Estruturar Ficheiros Estáticos:**
  * **Pasta:** `src/main/resources/static/`.
  * **Ação:** Criar os ficheiros de estilos CSS, *scripts* JavaScript (se necessário para interatividade base) e armazenar imagens/logótipos do *TascaEats*.
* [x] **1.3. Criar os Layouts Thymeleaf (Templates):**
  * **Pasta:** `src/main/resources/templates/`.
  * **Ação:** Criar os ficheiros `.html`. É recomendável criar um ficheiro base (ex: `layout.html` ou *fragments*) com o cabeçalho e rodapé, para não repetires código em todas as páginas.

---

#### Passo 2: Implementação dos Controladores Web (Spring MVC)
Para não haver mistura com a API REST que já têm, vais criar os teus próprios controladores focados em devolver páginas HTML.

* [x] **2.1. Criar os Controladores de Vista:**
  * **Pasta:** `src/main/java/.../web/`.
  * **Ação:** Criar classes como `WebRestauranteController.java` ou `WebUtilizadorController.java` (anotadas com `@Controller`).
* [ ] **2.2. Estabelecer Comunicação REST:**
  * **Ficheiros:** Os teus controladores Web.
  * **Ação:** Como a interface web tem de comunicar com o backend através de *endpoints* REST
  , os teus controladores web ou o teu JavaScript no frontend devem fazer chamadas HTTP (usando `RestTemplate`, `WebClient`, ou a API `fetch` no *browser*) para os *controllers* já existentes na pasta `src/main/java/.../controllers/`.

---

#### Passo 3: Implementar Casos de Uso "Azuis" (Consultas e Filtros)
O cliente pediu especificamente uma interface web interativa para realizar consultas e buscas complexas
.

* [x] **3.1. Ecrã de Pesquisa de Restaurantes (Caso E):**
  * **Ação:** Criar a página HTML e ligá-la ao backend para permitir procurar restaurantes utilizando os novos filtros: por nome, pedidos realizados, avaliações, morada, tipo de cozinha, horário de funcionamento e preço médio 
* [x] **3.2. Ecrã de Pesquisa de Produtos (Caso I):**
  * **Ação:** Criar a página HTML que liste produtos de um restaurante, aplicando os filtros: por nome, preço, categoria, disponibilidade e popularidade 
* [x] **3.3. Ecrã de Visualização de Menus e Utilizadores:**
  * **Ação:** Desenvolver páginas de consulta para aplicar os filtros de Menu (nome, número de produtos, preço médio) 
  e Filtros de Utilizador 

---

#### Passo 4: Implementar Casos de Uso "Castanhos" (Partilhados)
Estes são os casos de uso transversais, que precisam de existir tanto na versão Web como na versão Nativa

* [ ] **4.1. Autenticação e Registos (Casos A, B, C, D):**
  * **Ação:** Construir os formulários de *Login* (fazendo *mock* onde qualquer palavra-passe é aceite se o utilizador for válido 
  , e as páginas de registo, verificação, atualização e remoção para Clientes, Administradores e Entregadores 
* [ ] **4.2. Fluxo de Pedidos (Casos J, K, L, M, N):**
  * **Ação:** Criar as interfaces visuais para a criação de um pedido 
   registo do pagamento (recolhendo bandeira do cartão ou telemóvel conforme o tipo) 
  , atualização do estado 
  , visualização da atribuição do entregador 
   e cancelamento
  .
* [ ] **4.3. Avaliação de Restaurantes:**
  * **Ação:** Incluir na interface um formulário para que o cliente possa avaliar o restaurante após uma compra

---

#### Passo 5: Testes, Usabilidade e Demonstração
O vosso projeto será fortemente penalizado se falhar na execução ou se a usabilidade for pobre

* [ ] **5.1. Refinar a Usabilidade:**
  * **Ação:** Garantir que a interface desenhada cumpre os objetivos de forma intuitiva e proporciona uma experiência de utilizador consistente
* [ ] **5.2. Testar no Docker Localmente:**
  * **Ação:** Correr a aplicação usando o vosso ficheiro `docker-compose.yml` localmente
  . Aceder ao *browser* (`http://localhost:8080` ou similar) e testar todos os formulários e ecrãs para detetar possíveis problemas de integração.
* [ ] **5.3. Gravar o Vídeo Demonstrativo Web:**
  * **Ação:** Preparar e gravar o vídeo da Fase 2 focado exclusivamente na interface Web
  . Demonstrar com clareza o funcionamento real, a aplicação dos filtros e explicar o trabalho desenvolvido pelo grupo
* [ ] **5.4. Adicionar o Link do Vídeo:**
  * **Ação:** Adicionar o link final do vídeo da interface web no ficheiro `README.md`


# tratar de menus

### 📋 Phase 1: Database Entities (JPA Models)
- [ ] **Create `Menu` Entity**
  - Define class with `@Entity`.
  - Add standard fields: `@Id`, `nome`, `descricao`.
  - Add an aggregate field or calculated method for `precoMedio` (average price).
  - Define a `@OneToMany` relationship pointing to `Product`.
- [ ] **Create `Product` Entity**
  - Define class with `@Entity`.
  - Add fields: `nome`, `preco`, `categoria`, `disponivel`, `descricao`, `alergenios`.
  - Define a `@ManyToOne` relationship pointing back to the `Menu`.
- [ ] **Refactor `Restaurant` Entity**
  - Define class with `@Entity`.
  - Add a `@ManyToOne` relationship pointing to the `Menu` (This allows 50 franchise locations to share 1 Menu ID).

### ⚙️ Phase 2: Repositories & Services
- [ ] **Create Repositories**
  - Build `MenuRepository` extending `JpaRepository<Menu, Long>`.
  - Build `ProductRepository` extending `JpaRepository<Product, Long>`.
- [ ] **Implement Service Logic**
  - Create `MenuService` to handle business logic (e.g., calculating the total number of items or average price dynamically).
  - Create `ProductService` to handle the complex filtering (Name, Max Price, Category, Availability) using Spring Data JPA custom queries or Specifications instead of Java Streams.

### 🔌 Phase 3: Controller Refactoring
- [ ] **Update `WebMenuController`**
  - Inject `MenuService`.
  - Replace the `getMockMenus()` list with a call to `menuRepository.findAll()`.
- [ ] **Update `WebRestauranteController`**
  - Remove `getMockProdutos()`.
  - Update the `pesquisarProdutos` endpoint to query the actual database based on the selected `Menu ID` and active filters.

### 🍔 Phase 4: Franchise Data Seeding (Testing)
- [ ] **Update `data.sql` (or Database Seeder)**
  - Insert 1 core Franchise Menu (e.g., "Standard Burger Menu").
  - Insert 10 core Products linked to that 1 Menu.
  - Insert 3 different Restaurants (e.g., "Downtown Branch", "Airport Branch", "Mall Branch") and assign all of them to the exact same Menu ID.
- [ ] **Run and Verify**
  - Boot the application and verify that clicking on any of the 3 franchise locations loads the exact same products without duplicating data in the database.

### ## Membro 3: Interface Nativa com JavaFX (O "Native Developer")

O teu trabalho focar-se-á nos pacotes `grpc`, `ui/javafx` e `proto`.

**Fase A: Setup gRPC (`src/main/proto` e `src/main/java/.../grpc`)**
* [ ] **Definir Contratos:** Criar os ficheiros na pasta `proto/tascaeats` definindo os serviços e mensagens.
* [ ] **Atualizar pom.xml:** Garantir que o ficheiro `pom.xml` tem os plugins necessários para compilar os ficheiros `.proto` e gerar o código Java.
* [ ] **Implementar Serviços gRPC:** Criar as classes na pasta `grpc`  que estendem as bases geradas e ligam aos `Services` de negócio

**Fase B: Desenvolvimento Frontend Nativo (`src/main/java/.../ui/javafx`)**
* [ ] **Inicializar JavaFX:** Configurar o ciclo de vida do JavaFX dentro do ecossistema Spring Boot na pasta `ui/javafx`
* [ ] **Casos de Uso Roxos (Inserção):** Criar as janelas e a integração gRPC para inserir dados (ex: criar pedidos, gerir restaurantes)
* [ ] **Casos de Uso Castanhos (Híbridos):** Implementar os ecrãs para as funcionalidades transversais (ex: Login, Registos)

**Fase C: Validação**
* [ ] **Vídeo Nativo:** Filmar a interação com a aplicação nativa conectada via gRPC

---

### ## Todos Juntos: Check-list de Entrega Conjunta

* [ ] **Atualizar README.md:** Colocar os links para ambos os vídeos (Web e Nativo)
* [ ] **Teste Docker:** Executar `clean_docker.sh` seguido de `run.sh` em máquinas diferentes para garantir que o projeto compila e corre perfeitamente no ambiente fechado
* [ ] **Submissão via Git:** Certificar de que todo o código está em *commit*, usar `git tag fase2` e `git push origin fase2`
* [ ] **Permissões GitLab:** Confirmar que a conta `CSS000` tem acesso de *Reporter* e consegue ver a Tag submetida
