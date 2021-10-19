package save;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import consultas.contratosCons;

public class contratos {

	
    public static BigDecimal salvarContrato(
    		EntityFacade dwf,
    		String CODEMP,
    		String CODPARC,
    		String CODCONTATO,
    		String CODTIPVENDA,
    		String CODNAT,
    		String CODTIPOPER,
    		String nuNota) throws Exception {

    	Date parsedDate = new Date();
	    Timestamp DTCONTRATO = new Timestamp(parsedDate.getTime());
	    Timestamp DTBASEREAJ = new Timestamp(parsedDate.getTime());
	    JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();
	    String resumo = "";
	    String sqlConsulta = contratosCons.buscarDadosResumo(nuNota);
	    PreparedStatement  updateValidando = jdbcWrapper.getPreparedStatement(sqlConsulta);
  		ResultSet consultaValidando = updateValidando.executeQuery();

  	    while(consultaValidando.next()) {
  	    	
  	    	resumo = consultaValidando.getString("NUMERO");
  	    	
  	    }
	    
		BigDecimal CODUSU = (BigDecimal) JapeSessionContext.getRequiredProperty("usuario_logado");
		
    	DynamicVO gerarContratos = (DynamicVO) dwf.getDefaultValueObjectInstance("Contrato");
    	gerarContratos.setProperty("DTCONTRATO", DTCONTRATO);
    	gerarContratos.setProperty("CODEMP", new BigDecimal(CODEMP));
    	gerarContratos.setProperty("CODPARC", new BigDecimal(CODPARC));
    	gerarContratos.setProperty("CODCONTATO", new BigDecimal(CODCONTATO));
    	gerarContratos.setProperty("CODUSU", CODUSU);
    	gerarContratos.setProperty("CODNAT", new BigDecimal(CODNAT));
    	gerarContratos.setProperty("CODTIPVENDA", new BigDecimal(CODTIPVENDA));
    	gerarContratos.setProperty("AD_CODTIPOPER", new BigDecimal(CODTIPOPER));
    	gerarContratos.setProperty("ATIVO", "S");
    	gerarContratos.setProperty("AD_NUMEROLICITACAO", resumo);
    	gerarContratos.setProperty("DTBASEREAJ", DTBASEREAJ);
		dwf.createEntity("Contrato", (EntityVO) gerarContratos);
		return gerarContratos.asBigDecimal("NUMCONTRATO");
    	
    }
    
    
    
    
    public static void salvarContratoItens(
    		EntityFacade dwf,
    		String NUMCONTRATO,
    		String CODPROD,
    		String QTDEPREVISTA,
    		String VLRUNIT) throws Exception {


    	//Date parsedDate = new Date();
	    //Timestamp DTALTER = new Timestamp(parsedDate.getTime());
	    
	   /* if(true) {
			throw new PersistenceException("CODPROD = "+CODPROD+" - qtdNeg = "+QTDEPREVISTA+" - vlrUnit - "+VLRUNIT+" - NumContrato = "+NUMCONTRATO+" dtneg"+DTALTER);
		}*/
	    
	    
    	DynamicVO gerarItensContratos = (DynamicVO) dwf.getDefaultValueObjectInstance("ProdutoServicoContrato");
    	gerarItensContratos.setProperty("NUMCONTRATO", new BigDecimal(NUMCONTRATO));
    	gerarItensContratos.setProperty("CODPROD", new BigDecimal(CODPROD));
    	gerarItensContratos.setProperty("SITPROD", "A");
    	gerarItensContratos.setProperty("IMPRNOTA", "S");
    	gerarItensContratos.setProperty("LIMITANTE", "N");
    	gerarItensContratos.setProperty("QTDEPREVISTA", new BigDecimal(QTDEPREVISTA));
    	gerarItensContratos.setProperty("AD_QTDLIBERAR", new BigDecimal(QTDEPREVISTA));
    	gerarItensContratos.setProperty("VLRUNIT", new BigDecimal(VLRUNIT));
    //	gerarItensContratos.setProperty("DTALTER", DTALTER);
		dwf.createEntity("ProdutoServicoContrato", (EntityVO) gerarItensContratos);
		
    }
	
}
