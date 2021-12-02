package inicio;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import processamento.Acessorios;
import processamento.Impostos;
import processamento.ItensLicitacao;

import java.math.BigDecimal;

public class AtualizaAcessorios implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent arg0) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent arg0) throws Exception {

        if (arg0.getModifingFields().isModifing("CUSTO") || arg0.getModifingFields().isModifing("MARKUPFATOR") || arg0.getModifingFields().isModifing("VLRUNIT")) {
            Acessorios.atualizarCustoAcessorio(arg0);
        }

        Impostos.recalculaImpostos((BigDecimal) arg0.getEntityProperty("CODLIC"));

    }

    @Override
    public void beforeDelete(PersistenceEvent arg0) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent arg0) throws Exception {
        ItensLicitacao.insereItem(arg0);

        DynamicVO acessorioVO = (DynamicVO) arg0.getVo();
        BigDecimal codLic = acessorioVO.asBigDecimalOrZero("CODLIC");
        BigDecimal codLicCom = acessorioVO.asBigDecimalOrZero("CODLICCOM");
        BigDecimal codProd = acessorioVO.asBigDecimalOrZero("CODPROD");
        BigDecimal qtdNeg = acessorioVO.asBigDecimalOrZero("QTDNEG");
        //BigDecimal custo = acessorioVO.asBigDecimalOrZero("CUSTO");
        BigDecimal vlrUnit = acessorioVO.asBigDecimalOrZero("VLRUNIT");
        BigDecimal vlrTot = vlrUnit.multiply(qtdNeg);
        String codVol = acessorioVO.asString("CODVOL");
        BigDecimal markupFator = acessorioVO.asBigDecimalOrZero("MARKUPFATOR");

        if (markupFator.compareTo(BigDecimal.ZERO) <= 0) {
            markupFator = BigDecimal.ONE;
        }
        if (qtdNeg.compareTo(BigDecimal.ZERO) <= 0) {
            qtdNeg = BigDecimal.ONE;
        }

        DynamicVO licitacaoVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKey("AD_LICITACAO", codLic);
        BigDecimal nuNota = licitacaoVO.asBigDecimalOrZero("NUNOTA");
        BigDecimal codEmp = licitacaoVO.asBigDecimalOrZero("CODEMP");

        Acessorios.insereAcessorios(nuNota,codProd,qtdNeg, codVol, vlrUnit, vlrTot, codEmp, codLicCom,codLic);

    }

    @Override
    public void afterUpdate(PersistenceEvent arg0) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent arg0) throws Exception {
        Acessorios.deleteFromItemNota(arg0);
    }

    @Override
    public void beforeCommit(TransactionContext arg0) throws Exception {

    }


}
