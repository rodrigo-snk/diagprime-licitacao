package inicio;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import processamento.Impostos;

import java.math.BigDecimal;

public class AtualizaImpostos implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        BigDecimal codLic = (BigDecimal) contextoAcao.getLinhaPai().getCampo("CODLIC");
        Impostos.recalculaImpostos(codLic);
    }
}
