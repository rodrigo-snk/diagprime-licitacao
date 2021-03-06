package inicio;

import java.math.BigDecimal;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.ConfirmacaoNotaHelper;
import br.com.sankhya.modelcore.comercial.CentralFaturamento.ConfiguracaoFaturamento;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.dwfdata.vo.CabecalhoNotaVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;
import helpper.Impostos;
import helpper.Licitacao;

public class ConfirmaNota implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao arg0) throws Exception {

		Registro[] linhas = arg0.getLinhas();
		
		BigDecimal nuNota;
		String msg = "";
		//EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
	/*	JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();
		SessionHandle hnd = null;*/
		String chave = "";
		for (Registro linha : linhas) {

			nuNota = (BigDecimal) linha.getCampo("NUNOTA");
			BigDecimal codLic = (BigDecimal) linha.getCampo("CODLIC");
			Impostos.recalculaImpostos(codLic);
			DynamicVO licitacaoVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO("AD_LICITACAO", codLic);
			Licitacao.atualizaImpostosFederais(licitacaoVO);

			/*Collection<BigDecimal> numNota = new ArrayList<>();
			Map<BigDecimal, BigDecimal> mapas = null;
			numNota.add(new BigDecimal(nuNota));*/

			CabecalhoNotaVO cabVO = (CabecalhoNotaVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota, CabecalhoNotaVO.class);
			if (!cabVO.getSTATUSNOTA().equalsIgnoreCase("L")) {

				BarramentoRegra regra = BarramentoRegra.build(CACHelper.class, "regrasConfirmacaoCAC.xml", AuthenticationInfo.getCurrent());
				ConfirmacaoNotaHelper.confirmarNota(nuNota, regra, true);
				ConfiguracaoFaturamento fat = new ConfiguracaoFaturamento();
				fat.setFaturamentoNormal(true);
				fat.setCodTipOper(new BigDecimal("1007"));

				ServiceContext textoNovo = ServiceContext.getCurrent();
				chave = textoNovo.getHttpSessionId();

				msg = "Licita????o Aprovada!";

			} else {

				Impostos.recalculaImpostos((BigDecimal) linha.getCampo("CODLIC"));
				msg = "Impostos recalculados";
			}


			//hnd = JapeSession.getCurrentSession().getTopMostHandle();
			//FaturamentoHelper.faturar(textoNovo, hnd, fat, numNota, mapas);
			//JapeSession.close(hnd);

		}


		arg0.setMensagemRetorno(msg);
		
	}

}
