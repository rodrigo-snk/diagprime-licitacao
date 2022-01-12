package processamento;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.PersistenceException;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ProdutoUtils;
import com.sankhya.util.StringUtils;
import consultas.consultasDados;
import save.salvarDados;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

    public static void atualizaCustoVolume(PersistenceEvent arg0) throws Exception {

        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
        jdbcWrapper.openSession();

        DynamicVO itemVO = (DynamicVO) arg0.getVo();
        BigDecimal codLic = itemVO.asBigDecimalOrZero("CODLIC");
        BigDecimal codIteLic = itemVO.asBigDecimalOrZero("CODITELIC");
        BigDecimal codProd = itemVO.asBigDecimalOrZero("CODPROD");
        BigDecimal qtdNeg = itemVO.asBigDecimalOrZero("QTDE");
        BigDecimal vlrUnit = itemVO.asBigDecimalOrZero("VLRUNIT");
        BigDecimal markupFator = itemVO.asBigDecimalOrZero("MARKUPFATOR");
        String codVol = itemVO.asString("UNID");

        if (markupFator.compareTo(BigDecimal.ZERO) <= 0) {
            markupFator = BigDecimal.ONE;
        }
        if (qtdNeg.compareTo(BigDecimal.ZERO) <= 0) {
            qtdNeg = BigDecimal.ONE;
        }

        final String consultaDados = "SELECT coalesce(CUSGER,0) as CUSGER FROM TGFCUS WHERE CODPROD = "+codProd+" AND DTATUAL IN (\r\n"
                + "select MAX(DTATUAL) AS VALOR from TGFCUS WHERE CODPROD = "+codProd+")";

        PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(consultaDados);
        ResultSet rs = pstmt.executeQuery();
        BigDecimal custo = BigDecimal.ZERO;
        while(rs.next()){
            custo = rs.getBigDecimal("CUSGER");
        }

        ItensLicitacao.atualizaItemLic(codLic,codIteLic,custo.multiply(markupFator),custo.multiply(markupFator).multiply(qtdNeg),markupFator,custo);

        final String sqlunidade = "SELECT DIVIDEMULTIPLICA,MULTIPVLR,QUANTIDADE FROM TGFVOA WHERE CODPROD = "+codProd+" and CODVOL = '"+codVol+"'";
        pstmt = jdbcWrapper.getPreparedStatement(sqlunidade);
        rs = pstmt.executeQuery();
        if (rs.next()){
            final String divideOuMultiplica = rs.getString("DIVIDEMULTIPLICA");
            BigDecimal quantidade = rs.getBigDecimal("QUANTIDADE").multiply(rs.getBigDecimal("MULTIPVLR"));

            if (divideOuMultiplica.equalsIgnoreCase("M")) {
                custo = custo.multiply(quantidade);
                qtdNeg = qtdNeg.multiply(quantidade);
                vlrUnit = custo.multiply(markupFator).divide(quantidade, MathContext.DECIMAL128);
            }
            else if (divideOuMultiplica.equalsIgnoreCase("D")) {
                custo = custo.divide(quantidade, MathContext.DECIMAL128);
                qtdNeg = qtdNeg.divide(quantidade, MathContext.DECIMAL128);
                vlrUnit = custo.multiply(markupFator).multiply(quantidade);
            }
        }

        //Valores convertidos
        //vlrUnit = custo.multiply(markupFator);
        BigDecimal vlrTot = vlrUnit.multiply(qtdNeg);

        ItensLicitacao.atualizaItemLic(codLic, codIteLic, custo.multiply(markupFator), vlrTot, markupFator, custo);

        final String updateIte = "UPDATE TGFITE SET QTDNEG="+qtdNeg+",VLRTOT="+vlrTot+",VLRUNIT="+vlrUnit+", CODVOL= '"+codVol+"' where AD_CODITELIC="+codIteLic+" and AD_CODLIC="+codLic;
        pstmt = jdbcWrapper.getPreparedStatement(updateIte);
        pstmt.executeUpdate();

        jdbcWrapper.closeSession();
    }

    public static void atualizaCusto(PersistenceEvent arg0) throws Exception {

        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
        jdbcWrapper.openSession();

        DynamicVO itemVO = (DynamicVO) arg0.getVo();
        BigDecimal codLic = itemVO.asBigDecimalOrZero("CODLIC");
        BigDecimal codIteLic = itemVO.asBigDecimalOrZero("CODITELIC");
        BigDecimal codProd = itemVO.asBigDecimalOrZero("CODPROD");
        BigDecimal qtdNeg = itemVO.asBigDecimalOrZero("QTDE");
        BigDecimal vlrTot = itemVO.asBigDecimalOrZero("VLRTOTAL");
        BigDecimal vlrUnit = itemVO.asBigDecimalOrZero("VLRUNIT");
        BigDecimal custo = itemVO.asBigDecimalOrZero("CUSTO");
        BigDecimal markupFator = itemVO.asBigDecimalOrZero("MARKUPFATOR");
        String codVol = itemVO.asString("UNID");

        if (markupFator.compareTo(BigDecimal.ZERO) <= 0) {
            markupFator = BigDecimal.ONE;
        }
        if (qtdNeg.compareTo(BigDecimal.ZERO) <= 0) {
            qtdNeg = BigDecimal.ONE;
        }

        if (custo.compareTo(BigDecimal.ZERO) <= 0) {
            custo = BigDecimal.ONE;
        }

        //PreparedStatement pstmt = jdbcWrapper.getPreparedStatement("UPDATE AD_ITENSLICITACAO SET CUSTO="+custo+",VLRTOTAL="+vlrTot+",VLRUNIT="+vlrUnit+" where CODITELIC="+codIteLic+"  and CODLIC="+codLic);
        //pstmt.executeUpdate();
        ItensLicitacao.atualizaItemLic(codLic,codIteLic,custo.multiply(markupFator),custo.multiply(markupFator).multiply(qtdNeg),markupFator,custo);

        final String sqlunidade = "SELECT DIVIDEMULTIPLICA,MULTIPVLR,QUANTIDADE FROM TGFVOA WHERE CODPROD = "+codProd+" and CODVOL = '"+codVol+"'";
        PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(sqlunidade);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()){
            final String divideOuMultiplica = rs.getString("DIVIDEMULTIPLICA");
            BigDecimal quantidade = rs.getBigDecimal("QUANTIDADE").multiply(rs.getBigDecimal("MULTIPVLR"));

            if (divideOuMultiplica.equalsIgnoreCase("M")) {
                //custo = custo.multiply(quantidade);
                qtdNeg = qtdNeg.multiply(quantidade);
                vlrUnit = custo.multiply(markupFator).divide(quantidade, MathContext.DECIMAL128);

            }
            else if (divideOuMultiplica.equalsIgnoreCase("D")) {
                //custo = custo.divide(quantidade, MathContext.DECIMAL128);
                qtdNeg = qtdNeg.divide(quantidade, MathContext.DECIMAL128);
                vlrUnit = custo.multiply(markupFator).multiply(quantidade);

            }
        }
        vlrTot = vlrUnit.multiply(qtdNeg);

        final String updateIte = "UPDATE TGFITE SET QTDNEG="+qtdNeg+",VLRTOT="+vlrTot+",VLRUNIT="+vlrUnit+", CODVOL= '"+codVol+"' where AD_CODITELIC="+codIteLic+" and AD_CODLIC="+codLic;
        pstmt = jdbcWrapper.getPreparedStatement(updateIte);
        pstmt.executeUpdate();

        jdbcWrapper.closeSession();
    }

    public static void atualizaQtdNeg(PersistenceEvent arg0) throws Exception {

        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
        jdbcWrapper.openSession();

        DynamicVO itemVO = (DynamicVO) arg0.getVo();
        BigDecimal codLic = itemVO.asBigDecimalOrZero("CODLIC");
        BigDecimal codIteLic = itemVO.asBigDecimalOrZero("CODITELIC");
        BigDecimal codProd = itemVO.asBigDecimalOrZero("CODPROD");
        BigDecimal qtdNeg = itemVO.asBigDecimalOrZero("QTDE");
        BigDecimal vlrTot = itemVO.asBigDecimalOrZero("VLRTOTAL");
        BigDecimal vlrUnit = itemVO.asBigDecimalOrZero("VLRUNIT");
        BigDecimal custo = itemVO.asBigDecimalOrZero("CUSTO");
        BigDecimal markupFator = itemVO.asBigDecimalOrZero("MARKUPFATOR");
        String codVol = itemVO.asString("UNID");

        if (markupFator.compareTo(BigDecimal.ZERO) <= 0) {
            markupFator = BigDecimal.ONE;
        }
        if (qtdNeg.compareTo(BigDecimal.ZERO) <= 0) {
            qtdNeg = BigDecimal.ONE;
        }

        if (custo.compareTo(BigDecimal.ZERO) <= 0) {
            custo = BigDecimal.ONE;
        }

        ItensLicitacao.atualizaItemLic(codLic,codIteLic,custo.multiply(markupFator),custo.multiply(markupFator).multiply(qtdNeg),markupFator,custo);

        final String sqlunidade = "SELECT DIVIDEMULTIPLICA,MULTIPVLR,QUANTIDADE FROM TGFVOA WHERE CODPROD = "+codProd+" and CODVOL = '"+codVol+"'";
        PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(sqlunidade);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()){
            final String divideOuMultiplica = rs.getString("DIVIDEMULTIPLICA");
            BigDecimal quantidade = rs.getBigDecimal("QUANTIDADE").multiply(rs.getBigDecimal("MULTIPVLR"));

            if (divideOuMultiplica.equalsIgnoreCase("M")) {
                qtdNeg = qtdNeg.multiply(quantidade);
                vlrUnit = custo.multiply(markupFator).divide(quantidade, MathContext.DECIMAL128);

            }
            else if (divideOuMultiplica.equalsIgnoreCase("D")) {
                qtdNeg = qtdNeg.divide(quantidade, MathContext.DECIMAL128);
                vlrUnit = custo.multiply(markupFator).multiply(quantidade);
            }
        }

        vlrTot = vlrUnit.multiply(qtdNeg);

        //PreparedStatement pstmt = jdbcWrapper.getPreparedStatement("UPDATE AD_ITENSLICITACAO SET CUSTO="+custo+",VLRTOTAL="+vlrTot+",VLRUNIT="+vlrUnit+" where CODITELIC="+codIteLic+"  and CODLIC="+codLic);
        //pstmt.executeUpdate();

        final String updateIte = "UPDATE TGFITE SET QTDNEG="+qtdNeg+",VLRTOT="+vlrTot+",VLRUNIT="+vlrUnit+", CODVOL= '"+codVol+"' where AD_CODITELIC="+codIteLic+" and AD_CODLIC="+codLic;
        pstmt = jdbcWrapper.getPreparedStatement(updateIte);
        pstmt.executeUpdate();


        jdbcWrapper.closeSession();
    }

    public static void atualizaVlrUnit(PersistenceEvent arg0) throws Exception {
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
        jdbcWrapper.openSession();

        DynamicVO itemVO = (DynamicVO) arg0.getVo();
        BigDecimal codLic = itemVO.asBigDecimalOrZero("CODLIC");
        BigDecimal codIteLic = itemVO.asBigDecimalOrZero("CODITELIC");
        BigDecimal codProd = itemVO.asBigDecimalOrZero("CODPROD");
        BigDecimal qtdNeg = itemVO.asBigDecimalOrZero("QTDE");
        BigDecimal custo = itemVO.asBigDecimalOrZero("CUSTO");
        BigDecimal vlrUnit = itemVO.asBigDecimalOrZero("VLRUNIT");
        BigDecimal markupFator = itemVO.asBigDecimalOrZero("MARKUPFATOR");
        String codVol = itemVO.asString("UNID");

        if (markupFator.compareTo(BigDecimal.ZERO) <= 0) {
            markupFator = BigDecimal.ONE;
        }
        if (qtdNeg.compareTo(BigDecimal.ZERO) <= 0) {
            qtdNeg = BigDecimal.ONE;
        }

        markupFator = vlrUnit.divide(custo, MathContext.DECIMAL128);
        BigDecimal vlrTot = vlrUnit.multiply(qtdNeg);

        //ItensLicitacao.atualizaItemLicitacao(codLic,codIteLic,vlrUnit,vlrTot,markupFator,custo);
        ItensLicitacao.atualizaItemLic(codLic,codIteLic,vlrUnit,vlrTot,markupFator,custo);

        final String sqlunidade = "SELECT DIVIDEMULTIPLICA,MULTIPVLR,QUANTIDADE FROM TGFVOA WHERE CODPROD = "+codProd+" and CODVOL = '"+codVol+"'";
        PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(sqlunidade);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()){
            final String divideOuMultiplica = rs.getString("DIVIDEMULTIPLICA");
            BigDecimal quantidade = rs.getBigDecimal("QUANTIDADE").multiply(rs.getBigDecimal("MULTIPVLR"));

            if (divideOuMultiplica.equalsIgnoreCase("M")) {
                qtdNeg = qtdNeg.multiply(quantidade);
                vlrUnit = custo.multiply(markupFator).divide(quantidade, MathContext.DECIMAL128);
            }
            else if (divideOuMultiplica.equalsIgnoreCase("D")) {
                qtdNeg = qtdNeg.divide(quantidade, MathContext.DECIMAL128);
                vlrUnit = custo.multiply(markupFator).multiply(quantidade);
            }
        }

        //Valores convertidos
        //vlrUnit = custo.multiply(markupFator);
        vlrTot = vlrUnit.multiply(qtdNeg);

        // Atualiza item convertido na TGFITE
        final String updateIte = "UPDATE TGFITE SET QTDNEG="+qtdNeg+",VLRTOT="+vlrTot+",VLRUNIT="+vlrUnit+" where AD_CODITELIC="+codIteLic+" and AD_CODLIC="+codLic;
        pstmt = jdbcWrapper.getPreparedStatement(updateIte);
        pstmt.executeUpdate();

        //salvarDados.insertComponentes(codLic, jdbcWrapper);

        jdbcWrapper.closeSession();
    }

    //Chamado na RecalculaCustoItem
    public static void recalculaCusto(
            BigDecimal codIteLic,
            BigDecimal codLic,
            BigDecimal codProd,
            BigDecimal qtdNeg,
            BigDecimal vlrTot,
            BigDecimal vlrUnit,
            BigDecimal markupFator,
            String codVol) throws Exception {


        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
        jdbcWrapper.openSession();

        if (markupFator.compareTo(BigDecimal.ZERO) <= 0) {
            markupFator = BigDecimal.ONE;
        }
        if (qtdNeg.compareTo(BigDecimal.ZERO) <= 0) {
            qtdNeg = BigDecimal.ONE;
        }

        BigDecimal custo = ComercialUtils.obtemPrecoCusto("L", " ", BigDecimal.ONE, BigDecimal.ZERO,codProd);


        if(!(custo.intValue()>0)) {
            throw new PersistenceException("Custo do produto obrigatório, não encontrado ou está zerado o custo, no cadastro de custo.");
        }
        vlrUnit = custo.multiply(markupFator);
        vlrTot = vlrUnit.multiply(qtdNeg);

        String update = "UPDATE AD_ITENSLICITACAO SET MARKUPFATOR="+markupFator+",CUSTO="+custo+",VLRTOTAL="+vlrTot+",QTDE="+qtdNeg+",VLRUNIT="+vlrUnit+" "
                + "where CODITELIC="+codIteLic;
        PreparedStatement  updateValidando = jdbcWrapper.getPreparedStatement(update);
        updateValidando.executeUpdate();

        String consultaCabecalho = "select codemp,nunota,codlic from ad_licitacao  where codlic="+codLic;
        PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(consultaCabecalho);
        ResultSet rs = pstmt.executeQuery();

        while(rs.next()){

            BigDecimal nuNota = rs.getBigDecimal("NUNOTA");
            BigDecimal codEmp = rs.getBigDecimal("CODEMP");

            //ERRO adiciona um novo item na TGFITE
			/*salvarDados.salvarItensDados(
  				dwf,
  				nuNota,
  				codProd,
  				qtdNeg,
  				codVol,
  				vlrUnit,
  				vlrTot,
  				codEmp,
  				codIteLic,
  				codLic);*/

            ImpostosHelpper impostos = new ImpostosHelpper();
            impostos.setForcarRecalculo(true);
            impostos.calcularImpostos(nuNota);
        }

        salvarDados.insereAcessorios(codLic, jdbcWrapper);
        jdbcWrapper.closeSession();
    }

    public static void insereItem(PersistenceEvent arg0) throws Exception {

        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
        jdbcWrapper.openSession();

        DynamicVO itemVO = (DynamicVO) arg0.getVo();
        BigDecimal codIteLic = itemVO.asBigDecimalOrZero("CODITELIC");
        BigDecimal codLic = itemVO.asBigDecimalOrZero("CODLIC");
        BigDecimal codProd = itemVO.asBigDecimalOrZero("CODPROD");
        BigDecimal qtdNeg = itemVO.asBigDecimalOrZero("QTDE");
        BigDecimal vlrTot = itemVO.asBigDecimalOrZero("VLRTOTAL");
        BigDecimal vlrUnit = itemVO.asBigDecimalOrZero("VLRUNIT");
        BigDecimal item = itemVO.asBigDecimalOrZero("ITEM");
        BigDecimal markupFator = itemVO.asBigDecimalOrZero("MARKUPFATOR");
        String codVol = itemVO.asString("UNID");
        String loteGrupo = StringUtils.getNullAsEmpty(itemVO.asString("LOTEGRUPO"));

        BigDecimal replicando = itemVO.asBigDecimalOrZero("REPLICANDO");

        if (markupFator.compareTo(BigDecimal.ZERO) <= 0) {
            markupFator = BigDecimal.ONE;
        }

        if (qtdNeg.compareTo(BigDecimal.ZERO) <= 0) {
            qtdNeg = BigDecimal.ONE;
        }

        String consultaProd = consultasDados.retornaDadosItensProdutos(codProd.toString());
        PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(consultaProd);
        ResultSet rs = pstmt.executeQuery();
        boolean isServico = false;
        while(rs.next()) isServico = rs.getString("USOPROD").equals("S");

        BigDecimal custo = ComercialUtils.obtemPrecoCusto("L", " ", BigDecimal.ONE, BigDecimal.ZERO, codProd);

        if(!(custo.intValue() > 0) && !isServico) {
            throw new PersistenceException("Custo do produto obrigatório, não encontrado ou está zerado o custo, no cadastro de custo.");
        }

        //VERIFICAR NECESSIDADE
        vlrUnit = custo.multiply(markupFator);
        vlrTot = vlrUnit.multiply(qtdNeg);

        String update;
        String update1;
        if(!((replicando.intValue())>0)) {
            update = "UPDATE AD_ITENSLICITACAO SET ITEM=(select COALESCE(max(item),0)+1 item from AD_ITENSLICITACAO WHERE CODLIC="+codLic+"),MARKUPFATOR="+markupFator+",CUSTO="+custo+",VLRTOTAL="+vlrTot+",QTDE="+qtdNeg+",VLRUNIT="+vlrUnit+" "
                    + " where CODITELIC="+codIteLic+" and CODLIC="+codLic;

            update1 = "UPDATE TGFITE SET VLRTOT="+vlrTot+",QTDNEG="+qtdNeg+",VLRUNIT="+vlrUnit+" "
                    + " where AD_CODITELIC="+codIteLic+" and AD_CODLIC="+codLic;

            pstmt = jdbcWrapper.getPreparedStatement(update1);
            pstmt.executeUpdate();

        } else {
            //Quando esta replicando entra aqui
            update = "UPDATE AD_ITENSLICITACAO SET REPLICANDO = 0 WHERE CODITELIC = "+codIteLic+" AND CODLIC = "+codLic;
        }

        pstmt = jdbcWrapper.getPreparedStatement(update);
        pstmt.executeUpdate();

        ComercialUtils.MontantesVolumeAlternativo volumeAlternativo = ComercialUtils.calcularVolumeAlternativo(codProd, codVol, " ", qtdNeg, vlrUnit);
        //if (true) throw new MGEModelException("QTDNEG: " +qtdNeg+ " QTDNEG P/ MET: " +volumeAlternativo.getQtdVolAlternativo()+ "VLRUNIT: " +vlrUnit+ "VLRUNIT P/ MET: " +volumeAlternativo.getVlrVolAlternativo());

        DynamicVO licitacaoVO = (DynamicVO) dwf.findEntityByPrimaryKeyAsVO("AD_LICITACAO", codLic);
        BigDecimal nuNota = licitacaoVO.asBigDecimal("NUNOTA");
        BigDecimal codEmp = licitacaoVO.asBigDecimal("CODEMP");

        pstmt = jdbcWrapper.getPreparedStatement("SELECT DIVIDEMULTIPLICA,MULTIPVLR, QUANTIDADE FROM TGFVOA WHERE CODPROD = "+codProd+" and CODVOL = '"+codVol+"'");
        rs = pstmt.executeQuery();

        if(rs.next()){
            final String divideOuMultiplica = rs.getString("DIVIDEMULTIPLICA");
            BigDecimal quantidade = rs.getBigDecimal("QUANTIDADE").multiply(rs.getBigDecimal("MULTIPVLR"));

            if (divideOuMultiplica.equalsIgnoreCase("M")) {
                custo = custo.multiply(quantidade);
                qtdNeg = qtdNeg.multiply(quantidade);
                vlrUnit = custo.multiply(markupFator).divide(quantidade, MathContext.DECIMAL128);
            }
            else if (divideOuMultiplica.equalsIgnoreCase("D")) {
                custo = custo.divide(quantidade, MathContext.DECIMAL128);
                qtdNeg = qtdNeg.divide(quantidade, MathContext.DECIMAL128);
                vlrUnit = custo.multiply(markupFator).multiply(quantidade);
            }
        }

        //vlrUnit = custo.multiply(markupFator);
        vlrTot = vlrUnit.multiply(qtdNeg);

        ItensLicitacao.atualizaItemLic(codLic, codIteLic, custo.multiply(markupFator), vlrTot, markupFator, custo);
        //pstmt.executeUpdate("UPDATE AD_ITENSLICITACAO SET CUSTO="+custo+", VLRUNIT = "+custo.multiply(markupFator)+", VLRTOTAL = "+vlrTot.multiply(qtdNeg.multiply(volumeAlternativo.getQtdVolAlternativo()))+" where CODITELIC="+codIteLic+" and CODLIC="+codLic);

        salvarDados.salvaItemNota(dwf,nuNota,codProd,qtdNeg,codVol,vlrUnit,vlrTot,codEmp,codIteLic,codLic, loteGrupo);

        ImpostosHelpper impostos = new ImpostosHelpper();
        impostos.setForcarRecalculo(true);
        impostos.calcularImpostos(nuNota);



        if (!dwf.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.ITEM_COMPOSICAO_PRODUTO, "this.CODPROD = ?", codProd)).isEmpty()) {
             salvarDados.insereAcessorios(codLic, jdbcWrapper);
         }


        jdbcWrapper.closeSession();
    }

    //DELETE ITENS
    public static void atualizaTotal(PersistenceEvent arg0) throws Exception {

        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
        jdbcWrapper.openSession();

        DynamicVO dados = (DynamicVO) arg0.getVo();
        BigDecimal codLic = dados.asBigDecimalOrZero("CODLIC");
        BigDecimal codIteLic = dados.asBigDecimalOrZero("CODITELIC");
        BigDecimal qtdNeg = dados.asBigDecimalOrZero("QTDE");
        BigDecimal vlrTot = dados.asBigDecimalOrZero("VLRTOTAL");
        BigDecimal vlrUnit = dados.asBigDecimalOrZero("VLRUNIT");
        BigDecimal codProd = dados.asBigDecimalOrZero("CODPROD");

        DynamicVO licitacaoVO = (DynamicVO) dwf.findEntityByPrimaryKeyAsVO("AD_LICITACAO", codLic);
        BigDecimal nuNota = licitacaoVO.asBigDecimal("NUNOTA");
        BigDecimal codEmp = licitacaoVO.asBigDecimal("CODEMP");


        Licitacao.excluiReferencias(jdbcWrapper, nuNota);
        String update = "DELETE FROM TGFITE WHERE NUNOTA="+nuNota+" AND AD_CODITELIC="+codIteLic+" and CODPROD="+codProd;
        PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(update);
        pstmt.executeUpdate();

        Impostos.recalculaImpostos(codLic);

        jdbcWrapper.closeSession();
    }

}
