package helpper;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.mgecomercial.model.facades.helpper.ItemNotaHelpper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.dwfdata.vo.CabecalhoNotaVO;
import br.com.sankhya.modelcore.dwfdata.vo.ItemNotaVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;

import static helpper.Licitacao.excluiReferencias;

public class ItemNota {

    /**
     * Adiciona os itens no ItemNota (TGFITE)
     * @param dwfFacade dwfFacade
     * @param nuNota Nro.Único da Nota
     * @param codProd Cód. Produto
     * @param qtdNeg Quantidade
     * @param codVol Unidade de volume
     * @param vlrUnit Vlr. Unitário
     * @param vlrTot Vlr. Total
     * @param codEmp Empresa
     * @param codIteLic Cód. Item Licitação
     * @param codLic Cód. Licitação
     * @throws Exception Exceção
     */
    public static void salvaItemNota(
            EntityFacade dwfFacade,
            BigDecimal nuNota,
            BigDecimal codProd,
            BigDecimal qtdNeg,
            String codVol,
            BigDecimal vlrUnit,
            BigDecimal vlrTot,
            BigDecimal codEmp,
            BigDecimal codIteLic,
            BigDecimal codLic,
            String loteGrupo,
            String observacaoLote) throws Exception {

        ItemNotaVO itemVO = (ItemNotaVO) dwfFacade.getDefaultValueObjectInstance("ItemNota", ItemNotaVO.class);
        CabecalhoNotaVO cabVO = (CabecalhoNotaVO) dwfFacade.findEntityByPrimaryKeyAsVO("CabecalhoNota",nuNota,CabecalhoNotaVO.class);

        itemVO.setNUNOTA(nuNota);
        itemVO.setCODPROD(codProd);
        itemVO.setCODEMP(codEmp);
        itemVO.setQTDNEG(qtdNeg);
        itemVO.setCODVOL(codVol);
        itemVO.setVLRUNIT(vlrUnit);
        itemVO.setVLRTOT(vlrTot);
        itemVO.setUSOPROD("R");
        itemVO.setRESERVA("N");
        itemVO.setATUALESTOQUE(BigDecimal.ZERO);
        if (itemVO.containsProperty(("AD_CODITELIC"))) itemVO.setProperty("AD_CODITELIC", codIteLic);
        if (itemVO.containsProperty(("AD_CODLIC"))) itemVO.setProperty("AD_CODLIC", codLic);
        if (itemVO.containsProperty(("AD_ACESSORIOS"))) itemVO.setProperty("AD_ACESSORIOS","N");
        if (itemVO.containsProperty(("AD_LOTEGRUPO"))) itemVO.setProperty("AD_LOTEGRUPO",loteGrupo);
        if (itemVO.containsProperty(("AD_OBSLOTE"))) itemVO.setProperty("AD_OBSLOTE",observacaoLote);

        Collection<ItemNotaVO> itens = new ArrayList<>();
        itens.add(itemVO);

        ItemNotaHelpper.saveItensNota(itens, cabVO);

    }

    public static void insereAcessorios(BigDecimal codLic, JdbcWrapper jdbc) throws Exception {

        String insertSql = "insert into AD_LICITACAOCOMPONENTES(CODLICCOM,CODLIC,CODPROD,QTDNEG,CODVOL,CUSTO,MARKUPFATOR,VLRUNIT)\r\n"
                + "(select rownum,CODLIC,CODMATPRIMA,QTDNEG,CODVOL,CUSTOMATERIAPRIMA,MARKUPFATOR,VLRUNIT from\n" +
                "(select CODLIC,CODMATPRIMA,SUM(QTDNEG) as QTDNEG,CODVOL,(CUSTOMATERIAPRIMA) as CUSTOMATERIAPRIMA,MARKUPFATOR as MARKUPFATOR, VLRUNIT as VLRUNIT\n" +
                "FROM (select "+codLic+" as CODLIC,CODMATPRIMA,case when voa.dividemultiplica = 'M' THEN (qtde*qtdmistura)*voa.quantidade*voa.multipvlr WHEN voa.dividemultiplica = 'D' THEN (qtde*qtdmistura)/(voa.quantidade*voa.multipvlr) ELSE (qtde*qtdmistura) END QTDNEG,TGFICP.CODVOL,(CUSTO.CUSGER) AS CUSTOMATERIAPRIMA,1.10 AS MARKUPFATOR,\n" +
                "(CUSTO.CUSGER*1.10) AS VLRUNIT\n" +
                "from TGFICP INNER JOIN\n" +
                "(SELECT coalesce(CUSGER,0) as CUSGER,TGFCUS.CODPROD\n" +
                "FROM TGFCUS INNER JOIN\n" +
                "(select MAX(DTATUAL) AS VALOR,CODPROD from TGFCUS GROUP BY CODPROD)CUS\n" +
                "ON CUS.VALOR = DTATUAL AND CUS.CODPROD = TGFCUS.CODPROD)CUSTO ON CUSTO.CODPROD = TGFICP.CODMATPRIMA INNER JOIN\n" +
                "AD_ITENSLICITACAO ON AD_ITENSLICITACAO.CODPROD = TGFICP.CODPROD\n" +
                "inner join tgfvoa voa on voa.codprod = AD_ITENSLICITACAO.codprod and voa.codvol = AD_ITENSLICITACAO.unid \n" +
                "WHERE CODLIC=" +codLic+
                ")A\n" +
                "group by CODLIC,CODMATPRIMA,CODVOL,MARKUPFATOR,vlrunit,CUSTOMATERIAPRIMA order by CODMATPRIMA asc)a)";

        String selectSQL =  "select 1\n" +
                "FROM TGFICP ICP\n" +
                "JOIN AD_ITENSLICITACAO ITE ON ITE.CODPROD = ICP.CODPROD\n" +
                "WHERE ITE.CODLIC = "+codLic;

        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();

        // Execute DELETE on AD_LICITACAOCOMPONENTES
        dwf.removeByCriteria(new FinderWrapper("AD_LICITACAOCOMPONENTES", "this.CODLIC = ?", codLic));
        //PreparedStatement deleteComponentes = jdbc.getPreparedStatement("DELETE FROM AD_LICITACAOCOMPONENTES WHERE CODLIC = "+codLic);
        //deleteComponentes.executeUpdate();

        // Deleta as referencias da pré-proposta
        DynamicVO licitacaoVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO("AD_LICITACAO", codLic);
        excluiReferencias(jdbc, licitacaoVO.asBigDecimalOrZero("NUNOTA"));

        // Execute DELETE on TGFITE
        dwf.removeByCriteria(new FinderWrapper(DynamicEntityNames.ITEM_NOTA, "this.AD_CODLIC = ? AND AD_CODLICCOM IS NOT NULL", codLic));
        //PreparedStatement deleteItemComponentes = jdbc.getPreparedStatement("DELETE FROM TGFITE WHERE AD_CODLIC = "+codLic+" AND AD_CODLICCOM IS NOT NULL");
        //deleteItemComponentes.executeUpdate();

        PreparedStatement selectComponentes = jdbc.getPreparedStatement(selectSQL);
        ResultSet rs = selectComponentes.executeQuery();

        if (rs.next()) {
            // Execute INSERT on AD_LICITACAOCOMPONENTES
            PreparedStatement insert = jdbc.getPreparedStatement(insertSql);
            insert.executeUpdate();
        }

        final String sql = "select LIC.NUNOTA, LIC.CODEMP, LIC.LOTEGRUPO, COMP.* from AD_LICITACAOCOMPONENTES COMP INNER JOIN AD_LICITACAO LIC ON COMP.CODLIC = LIC.CODLIC where LIC.CODLIC="+codLic;
        PreparedStatement consultaLic = jdbc.getPreparedStatement(sql);
        ResultSet componente = consultaLic.executeQuery();
        BigDecimal nuNota = null;

        while (componente.next()) {

            BigDecimal codLicCom = componente.getBigDecimal("CODLICCOM");
            nuNota = componente.getBigDecimal("NUNOTA");
            BigDecimal codEmp = componente.getBigDecimal("CODEMP");
            BigDecimal codProd = componente.getBigDecimal("CODPROD");
            BigDecimal qtdNeg = componente.getBigDecimal("QTDNEG");
            String codVol = componente.getString("CODVOL");
            String loteGrupo = componente.getString("LOTEGRUPO");
            BigDecimal vlrUnit = componente.getBigDecimal("VLRUNIT");
            BigDecimal vlrTot = vlrUnit.multiply(qtdNeg);
            BigDecimal markupFator = componente.getBigDecimal("MARKUPFATOR");
            if(!(markupFator.doubleValue()>0)) markupFator = BigDecimal.ONE;
            if(!(qtdNeg.doubleValue()>0)) qtdNeg = BigDecimal.ONE;

            Acessorios.insereAcessorios(nuNota,codProd,qtdNeg,codVol,vlrUnit,vlrTot,codEmp,codLicCom,codLic,loteGrupo, licitacaoVO.asString("OBSERVACAOLOTE"));

        }

        if (nuNota != null) {
            ImpostosHelpper impostos = new ImpostosHelpper();
            impostos.setForcarRecalculo(true);
            impostos.calcularImpostos(nuNota);
        }

        jdbc.closeSession();
    }

    public static DynamicVO getOne(String where, Object[] param) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeWrapper itemDAO = JapeFactory.dao(DynamicEntityNames.ITEM_NOTA);
            return itemDAO.findOne(where, param);
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
        return null;
    }

    /* public static void salvarItensDados(
    		EntityFacade dwf,
    		BigDecimal codLic,
    		BigDecimal codProd,
    		BigDecimal qtdNeg,
    		String codVol,
    		BigDecimal vlrUnit,
    		BigDecimal custo) throws Exception {

    	 DynamicVO itemVO = (DynamicVO)dwf.getDefaultValueObjectInstance("AD_ITENSLICITACAO");
         itemVO.setProperty("CODLIC", codLic);
         itemVO.setProperty("CODPROD", codProd);
         itemVO.setProperty("QTDE", qtdNeg);
         itemVO.setProperty("CUSTO", custo);
         itemVO.setProperty("CODVOL", codVol);
         itemVO.setProperty("VLRUNIT", vlrUnit);
         dwf.createEntity("ItemNota", (EntityVO)itemVO);

    }*/
}
