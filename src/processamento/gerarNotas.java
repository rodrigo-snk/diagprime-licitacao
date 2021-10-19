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
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import consultas.consultasDados;
import save.salvarDados;
import util.login;

public class gerarNotas {

	
	public static void inserirNota(PersistenceEvent arg0) throws Exception {
		
		BigDecimal nuNota = BigDecimal.ZERO;
		DynamicVO financeiro = (DynamicVO) arg0.getVo();
		BigDecimal codParc = financeiro.asBigDecimalOrZero("CODPARC");
		BigDecimal codLic =  financeiro.asBigDecimalOrZero("CODLIC");
		BigDecimal codEmp = financeiro.asBigDecimalOrZero("CODEMP");
		BigDecimal codNat = financeiro.asBigDecimalOrZero("CODNAT");
		BigDecimal codCencus = financeiro.asBigDecimalOrZero("CODCENCUS");
		BigDecimal codTipVenda = financeiro.asBigDecimalOrZero("CODTIPVENDA");
	
		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		
			jdbcWrapper.openSession();

		
		Date data1 = new Date();
		SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy");
		String dtNeg = formatador.format(data1);
		
		
		String consulta = consultasDados.retornoDados();
		PreparedStatement  consultaPar = jdbcWrapper.getPreparedStatement(consulta);
		ResultSet retornoParametros = consultaPar.executeQuery();
		while (retornoParametros.next()) {
		
			String url = retornoParametros.getString("URL");
			String login11 = retornoParametros.getString("LOGIN");
			String senha = retornoParametros.getString("SENHA");
			BigDecimal codTipOper = retornoParametros.getBigDecimal("TOP");

			nuNota = salvarDados.salvarCabecalhoDados(
					dwf, 
					codEmp, 
					codParc, 
					codTipOper, 
					codTipVenda, 
					codNat, 
					codCencus, 
					BigDecimal.ZERO, 
					dtNeg,
					codLic);
			

		    //String chave = login.loginNovo1(url,login11,senha);
			//Retorno1 ret = incluirNota.IncluirNota(url, codParc+"", codEmp+"", DTNEG, codTipVenda+"", chave,codTipOper);
			//nuNota = ret.getNunota();
			
			String update = "UPDATE AD_LICITACAO SET NUNOTA="+nuNota+" where CODLIC="+codLic;
			PreparedStatement  updatePar = jdbcWrapper.getPreparedStatement(update);
			updatePar.executeUpdate();
		
		}
		jdbcWrapper.closeSession();

	}

	
	

	
}
