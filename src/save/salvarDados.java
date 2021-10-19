package save;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;


import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;

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
         //if(tipo.equalsIgnoreCase("F")) {
         //itemVO.setProperty("STATUSNOTA", "L");
         //}
         itemVO.setProperty("ATUALESTOQUE", new BigDecimal(0));
         //itemVO.setProperty("CODLOCALORIG", codLocal);
         dwf.createEntity("ItemNota", (EntityVO)itemVO);
         
    }
    
    public static void insertComponentes(BigDecimal codLic,JdbcWrapper jdbcWrapper) throws Exception {
    	
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
    			+ "    			    group by CODLIC,CODMATPRIMA,CODVOL,MARKUPFATOR,vlrunit,CUSTOMATERIAPRIMA   order by CODMATPRIMA asc)a)";

    	String deleteSql = "delete from AD_LICITACAOCOMPONENTES where codlic="+codLic;
    	
    	PreparedStatement  deleteSql1 = jdbcWrapper.getPreparedStatement(deleteSql);
    	deleteSql1.executeUpdate();
  		
    	PreparedStatement  updateValidando = jdbcWrapper.getPreparedStatement(insertSql);
  		updateValidando.executeUpdate();
    }



    public static void salvarItensDados(
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
         
    }

	public static void salvarAcessoriosDados(
			EntityFacade dwf,
			BigDecimal codLic,
			BigDecimal codProd,
			BigDecimal qtdNeg,
			String codVol,
			BigDecimal vlrUnit,
			BigDecimal custo) throws Exception {

		DynamicVO itemVO = (DynamicVO)dwf.getDefaultValueObjectInstance("AD_LICITACAOCOMPONENTES");
		itemVO.setProperty("CODLIC", codLic);
		itemVO.setProperty("CODPROD", codProd);
		itemVO.setProperty("QTDNEG", qtdNeg);
		itemVO.setProperty("CUSTO", custo);
		itemVO.setProperty("CODVOL", codVol);
		itemVO.setProperty("VLRUNIT", vlrUnit);
		dwf.createEntity("ItemNota", (EntityVO)itemVO);

	}
    
    public static void salvarDadosCabecalho(
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
         
    }

}