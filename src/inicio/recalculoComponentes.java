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
import processamento.recalcularItens;
import save.salvarDados;

public class recalculoComponentes implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao arg0) throws Exception {
		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();
		
		Registro[] registros = arg0.getLinhas();
		Integer usuario = Integer.parseInt(""+arg0.getUsuarioLogado());

		for (Registro registro : registros) {

			BigDecimal CODLIC = (BigDecimal) registro.getCampo("CODLIC");

			String sql = "select codemp,nunota,codlic from ad_licitacao  where codlic=" + CODLIC;
			PreparedStatement consulta = jdbcWrapper.getPreparedStatement(sql);
			ResultSet rset = consulta.executeQuery();

			while (rset.next()) {

				BigDecimal nuNota = rset.getBigDecimal("NUNOTA");
				BigDecimal codEmp = rset.getBigDecimal("CODEMP");

				ImpostosHelpper impostos = new ImpostosHelpper();
				impostos.setForcarRecalculo(true);
				impostos.calcularImpostos(nuNota);
			}

			salvarDados.insertComponentes(CODLIC, jdbcWrapper);

		}
		jdbcWrapper.openSession();
		arg0.setMensagemRetorno("Recalculado com sucesso");
	}

}
