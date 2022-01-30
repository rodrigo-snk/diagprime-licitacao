package helpper;

import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.PreparedStatement;
import java.util.Optional;

public class Licitacao {

    public static void atualizaImpostosFederais(DynamicVO licitacaoVO) throws Exception {

        BigDecimal percentualCSLLRetido = licitacaoVO.asBigDecimalOrZero("PERCENTUAL_CSLL");
        BigDecimal percentualCSLLDevido = licitacaoVO.asBigDecimalOrZero("PERCENTUAL_CSLLDEVIDO");
        BigDecimal percentualIRRetido = licitacaoVO.asBigDecimalOrZero("PERCENTUAL_IR");
        BigDecimal percentualIRDevido = licitacaoVO.asBigDecimalOrZero("PERCENTUAL_IRDEVIDO");

        // Se % CSLL devido for 0,00 ou nulo, atribui o valor do % CSLL retido
        if (percentualCSLLDevido.compareTo(BigDecimal.ZERO) == 0){
            licitacaoVO.setProperty("PERCENTUAL_CSLLDEVIDO", percentualCSLLRetido);
        }

        // Se % IR devido for 0,00 ou nulo, atribui o valor do % IR retido
        if (percentualIRDevido.compareTo(BigDecimal.ZERO) == 0){
            licitacaoVO.setProperty("PERCENTUAL_IRDEVIDO", percentualIRRetido);
        }

        // Cálculo dos valores de CSSS e IR devido.
        licitacaoVO.setProperty("CSLLDEVIDO", licitacaoVO.asBigDecimalOrZero("VLRTOTAL").multiply(licitacaoVO.asBigDecimalOrZero("PERCENTUAL_CSLLDEVIDO").divide(BigDecimal.valueOf(100), MathContext.DECIMAL128)));
        licitacaoVO.setProperty("IRDEVIDO", licitacaoVO.asBigDecimalOrZero("VLRTOTAL").multiply(licitacaoVO.asBigDecimalOrZero("PERCENTUAL_IRDEVIDO").divide(BigDecimal.valueOf(100), MathContext.DECIMAL128)));

        EntityFacadeFactory.getDWFFacade().saveEntity("AD_LICITACAO", (EntityVO) licitacaoVO);
    }

    public static void excluiReferencias(JdbcWrapper jdbc, BigDecimal nuNota) throws Exception {
        PreparedStatement deleteReferencia = jdbc.getPreparedStatement("DELETE FROM TGFVAR WHERE NUNOTAORIG = "+ nuNota);
        deleteReferencia.executeUpdate();
    }

    public static BigDecimal getContatoPregoeiro(BigDecimal codParc) throws Exception {
     Optional<DynamicVO> contatoVO = EntityFacadeFactory.getDWFFacade().findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.CONTATO, "this.CODPARC = ? AND this.NOMECONTATO LIKE 'SR(A). PREGOEIRO(A)%'", codParc)).stream().findFirst();
            if (contatoVO.isPresent()) {
                return contatoVO.get().asBigDecimalOrZero("CODCONTATO");
            }
        return BigDecimal.ZERO;
    }
}

