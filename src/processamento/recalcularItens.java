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

		if (markupFator.compareTo(BigDecimal.ZERO) <= 0) {
			markupFator = BigDecimal.ONE;
		}
		if (qtdNeg.compareTo(BigDecimal.ZERO) <= 0) {
			qtdNeg = BigDecimal.ONE;
		}
		
		final String consultaDados = "SELECT coalesce(CUSGER,0) as CUSGER FROM TGFCUS WHERE CODPROD = "+codProd+" AND DTATUAL IN (\r\n"
				+ "select MAX(DTATUAL) AS VALOR from TGFCUS WHERE CODPROD = "+codProd+")";
		
		PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(consultaDados);
		ResultSet rs = pstmt.executeQuery();
		BigDecimal custo = BigDecimal.ZERO;
		while(rs.next()){
			custo = rs.getBigDecimal("CUSGER");
		}
		
		if(!(custo.intValue()>0)) {
			throw new PersistenceException("Custo do produto obrigatório, não encontrado ou está zerado o custo, no cadastro de custo "+consultaDados);
		}
		vlrUnit = custo.multiply(markupFator);
		vlrTot = vlrUnit.multiply(qtdNeg);

		String update = "UPDATE AD_ITENSLICITACAO SET MARKUPFATOR="+markupFator+",CUSTO="+custo+",VLRTOTAL="+vlrTot+",QTDE="+qtdNeg+",VLRUNIT="+vlrUnit+" "
				+ "where CODITELIC="+codIteLic;
		PreparedStatement  updateValidando = jdbcWrapper.getPreparedStatement(update);
  		updateValidando.executeUpdate();
		
  		String consultaCabecalho = "select codemp,nunota,codlic from ad_licitacao  where codlic="+codLic;
		pstmt = jdbcWrapper.getPreparedStatement(consultaCabecalho);
		rs = pstmt.executeQuery();

		while(rs.next()){

			BigDecimal nuNota = rs.getBigDecimal("NUNOTA");
			BigDecimal codEmp = rs.getBigDecimal("CODEMP");

			//ERRO adiciona um novo item na TGFITE
			/*salvarDados.salvarItensDados(
  				dwf, 
  				nuNota, 
  				codProd, 
  				qtdNeg, 
  				codVol, 
  				vlrUnit, 
  				vlrTot, 
  				codEmp,
  				codIteLic,
  				codLic);*/
				
				ImpostosHelpper impostos = new ImpostosHelpper();
				impostos.setForcarRecalculo(true);
				impostos.calcularImpostos(nuNota);
		}

		salvarDados.insertComponentes(codLic, jdbcWrapper);
		jdbcWrapper.closeSession();
	}

}
