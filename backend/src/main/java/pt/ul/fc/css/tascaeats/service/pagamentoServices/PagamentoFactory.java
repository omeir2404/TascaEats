// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.service.pagamentoServices;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import pt.ul.fc.css.tascaeats.domain.pagamento.Pagamento;
import pt.ul.fc.css.tascaeats.domain.pagamento.PagamentoCartao;
import pt.ul.fc.css.tascaeats.domain.pagamento.PagamentoDinheiro;
import pt.ul.fc.css.tascaeats.domain.pagamento.PagamentoMBWay;
import pt.ul.fc.css.tascaeats.domain.pagamento.PagamentoMultibanco;

@Component
public class PagamentoFactory {

    public Pagamento create(String tipoPagamento, double valor, Map<String, Object> detalhes) {
        if (tipoPagamento == null || tipoPagamento.trim().isEmpty()) {
            throw new IllegalArgumentException("O tipo de pagamento é obrigatório.");
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor do pagamento tem de ser maior que zero.");
        }

        String referenciaPagamento = getString(detalhes, "referenciaPagamento", gerarReferenciaPagamento());
        LocalDate dataPagamento = LocalDate.now();

        Pagamento pagamento;
        String upperPagamentoType = tipoPagamento.trim().toUpperCase();
        switch (upperPagamentoType) {
            case "MBWAY":
                pagamento = new PagamentoMBWay( null, valor, dataPagamento, referenciaPagamento, getStringObrigatorio(detalhes, "numeroTelemovel"));
                break;
            case "DINHEIRO":
                pagamento = new PagamentoDinheiro( null, valor, dataPagamento, referenciaPagamento, getDoubleObrigatorio(detalhes, "valorRecebido"));
                break;
            case "MULTIBANCO":
                pagamento = new PagamentoMultibanco( null, valor, dataPagamento, referenciaPagamento, getStringObrigatorio(detalhes, "entidade"),
                        getStringObrigatorio(detalhes, "referencia"), getString(detalhes, "bandeiraCartao", "MULTIBANCO"));
                break;
            case "CARTAO":
                pagamento = new PagamentoCartao(null, valor, dataPagamento, referenciaPagamento,
                        getStringObrigatorio(detalhes, "numeroCartao"));
                break;
            default:
                throw new IllegalArgumentException("Tipo de pagamento não suportado: " + upperPagamentoType);
        }
    

        pagamento.validarDetalhes();
        return pagamento;
    }

    private static String getStringObrigatorio(Map<String, Object> detalhes, String campo) {
        Object valor = detalhes.get(campo);
        if (valor == null) {
            throw new IllegalArgumentException("Campo obrigatório em detalhes: " + campo);
        }
        String texto = String.valueOf(valor).trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("Campo obrigatório em detalhes: " + campo);
        }
        return texto;
    }

    private static double getDoubleObrigatorio(Map<String, Object> detalhes, String campo) {
        Object valor = detalhes.get(campo);
        if (valor == null) {
            throw new IllegalArgumentException("Campo obrigatório em detalhes: " + campo);
        }

        if (valor instanceof Number numero) {
            return numero.doubleValue();
        }

        try {
            return Double.parseDouble(String.valueOf(valor).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Campo numérico inválido em detalhes: " + campo);
        }
    }

    private static String getString(Map<String, Object> detalhes, String campo, String valorDefault) {
        Object valor = detalhes.get(campo);
        if (valor == null) {
            return valorDefault;
        }
        String texto = String.valueOf(valor).trim();
        if (texto.isEmpty())
            return valorDefault;
        else
            return texto;

    }

    private static String gerarReferenciaPagamento() {
        return "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
