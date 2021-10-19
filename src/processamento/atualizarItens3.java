package processamento;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class atualizarItens3 {
	
	
	
	public static void atualizarTotal(PersistenceEvent arg0) throws Exception {
		
		
		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();
		
		
		DynamicVO dados = (DynamicVO) arg0.getVo();
		BigDecimal codIteLic = (dados.asBigDecimalOrZero("CODITELIC"));
		BigDecimal qtdNeg = (dados.asBigDecimalOrZero("QTDE"));
		BigDecimal vlrTot = (dados.asBigDecimalOrZero("VLRTOTAL"));
		BigDecimal vlrUnit = (dados.asBigDecimalOrZero("VLRUNIT"));
		BigDecimal codLic = (dados.asBigDecimalOrZero("CODLIC"));

		vlrTot = vlrUnit.multiply(qtdNeg);
		String update = "UPDATE AD_ITENSLICITACAO SET VLRTOTAL="+vlrTot+" where CODITELIC="+codIteLic+" and CODLIC="+codLic;
		PreparedStatement  updateValidando = jdbcWrapper.getPreparedStatement(update);
  		updateValidando.executeUpdate();

  		String consultaCabecalho = "select codemp,nunota,codlic from ad_licitacao  where codlic="+codLic;
		PreparedStatement  consultaValidando2 = jdbcWrapper.getPreparedStatement(consultaCabecalho);
		ResultSet consultaCabecalho2 = consultaValidando2.executeQuery();

		while(consultaCabecalho2.next()){

			BigDecimal nuNota = consultaCabecalho2.getBigDecimal("NUNOTA");
			BigDecimal codEmp = consultaCabecalho2.getBigDecimal("CODEMP");
				
				ImpostosHelpper impostos = new ImpostosHelpper();
				impostos.setForcarRecalculo(true);
				impostos.calcularImpostos(nuNota);
				
		}
  		
		jdbcWrapper.closeSession();
	}

}
