package processamento;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.TimeUtils;
import consultas.consultasDados;
import save.salvarDados;
import util.login;

public class gerarNotas {

	
	public static void inserirNota(PersistenceEvent arg0) throws Exception {
		
		DynamicVO licitacaoVO = (DynamicVO) arg0.getVo();
		BigDecimal codParc = licitacaoVO.asBigDecimalOrZero("CODPARC");
		BigDecimal codLic =  licitacaoVO.asBigDecimalOrZero("CODLIC");
		BigDecimal codEmp = licitacaoVO.asBigDecimalOrZero("CODEMP");
		BigDecimal codNat = licitacaoVO.asBigDecimalOrZero("CODNAT");
		BigDecimal codCencus = licitacaoVO.asBigDecimalOrZero("CODCENCUS");
		BigDecimal codTipVenda = licitacaoVO.asBigDecimalOrZero("CODTIPVENDA");
	
		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();

		final String sql = consultasDados.retornoDados();
		PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(sql);
		ResultSet rs = pstmt.executeQuery();
		while (rs.next()) {
		
			String url = rs.getString("URL");
			String login11 = rs.getString("LOGIN");
			String senha = rs.getString("SENHA");
			BigDecimal codTipOper = rs.getBigDecimal("TOP");

			BigDecimal nuNota = salvarDados.salvarCabecalhoDados(
					dwf, 
					codEmp, 
					codParc, 
					codTipOper, 
					codTipVenda, 
					codNat, 
					codCencus, 
					BigDecimal.ZERO,
					TimeUtils.getNow(),
					codLic);


			//String chave = login.loginNovo1(url,login11,senha);
			//Retorno1 ret = incluirNota.IncluirNota(url, codParc+"", codEmp+"", DTNEG, codTipVenda+"", chave,codTipOper);
			//nuNota = ret.getNunota();

			licitacaoVO.setProperty("NUNOTA", nuNota);
			dwf.saveEntity("AD_LICITACAO", (EntityVO) licitacaoVO);

		}

		jdbcWrapper.closeSession();

	}

	
	

	
}
