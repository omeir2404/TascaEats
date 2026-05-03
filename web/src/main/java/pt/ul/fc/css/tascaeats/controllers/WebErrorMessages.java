// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################

package pt.ul.fc.css.tascaeats.controllers;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

final class WebErrorMessages {

    private WebErrorMessages() {
    }

    static String backendFailure(String action, Exception exception, String backendUrl) {
        if (exception instanceof ResourceAccessException) {
            return action + ": nao foi possivel contactar o backend em " + backendUrl
                    + ". Confirma se o servico backend esta ligado e se o endereco BACKEND_API_URL esta correto.";
        }

        if (exception instanceof RestClientResponseException responseException) {
            String details = extractBackendMessage(responseException);
            return action + ": o backend respondeu com erro "
                    + responseException.getStatusCode().value()
                    + (details.isBlank() ? "." : " - " + details);
        }

        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return action + ": ocorreu um erro inesperado ao comunicar com o backend.";
        }
        return action + ": ocorreu um erro inesperado ao comunicar com o backend - " + message;
    }

    static String validationFailure(String action, Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return action + ": confirma os dados e tenta novamente.";
        }
        return action + ": " + message;
    }

    private static String extractBackendMessage(RestClientResponseException exception) {
        String body = exception.getResponseBodyAsString();
        if (body == null || body.isBlank()) {
            return "";
        }

        String compactBody = body.replaceAll("\\s+", " ").trim();
        if (compactBody.length() > 180) {
            return compactBody.substring(0, 177) + "...";
        }
        return compactBody;
    }
}
