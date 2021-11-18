package inicio;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import processamento.Acessorios;
import processamento.insertItens;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class atualizarAcessorios implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent arg0) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent arg0) throws Exception {
        if (arg0.getModifingFields().isModifing("CUSTO") || arg0.getModifingFields().isModifing("MARKUPFATOR")) {
            Acessorios.atualizarCustoAcessorio(arg0);
        }

    }

    @Override
    public void beforeDelete(PersistenceEvent arg0) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent arg0) throws Exception {
        insertItens.atualizarCusto(arg0);
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbc = dwfFacade.getJdbcWrapper();
        jdbc.openSession();

        DynamicVO acessorio = (DynamicVO) arg0.getVo();
        BigDecimal codLic = acessorio.asBigDecimalOrZero("CODLIC");
        BigDecimal codLicCom = acessorio.asBigDecimalOrZero("CODLICCOM");
        BigDecimal codProd = acessorio.asBigDecimalOrZero("CODPROD");
        BigDecimal qtdNeg = acessorio.asBigDecimalOrZero("QTDNEG");
        //BigDecimal custo = acessorio.asBigDecimalOrZero("CUSTO");
        BigDecimal vlrUnit = acessorio.asBigDecimalOrZero("VLRUNIT");
        BigDecimal vlrTot = vlrUnit.multiply(qtdNeg);
        String codVol = acessorio.asString("CODVOL");
        BigDecimal markupFator = acessorio.asBigDecimalOrZero("MARKUPFATOR");

        //throw new Exception(codLic.toString() + " Esse é o codlic");
        if(!(markupFator.doubleValue()>0)) markupFator = BigDecimal.ONE;
        if(!(qtdNeg.doubleValue()>0)) qtdNeg = BigDecimal.ONE;

        String sql = "select CODEMP, NUNOTA, CODLIC from AD_LICITACAO where CODLIC="+codLic;
        PreparedStatement consultaLic = jdbc.getPreparedStatement(sql);
        ResultSet licitacao = consultaLic.executeQuery();

        BigDecimal nuNota = null;
        BigDecimal codEmp = null;
        while (licitacao.next()) {
            nuNota = licitacao.getBigDecimal("NUNOTA");
            codEmp = licitacao.getBigDecimal("CODEMP");
        }
        Acessorios.salvarAcessoriosDados(nuNota,codProd,qtdNeg, codVol, vlrUnit, vlrTot, codEmp, codLicCom,codLic);

        jdbc.closeSession();

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
