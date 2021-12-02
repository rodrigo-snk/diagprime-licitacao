package inicio;

import java.math.BigDecimal;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import processamento.Empenho;

public class LiberaEmpenho implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao arg0) throws Exception {
		BigDecimal contrato = (BigDecimal) arg0.getParam("NUMCONTRATO");
		String empenho = (String) arg0.getParam("EMPENHO");
		Empenho.liberarEmpenho(arg0, contrato, empenho);
		
		arg0.setMensagemRetorno("Empenho preparado agora vamos liberar!");
	}

}
