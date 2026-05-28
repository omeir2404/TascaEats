# TascaEats Fase 3 - Plano de Ação e TODOs

Este documento detalha o plano de migração da Fase 3 para a arquitetura de microserviços (Domain-Driven Design), focado no Bounded Context de **Gestão de Entregas**. 

O trabalho está dividido em três partes iguais para facilitar a distribuição entre o grupo (Omeir, Isa e Diogo). Sendo o ambiente de desenvolvimento baseado em Linux e Docker, os scripts assumem execução em bash.

---

## 🏗️ Parte 1: Infraestrutura e Reverse Proxy (Scaffolding)
*Objetivo: Estabelecer a infraestrutura distribuída com Docker, configurar o Nginx como ponto único de entrada e criar o esqueleto do novo serviço.*

### 1.1 Atualização do `docker-compose.yml`
- [x] **Adicionar Apache Kafka:** Configurar o serviço do Kafka (e Zookeeper/KRaft) para suportar mensageria. Garantir que expõe as portas corretas para a rede interna `dbnet`.
- [x] **Adicionar PostgreSQL Isolado:** Criar um novo serviço de base de dados para o microserviço de entregas (ex: `pgserver-entregas`), distinto do `pgserver` do monólito.
- [x] **Adicionar Nginx:** Configurar o contentor Nginx que irá atuar como Reverse Proxy.
- [x] **Adicionar Novo Microserviço:** Adicionar a definição do serviço `delivery-backend` ao `docker-compose.yml`, dependente do `pgserver-entregas` e do Kafka.

### 1.2 Configuração do Nginx (`nginx.conf`)
- [x] Criar o ficheiro `nginx.conf` na raiz do projeto (ou pasta `config`).
- [x] Configurar blocos `upstream` para apontar para o monólito (`backend:8080`).
- [x] Configurar rotas para encaminhar pedidos REST (provenientes da Web).
- [x] Configurar rotas para encaminhar pedidos gRPC (provenientes da interface JavaFX).
- [x] Atualizar o serviço Web (`web`) para fazer chamadas ao Nginx em vez de chamar o monólito diretamente.

### 1.3 Esqueleto do Microserviço de Entregas
- [x] Criar a diretoria `/delivery-service` ao lado do `/backend` e `/web`.
- [x] Inicializar um projeto Spring Boot (Maven, Java 17).
- [x] Configurar `application.properties` para ligar exclusivamente à nova base de dados PostgreSQL (`pgserver-entregas`).
- [x] Configurar dependências: Spring Web, Spring Data JPA, PostgreSQL Driver, Spring Kafka.

---

## ✂️ Parte 2: Extração do Domínio (O Microserviço)
*Objetivo: Migrar a lógica de negócio de entregas do monólito para o novo microserviço, garantindo isolamento de dados.*

### 2.1 Migração de Entidades e DTOs
- [x] Mover/Replicar a entidade `Entregador` para o novo microserviço.
- [x] Mover/Replicar a entidade `Entrega` (e os seus estados: "A caminho", "Entregue") para o novo microserviço.
- [x] Importar os DTOs correspondentes (`AtribuirEntregadorDTO`, `EntregaResumoDTO`, etc.).
- [ ] Garantir que o novo serviço guarda apenas referências lógicas (ex: `pedidoId` (Long/UUID), `morada` (String/Objeto de Valor)) e não referências diretas de chaves estrangeiras a clientes ou restaurantes.

### 2.2 Migração de Repositórios e Controladores
- [ ] Migrar `IEntregaRepository` e `IEntregadorRepository` para o novo microserviço.
- [ ] Migrar lógica do `EntregaService` para o novo serviço.
- [ ] Recriar o `EntregaController` no novo serviço (apenas para gestão interna, se aplicável, ou para ser consumido por eventos).
- [ ] Implementar as 4 funcionalidades principais descritas no guião:
    - [ ] 1. Registo, atualização e gestão do perfil de Entregadores.
    - [ ] 2. Filtros e buscas relativos a entregadores.
    - [ ] 3. Atribuição de entregador a um pedido recém-pago.
    - [ ] 4. Atualização do estado da entrega.

### 2.3 Limpeza do Monólito (Backend)
- [ ] Remover entidades `Entrega` e dependências diretas de bases de dados relacionais ao módulo de entregas.
- [ ] Adaptar o `Pedido` no monólito para armazenar apenas o ID do entregador atribuído ou o estado da entrega em formato "read-only" (para consistência eventual).
- [ ] Adaptar/Remover `EntregaController` do monólito, pois o domínio passou para o microserviço.

---

## 🔄 Parte 3: Integração Kafka e Consistência Eventual
*Objetivo: Estabelecer a comunicação assíncrona orientada a eventos e validar as interfaces gráficas.*

### 3.1 O Monólito como Produtor (Producer)
- [ ] Configurar o `KafkaTemplate` no `backend` (monólito).
- [ ] Criar tópicos Kafka (ex: `pedido-pago-topic`).
- [ ] Injetar produção de eventos no `PagamentoService` ou `PedidoService`: quando o estado de um pedido passa a "PAGO", emitir um evento para o Kafka contendo o `pedidoId` e dados da morada.

### 3.2 O Microserviço como Consumidor (Consumer)
- [ ] Configurar `@KafkaListener` no `delivery-service`.
- [ ] Implementar a escuta do tópico `pedido-pago-topic`.
- [ ] Ao receber a mensagem, acionar a lógica de "Atribuição de Entregador" (automática ou manual).
- [ ] (Opcional, mas recomendado - Saga/Coreografia) Após atualizar o estado da entrega para "Entregue", o microserviço emite um evento (ex: `entrega-concluida-topic`) para o monólito atualizar a sua vista de leitura do Pedido.

### 3.3 Validação de Interfaces e UI
- [ ] Confirmar que a Interface Web (Thymeleaf) consegue ler o estado dos pedidos através do monólito (que foi atualizado por consistência eventual).
- [ ] Confirmar que a GUI nativa (JavaFX) ainda funciona sem alterações estruturais, apenas comunicando via gRPC através do Nginx.
- [ ] Corrigir possíveis *bugs* de dessincronização visual que possam surgir da latência dos eventos.

### 3.4 Testes Finais e Entrega
- [ ] **Teste de Orquestração:** Garantir que o comando `docker-compose up --build` levanta todo o sistema sem erros (Postgres 1, Postgres 2, Kafka, Monólito, Microserviço, Nginx, Web).
- [ ] **Teste de Fluxo Completo:** Simular um pedido > Pagamento > Criação de Evento Kafka > Processamento no Microserviço de Entregas > Atualização de Estado.
- [ ] **Gravação do Vídeo de Demonstração:** Gravar o ecrã mostrando claramente o caminho do pedido, os logs do Kafka e a divisão de base de dados.
- [ ] Colocar o link do novo vídeo no `README.md`.
- [ ] Submeter versão final:
    - `git add .`
    - `git commit -m "feat: complete Phase 3 microservice migration"`
    - `git tag fase3`
    - `git push origin fase3`

---
*Dica: Como o ambiente é Linux, utilizem ferramentas como o `kcat` (antigo kafkacat) para monitorizar os tópicos do Kafka no terminal durante o desenvolvimento e debugging!*
