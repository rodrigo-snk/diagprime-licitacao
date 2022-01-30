package helpper;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.mgecomercial.model.facades.helpper.ItemNotaHelpper;
import br.com.sankhya.modelcore.dwfdata.vo.CabecalhoNotaVO;
import br.com.sankhya.modelcore.dwfdata.vo.ItemNotaVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;


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

            Licitacao.excluiReferencias(jdbcWrapper, nuNota);
            String update = "DELETE FROM TGFITE WHERE NUNOTA="+nuNota+" AND AD_CODLICCOM="+codLicCom+" and CODPROD="+codProd;
            PreparedStatement  updateValidando = jdbcWrapper.getPreparedStatement(update);
            updateValidando.executeUpdate();

            Impostos.recalculaImpostos(codLic);

        }

        jdbcWrapper.closeSession();
    }

    /**
     * Adiciona os acessórios na ItemNota (TGFITE)
     * @param nuNota Nro. Único Nota
     * @param codProd Cód. Produto
     * @param qtdNeg Quantidade
     * @param codVol Unidade de Volume
     * @param vlrUnit Vlr. Unitário
     * @param vlrTot Vlr. Total
     * @param codEmp Empresa
     * @param codLicCom Cód. Componente Licitação
     * @param codLic Cód. Licitação
     * @throws Exception
     */
    public static void insereAcessorios(
            BigDecimal nuNota,
            BigDecimal codProd,
            BigDecimal qtdNeg,
            String codVol,
            BigDecimal vlrUnit,
            BigDecimal vlrTot,
            BigDecimal codEmp,
            BigDecimal codLicCom,
            BigDecimal codLic,
            String loteGrupo) throws Exception {

        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbc = dwfFacade.getJdbcWrapper();
        jdbc.openSession();

        //Conversão de unidades
        PreparedStatement pstmt = jdbc.getPreparedStatement("SELECT DIVIDEMULTIPLICA,MULTIPVLR,QUANTIDADE FROM TGFVOA WHERE CODPROD = "+codProd+" and CODVOL = '"+codVol+"'");
        ResultSet rs = pstmt.executeQuery();

        if(rs.next()){

            String divideOuMultiplica = rs.getString("DIVIDEMULTIPLICA");
            BigDecimal quantidade = rs.getBigDecimal("QUANTIDADE").multiply(rs.getBigDecimal("MULTIPVLR"));

            if (divideOuMultiplica.equalsIgnoreCase("M")) {
                qtdNeg = qtdNeg.multiply(quantidade);
                vlrUnit = vlrUnit.divide(quantidade, MathContext.DECIMAL128);
            }
            else if (divideOuMultiplica.equalsIgnoreCase("D")) {
                qtdNeg = qtdNeg.divide(quantidade, MathContext.DECIMAL128);
                vlrUnit = vlrUnit.multiply(quantidade);
            }
        }

        ItemNotaVO itemVO = (ItemNotaVO) dwfFacade.getDefaultValueObjectInstance("ItemNota", ItemNotaVO.class);
        CabecalhoNotaVO cabVO = (CabecalhoNotaVO) dwfFacade.findEntityByPrimaryKeyAsVO("CabecalhoNota",nuNota,CabecalhoNotaVO.class);

        itemVO.setNUNOTA(nuNota);
        itemVO.setCODPROD(codProd);
        itemVO.setCODEMP(codEmp);
        itemVO.setQTDNEG(qtdNeg);
        itemVO.setCODVOL(codVol);
        itemVO.setVLRUNIT(vlrUnit);
        itemVO.setVLRTOT(vlrTot);
        itemVO.setUSOPROD("D");
        itemVO.setRESERVA("N");
        itemVO.setATUALESTOQUE(BigDecimal.ZERO);
        itemVO.setProperty("AD_CODLICCOM", codLicCom);
        itemVO.setProperty("AD_CODLIC", codLic);
        itemVO.setProperty("AD_ACESSORIOS", "S");
        itemVO.setProperty("AD_LOTEGRUPO", loteGrupo);

        Collection<ItemNotaVO> itens = new ArrayList<>();
        itens.add(itemVO);

        //dwfFacade.createEntity("ItemNota", (EntityVO) itemVO);
        ItemNotaHelpper.saveItensNota(itens, cabVO);

        /*String update1 = "UPDATE TGFITE SET VLRUNIT="+vlrUnit+" "
                + "where AD_CODLICCOM="+codLicCom+" and AD_CODLIC="+codLic;
        PreparedStatement  updateValidand1 = jdbc.getPreparedStatement(update1);
        updateValidand1.executeUpdate();
        jdbc.closeSession();*/

        jdbc.closeSession();


    }

    public static void atualizarCustoAcessorio(PersistenceEvent arg0) throws Exception {

            EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
            JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
            jdbcWrapper.openSession();

            DynamicVO acessorioVO = (DynamicVO) arg0.getVo();
            BigDecimal codLic = acessorioVO.asBigDecimalOrZero("CODLIC");
            BigDecimal codLicCom = acessorioVO.asBigDecimalOrZero("CODLICCOM");
            BigDecimal custo = acessorioVO.asBigDecimalOrZero("CUSTO");
            BigDecimal vlrUnit = acessorioVO.asBigDecimalOrZero("VLRUNIT");
            BigDecimal markUpFator = acessorioVO.asBigDecimalOrZero("MARKUPFATOR");
             if (!arg0.getModifingFields().isModifing("MARKUPFATOR") && !arg0.getModifingFields().isModifing("CUSTO") ) {
                markUpFator = vlrUnit.divide(custo, MathContext.DECIMAL128);
            }
            if (!arg0.getModifingFields().isModifing("VLRUNIT")) {
                vlrUnit = custo.multiply(markUpFator);
            } else {
                vlrUnit = acessorioVO.asBigDecimalOrZero("VLRUNIT");
            }
            BigDecimal vlrTot = vlrUnit.multiply(acessorioVO.asBigDecimalOrZero("QTDNEG"));

            //dwf.saveEntity("AD_LICITACAOCOMPONENTES", (EntityVO) acessorioVO);

            final String sql = "UPDATE AD_LICITACAOCOMPONENTES SET VLRUNIT = "+vlrUnit+", MARKUPFATOR = "+markUpFator+", CUSTO = "+custo+" where CODLIC="+codLic+" AND CODLICCOM =" +codLicCom;
            PreparedStatement updateAcessorio = jdbcWrapper.getPreparedStatement(sql);
            updateAcessorio.executeUpdate();
            final String sql2 = "UPDATE TGFITE SET VLRUNIT = "+vlrUnit+", VLRTOT = "+vlrTot+" where AD_CODLIC="+codLic+" AND AD_CODLICCOM =" +codLicCom;
            updateAcessorio = jdbcWrapper.getPreparedStatement(sql2);
            updateAcessorio.executeUpdate();

            jdbcWrapper.closeSession();

    }
}
