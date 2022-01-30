package inicio;

import java.math.BigDecimal;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import helpper.ItensLicitacao;

public class RecalculaCustoItem implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao arg0) throws Exception {

		Registro[] registros = arg0.getLinhas();
		Integer usuario = Integer.parseInt(""+arg0.getUsuarioLogado());

		for (Registro registro : registros) {

			BigDecimal codLic = (BigDecimal) registro.getCampo("CODLIC");
			BigDecimal codIteLic = (BigDecimal) registro.getCampo("CODITELIC");
			BigDecimal codProd = (BigDecimal) registro.getCampo("CODPROD");
			BigDecimal qtde = (BigDecimal) registro.getCampo("QTDE");
			BigDecimal vlrTotal = (BigDecimal) registro.getCampo("VLRTOTAL");
			BigDecimal vlrUnit = (BigDecimal) registro.getCampo("VLRUNIT");
			BigDecimal markUpFator = (BigDecimal) registro.getCampo("MARKUPFATOR");
			String codVol = (String) registro.getCampo("UNID");

			ItensLicitacao.recalculaCusto(
					codIteLic,
					codLic,
					codProd,
					qtde,
					vlrTotal,
					vlrUnit,
					markUpFator,
					codVol);
		}
		arg0.setMensagemRetorno("Recalculado com sucesso");
	}

}
