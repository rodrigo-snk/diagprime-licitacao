package save;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;


import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.mgecomercial.model.facades.helpper.ItemNotaHelpper;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.dwfdata.vo.CabecalhoNotaVO;
import br.com.sankhya.modelcore.dwfdata.vo.ItemNotaVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import processamento.Acessorios;

public class salvarDados {

    public static BigDecimal salvarCabecalhoDados(
    		EntityFacade dwf,
    		BigDecimal codEmp,
    		BigDecimal codParc,
    		BigDecimal codTipOper,
    		BigDecimal codTipVenda,
    		BigDecimal codNat, 
    		BigDecimal codCencus,
    		BigDecimal vlrNota,
    		Timestamp dtNeg,
    		BigDecimal codLic) throws Exception {

	    
		DynamicVO cabVO = (DynamicVO)dwf.getDefaultValueObjectInstance("CabecalhoNota");
    	cabVO.setProperty("CODEMP", codEmp);
        cabVO.setProperty("CODPARC", codParc);
        cabVO.setProperty("CODTIPOPER", codTipOper);
        cabVO.setProperty("CODTIPVENDA", codTipVenda);
        cabVO.setProperty("CODNAT", codNat);
        cabVO.setProperty("CODCENCUS", codCencus);
        cabVO.setProperty("VLRNOTA", vlrNota);
        cabVO.setProperty("CIF_FOB", "S");
        cabVO.setProperty("NUMNOTA", BigDecimal.ZERO);
        cabVO.setProperty("DTNEG", dtNeg);
        cabVO.setProperty("AD_CODLIC", codLic);
        
        //if(tipo.equalsIgnoreCase("F")) {
        //cabVO.setProperty("STATUSNOTA", "L");
       // }
        dwf.createEntity("CabecalhoNota", (EntityVO)cabVO);
    	
    	return cabVO.asBigDecimal("NUNOTA");
    }

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
    public static void salvarItensDados(
    		EntityFacade dwfFacade,
    		BigDecimal nuNota,
    		BigDecimal codProd,
    		BigDecimal qtdNeg,
    		String codVol, 
    		BigDecimal vlrUnit,
    		BigDecimal vlrTot,
    		BigDecimal codEmp,
    		BigDecimal codIteLic,
    		BigDecimal codLic) throws Exception {

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
		itemVO.setProperty("AD_CODITELIC", codIteLic);
		itemVO.setProperty("AD_CODLIC", codLic);

			//if(tipo.equalsIgnoreCase("F")) {
         //itemVO.setProperty("STATUSNOTA", "L");
         //}
         //itemVO.setProperty("CODLOCALORIG", codLocal);

		Collection<ItemNotaVO> itens = new ArrayList<>();
		itens.add(itemVO);

		//dwfFacade.createEntity("ItemNota", (EntityVO) itemVO);
		ItemNotaHelpper.saveItensNota(itens, cabVO);

		/*EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = dwf.getJdbcWrapper();
		jdbc.openSession();

		String update1 = "UPDATE TGFITE SET QTDNEG="+qtdNeg+",VLRTOT="+vlrTot+",VLRUNIT="+vlrUnit+" "
				+ "where AD_CODITELIC="+codIteLic+" and AD_CODLIC="+codLic;
		PreparedStatement  updateValidand1 = jdbc.getPreparedStatement(update1);
		updateValidand1.executeUpdate();
		jdbc.closeSession();*/


	}
    
    public static void insertComponentes(BigDecimal codLic,JdbcWrapper jdbc) throws Exception {

    	String insertSql = "insert into AD_LICITACAOCOMPONENTES(CODLICCOM,CODLIC,CODPROD,QTDNEG,CODVOL,CUSTO,MARKUPFATOR,VLRUNIT)\r\n"
    			+ "  (select rownum,CODLIC,CODMATPRIMA,QTDNEG,CODVOL,CUSTOMATERIAPRIMA,MARKUPFATOR,VLRUNIT from\n" +
				"(select CODLIC,CODMATPRIMA,SUM(QTDNEG) as QTDNEG,CODVOL,(CUSTOMATERIAPRIMA) as CUSTOMATERIAPRIMA,MARKUPFATOR as MARKUPFATOR, VLRUNIT as VLRUNIT\n" +
				"FROM (select "+codLic+" as CODLIC,CODMATPRIMA,case when voa.dividemultiplica = 'M' THEN (qtde*qtdmistura)*voa.quantidade*voa.multipvlr ELSE (qtde*qtdmistura)/(voa.quantidade*voa.multipvlr) END QTDNEG,TGFICP.CODVOL,(CUSTO.CUSGER) AS CUSTOMATERIAPRIMA,1.10 AS MARKUPFATOR,\n" +
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

		// Execute DELETE on AD_LICITACAOCOMPONENTES
    	PreparedStatement deleteComponentes = jdbc.getPreparedStatement("DELETE FROM AD_LICITACAOCOMPONENTES WHERE CODLIC = "+codLic);
		deleteComponentes.executeUpdate();
		// Execute DELETE on TGFITE
		PreparedStatement deleteItemComponentes = jdbc.getPreparedStatement("DELETE FROM TGFITE WHERE AD_CODLIC = "+codLic+" AND AD_CODLICCOM IS NOT NULL");
		deleteItemComponentes.executeUpdate();
		// Execute INSERT on AD_LICITACAOCOMPONENTES
    	PreparedStatement insert = jdbc.getPreparedStatement(insertSql);
  		insert.executeUpdate();

		final String sql = "select LIC.NUNOTA, LIC.CODEMP, COMP.* from AD_LICITACAOCOMPONENTES COMP INNER JOIN AD_LICITACAO LIC ON COMP.CODLIC = LIC.CODLIC where LIC.CODLIC="+codLic;
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
			BigDecimal vlrUnit = componente.getBigDecimal("VLRUNIT");
			BigDecimal vlrTot = vlrUnit.multiply(qtdNeg);
			BigDecimal markupFator = componente.getBigDecimal("MARKUPFATOR");
			if(!(markupFator.doubleValue()>0)) markupFator = BigDecimal.ONE;
			if(!(qtdNeg.doubleValue()>0)) qtdNeg = BigDecimal.ONE;

			Acessorios.salvarAcessoriosDados(nuNota,codProd,qtdNeg,codVol,vlrUnit,vlrTot,codEmp,codLicCom,codLic);

			
		}

		ImpostosHelpper impostos = new ImpostosHelpper();
		impostos.setForcarRecalculo(true);
		impostos.calcularImpostos(nuNota);


		jdbc.closeSession();
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