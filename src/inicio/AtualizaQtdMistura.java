package inicio;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;

// Está sendo utilizado o evento em script de banco (STP_EVT_CAB_QTDMISTURA)
public class AtualizaQtdMistura implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

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

        if (persistenceEvent.getModifingFields().isModifing("AD_QTDACESSORIO")) {
            DynamicVO composicaoVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.ITEM_COMPOSICAO_PRODUTO, persistenceEvent.getEntityProperty("CODPROD"));

            composicaoVO.setProperty("QTDMISTURA", composicaoVO.asBigDecimalOrZero("AD_QTDACESSORIO"));


            atualizaQtdMistura(composicaoVO, composicaoVO.asBigDecimalOrZero("AD_QTDACESSORIO"));
        }

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }

    public static void atualizaQtdMistura(DynamicVO itemComposicaoVO, BigDecimal qtdMistura) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();

            JapeFactory.dao(DynamicEntityNames.ITEM_COMPOSICAO_PRODUTO).
                    prepareToUpdateByPK(itemComposicaoVO.asBigDecimal("CODPROD"), itemComposicaoVO.asBigDecimal("VARIACAO"), itemComposicaoVO.asBigDecimal("CODLOCAL"), itemComposicaoVO.asString("CONTROLE"),itemComposicaoVO.asBigDecimal("CODETAPA"), itemComposicaoVO.asBigDecimal("CODMATPRIMA"), itemComposicaoVO.asBigDecimal("CODLOCALMP"), itemComposicaoVO.asString("CONTROLEMP"))
                    .set("QTDMISTURA", qtdMistura)
                    .update();

        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }
}
