// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################


package pt.ul.fc.css.tascaeats.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class RestauranteApiClient {


    public record RestaurantSearchResult(List<RestauranteResumo> restaurantes, boolean fallbackUsado, String mensagem) {
    }

    private final NativeGrpcClient grpcClient;

    public RestauranteApiClient(NativeGrpcClient grpcClient) {
        this.grpcClient = grpcClient;
    }

    public String getBackendUrl() {
        return AppState.GRPC_HOST + ":" + AppState.GRPC_PORT;
    }

    public List<RestauranteResumo> listarRestaurantes(String nome, String estado) {
        var payload = new java.util.LinkedHashMap<String, Object>();
        if (nome != null && !nome.isBlank()) {
            payload.put("nome", nome.trim());
        } else if (estado != null && !estado.isBlank()) {
            payload.put("estado", estado.trim());
        }
        List<Map<String, Object>> resposta = grpcClient.invokeList("listRestaurants", payload);

        if (resposta == null || resposta.isEmpty()) {
            return List.of();
        }

        List<RestauranteResumo> restaurantes = new ArrayList<>(resposta.size());
        for (Map<String, Object> item : resposta) {
            restaurantes.add(toResumo(item));
        }
        return restaurantes;
    }


    public RestaurantSearchResult listarRestaurantesComFallback(String nome, String estado) {
        List<RestauranteResumo> restaurantes = listarRestaurantes(nome, estado);
        String mensagem = restaurantes.isEmpty()
                ? "Nenhum restaurante encontrado."
                : restaurantes.size() + " restaurante(s) carregado(s) com sucesso.";
        return new RestaurantSearchResult(restaurantes, false, mensagem);
    }

    private RestauranteResumo toResumo(Map<String, Object> item) {
        Long id = null;
        Object rawId = item.get("id");
        if (rawId instanceof Number number) {
            id = number.longValue();
        } else if (rawId != null) {
            try {
                id = Long.parseLong(rawId.toString());
            } catch (NumberFormatException ignored) {
                id = null;
            }
        }

        String nome = Objects.toString(item.get("nome"), "");
        String estado = Objects.toString(item.get("estado"), "");
        return new RestauranteResumo(id, nome, estado);
    }

    private List<RestauranteResumo> filtrarDemoRestaurantes(String nome, String estado) {
        List<RestauranteResumo> demo = demoRestaurantes();
        String nomeNormalizado = nome == null ? "" : nome.trim().toLowerCase();
        String estadoNormalizado = estado == null ? "" : estado.trim().toUpperCase();

        List<RestauranteResumo> filtrados = new ArrayList<>();
        for (RestauranteResumo resumo : demo) {
            boolean matchNome = nomeNormalizado.isBlank() || resumo.nome().toLowerCase().contains(nomeNormalizado);
            boolean matchEstado = estadoNormalizado.isBlank() || resumo.estado().equalsIgnoreCase(estadoNormalizado);
            if (matchNome && matchEstado) {
                filtrados.add(resumo);
            }
        }
        return filtrados;
    }

    private List<RestauranteResumo> demoRestaurantes() {
        return List.of(
                new RestauranteResumo(1L, "Tasca do Zé", "ABERTO"),
                new RestauranteResumo(2L, "Sabor da Vila", "FECHADO"),
                new RestauranteResumo(3L, "Casa da Avó", "RESERVADO"),
                new RestauranteResumo(4L, "Maré Alta", "ABERTO")
        );
    }
}
