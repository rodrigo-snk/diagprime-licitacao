package processamento;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import save.salvarDados;

public class updateItens {

	public static void atualizarCustoVolume(PersistenceEvent arg0) throws Exception {

		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();

		DynamicVO itemVO = (DynamicVO) arg0.getVo();
		BigDecimal codLic = itemVO.asBigDecimalOrZero("CODLIC");
		BigDecimal codIteLic = itemVO.asBigDecimalOrZero("CODITELIC");
		BigDecimal codProd = itemVO.asBigDecimalOrZero("CODPROD");
		BigDecimal qtdNeg = itemVO.asBigDecimalOrZero("QTDE");
		BigDecimal vlrUnit = itemVO.asBigDecimalOrZero("VLRUNIT");
		BigDecimal markupFator = itemVO.asBigDecimalOrZero("MARKUPFATOR");
		String codVol = itemVO.asString("UNID");

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

		ItensLicitacao.atualizaItemLic(codLic,codIteLic,custo.multiply(markupFator),custo.multiply(markupFator).multiply(qtdNeg),markupFator,custo);

		final String sqlunidade = "SELECT DIVIDEMULTIPLICA,MULTIPVLR,QUANTIDADE FROM TGFVOA WHERE CODPROD = "+codProd+" and CODVOL = '"+codVol+"'";
		pstmt = jdbcWrapper.getPreparedStatement(sqlunidade);
		rs = pstmt.executeQuery();
		if (rs.next()){
			final String divideOuMultiplica = rs.getString("DIVIDEMULTIPLICA");
			BigDecimal quantidade = rs.getBigDecimal("QUANTIDADE").multiply(rs.getBigDecimal("MULTIPVLR"));

			if (divideOuMultiplica.equalsIgnoreCase("M")) {
				custo = custo.multiply(quantidade);
				qtdNeg = qtdNeg.multiply(quantidade);
				vlrUnit = custo.multiply(markupFator).divide(quantidade, MathContext.DECIMAL128);

			}
			else if (divideOuMultiplica.equalsIgnoreCase("D")) {
				custo = custo.divide(quantidade, MathContext.DECIMAL128);
				qtdNeg = qtdNeg.divide(quantidade, MathContext.DECIMAL128);
				vlrUnit = custo.multiply(markupFator).multiply(quantidade);
			}
		}

		//Valores convertidos
		//vlrUnit = custo.multiply(markupFator);
		BigDecimal vlrTot = vlrUnit.multiply(qtdNeg);
		markupFator = vlrUnit.divide(custo, MathContext.DECIMAL128);

		ItensLicitacao.atualizaItemLic(codLic, codIteLic, custo, vlrTot, markupFator, custo);

		final String updateIte = "UPDATE TGFITE SET QTDNEG="+qtdNeg+",VLRTOT="+vlrTot+",VLRUNIT="+vlrUnit+", CODVOL= '"+codVol+"' where AD_CODITELIC="+codIteLic+" and AD_CODLIC="+codLic;
		pstmt = jdbcWrapper.getPreparedStatement(updateIte);
		pstmt.executeUpdate();

		jdbcWrapper.closeSession();
	}

	public static void atualizaImpostosFederais(PersistenceEvent arg0) throws Exception {

		DynamicVO licitacaoVO = (DynamicVO) arg0.getVo();
		BigDecimal percentualCSLLRetido = licitacaoVO.asBigDecimalOrZero("PERCENTUAL_CSLL");
		BigDecimal percentualCSLLDevido = licitacaoVO.asBigDecimalOrZero("PERCENTUAL_CSLLDEVIDO");
		BigDecimal percentualIRRetido = licitacaoVO.asBigDecimalOrZero("PERCENTUAL_IR");
		BigDecimal percentualIRDevido = licitacaoVO.asBigDecimalOrZero("PERCENTUAL_IRDEVIDO");

		// Se % CSLL devido for 0,00 ou nulo, atribui o valor do % CSLL retido
		if (percentualCSLLDevido.compareTo(BigDecimal.ZERO) == 0){
			licitacaoVO.setProperty("PERCENTUAL_CSLLDEVIDO", percentualCSLLRetido);
		}

		// Se % IR devido for 0,00 ou nulo, atribui o valor do % IR retido
		if (percentualIRDevido.compareTo(BigDecimal.ZERO) == 0){
			licitacaoVO.setProperty("PERCENTUAL_IRDEVIDO", percentualIRRetido);
		}

		// Cálculo dos valores de CSSS e IR devido.
		licitacaoVO.setProperty("CSLLDEVIDO", licitacaoVO.asBigDecimalOrZero("VLRTOTAL").multiply(licitacaoVO.asBigDecimalOrZero("PERCENTUAL_CSLLDEVIDO").divide(BigDecimal.valueOf(100), MathContext.DECIMAL128)));
		licitacaoVO.setProperty("IRDEVIDO", licitacaoVO.asBigDecimalOrZero("VLRTOTAL").multiply(licitacaoVO.asBigDecimalOrZero("PERCENTUAL_IRDEVIDO").divide(BigDecimal.valueOf(100), MathContext.DECIMAL128)));

		EntityFacadeFactory.getDWFFacade().saveEntity("AD_LICITACAO", (EntityVO) licitacaoVO);
	}

	public static void atualizarCustoProduto(PersistenceEvent arg0) throws Exception {

		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();

		DynamicVO itemVO = (DynamicVO) arg0.getVo();
		BigDecimal codLic = itemVO.asBigDecimalOrZero("CODLIC");
		BigDecimal codIteLic = itemVO.asBigDecimalOrZero("CODITELIC");
		BigDecimal codProd = itemVO.asBigDecimalOrZero("CODPROD");
		BigDecimal qtdNeg = itemVO.asBigDecimalOrZero("QTDE");
		BigDecimal vlrTot = itemVO.asBigDecimalOrZero("VLRTOTAL");
		BigDecimal vlrUnit = itemVO.asBigDecimalOrZero("VLRUNIT");
		BigDecimal custo = itemVO.asBigDecimalOrZero("CUSTO");
		BigDecimal markupFator = itemVO.asBigDecimalOrZero("MARKUPFATOR");
		String codVol = itemVO.asString("UNID");

		if (markupFator.compareTo(BigDecimal.ZERO) <= 0) {
			markupFator = BigDecimal.ONE;
		}
		if (qtdNeg.compareTo(BigDecimal.ZERO) <= 0) {
			qtdNeg = BigDecimal.ONE;
		}

		if (custo.compareTo(BigDecimal.ZERO) <= 0) {
			custo = BigDecimal.ONE;
		}

		//PreparedStatement pstmt = jdbcWrapper.getPreparedStatement("UPDATE AD_ITENSLICITACAO SET CUSTO="+custo+",VLRTOTAL="+vlrTot+",VLRUNIT="+vlrUnit+" where CODITELIC="+codIteLic+"  and CODLIC="+codLic);
		//pstmt.executeUpdate();
		ItensLicitacao.atualizaItemLic(codLic,codIteLic,custo.multiply(markupFator),custo.multiply(markupFator).multiply(qtdNeg),markupFator,custo);

		final String sqlunidade = "SELECT DIVIDEMULTIPLICA,MULTIPVLR,QUANTIDADE FROM TGFVOA WHERE CODPROD = "+codProd+" and CODVOL = '"+codVol+"'";
		PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(sqlunidade);
		ResultSet rs = pstmt.executeQuery();

		if (rs.next()){
			final String divideOuMultiplica = rs.getString("DIVIDEMULTIPLICA");
			BigDecimal quantidade = rs.getBigDecimal("QUANTIDADE").multiply(rs.getBigDecimal("MULTIPVLR"));

			if (divideOuMultiplica.equalsIgnoreCase("M")) {
				//custo = custo.multiply(quantidade);
				qtdNeg = qtdNeg.multiply(quantidade);
				vlrUnit = custo.multiply(markupFator).divide(quantidade, MathContext.DECIMAL128);

			}
			else if (divideOuMultiplica.equalsIgnoreCase("D")) {
				//custo = custo.divide(quantidade, MathContext.DECIMAL128);
				qtdNeg = qtdNeg.divide(quantidade, MathContext.DECIMAL128);
				vlrUnit = custo.multiply(markupFator).multiply(quantidade);

			}
		}
		vlrTot = vlrUnit.multiply(qtdNeg);

		final String updateIte = "UPDATE TGFITE SET QTDNEG="+qtdNeg+",VLRTOT="+vlrTot+",VLRUNIT="+vlrUnit+", CODVOL= '"+codVol+"' where AD_CODITELIC="+codIteLic+" and AD_CODLIC="+codLic;
		pstmt = jdbcWrapper.getPreparedStatement(updateIte);
		pstmt.executeUpdate();

		jdbcWrapper.closeSession();
	}

	public static void atualizarQtdNeg(PersistenceEvent arg0) throws Exception {

		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();

		DynamicVO itemVO = (DynamicVO) arg0.getVo();
		BigDecimal codLic = itemVO.asBigDecimalOrZero("CODLIC");
		BigDecimal codIteLic = itemVO.asBigDecimalOrZero("CODITELIC");
		BigDecimal codProd = itemVO.asBigDecimalOrZero("CODPROD");
		BigDecimal qtdNeg = itemVO.asBigDecimalOrZero("QTDE");
		BigDecimal vlrTot = itemVO.asBigDecimalOrZero("VLRTOTAL");
		BigDecimal vlrUnit = itemVO.asBigDecimalOrZero("VLRUNIT");
		BigDecimal custo = itemVO.asBigDecimalOrZero("CUSTO");
		BigDecimal markupFator = itemVO.asBigDecimalOrZero("MARKUPFATOR");
		String codVol = itemVO.asString("UNID");

		if (markupFator.compareTo(BigDecimal.ZERO) <= 0) {
			markupFator = BigDecimal.ONE;
		}
		if (qtdNeg.compareTo(BigDecimal.ZERO) <= 0) {
			qtdNeg = BigDecimal.ONE;
		}

		if (custo.compareTo(BigDecimal.ZERO) <= 0) {
			custo = BigDecimal.ONE;
		}

		ItensLicitacao.atualizaItemLic(codLic,codIteLic,custo.multiply(markupFator),custo.multiply(markupFator).multiply(qtdNeg),markupFator,custo);

		final String sqlunidade = "SELECT DIVIDEMULTIPLICA,MULTIPVLR,QUANTIDADE FROM TGFVOA WHERE CODPROD = "+codProd+" and CODVOL = '"+codVol+"'";
		PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(sqlunidade);
		ResultSet rs = pstmt.executeQuery();

		if (rs.next()){
			final String divideOuMultiplica = rs.getString("DIVIDEMULTIPLICA");
			BigDecimal quantidade = rs.getBigDecimal("QUANTIDADE").multiply(rs.getBigDecimal("MULTIPVLR"));

			if (divideOuMultiplica.equalsIgnoreCase("M")) {
				qtdNeg = qtdNeg.multiply(quantidade);
				vlrUnit = custo.multiply(markupFator).divide(quantidade, MathContext.DECIMAL128);

			}
			else if (divideOuMultiplica.equalsIgnoreCase("D")) {
				qtdNeg = qtdNeg.divide(quantidade, MathContext.DECIMAL128);
				vlrUnit = custo.multiply(markupFator).multiply(quantidade);
			}
		}

		vlrTot = vlrUnit.multiply(qtdNeg);

		//PreparedStatement pstmt = jdbcWrapper.getPreparedStatement("UPDATE AD_ITENSLICITACAO SET CUSTO="+custo+",VLRTOTAL="+vlrTot+",VLRUNIT="+vlrUnit+" where CODITELIC="+codIteLic+"  and CODLIC="+codLic);
		//pstmt.executeUpdate();

		final String updateIte = "UPDATE TGFITE SET QTDNEG="+qtdNeg+",VLRTOT="+vlrTot+",VLRUNIT="+vlrUnit+", CODVOL= '"+codVol+"' where AD_CODITELIC="+codIteLic+" and AD_CODLIC="+codLic;
		pstmt = jdbcWrapper.getPreparedStatement(updateIte);
		pstmt.executeUpdate();


		jdbcWrapper.closeSession();
	}

	public static void atualizarVlrUnit(PersistenceEvent arg0) throws Exception {
		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();

		DynamicVO itemVO = (DynamicVO) arg0.getVo();
		BigDecimal codLic = itemVO.asBigDecimalOrZero("CODLIC");
		BigDecimal codIteLic = itemVO.asBigDecimalOrZero("CODITELIC");
		BigDecimal codProd = itemVO.asBigDecimalOrZero("CODPROD");
		BigDecimal qtdNeg = itemVO.asBigDecimalOrZero("QTDE");
		BigDecimal custo = itemVO.asBigDecimalOrZero("CUSTO");
		BigDecimal vlrUnit = itemVO.asBigDecimalOrZero("VLRUNIT");
		BigDecimal markupFator = itemVO.asBigDecimalOrZero("MARKUPFATOR");
		String codVol = itemVO.asString("UNID");

		if (markupFator.compareTo(BigDecimal.ZERO) <= 0) {
			markupFator = BigDecimal.ONE;
		}
		if (qtdNeg.compareTo(BigDecimal.ZERO) <= 0) {
			qtdNeg = BigDecimal.ONE;
		}

		markupFator = vlrUnit.divide(custo, MathContext.DECIMAL128);
		BigDecimal vlrTot = vlrUnit.multiply(qtdNeg);

		//ItensLicitacao.atualizaItemLicitacao(codLic,codIteLic,vlrUnit,vlrTot,markupFator,custo);
		ItensLicitacao.atualizaItemLic(codLic,codIteLic,vlrUnit,vlrTot,markupFator,custo);

		final String sqlunidade = "SELECT DIVIDEMULTIPLICA,MULTIPVLR,QUANTIDADE FROM TGFVOA WHERE CODPROD = "+codProd+" and CODVOL = '"+codVol+"'";
		PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(sqlunidade);
		ResultSet rs = pstmt.executeQuery();

		if (rs.next()){
			final String divideOuMultiplica = rs.getString("DIVIDEMULTIPLICA");
			BigDecimal quantidade = rs.getBigDecimal("QUANTIDADE").multiply(rs.getBigDecimal("MULTIPVLR"));

			if (divideOuMultiplica.equalsIgnoreCase("M")) {
				qtdNeg = qtdNeg.multiply(quantidade);
				vlrUnit = custo.multiply(markupFator).divide(quantidade, MathContext.DECIMAL128);
			}
			else if (divideOuMultiplica.equalsIgnoreCase("D")) {
				qtdNeg = qtdNeg.divide(quantidade, MathContext.DECIMAL128);
				vlrUnit = custo.multiply(markupFator).multiply(quantidade);
			}
		}

		//Valores convertidos
		//vlrUnit = custo.multiply(markupFator);
		vlrTot = vlrUnit.multiply(qtdNeg);

		// Atualiza item convertido na TGFITE
		final String updateIte = "UPDATE TGFITE SET QTDNEG="+qtdNeg+",VLRTOT="+vlrTot+",VLRUNIT="+vlrUnit+" where AD_CODITELIC="+codIteLic+" and AD_CODLIC="+codLic;
		pstmt = jdbcWrapper.getPreparedStatement(updateIte);
		pstmt.executeUpdate();

		//salvarDados.insertComponentes(codLic, jdbcWrapper);

		jdbcWrapper.closeSession();
	}

}
