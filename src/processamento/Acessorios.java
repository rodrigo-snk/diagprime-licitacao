package processamento;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Acessorios {

    public static void deleteFromItemNota(PersistenceEvent arg0) throws Exception {

        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
        jdbcWrapper.openSession();

        DynamicVO acessorioVO = (DynamicVO) arg0.getVo();
        BigDecimal codLic = acessorioVO.asBigDecimalOrZero("CODLIC");
        BigDecimal codLicCom = acessorioVO.asBigDecimalOrZero("CODLICCOM");
        BigDecimal codProd = acessorioVO.asBigDecimalOrZero("CODPROD");

        String consultaCabecalho = "select * from ad_licitacao where codlic="+codLic;
        PreparedStatement consultaValidando2 = jdbcWrapper.getPreparedStatement(consultaCabecalho);
        ResultSet consultaCabecalho2 = consultaValidando2.executeQuery();

        while(consultaCabecalho2.next()){

            BigDecimal nuNota = consultaCabecalho2.getBigDecimal("NUNOTA");

            String update = "DELETE FROM TGFITE WHERE NUNOTA="+nuNota+" AND AD_CODLICCOM="+codLicCom+" and CODPROD="+codProd;
            PreparedStatement  updateValidando = jdbcWrapper.getPreparedStatement(update);
            updateValidando.executeUpdate();

            ImpostosHelpper impostos = new ImpostosHelpper();
            impostos.setForcarRecalculo(true);
            impostos.calcularImpostos(nuNota);

        }

        jdbcWrapper.closeSession();
    }

    public static void salvarAcessoriosDados(
            BigDecimal nuNota,
            BigDecimal codProd,
            BigDecimal qtdNeg,
            String codVol,
            BigDecimal vlrUnit,
            BigDecimal vlrTot,
            BigDecimal codEmp,
            BigDecimal codLicCom,
            BigDecimal codLic) throws Exception {

        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbc = dwfFacade.getJdbcWrapper();
        jdbc.openSession();

        DynamicVO itemVO = (DynamicVO)dwfFacade.getDefaultValueObjectInstance("ItemNota");
        itemVO.setProperty("NUNOTA", nuNota);
        itemVO.setProperty("CODPROD", codProd);
        itemVO.setProperty("CODEMP", codEmp);
        itemVO.setProperty("QTDNEG", qtdNeg);
        itemVO.setProperty("CODVOL", codVol);
        itemVO.setProperty("VLRUNIT", vlrUnit);
        itemVO.setProperty("VLRTOT", vlrTot);
        itemVO.setProperty("AD_CODLICCOM", codLicCom);
        itemVO.setProperty("AD_CODLIC", codLic);
        itemVO.setProperty("AD_ACESSORIOS", "S");
        itemVO.setProperty("USOPROD", "V");
        itemVO.setProperty("RESERVA", "N");
        itemVO.setProperty("ATUALESTOQUE", new BigDecimal(0));
        dwfFacade.createEntity("ItemNota", (EntityVO)itemVO);

        jdbc.closeSession();


    }
}
