package inicio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.ConfirmacaoNotaHelper;
import br.com.sankhya.modelcore.comercial.CentralFaturamento.ConfiguracaoFaturamento;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.ws.ServiceContext;

public class confirmarNota implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao arg0) throws Exception {

		Registro[] linhas = arg0.getLinhas();
		
		String nuNota = "";
		//EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
	/*	JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();*/
		SessionHandle hnd = null;
		String chave = "";
		for (int i = 0; i < linhas.length; i++) {
			
			nuNota += linhas[i].getCampo("NUNOTA");

			Collection<BigDecimal> numNota = new ArrayList<>();
			Map<BigDecimal, BigDecimal> mapas = null;
			numNota.add(new BigDecimal(nuNota));
			
				BarramentoRegra regra = BarramentoRegra.build(CACHelper.class, "regrasConfirmacaoCAC.xml", AuthenticationInfo.getCurrent());
				ConfirmacaoNotaHelper.confirmarNota(new BigDecimal(nuNota), regra, true);
				ConfiguracaoFaturamento fat = new ConfiguracaoFaturamento();
				fat.setFaturamentoNormal(true);
				fat.setCodTipOper(new BigDecimal("1007"));
				
				ServiceContext textoNovo = ServiceContext.getCurrent();
				chave = textoNovo.getHttpSessionId();
				//hnd = JapeSession.getCurrentSession().getTopMostHandle();
				//FaturamentoHelper.faturar(textoNovo, hnd, fat, numNota, mapas);
				//JapeSession.close(hnd);
				
		}
		arg0.setMensagemRetorno("Licita��o Aprovada !"+chave);
		
	}

}
