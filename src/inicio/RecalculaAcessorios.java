package inicio;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import save.salvarDados;

public class RecalculaAcessorios implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao arg0) throws Exception {
		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = dwf.getJdbcWrapper();
		jdbc.openSession();
		
		Registro[] registros = arg0.getLinhas();
		//int usuario = Integer.parseInt(""+arg0.getUsuarioLogado());
		BigDecimal codLic = (BigDecimal) registros[0].getCampo("CODLIC");

		salvarDados.insereAcessorios(codLic, jdbc);
		jdbc.closeSession();
		arg0.setMensagemRetorno("Recalculado com sucesso");
	}

}
