package processamento;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.sql.PreparedStatement;

public class ItensLicitacao {

    public static void atualizaItemLicitacao(Object codLic, Object codIteLic, BigDecimal vlrUnit, BigDecimal vlrTotal, BigDecimal markupFator, BigDecimal custo) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeFactory.dao("AD_ITENSLICITACAO").
                    prepareToUpdateByPK(codLic, codIteLic)
                    .set("VLRUNIT", vlrUnit)
                    .set("VLRTOTAL", vlrTotal)
                    .set("CUSTO", custo)
                    .set("MARKUPFATOR", markupFator)
                    .update();
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

    public static void atualizaItemLic(Object codLic, Object codIteLic, BigDecimal vlrUnit, BigDecimal vlrTotal, BigDecimal markupFator, BigDecimal custo) throws Exception {
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
        jdbcWrapper.openSession();

        String update = "UPDATE AD_ITENSLICITACAO SET CUSTO="+custo+",VLRTOTAL="+vlrTotal+",VLRUNIT="+vlrUnit+", MARKUPFATOR = " +markupFator+
                " where CODITELIC="+codIteLic+"  and CODLIC="+codLic;
        PreparedStatement updateValidando = jdbcWrapper.getPreparedStatement(update);
        updateValidando.executeUpdate();

        jdbcWrapper.closeSession();
    }

}
