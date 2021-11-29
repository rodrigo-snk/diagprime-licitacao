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
    		BigDecimal numContrato,
    		String empenho) throws Exception {
    	
		//SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		//Date parsedDate = dateFormat.parse(dataEntrega);
	   // Timestamp timestamp1 = new java.sql.Timestamp(parsedDate.getTime());
	    
        DynamicVO cabVO = (DynamicVO) dwf.getDefaultValueObjectInstance("CabecalhoNota");
    	cabVO.setProperty("CODEMP", codEmp);
        cabVO.setProperty("CODPARC", codParc);
        cabVO.setProperty("CODTIPOPER", codTipOper);
        cabVO.setProperty("CODTIPVENDA", codTipVenda);
        cabVO.setProperty("CODNAT", codNat);
        cabVO.setProperty("CODCENCUS", codCencus);
        cabVO.setProperty("CODPROJ", codProj);
        cabVO.setProperty("VLRNOTA", vlrNota);
        cabVO.setProperty("AD_EMPENHO", empenho);
        cabVO.setProperty("NUMCONTRATO", numContrato);
        cabVO.setProperty("CIF_FOB", "S");
        cabVO.setProperty("NUMNOTA", BigDecimal.ZERO);
        dwf.createEntity("CabecalhoNota", (EntityVO)cabVO);
        return cabVO.asBigDecimal("NUNOTA");
    	
    }

    public static void salvarItensDados(
    		EntityFacade dwf,
    		BigDecimal codEmp,
    		BigDecimal nuNota,
    		BigDecimal codProd,
    		BigDecimal qtdNeg,
    		String codVol, 
    		BigDecimal vlrUnit,
    		BigDecimal vlrTot) throws Exception {

    	 DynamicVO itemVO = (DynamicVO) dwf.getDefaultValueObjectInstance("ItemNota");
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
         dwf.createEntity("AD_PERIODOANO", (EntityVO) itemVO);
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
    		BigDecimal codProd,
            String codVol,
    		BigDecimal numContrato,
    		BigDecimal codParc,
    		BigDecimal qtdDisponivel,
    		BigDecimal qtdLiberar,
    		String empenho,
    		BigDecimal adDisponivel) throws Exception {

    	 DynamicVO itemVO = (DynamicVO)dwf.getDefaultValueObjectInstance("AD_ITENSEMPENHO");
         itemVO.setProperty("CODPROD", codProd);
         itemVO.setProperty("CODVOL", codVol);
         itemVO.setProperty("NUMCONTRATO", numContrato);
         // itemVO.setProperty("CODPARC", codParc);
         itemVO.setProperty("QTDDISPONIVEL", qtdDisponivel);
         itemVO.setProperty("QTDLIBERAR", BigDecimal.ZERO);
         itemVO.setProperty("EMPENHO", empenho);
         itemVO.setProperty("AD_DISPONIVEL", adDisponivel);
         dwf.createEntity("AD_ITENSEMPENHO", (EntityVO) itemVO);
         
    }
    
    public static void gerarEmpenhoConverter(
    		EntityFacade dwf,
    		BigDecimal codProd,
    		BigDecimal numContrato,
    		BigDecimal qtdDisponivel,
    		BigDecimal qtdLiberada,
    		String empenho) throws Exception {

    	 DynamicVO itemVO = (DynamicVO)dwf.getDefaultValueObjectInstance("AD_CONVERTEREMPENHO");
         itemVO.setProperty("CODPROD", codProd);
         itemVO.setProperty("NUMCONTRATO", numContrato);
         itemVO.setProperty("QTDDISPONIVEL", qtdDisponivel);
         itemVO.setProperty("QTDLIBERAR", BigDecimal.ZERO);
         itemVO.setProperty("EMPENHO", empenho);
         itemVO.setProperty("QTDLIBERADA", qtdLiberada);
         dwf.createEntity("AD_CONVERTEREMPENHO", (EntityVO) itemVO);
    }

}
