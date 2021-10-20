package processamento;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.PersistenceException;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import consultas.consultasDados;
import save.salvarDados;

public class inserirItens {
	
	
	
	public static void atualizarCusto(PersistenceEvent arg0) throws Exception {
		
		
		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();
		
		
		DynamicVO dados = (DynamicVO) arg0.getVo();
		BigDecimal codIteLic = dados.asBigDecimalOrZero("CODITELIC");
		BigDecimal codLic = dados.asBigDecimalOrZero("CODLIC");
		BigDecimal codProd = dados.asBigDecimalOrZero("CODPROD");
		BigDecimal qtdNeg = dados.asBigDecimalOrZero("QTDE");
		BigDecimal vlrTot = dados.asBigDecimalOrZero("VLRTOTAL");
		BigDecimal vlrUnit = dados.asBigDecimalOrZero("VLRUNIT");
		BigDecimal item = dados.asBigDecimalOrZero("ITEM");
		BigDecimal markupFator = dados.asBigDecimalOrZero("MARKUPFATOR");
		String codVol = dados.asString("UNID");
		BigDecimal replicando = (dados.asBigDecimalOrZero("REPLICANDO"));
		
		
		if(!(markupFator.doubleValue()>0)) {
			markupFator = BigDecimal.ONE;
		}
		
		if(!(qtdNeg.doubleValue()>0)) {
			qtdNeg = BigDecimal.ONE;
			
		}
		String consultaProd = consultasDados.retornaDadosItensProdutos(codProd.toString());
		PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(consultaProd);
		ResultSet rset = pstmt.executeQuery();
		boolean isServico = false;
		while(rset.next()) isServico = rset.getString("USOPROD").equals("S");

		
		String  consultaDados = "SELECT coalesce(CUSGER,0) as CUSGER FROM TGFCUS WHERE CODPROD = "+codProd+" AND DTATUAL IN (\r\n"
				+ "select MAX(DTATUAL) AS VALOR from TGFCUS WHERE CODPROD = "+codProd+")";
		
		PreparedStatement  consultaValidando = jdbcWrapper.getPreparedStatement(consultaDados);
		ResultSet consulta = consultaValidando.executeQuery();
		BigDecimal valor = new BigDecimal(0);

		while(consulta.next()) valor = (consulta.getBigDecimal("CUSGER") == null) ? BigDecimal.ZERO : consulta.getBigDecimal("CUSGER");

		if(!(valor.intValue() > 0) && !isServico) {
			
			throw new PersistenceException("Custo do produto obrigat�rio, n�o encontrado ou est� zerado o custo, no cadastro de custo "+consultaDados);
	
		}
		vlrTot = (valor.multiply(markupFator)).multiply(qtdNeg);
		vlrUnit = (valor.multiply(markupFator));
		String update;
		String update1;
		if(!((replicando.intValue())>0)) {
				update = "UPDATE AD_ITENSLICITACAO SET ITEM=(select COALESCE(max(item),0)+1 item from AD_ITENSLICITACAO WHERE CODLIC="+codLic+"),MARKUPFATOR="+markupFator+",CUSTO="+valor+",VLRTOTAL="+vlrTot+",QTDE="+qtdNeg+",VLRUNIT="+vlrUnit+" "
				+ " where CODITELIC="+codIteLic+" and CODLIC="+codLic;
				
				update1 = "UPDATE TGFITE SET VLRTOT="+vlrTot+",QTDNEG="+qtdNeg+",VLRUNIT="+vlrUnit+" "
						+ " where AD_CODITELIC="+codIteLic+" and AD_CODLIC="+codLic;
				
				PreparedStatement  updateValidando1 = jdbcWrapper.getPreparedStatement(update1);
				updateValidando1.executeUpdate();
				
				
		}else {
				update = "UPDATE AD_ITENSLICITACAO SET REPLICANDO=0 "
						+ "WHERE CODITELIC="+codIteLic+" AND CODLIC="+codLic;
		}
		PreparedStatement  updateValidando = jdbcWrapper.getPreparedStatement(update);
  		updateValidando.executeUpdate();

  		String consultaCabecalho = "select CODEMP, NUNOTA, CODLIC from AD_LICITACAO where CODLIC="+codLic;
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
