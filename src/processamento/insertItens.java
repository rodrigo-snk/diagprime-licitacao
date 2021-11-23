package processamento;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.PersistenceException;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import consultas.consultasDados;
import save.salvarDados;

public class insertItens {

	public static void atualizarCusto(PersistenceEvent arg0) throws Exception {

		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();

		DynamicVO itemVO = (DynamicVO) arg0.getVo();
		BigDecimal codIteLic = itemVO.asBigDecimalOrZero("CODITELIC");
		BigDecimal codLic = itemVO.asBigDecimalOrZero("CODLIC");
		BigDecimal codProd = itemVO.asBigDecimalOrZero("CODPROD");
		BigDecimal qtdNeg = itemVO.asBigDecimalOrZero("QTDE");
		BigDecimal vlrTot = itemVO.asBigDecimalOrZero("VLRTOTAL");
		BigDecimal vlrUnit = itemVO.asBigDecimalOrZero("VLRUNIT");
		BigDecimal item = itemVO.asBigDecimalOrZero("ITEM");
		BigDecimal markupFator = itemVO.asBigDecimalOrZero("MARKUPFATOR");
		String codVol = itemVO.asString("UNID");
		BigDecimal replicando = (itemVO.asBigDecimalOrZero("REPLICANDO"));

		if (markupFator.compareTo(BigDecimal.ZERO) <= 0) {
			markupFator = BigDecimal.ONE;
		}
		if (qtdNeg.compareTo(BigDecimal.ZERO) <= 0) {
			qtdNeg = BigDecimal.ONE;
		}

		String consultaProd = consultasDados.retornaDadosItensProdutos(codProd.toString());
		PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(consultaProd);
		ResultSet rs = pstmt.executeQuery();
		boolean isServico = false;
		while(rs.next()) isServico = rs.getString("USOPROD").equals("S");

		
		String consultaDados = "SELECT coalesce(CUSGER,0) as CUSGER FROM TGFCUS WHERE CODPROD = "+codProd+" AND DTATUAL IN (\r\n"
				+ "select MAX(DTATUAL) AS VALOR from TGFCUS WHERE CODPROD = "+codProd+")";
		
		pstmt = jdbcWrapper.getPreparedStatement(consultaDados);
		rs = pstmt.executeQuery();
		BigDecimal valor = BigDecimal.ZERO;

		while(rs.next()) valor = (rs.getBigDecimal("CUSGER") == null) ? BigDecimal.ZERO : rs.getBigDecimal("CUSGER");

		if(!(valor.intValue() > 0) && !isServico) {
			throw new PersistenceException("Custo do produto obrigatório, não encontrado ou está zerado o custo, no cadastro de custo "+consultaDados);
		}

		vlrUnit = valor.multiply(markupFator);
		vlrTot = vlrUnit.multiply(qtdNeg);

		String update;
		String update1;
		if(!((replicando.intValue())>0)) {
				update = "UPDATE AD_ITENSLICITACAO SET ITEM=(select COALESCE(max(item),0)+1 item from AD_ITENSLICITACAO WHERE CODLIC="+codLic+"),MARKUPFATOR="+markupFator+",CUSTO="+valor+",VLRTOTAL="+vlrTot+",QTDE="+qtdNeg+",VLRUNIT="+vlrUnit+" "
				+ " where CODITELIC="+codIteLic+" and CODLIC="+codLic;
				
				update1 = "UPDATE TGFITE SET VLRTOT="+vlrTot+",QTDNEG="+qtdNeg+",VLRUNIT="+vlrUnit+" "
						+ " where AD_CODITELIC="+codIteLic+" and AD_CODLIC="+codLic;

				pstmt = jdbcWrapper.getPreparedStatement(update1);
				pstmt.executeUpdate();
				
				
		}else {
				update = "UPDATE AD_ITENSLICITACAO SET REPLICANDO=0 "
						+ "WHERE CODITELIC="+codIteLic+" AND CODLIC="+codLic;
		}

		pstmt = jdbcWrapper.getPreparedStatement(update);
		pstmt.executeUpdate();

		DynamicVO licitacaoVO = (DynamicVO) dwf.findEntityByPrimaryKeyAsVO("AD_LICITACAO", codLic);
		BigDecimal nuNota = licitacaoVO.asBigDecimal("NUNOTA");
		BigDecimal codEmp = licitacaoVO.asBigDecimal("CODEMP");

		pstmt = jdbcWrapper.getPreparedStatement("SELECT DIVIDEMULTIPLICA,MULTIPVLR, QUANTIDADE FROM TGFVOA WHERE CODPROD = "+codProd+" and CODVOL = '"+codVol+"'");
		rs = pstmt.executeQuery();

		if(rs.next()){
			final String divideOuMultiplica = rs.getString("DIVIDEMULTIPLICA");
			BigDecimal quantidade = rs.getBigDecimal("QUANTIDADE").multiply(rs.getBigDecimal("MULTIPVLR"));

			if (divideOuMultiplica.equalsIgnoreCase("M")) {
				qtdNeg = qtdNeg.multiply(quantidade);
				vlrUnit = vlrUnit.divide(quantidade, MathContext.DECIMAL128);
			}
			else if (divideOuMultiplica.equalsIgnoreCase("D")) {
				qtdNeg = qtdNeg.divide(quantidade, MathContext.DECIMAL128);
				vlrUnit = vlrUnit.multiply(quantidade);
			}

			salvarDados.salvarItensDados(dwf,nuNota,codProd,qtdNeg,codVol,vlrUnit,vlrTot,codEmp,codIteLic,codLic);

		}

		ImpostosHelpper impostos = new ImpostosHelpper();
		impostos.setForcarRecalculo(true);
		impostos.calcularImpostos(nuNota);

		salvarDados.insertComponentes(codLic, jdbcWrapper);
		jdbcWrapper.closeSession();
	}

}
