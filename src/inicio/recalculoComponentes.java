package inicio;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import save.salvarDados;

public class recalculoComponentes implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao arg0) throws Exception {
		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = dwf.getJdbcWrapper();
		jdbc.openSession();
		
		Registro[] registros = arg0.getLinhas();
		//int usuario = Integer.parseInt(""+arg0.getUsuarioLogado());
		BigDecimal codLic = (BigDecimal) registros[0].getCampo("CODLIC");

		String sql = "select * from ad_licitacao  where codlic=" + codLic;
		PreparedStatement consulta = jdbc.getPreparedStatement(sql);
		ResultSet rset = consulta.executeQuery();

		while (rset.next()) {

			BigDecimal nuNota = rset.getBigDecimal("NUNOTA");
			ImpostosHelpper impostos = new ImpostosHelpper();
			impostos.setForcarRecalculo(true);
			impostos.calcularImpostos(nuNota);
		}

		salvarDados.insertComponentes(codLic, jdbc);
		jdbc.closeSession();
		arg0.setMensagemRetorno("Recalculado com sucesso");
	}

}
