package processamento;

import java.math.BigDecimal;
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
		
		if(!(markupFator.doubleValue()>0)) {
			markupFator = BigDecimal.ONE;
		}
		
		if(!(qtdNeg.doubleValue()>0)) {
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

		BigDecimal vlrTot = vlrUnit.multiply(qtdNeg);

		pstmt = jdbcWrapper.getPreparedStatement("UPDATE AD_ITENSLICITACAO SET CUSTO="+custo+",VLRTOTAL="+vlrTot+",VLRUNIT="+vlrUnit+" where CODITELIC="+codIteLic+"  and CODLIC="+codLic);
		pstmt.executeUpdate();

		//ItensLicitacao.atualizaItemLicitacao(codLic,codIteLic,vlrUnit,vlrTot,markupFator,custo);
		//ItensLicitacao.atualizaItemLic(codLic,codIteLic,vlrUnit,vlrTot,markupFator,custo);


		final String sqlunidade = "SELECT DIVIDEMULTIPLICA,MULTIPVLR,QUANTIDADE FROM TGFVOA WHERE CODPROD = "+codProd+" and CODVOL = '"+codVol+"'";
		pstmt = jdbcWrapper.getPreparedStatement(sqlunidade);
		rs = pstmt.executeQuery();

		if (rs.next()){
			final String divideOuMultiplica = rs.getString("DIVIDEMULTIPLICA");
			BigDecimal quantidade = rs.getBigDecimal("QUANTIDADE").multiply(rs.getBigDecimal("MULTIPVLR"));

			if (divideOuMultiplica.equalsIgnoreCase("M")) {
				qtdNeg = qtdNeg.multiply(quantidade);
				vlrUnit = custo.multiply(markupFator).divide(quantidade);
			}
			else if (divideOuMultiplica.equalsIgnoreCase("D")) {
				qtdNeg = qtdNeg.divide(quantidade);
				vlrUnit = custo.multiply(markupFator).multiply(quantidade);
			}

		}

		//Valores convertidos
		//vlrUnit = custo.multiply(markupFator);
		vlrTot = vlrUnit.multiply(qtdNeg);

		final String updateIte = "UPDATE TGFITE SET QTDNEG="+qtdNeg+",VLRTOT="+vlrTot+",VLRUNIT="+vlrUnit+", CODVOL= '"+codVol+"' where AD_CODITELIC="+codIteLic+" and AD_CODLIC="+codLic;
		pstmt = jdbcWrapper.getPreparedStatement(updateIte);
		pstmt.executeUpdate();

  		salvarDados.insertComponentes(codLic, jdbcWrapper);

		DynamicVO licitacaoVO = (DynamicVO) dwf.findEntityByPrimaryKeyAsVO("AD_LICITACAO", codLic);

		ImpostosHelpper impostos = new ImpostosHelpper();
		impostos.setForcarRecalculo(true);
		impostos.calcularImpostos(licitacaoVO.asBigDecimalOrZero("NUNOTA"));

		jdbcWrapper.closeSession();
	}

	public static void atualizaImpostosFederais(PersistenceEvent arg0) throws Exception {
		EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = dwfFacade.getJdbcWrapper();
		jdbc.openSession();

		DynamicVO licitacao = (DynamicVO) arg0.getVo();
		BigDecimal percentualCSLLRetido = licitacao.asBigDecimalOrZero("PERCENTUAL_CSLL");
		BigDecimal percentualCSLLDevido = licitacao.asBigDecimalOrZero("PERCENTUAL_CSLLDEVIDO");
		BigDecimal percentualIRRetido = licitacao.asBigDecimalOrZero("PERCENTUAL_IR");
		BigDecimal percentualIRDevido = licitacao.asBigDecimalOrZero("PERCENTUAL_IRDEVIDO");


		if (percentualCSLLDevido.compareTo(BigDecimal.ZERO) == 0){
			licitacao.setProperty("PERCENTUAL_CSLLDEVIDO", percentualCSLLRetido);
		}

		if (percentualIRDevido.compareTo(BigDecimal.ZERO) == 0){
			licitacao.setProperty("PERCENTUAL_IRDEVIDO", percentualIRRetido);
		}
		licitacao.setProperty("CSLLDEVIDO", licitacao.asBigDecimalOrZero("VLRTOTAL").multiply(licitacao.asBigDecimalOrZero("PERCENTUAL_CSLLDEVIDO").divide(BigDecimal.valueOf(100))));
		licitacao.setProperty("IRDEVIDO", licitacao.asBigDecimalOrZero("VLRTOTAL").multiply(licitacao.asBigDecimalOrZero("PERCENTUAL_IRDEVIDO").divide(BigDecimal.valueOf(100))));
		dwfFacade.saveEntity("AD_LICITACAO", (EntityVO) licitacao);
		jdbc.closeSession();
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

		if(!(markupFator.doubleValue()>0)) markupFator = BigDecimal.ONE;
		if(!(qtdNeg.doubleValue()>0)) qtdNeg = BigDecimal.ONE;

		vlrUnit = custo.multiply(markupFator);
		vlrTot = vlrUnit.multiply(qtdNeg);

		PreparedStatement pstmt = jdbcWrapper.getPreparedStatement("UPDATE AD_ITENSLICITACAO SET CUSTO="+custo+",VLRTOTAL="+vlrTot+",VLRUNIT="+vlrUnit+" where CODITELIC="+codIteLic+"  and CODLIC="+codLic);
		pstmt.executeUpdate();
		//ItensLicitacao.atualizaItemLicitacao(codLic,codIteLic,vlrUnit,vlrTot,markupFator,custo);
		//ItensLicitacao.atualizaItemLic(codLic,codIteLic,vlrUnit,vlrTot,markupFator,custo);

		final String sqlunidade = "SELECT DIVIDEMULTIPLICA,MULTIPVLR,QUANTIDADE FROM TGFVOA WHERE CODPROD = "+codProd+" and CODVOL = '"+codVol+"'";
		pstmt = jdbcWrapper.getPreparedStatement(sqlunidade);
		ResultSet rs = pstmt.executeQuery();

		if (rs.next()){
			final String divideOuMultiplica = rs.getString("DIVIDEMULTIPLICA");
			BigDecimal quantidade = rs.getBigDecimal("QUANTIDADE").multiply(rs.getBigDecimal("MULTIPVLR"));

			if (divideOuMultiplica.equalsIgnoreCase("M")) {
				qtdNeg = qtdNeg.multiply(quantidade);
				vlrUnit = custo.multiply(markupFator).divide(quantidade);
			}
			else if (divideOuMultiplica.equalsIgnoreCase("D")) {
				qtdNeg = qtdNeg.divide(quantidade);
				vlrUnit = custo.multiply(markupFator).multiply(quantidade);
			}

		}

		final String updateIte = "UPDATE TGFITE SET QTDNEG="+qtdNeg+",VLRTOT="+vlrTot+",VLRUNIT="+vlrUnit+", CODVOL= '"+codVol+"' where AD_CODITELIC="+codIteLic+" and AD_CODLIC="+codLic;
		pstmt = jdbcWrapper.getPreparedStatement(updateIte);
		pstmt.executeUpdate();

		DynamicVO licitacaoVO = (DynamicVO) dwf.findEntityByPrimaryKeyAsVO("AD_LICITACAO", codLic);

		ImpostosHelpper impostos = new ImpostosHelpper();
		impostos.setForcarRecalculo(true);
		impostos.calcularImpostos(licitacaoVO.asBigDecimalOrZero("NUNOTA"));

		salvarDados.insertComponentes(codLic, jdbcWrapper);

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

		if(!(markupFator.doubleValue()>0)) {
			markupFator = BigDecimal.ONE;
		}

		if(!(qtdNeg.doubleValue()>0)) {
			qtdNeg = BigDecimal.ONE;
		}

		markupFator = vlrUnit.divide(custo,4, RoundingMode.HALF_UP);
		BigDecimal vlrTot = vlrUnit.multiply(qtdNeg);

		PreparedStatement pstmt = jdbcWrapper.getPreparedStatement("UPDATE AD_ITENSLICITACAO SET CUSTO="+custo+",VLRTOTAL="+vlrTot+",VLRUNIT="+vlrUnit+",MARKUPFATOR="+markupFator+" where CODITELIC="+codIteLic+"  and CODLIC="+codLic);
		pstmt.executeUpdate();
		//ItensLicitacao.atualizaItemLicitacao(codLic,codIteLic,vlrUnit,vlrTot,markupFator,custo);
		//ItensLicitacao.atualizaItemLic(codLic,codIteLic,vlrUnit,vlrTot,markupFator,custo);


		final String sqlunidade = "SELECT DIVIDEMULTIPLICA,MULTIPVLR,QUANTIDADE FROM TGFVOA WHERE CODPROD = "+codProd+" and CODVOL = '"+codVol+"'";
		pstmt = jdbcWrapper.getPreparedStatement(sqlunidade);
		ResultSet rs = pstmt.executeQuery();

		if (rs.next()){
			final String divideOuMultiplica = rs.getString("DIVIDEMULTIPLICA");
			BigDecimal quantidade = rs.getBigDecimal("QUANTIDADE").multiply(rs.getBigDecimal("MULTIPVLR"));

			if (divideOuMultiplica.equalsIgnoreCase("M")) {
				qtdNeg = qtdNeg.multiply(quantidade);
				vlrUnit = custo.multiply(markupFator).divide(quantidade);
			}
			else if (divideOuMultiplica.equalsIgnoreCase("D")) {
				qtdNeg = qtdNeg.divide(quantidade);
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


		DynamicVO licitacaoVO = (DynamicVO) dwf.findEntityByPrimaryKeyAsVO("AD_LICITACAO", codLic);

		ImpostosHelpper impostos = new ImpostosHelpper();
		impostos.setForcarRecalculo(true);
		impostos.calcularImpostos(licitacaoVO.asBigDecimalOrZero("NUNOTA"));

		salvarDados.insertComponentes(codLic, jdbcWrapper);

		jdbcWrapper.closeSession();
	}

}
