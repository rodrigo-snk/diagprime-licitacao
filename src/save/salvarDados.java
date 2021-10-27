package save;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;


import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
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
    		String dtNeg,
    		BigDecimal codLic) throws Exception {
    	
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date parsedDate = dateFormat.parse(dtNeg);
	    Timestamp timestamp1 = new Timestamp(parsedDate.getTime());
	    
		DynamicVO faturaVO = (DynamicVO)dwf.getDefaultValueObjectInstance("CabecalhoNota");
    	faturaVO.setProperty("CODEMP", codEmp);
        faturaVO.setProperty("CODPARC", codParc);
        faturaVO.setProperty("CODTIPOPER", codTipOper);
        faturaVO.setProperty("CODTIPVENDA", codTipVenda);
        faturaVO.setProperty("CODNAT", codNat);
        faturaVO.setProperty("CODCENCUS", codCencus);
        faturaVO.setProperty("VLRNOTA", vlrNota);
        faturaVO.setProperty("CIF_FOB", "S");
        faturaVO.setProperty("NUMNOTA", BigDecimal.ZERO);
        faturaVO.setProperty("DTNEG", timestamp1);
        faturaVO.setProperty("AD_CODLIC", codLic);
        
        //if(tipo.equalsIgnoreCase("F")) {
        //faturaVO.setProperty("STATUSNOTA", "L");
       // }
        dwf.createEntity("CabecalhoNota", (EntityVO)faturaVO);
    	
    	return faturaVO.asBigDecimal("NUNOTA");
    }

	/**
	 * Adiciona os itens no ItemNota (TGFITE)
	 * @param dwf dwfFacade
	 * @param nuNota Nro.Único da Nota
	 * @param codProd Cód. Produto
	 * @param qtdNeg Quantidade
	 * @param codVol Unidade de volume
	 * @param vlrUnit Vlr. Unitário
	 * @param vlrTot Vlr. Total
	 * @param codEmp Empresa
	 * @param codIteLic Cód. Item Licitação
	 * @param codLic Cód. Licitação
	 * @throws Exception
	 */
    public static void salvarItensDados(
    		EntityFacade dwf,
    		BigDecimal nuNota,
    		BigDecimal codProd,
    		BigDecimal qtdNeg,
    		String codVol, 
    		BigDecimal vlrUnit,
    		BigDecimal vlrTot,
    		BigDecimal codEmp,
    		BigDecimal codIteLic,
    		BigDecimal codLic) throws Exception {

    	 DynamicVO itemVO = (DynamicVO)dwf.getDefaultValueObjectInstance("ItemNota");
         itemVO.setProperty("NUNOTA", nuNota);
         itemVO.setProperty("CODPROD", codProd);
         itemVO.setProperty("CODEMP", codEmp);
         itemVO.setProperty("QTDNEG", qtdNeg);
         itemVO.setProperty("CODVOL", codVol);
         itemVO.setProperty("VLRUNIT", vlrUnit);
         itemVO.setProperty("VLRTOT", vlrTot);
         itemVO.setProperty("AD_CODITELIC", codIteLic);
         itemVO.setProperty("AD_CODLIC", codLic);
         itemVO.setProperty("USOPROD", "V");
         itemVO.setProperty("RESERVA", "N");
		 itemVO.setProperty("ATUALESTOQUE", BigDecimal.ZERO);
		//if(tipo.equalsIgnoreCase("F")) {
         //itemVO.setProperty("STATUSNOTA", "L");
         //}
         //itemVO.setProperty("CODLOCALORIG", codLocal);
         dwf.createEntity("ItemNota", (EntityVO)itemVO);
         
    }
    
    public static void insertComponentes(BigDecimal codLic,JdbcWrapper jdbc) throws Exception {

    	String insertSql = "insert into AD_LICITACAOCOMPONENTES(CODLICCOM,CODLIC,CODPROD,QTDNEG,CODVOL,CUSTO,MARKUPFATOR,VLRUNIT)\r\n"
    			+ "  (select rownum,CODLIC,CODMATPRIMA,QTDNEG,CODVOL,CUSTOMATERIAPRIMA,MARKUPFATOR,VLRUNIT from (\r\n"
    			+ "select CODLIC,CODMATPRIMA,SUM(QTDNEG) as QTDNEG,CODVOL,(CUSTOMATERIAPRIMA) as CUSTOMATERIAPRIMA,MARKUPFATOR as MARKUPFATOR, VLRUNIT as VLRUNIT\r\n"
    			+ "    			FROM (select "+codLic+" as CODLIC,CODMATPRIMA,(QTDMISTURA*QTDE) AS QTDNEG,CODVOL,(CUSTO.CUSGER) AS CUSTOMATERIAPRIMA,1.10 AS MARKUPFATOR,\r\n"
    			+ "    			    (((CUSTO.CUSGER*1.10))) AS VLRUNIT\r\n"
    			+ "    			 from TGFICP INNER JOIN \r\n"
    			+ "    			 (SELECT coalesce(CUSGER,0) as CUSGER,TGFCUS.CODPROD\r\n"
    			+ "    			    FROM TGFCUS INNER JOIN \r\n"
    			+ "    			    (select MAX(DTATUAL) AS VALOR,CODPROD from TGFCUS GROUP BY CODPROD)CUS\r\n"
    			+ "    			    ON CUS.VALOR = DTATUAL AND CUS.CODPROD = TGFCUS.CODPROD)CUSTO ON CUSTO.CODPROD = TGFICP.CODMATPRIMA INNER JOIN \r\n"
    			+ "    			    AD_ITENSLICITACAO ON AD_ITENSLICITACAO.CODPROD = TGFICP.CODPROD\r\n"
    			+ "    			    WHERE CODLIC="+codLic+"\r\n"
    			+ "    			  )A\r\n"
    			+ "    			    group by CODLIC,CODMATPRIMA,CODVOL,MARKUPFATOR,vlrunit,CUSTOMATERIAPRIMA order by CODMATPRIMA asc)a)";

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

		while (componente.next()) {
			BigDecimal codLicCom = componente.getBigDecimal("CODLICCOM");
			BigDecimal nuNota = componente.getBigDecimal("NUNOTA");
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