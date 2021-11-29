package inicio;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.omg.PortableInterceptor.ORBInitializerOperations;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import consultas.consultasEmpenho;
import processamento.empenhoFuncionalidades;
import save.salvarDadosEmpenho;

public class liberarEmpenho implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao arg0) throws Exception {
		BigDecimal contrato = (BigDecimal) arg0.getParam("NUMCONTRATO");
		String empenho = (String) arg0.getParam("EMPENHO");
		empenhoFuncionalidades.liberarEmpenho(arg0, contrato, empenho);
		
		arg0.setMensagemRetorno("Empenho preparado agora vamos liberar!");
	}

}
