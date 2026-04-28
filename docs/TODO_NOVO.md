### ## Membro 1: Backend, Domínio e Filtros (O "Arquiteto de Dados")

O teu trabalho focar-se-á nos pacotes `domain`, `dto`, `repository` e `service`.

**Fase A: Refatoração do Modelo de Domínio (`src/main/java/.../domain`)**
* [ ] **Entidade Menu Partilhado:** Criar as classes de menu na pasta vazia `domain/menu`
. Garantir que um menu pode ser partilhado por vários restaurantes e que as modificações se refletem em todos
* [ ] **Entidade Avaliação:** Criar as classes necessárias na pasta vazia `domain/avaliacao` para suportar a avaliação do restaurante
* [ ] **Atualizar Moradas:** Editar `domain/utilizador/Cliente.java` e `Morada.java` para suportar uma relação de 1:N (múltiplas moradas)
* [ ] **Atualizar Pagamentos:** Editar `PagamentoMultibanco.java`, `PagamentoMBWay.java` e `PagamentoDinheiro.java` para adicionar os novos atributos: bandeira do cartão, número de telemóvel e informação sobre o troco
* [ ] **Atualizar Pedidos:** Editar `domain/pedido/Pedido.java` para suportar produtos vindos de diferentes restaurantes num só pedido

**Fase B: Lógica de Negócio (`src/main/java/.../service`)**
* [ ] **Automatizar Entregadores:** Editar `EntregaService.java` e `PedidoService.java` para associar automaticamente um entregador (válido) a um pedido
* [ ] **Atualizar DTOs:** Modificar ou criar novos DTOs em `dto/` para suportar as novas entidades e atributos nas respostas da API.

**Fase C: Sistema de Buscas e Filtros (`src/main/java/.../repository`)**
* [ ] **Implementar Filtros Complexos:** Utilizar o *CriteriaBuilder* ou anotações `@Query` nos ficheiros de repositório (ex: `IRestauranteRepository.java`, `IProdutoRepository.java`) para suportar todos os filtros exigidos 
* [ ] **Validar Queries de Negócio:** Garantir que as queries respondem às novas perguntas (média do troco, item mais pedido, etc.) Adicionar os testes adequados na pasta `src/test/java/.../repositories`

---

### ## Membro 2: Interface Web com Thymeleaf (O "Web Developer")

O teu trabalho focar-se-á nos pacotes `web`, na pasta `resources` e parcialmente nos `controllers`.

**Fase A: Setup de Views e Estilos (`src/main/resources/`)**
* [ ] **Ficheiros Estáticos:** Adicionar CSS, JS e Imagens na pasta `resources/static`
* [ ] **Templates Thymeleaf:** Criar os ficheiros HTML (ex: `index.html`, `restaurantes.html`) na pasta `resources/templates`

**Fase B: Desenvolvimento Frontend Web (`src/main/java/.../web`)**
* [ ] **Criar Web Controllers:** Preencher a pasta `web` com controladores Spring MVC (anotados com `@Controller`, não `@RestController`) que intercetam as rotas do browser e devolvem as *views* do Thymeleaf compiladas
* [ ] **Casos de Uso Azuis (Consultas):** Implementar as páginas e a ligação ao backend para os filtros de pesquisa
* [ ] **Casos de Uso Castanhos (Híbridos):** Desenhar as páginas partilhadas (ex: Login, Registos)

**Fase C: Integração e Testes**
* [ ] **Manter a API REST Intacta:** Garantir que o trabalho no frontend não parte os controladores REST antigos existentes em `controllers/`
* [ ] **Vídeo Web:** Gravar a demonstração mostrando as funcionalidades implementadas e executadas a partir do browser

---

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
