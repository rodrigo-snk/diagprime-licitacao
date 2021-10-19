package processamento;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.PersistenceException;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import save.salvarDados;

public class recalcularItens {

	public static void atualizarCusto(
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
	
		
		if(!(markupFator.doubleValue()>0)) {
			markupFator = BigDecimal.ONE;
		}
		
		if(!(qtdNeg.doubleValue()>0)) {
			qtdNeg = BigDecimal.ONE;
		}
		
		String  consultaDados = "SELECT coalesce(CUSGER,0) as CUSGER FROM TGFCUS WHERE CODPROD = "+codProd+" AND DTATUAL IN (\r\n"
				+ "select MAX(DTATUAL) AS VALOR from TGFCUS WHERE CODPROD = "+codProd+")";
		
		PreparedStatement  consultaValidando = jdbcWrapper.getPreparedStatement(consultaDados);
		ResultSet consulta = consultaValidando.executeQuery();
		BigDecimal valor = new BigDecimal(0);
		while(consulta.next()){
			
			valor = consulta.getBigDecimal("CUSGER");
			
		}
		
		if(!(valor.intValue()>0)) {
			
			throw new PersistenceException("Custo do produto obrigatório, não encontrado ou está zerado o custo, no cadastro de custo "+consultaDados);
	
		}
		vlrTot = (valor.multiply(markupFator)).multiply(qtdNeg);
		vlrUnit = (valor.multiply(markupFator));
		
		String update = "UPDATE AD_ITENSLICITACAO SET MARKUPFATOR="+markupFator+",CUSTO="+valor+",VLRTOTAL="+vlrTot+",QTDE="+qtdNeg+",VLRUNIT="+vlrUnit+" "
				+ "where CODITELIC="+codIteLic;
		PreparedStatement  updateValidando = jdbcWrapper.getPreparedStatement(update);
  		updateValidando.executeUpdate();
		
  		String consultaCabecalho = "select codemp,nunota,codlic from ad_licitacao  where codlic="+codLic;
		PreparedStatement  consultaValidando2 = jdbcWrapper.getPreparedStatement(consultaCabecalho);
		ResultSet consultaCabecalho2 = consultaValidando2.executeQuery();

		while(consultaCabecalho2.next()){

			BigDecimal nuNota = consultaCabecalho2.getBigDecimal("NUNOTA");
			BigDecimal codEmp = consultaCabecalho2.getBigDecimal("CODEMP");
				salvarDados.salvarItensDados(
  				dwf, 
  				nuNota, 
  				codProd, 
  				qtdNeg, 
  				codVol, 
  				vlrUnit, 
  				vlrTot, 
  				codEmp,
  				codIteLic,
  				codLic);
				
				ImpostosHelpper impostos = new ImpostosHelpper();
				impostos.setForcarRecalculo(true);
				impostos.calcularImpostos(nuNota);
		}

		salvarDados.insertComponentes(codLic, jdbcWrapper);
		jdbcWrapper.closeSession();
	}

}
