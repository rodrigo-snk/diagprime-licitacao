package eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.dwfdata.vo.CabecalhoNotaVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.StringUtils;
import org.apache.commons.beanutils.DynaClass;

import java.math.BigDecimal;
import java.util.Collection;

public class AtualizaAdicionaisIte implements EventoProgramavelJava {

    private JdbcWrapper jdbc = null;

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

        JapeSession.SessionHandle hnd = null;

        try {

            hnd = JapeSession.open();

            EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
            jdbc = dwfEntityFacade.getJdbcWrapper();

            DynamicVO itemVO = (DynamicVO) persistenceEvent.getVo();
            CabecalhoNotaVO cabVO = (CabecalhoNotaVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, itemVO.asBigDecimalOrZero("NUNOTA"), CabecalhoNotaVO.class);
            Collection<DynamicVO> varVOs = dwfEntityFacade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.COMPRA_VENDA_VARIOS_PEDIDO, "this.NUNOTA = ? and this.SEQUENCIA = ? ", new Object[] {itemVO.asBigDecimalOrZero("NUNOTA"), itemVO.asBigDecimalOrZero("SEQUENCIA")}));

            if (varVOs.stream().findFirst().isPresent()) {
                DynamicVO varVO = varVOs.stream().findFirst().get();
                BigDecimal nuNotaOrig = varVO.asBigDecimalOrZero("NUNOTAORIG");
                BigDecimal sequenciaOrig = varVO.asBigDecimalOrZero("SEQUENCIAORIG");

                DynamicVO itemOrigVO = (DynamicVO) dwfEntityFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.ITEM_NOTA, new Object[] {nuNotaOrig, sequenciaOrig});

                if (itemVO.containsProperty(("AD_ACESSORIOS"))) itemVO.setProperty("AD_ACESSORIOS", itemOrigVO.asString("AD_ACESSORIOS"));
                if (itemVO.containsProperty(("AD_AGRUPAMENTO"))) itemVO.setProperty("AD_AGRUPAMENTO", itemOrigVO.asString("AD_AGRUPAMENTO"));
                if (itemVO.containsProperty(("AD_ATUALIZA"))) itemVO.setProperty("AD_ATUALIZA", itemOrigVO.asString("AD_ATUALIZA"));
                if (itemVO.containsProperty(("AD_CODGRUPOPROD"))) itemVO.setProperty("AD_CODGRUPOPROD", itemOrigVO.asString("AD_CODGRUPOPROD"));
                if (itemVO.containsProperty(("AD_CODITELIC"))) itemVO.setProperty("AD_CODITELIC", itemOrigVO.asString("AD_CODITELIC"));
                if (itemVO.containsProperty(("AD_CODLIC"))) itemVO.setProperty("AD_CODLIC", itemOrigVO.asString("AD_CODLIC"));
                if (itemVO.containsProperty(("AD_CODLICCOM"))) itemVO.setProperty("AD_CODLICCOM", itemOrigVO.asString("AD_CODLICCOM"));
                if (itemVO.containsProperty(("AD_CUSFORN"))) itemVO.setProperty("AD_CUSFORN", itemOrigVO.asString("AD_CUSFORN"));
                if (itemVO.containsProperty(("AD_CUSTO_NOVO"))) itemVO.setProperty("AD_CUSTO_NOVO", itemOrigVO.asString("AD_CUSTO_NOVO"));
                if (itemVO.containsProperty(("AD_ITEM"))) itemVO.setProperty("AD_ITEM", itemOrigVO.asString("AD_ITEM"));
                if (itemVO.containsProperty(("AD_LOTEGRUPO"))) itemVO.setProperty("AD_LOTEGRUPO", itemOrigVO.asString("AD_LOTEGRUPO"));
                if (itemVO.containsProperty(("AD_MARK_FAT"))) itemVO.setProperty("AD_MARK_FAT", itemOrigVO.asString("AD_MARK_FAT"));
                if (itemVO.containsProperty(("AD_SUBITEM"))) itemVO.setProperty("AD_SUBITEM", itemOrigVO.asString("AD_SUBITEM"));

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JdbcWrapper.closeSession(jdbc);
            JapeSession.close(hnd);
        }


    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
