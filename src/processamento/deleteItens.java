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

public class deleteItens {

	public static void atualizarTotal(PersistenceEvent arg0) throws Exception {

		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();

		DynamicVO dados = (DynamicVO) arg0.getVo();
		BigDecimal codLic = dados.asBigDecimalOrZero("CODLIC");
		BigDecimal codIteLic = dados.asBigDecimalOrZero("CODITELIC");
		BigDecimal qtdNeg = dados.asBigDecimalOrZero("QTDE");
		BigDecimal vlrTot = dados.asBigDecimalOrZero("VLRTOTAL");
		BigDecimal vlrUnit = dados.asBigDecimalOrZero("VLRUNIT");
		BigDecimal codProd = dados.asBigDecimalOrZero("CODPROD");

		DynamicVO licitacaoVO = (DynamicVO) dwf.findEntityByPrimaryKeyAsVO("AD_LICITACAO", codLic);
		BigDecimal nuNota = licitacaoVO.asBigDecimal("NUNOTA");
		BigDecimal codEmp = licitacaoVO.asBigDecimal("CODEMP");

		String update = "DELETE FROM TGFITE WHERE NUNOTA="+nuNota+" AND AD_CODITELIC="+codIteLic+" and CODPROD="+codProd;
		PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(update);
	  	pstmt.executeUpdate();
	  		
		Impostos.recalculaImpostos(codLic);
  		
		jdbcWrapper.closeSession();
	}



}
