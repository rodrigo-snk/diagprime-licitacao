package save;

import java.math.BigDecimal;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;


public class salvarDadosEmpenho {
	
    
    public static BigDecimal salvarCabecalhoDados(
    		EntityFacade dwf,
    		ContextoAcao arg0,
    		BigDecimal codEmp,
    		BigDecimal codParc,
    		BigDecimal codTipOper,
    		BigDecimal codTipVenda,
    		BigDecimal codNat, 
    		BigDecimal codCencus,
    		BigDecimal codProj,
    		BigDecimal vlrNota,
    		String numContrato,
    		String empenho) throws Exception {
    	
		//SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		//Date parsedDate = dateFormat.parse(dataEntrega);
	   // Timestamp timestamp1 = new java.sql.Timestamp(parsedDate.getTime());
	    
    	 DynamicVO faturaVO = (DynamicVO)dwf.getDefaultValueObjectInstance("CabecalhoNota");
    	faturaVO.setProperty("CODEMP", codEmp);
        faturaVO.setProperty("CODPARC", codParc);
        faturaVO.setProperty("CODTIPOPER", codTipOper);
        faturaVO.setProperty("CODTIPVENDA", codTipVenda);
        faturaVO.setProperty("CODNAT", codNat);
        faturaVO.setProperty("CODCENCUS", codCencus);
        faturaVO.setProperty("CODPROJ", codProj);
        faturaVO.setProperty("VLRNOTA", vlrNota);
        faturaVO.setProperty("AD_EMPENHO", empenho);
        faturaVO.setProperty("NUMCONTRATO", new BigDecimal(numContrato));
        faturaVO.setProperty("CIF_FOB", "S");
        faturaVO.setProperty("NUMNOTA", BigDecimal.ZERO);
        dwf.createEntity("CabecalhoNota", (EntityVO)faturaVO);
        return faturaVO.asBigDecimal("NUNOTA");
    	
    }
    
    
    public static void salvarItensDados(
    		EntityFacade dwf,
    		ContextoAcao arg0,
    		BigDecimal codEmp,
    		BigDecimal nuNota,
    		BigDecimal codProd,
    		BigDecimal qtdNeg,
    		String codVol, 
    		BigDecimal vlrUnit,
    		BigDecimal vlrTot) throws Exception {
    	
    	
    	 DynamicVO itemVO = (DynamicVO)dwf.getDefaultValueObjectInstance("ItemNota");
         itemVO.setProperty("NUNOTA", nuNota);
         itemVO.setProperty("CODEMP", codEmp);
         itemVO.setProperty("CODPROD", codProd);
         itemVO.setProperty("QTDNEG", qtdNeg);
         itemVO.setProperty("CODVOL", codVol);
         itemVO.setProperty("VLRUNIT", vlrUnit);
         //itemVO.setProperty("CONTROLE", controle);
         itemVO.setProperty("VLRTOT", vlrTot);
        // itemVO.setProperty("NUTAB", nuTab);
         itemVO.setProperty("USOPROD", "P");
         itemVO.setProperty("RESERVA", "N");
         itemVO.setProperty("ATUALESTOQUE", new BigDecimal(0));
        //itemVO.setProperty("CODLOCALORIG", codLocal);
         dwf.createEntity("ItemNota", (EntityVO)itemVO);
         
    }
    
    
    public static void salvarDadosPeriodos(
    		EntityFacade dwf,
    		String mes,
    		String referencia,
    		String ano,
    		String dia,
    		String diaSemana) throws Exception {
    	
    	
    	 DynamicVO itemVO = (DynamicVO)dwf.getDefaultValueObjectInstance("AD_PERIODOANO");
         itemVO.setProperty("MES", mes);
         itemVO.setProperty("REFERENCIA", referencia);
         itemVO.setProperty("ANO", ano);
         itemVO.setProperty("DIA_SEMANA", diaSemana);
         itemVO.setProperty("DIA", dia);
         dwf.createEntity("AD_PERIODOANO", (EntityVO)itemVO);
    }
    
    
    /*public static void gerarEmpenho(
    		EntityFacade dwf,
    		BigDecimal CODPROD,
    		BigDecimal NUMCONTRATO,
    		BigDecimal CODPARC,
    		BigDecimal QTDDISPONIVEL, 
    		BigDecimal QTDLIBERAR, 
    		String EMPENHO,
    		BigDecimal AD_DISPONIVEL) throws Exception {
    	
    	
    	 DynamicVO itemVO = (DynamicVO)dwf.getDefaultValueObjectInstance("AD_EMPENHO");
         itemVO.setProperty("CODPROD", CODPROD);
         itemVO.setProperty("NUMCONTRATO", NUMCONTRATO);
         itemVO.setProperty("CODPARC", CODPARC);
         itemVO.setProperty("QTDDISPONIVEL", QTDDISPONIVEL);
         itemVO.setProperty("QTDLIBERAR", QTDLIBERAR);
         itemVO.setProperty("EMPENHO", EMPENHO);
         itemVO.setProperty("AD_DISPONIVEL", AD_DISPONIVEL);
         dwf.createEntity("AD_EMPENHO", (EntityVO)itemVO);
         
    }*/
    
    
    public static void gerarEmpenho(
    		EntityFacade dwf,
    		BigDecimal CODPROD,
    		BigDecimal NUMCONTRATO,
    		BigDecimal CODPARC,
    		BigDecimal QTDDISPONIVEL, 
    		BigDecimal QTDLIBERAR, 
    		String EMPENHO,
    		BigDecimal AD_DISPONIVEL) throws Exception {
    	
    	
    	 DynamicVO itemVO = (DynamicVO)dwf.getDefaultValueObjectInstance("AD_ITENSEMPENHO");
         itemVO.setProperty("CODPROD", CODPROD);
         itemVO.setProperty("NUMCONTRATO", NUMCONTRATO);
       //  itemVO.setProperty("CODPARC", CODPARC);
         itemVO.setProperty("QTDDISPONIVEL", QTDDISPONIVEL);
         itemVO.setProperty("QTDLIBERAR", BigDecimal.ZERO);
         itemVO.setProperty("EMPENHO", EMPENHO);
         itemVO.setProperty("AD_DISPONIVEL", AD_DISPONIVEL);
         dwf.createEntity("AD_ITENSEMPENHO", (EntityVO)itemVO);
         
    }
    
    public static void gerarEmpenhoConverter(
    		EntityFacade dwf,
    		BigDecimal CODPROD,
    		BigDecimal NUMCONTRATO,
    		BigDecimal QTDDISPONIVEL, 
    		BigDecimal QTDLIBERADA, 
    		String EMPENHO) throws Exception {
    	
    	
    	 DynamicVO itemVO = (DynamicVO)dwf.getDefaultValueObjectInstance("AD_CONVERTEREMPENHO");
         itemVO.setProperty("CODPROD", CODPROD);
         itemVO.setProperty("NUMCONTRATO", NUMCONTRATO);
         itemVO.setProperty("QTDDISPONIVEL", QTDDISPONIVEL);
         itemVO.setProperty("QTDLIBERAR", BigDecimal.ZERO);
         itemVO.setProperty("EMPENHO", EMPENHO);
         itemVO.setProperty("QTDLIBERADA", QTDLIBERADA);
         dwf.createEntity("AD_CONVERTEREMPENHO", (EntityVO)itemVO);
         
    }

}
