This is a structured Markdown version of your project video instructions. I’ve organized it with clear headers, checklists, and callouts to make it easy to follow during your recording process.

---

# 📹 Instruções para os Vídeos do Projeto: **TascaEats**

O vídeo submetido deve demonstrar o funcionamento completo do projeto e cumprir **obrigatoriamente** os seguintes requisitos técnicos e funcionais.

## 🛠️ Requisitos Técnicos
- **Clone do Projeto:** O vídeo deve iniciar com o comando `git clone`, mostrando claramente a URL e o terminal.
- **Execução do Docker:** Deve ser visível a diretoria onde o comando Docker é executado. O caminho (*path*) no terminal deve estar legível.
- **Vídeo Contínuo:** **Sem cortes ou edições.** A execução deve ser fluida do início ao fim para garantir a integridade do teste.
- **Legibilidade:** Garanta que o texto do terminal e as respostas da API (Postman/Swagger) estejam nítidos.

## 📝 Demonstração de Casos de Uso
Todos os casos abaixo devem ser demonstrados. A ausência de qualquer um resultará em **0 valores** para esse item específico. 

**Como identificar:** Utilize um Bloco de Notas visível no ecrã com a letra correspondente ou adicione legendas na edição (se não interromper o fluxo).

| ID | Caso de Uso |
| :--- | :--- |
| **A** | Login com autenticação |
| **B** | Registo de utilizadores (Clientes, Administradores e Entregadores) |
| **C** | Verificar, remover e atualizar utilizadores |
| **D** | Buscar por restaurantes (por nome ou cidade) |
| **E** | Criação de restaurantes e gestão do seu estado (aberto/fechado) |
| **F** | Adicionar, remover e atualizar produtos no cardápio |
| **G** | Buscar produtos de um restaurante |
| **H** | Criação de um pedido |
| **I** | Registo e processamento do pagamento associado a um pedido |
| **J** | Atualização do estado do pedido |
| **K** | Atribuição de um entregador a um pedido |
| **L** | Cancelamento de um pedido |

---

## ✅ Validações (Obrigatório)
Para cada funcionalidade relevante, deve demonstrar o comportamento do sistema em dois cenários:
1.  **Caso Positivo:** Input válido e sucesso na operação.
2.  **Caso Negativo:** Input inválido (ex: erro de permissão ou dados incorretos) e a respetiva mensagem de erro.

---

## 💡 Dicas para um Vídeo Eficiente
* **Dados Iniciais:** Utilize um script de *seed* (população de base de dados) no arranque do projeto para evitar perder tempo a inserir dados manualmente.
* **Duração:** Foque no essencial. O vídeo deve ser o mais curto possível, desde que cubra todos os pontos acima.
* **Postman/Swagger:** Utilize as ferramentas mencionadas no chat (`use_case_sequence.js` ou Swagger UI) para agilizar as chamadas de API.

---

## 📤 Submissão
* **Local:** Diretamente no repositório Git **OU** via link no `README.md`.
* **Privacidade:** Se usar Google Drive ou similar, garanta que o link está configurado para **"Qualquer pessoa com o link pode visualizar"**. 
* **Teste:** Verifique o link numa janela anónima do browser antes de entregar.

---
**Links Úteis do Projeto:**
* **Repo:** [https://git.alunos.di.fc.ul.pt/fc61810/tascaeats](https://git.alunos.di.fc.ul.pt/fc61810/tascaeats)
* **Swagger:** `http://localhost:8080/swagger-ui/index.html`

---

## ✅ Checklist Rápido (Gravação com Postman)

Se usares Postman em vez de Swagger, este fluxo é válido para o vídeo:

1. Mostrar no ecrã a collection `TascaEats_video.postman_collection.json`.
2. Executar primeiro a pasta `00 - Uniform Video Flow (A-L)` pela ordem A → L.
3. Em cada letra, demonstrar:
	- `1` pedido **VALID**.
	- `2` pedidos **INVALID**.
4. Em cada pedido, mostrar claramente:
	- Método + endpoint.
	- Body/query usados.
	- Status code + resposta.
5. Manter visível no ecrã a letra do caso de uso (A, B, C, ...).
6. Usar a pasta `Extras` apenas para casos fora do PDF (ex.: M).
7. Confirmar que o vídeo continua sem cortes desde o clone/setup até ao fim da demonstração.

---

## 📘 Abrir a Coleção em Formato Swagger (OpenAPI)

Foram gerados os ficheiros:

- `swagger-video.json`
- `swagger-video.yaml`

Podes abrir no **Swagger Editor** (mais simples):

1. Ir para [https://editor.swagger.io/](https://editor.swagger.io/).
2. Selecionar **File > Import file**.
3. Importar `swagger-video.yaml`.

Ou usar no **Swagger UI local**:

1. Garantir que a aplicação está a correr.
2. Aceder a `http://localhost:8080/swagger-ui/index.html`.
3. Se necessário, carregar o endpoint `/v3/api-docs` (ou `/v3/api-docs.yaml`) para visualizar a documentação.

> Nota: O Swagger mostra a documentação da API, mas não mantém a organização de execução por letras (A1, A2, ...), que está na coleção Postman.