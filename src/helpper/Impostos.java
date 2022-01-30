package helpper;

import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;

public class Impostos {

    public static void recalculaImpostos(BigDecimal codLic) throws Exception {
        DynamicVO licitacaoVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO("AD_LICITACAO",codLic);
        ImpostosHelpper impostos = new ImpostosHelpper();
        impostos.setForcarRecalculo(true);
        impostos.calcularImpostos(licitacaoVO.asBigDecimalOrZero("NUNOTA"));
    }
}
