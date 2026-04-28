# Divisão de Trabalho — Fase 2

## 1. Fase de Planeamento (todos juntos — 1–2 dias)

Antes de escrever qualquer lógica de negócio, os três membros devem reunir-se para definir os contratos de comunicação — isto resolve a maioria dos conflitos posteriores.

- **Definir DTOs e interfaces de serviço**: acordar nomes das classes DTO e assinaturas dos métodos que os controllers/Thymeleaf irão chamar.
- **Definir o ficheiro .proto (gRPC)**: escrever o contrato gRPC em conjunto para especificar exatamente as mensagens que a aplicação JavaFX nativa irá enviar/receber.
- **Atualizar modelo de domínio e BD**: aplicar em conjunto as alterações nas entidades base (por exemplo, `Restaurante`, `Menu`, `Pedido`, `Morada`) num único commit inicial para a branch principal (`main`).

## 2. Desenvolvimento paralelo (divisão híbrida)

Com os contratos (interfaces, DTOs e `.proto`) na branch principal, cada membro pode trabalhar em branches isoladas sem interromper os demais. Se a interface gráfica precisar de dados, usem mocks baseados no contrato.

### Membro 1 — Especialista de dados e filtros (backend)

- **Foco**: repositórios, especificações JPA (filtros) e lógica de negócio (services).
- **Isolamento**: responsabilidade por ficheiros `*Repository.java` e queries SQL; outros membros não devem alterar esses ficheiros.
- **Teste**: escrever testes unitários para garantir que o backend funciona independentemente da UI.

### Membro 2 — Especialista Web (Thymeleaf & REST)

- **Foco**: controllers web, templates Thymeleaf e endpoints REST.
- **Isolamento**: trabalha na pasta de templates e nos controllers web.
- **Como avançar sem o Membro 1**: chamar a interface do service (definida na Fase 1) e devolver dados mock enquanto o backend real não estiver pronto; remover o mock quando o backend estiver implementado.

### Membro 3 — Especialista Nativo (JavaFX & gRPC)

- **Foco**: views JavaFX e integração gRPC (cliente/servidor).
- **Isolamento**: trabalhar nas views JavaFX e no cliente/servidor gRPC.
- **Como avançar sem o Membro 1**: usar um servidor de testes local que responde com valores estáticos conforme o `.proto` acordado.

## 3. Estratégia prática com Git para evitar conflitos

Algumas regras simples ajudam a garantir que a avaliação da participação seja clara e que os merges sejam fáceis:

- **Uma branch por funcionalidade**: criar `feature/...` em vez de trabalhar diretamente na `main` (ex.: `feature/filtros-restaurante`, `feature/ui-javafx-login`).
- **Atualizar antes de submeter**: usar `git pull --rebase` para trazer as últimas alterações da `main` e resolver conflitos localmente antes do Pull/Merge Request.
- **Pre-commit / formatação automática**: ativar pre-commit hooks e usar `format-code.sh` para evitar diffs por estilo (espaços/linhas).
- **Separação clara de diretórios**: designar áreas de responsabilidade (ex.: `src/main/resources/templates` para o Membro 2, `src/main/.../ui` para o Membro 3) para reduzir conflitos.
