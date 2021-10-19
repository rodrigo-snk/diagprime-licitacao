package processamento;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import consultas.consultasEmpenho;
import save.salvarDadosEmpenho;

public class empenhoFuncionalidades {
	
	
	public static void liberarEmpenho(ContextoAcao arg0,BigDecimal contrato,String empenho) throws Exception {
		
		
		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = dwf.getJdbcWrapper();
		jdbc.openSession();
		
		String deleteEmpenho = "delete from AD_EMPENHO where NUMCONTRATO="+contrato;
				PreparedStatement  consultaParEmepenho = jdbc.getPreparedStatement(deleteEmpenho);
				consultaParEmepenho.execute();

		String consulta = consultasEmpenho.empenhoConsulta(""+contrato);
		PreparedStatement  consultaPar2 = jdbc.getPreparedStatement(consulta);
		ResultSet retornoParametros12 = consultaPar2.executeQuery();
		while(retornoParametros12.next()) {
			
			BigDecimal CODPROD = retornoParametros12.getBigDecimal("CODPROD");
			BigDecimal CODPARC = retornoParametros12.getBigDecimal("CODPARC");
			BigDecimal QTDDISPONIVEL = retornoParametros12.getBigDecimal("QTD_DISPONIVEL");
			BigDecimal AD_DISPONIVEL = retornoParametros12.getBigDecimal("AD_DISPONIVEL");
			
			
			salvarDadosEmpenho.gerarEmpenho(
					dwf, 
					CODPROD, 
					contrato, 
					CODPARC, 
					QTDDISPONIVEL, 
					BigDecimal.ZERO, 
					empenho,
					AD_DISPONIVEL);
			
		}
		
		jdbc.closeSession();
		
		
	}
	
	public static void liberarEmpenhoTodos(ContextoAcao arg0,BigDecimal contrato,String empenho) throws Exception {
		
		
		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = dwf.getJdbcWrapper();
		jdbc.openSession();
		
		/*String deleteEmpenho = "delete from AD_EMPENHO where NUMCONTRATO="+contrato;
				PreparedStatement  consultaParEmepenho = jdbc.getPreparedStatement(deleteEmpenho);
				consultaParEmepenho.execute();*/

		String consulta = consultasEmpenho.empenhoConsulta(""+contrato);
		PreparedStatement  consultaPar2 = jdbc.getPreparedStatement(consulta);
		ResultSet retornoParametros12 = consultaPar2.executeQuery();
		while(retornoParametros12.next()) {
			
			BigDecimal CODPROD = retornoParametros12.getBigDecimal("CODPROD");
			BigDecimal CODPARC = retornoParametros12.getBigDecimal("CODPARC");
			BigDecimal QTDDISPONIVEL = retornoParametros12.getBigDecimal("QTD_DISPONIVEL");
			BigDecimal AD_DISPONIVEL = retornoParametros12.getBigDecimal("AD_DISPONIVEL");
			
			
	    		String update1 = "UPDATE AD_ITENSEMPENHO set QTDLIBERAR=0,AD_DISPONIVEL=AD_DISPONIVEL-"+AD_DISPONIVEL+"  WHERE NUMCONTRATO = "+contrato+" AND CODPROD = "+CODPROD;
			PreparedStatement  preUpdt1 = jdbc.getPreparedStatement(update1);
			preUpdt1.executeUpdate();
			
            salvarDadosEmpenho.gerarEmpenhoConverter(
            		dwf, 
            		CODPROD, 
            		contrato, 
            		AD_DISPONIVEL, 
            		AD_DISPONIVEL, 
            		empenho);
            
			/*salvarDadosEmpenho.gerarEmpenho(
					dwf, 
					CODPROD, 
					contrato, 
					CODPARC, 
					QTDDISPONIVEL, 
					AD_DISPONIVEL, 
					empenho,
					AD_DISPONIVEL);*/
			
		}
		
		jdbc.closeSession();
		
		
	}

}
