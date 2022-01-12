package inicio;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.helper.CalculoPrecosCustosHelper;
import com.sankhya.util.BigDecimalUtil;
import processamento.*;

import java.math.BigDecimal;

public class AtualizaLicitacao implements EventoProgramavelJava {

	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterInsert(PersistenceEvent arg0) throws Exception {
		DynamicVO licitacaoVO = (DynamicVO) arg0.getVo();
		if (BigDecimalUtil.isNullOrZero(licitacaoVO.asBigDecimalOrZero("CODCONTATO"))) {
			licitacaoVO.setProperty("CODCONTATO", Licitacao.getContatoPregoeiro(licitacaoVO.asBigDecimalOrZero("CODPARC")));
		}
		CabecalhoNota.insereNota(licitacaoVO);
		Licitacao.atualizaImpostosFederais(licitacaoVO);


	}

	@Override
	public void afterUpdate(PersistenceEvent arg0) throws Exception {
		boolean isModifyingParceiro = arg0.getModifingFields().isModifing("CODPARC");
		if (isModifyingParceiro) {
			CabecalhoNota.atualizaParceiro(arg0.getEntityProperty("NUNOTA"), arg0.getEntityProperty("CODPARC"));
			Impostos.recalculaImpostos((BigDecimal) arg0.getEntityProperty("CODLIC"));
		}
		DynamicVO licitacaoVO = (DynamicVO) arg0.getVo();
		Licitacao.atualizaImpostosFederais(licitacaoVO);

		CabecalhoNota.setPendente(licitacaoVO.asBigDecimalOrZero("NUNOTA"), true);
	}

	@Override
	public void beforeCommit(TransactionContext arg0) throws Exception {
		// TODO Auto-generated method stub


	}

	@Override
	public void beforeDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeInsert(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeUpdate(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub

	}

}
