package eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import helpper.CabecalhoNota;

public class AtualizaAdicionaisCab implements EventoProgramavelJava {

    private JdbcWrapper jdbc = null;

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

        JapeSession.SessionHandle hnd = null;

        try {

            hnd = JapeSession.open();

            EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
            jdbc = dwfEntityFacade.getJdbcWrapper();

            CabecalhoNota.preencheAdicionaisCabOrig((DynamicVO) persistenceEvent.getVo());

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
