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
import save.salvarDados;

public class atualizarItens2 {
	
	
	
	public static void atualizarCustoProduto(PersistenceEvent arg0) throws Exception {
		
		
		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();
		
		
		DynamicVO dados = (DynamicVO) arg0.getVo();
		BigDecimal codLic = (dados.asBigDecimalOrZero("CODLIC"));
		BigDecimal codIteLic = (dados.asBigDecimalOrZero("CODITELIC"));
		BigDecimal codProd = (dados.asBigDecimalOrZero("CODPROD"));
		BigDecimal qtdNeg = (dados.asBigDecimalOrZero("QTDE"));
		BigDecimal custo = (dados.asBigDecimalOrZero("CUSTO"));
		BigDecimal vlrTot = (dados.asBigDecimalOrZero("VLRTOTAL"));
		BigDecimal vlrUnit = (dados.asBigDecimalOrZero("VLRUNIT"));
		BigDecimal markupFator = (dados.asBigDecimalOrZero("MARKUPFATOR"));
		String codVol = (dados.asString("UNID"));
		
		if(!(markupFator.doubleValue()>0)) {
			markupFator = BigDecimal.ONE;
		}
		
		if(!(qtdNeg.doubleValue()>0)) {
			qtdNeg = BigDecimal.ONE;
		}
		
        BigDecimal valor = custo;
        BigDecimal quantidade = BigDecimal.ZERO;
			String sqlunidade = "SELECT DIVIDEMULTIPLICA,QUANTIDADE FROM TGFVOA WHERE CODPROD = "+codProd+" and CODVOL = '"+codVol+"'";
			PreparedStatement  consultaValidando1 = jdbcWrapper.getPreparedStatement(sqlunidade);
			ResultSet consultaUnidade = consultaValidando1.executeQuery();

			if(consultaUnidade.next()){
			
				String divide = consultaUnidade.getString("DIVIDEMULTIPLICA");
				quantidade = consultaUnidade.getBigDecimal("QUANTIDADE");
				
				valor = valor.multiply(quantidade);
				
			}
			
	
		vlrTot = (valor.multiply(markupFator)).multiply(qtdNeg);
		vlrUnit = (valor.multiply(markupFator));
		
		String update = "UPDATE AD_ITENSLICITACAO SET VLRTOTAL="+vlrTot+",VLRUNIT="+vlrUnit+" "
				+ "where CODITELIC="+codIteLic+" and CODLIC="+codLic;
		PreparedStatement  updateValidando = jdbcWrapper.getPreparedStatement(update);
  		updateValidando.executeUpdate();
  		

  		String update1 = "UPDATE TGFITE SET QTDNEG="+qtdNeg+",VLRTOT="+vlrTot+",VLRUNIT="+vlrUnit+" "
				+ "where AD_CODITELIC="+codIteLic+" and AD_CODLIC="+codLic;
		PreparedStatement  updateValidand1 = jdbcWrapper.getPreparedStatement(update1);
		updateValidand1.executeUpdate();
		
  		
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
		
  		salvarDados.insertComponentes(codLic, jdbcWrapper);
  		
		jdbcWrapper.closeSession();
	}

}