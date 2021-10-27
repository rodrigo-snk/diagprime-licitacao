package inicio;

import java.math.BigDecimal;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import processamento.recalcularItens;

public class recalculoCustoAction implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao arg0) throws Exception {

		Registro[] registros = arg0.getLinhas();
		Integer usuario = Integer.parseInt(""+arg0.getUsuarioLogado());

		for (Registro registro : registros) {

			BigDecimal CODLIC = (BigDecimal) registro.getCampo("CODLIC");
			BigDecimal CODITELIC = (BigDecimal) registro.getCampo("CODITELIC");
			BigDecimal CODPROD = (BigDecimal) registro.getCampo("CODPROD");
			BigDecimal QTDE = (BigDecimal) registro.getCampo("QTDE");
			BigDecimal VLRTOTAL = (BigDecimal) registro.getCampo("VLRTOTAL");
			BigDecimal VLRUNIT = (BigDecimal) registro.getCampo("VLRUNIT");
			BigDecimal MARKUPFATOR = (BigDecimal) registro.getCampo("MARKUPFATOR");
			String CODVOL = "" + registro.getCampo("UNID");

			recalcularItens.atualizarCusto(
					CODITELIC,
					CODLIC,
					CODPROD,
					QTDE,
					VLRTOTAL,
					VLRUNIT,
					MARKUPFATOR,
					CODVOL);
		}
		arg0.setMensagemRetorno("Recalculado com sucesso");
	}

}
