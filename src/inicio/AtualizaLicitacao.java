package inicio;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.BigDecimalUtil;
import helpper.*;

import java.math.BigDecimal;

public class AtualizaLicitacao implements EventoProgramavelJava {

	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {

		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();

		if (dwf.findEntityByPrimaryKey(DynamicEntityNames.CABECALHO_NOTA, arg0.getEntityProperty("NUNOTA")) != null){
			EntityFacadeFactory.getDWFFacade().removeEntity(DynamicEntityNames.CABECALHO_NOTA, new Object[]{arg0.getEntityProperty("NUNOTA")});
		}
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
		DynamicVO licitacaoVO = (DynamicVO) arg0.getVo();
		final boolean isModifyingParceiro = arg0.getModifingFields().isModifing("CODPARC");
		final boolean isModifingAny = !arg0.getModifingFields().isEmpty();
		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();

		if (isModifyingParceiro) {
			CabecalhoNota.atualizaParceiro(arg0.getEntityProperty("NUNOTA"), arg0.getEntityProperty("CODPARC"));
			Impostos.recalculaImpostos((BigDecimal) arg0.getEntityProperty("CODLIC"));
		}
		if (isModifingAny) {
			CabecalhoNota.atualizaCabecalhoNota(dwf, licitacaoVO);
		}

		Licitacao.atualizaImpostosFederais(licitacaoVO);
		CabecalhoNota.setPendente(licitacaoVO.asBigDecimalOrZero("NUNOTA"), true);
	}

	@Override
	public void beforeCommit(TransactionContext arg0) throws Exception {
		// TODO Auto-generated method stub


	}

	@Override
	public void beforeDelete(PersistenceEvent arg0) throws Exception {

		EntityFacadeFactory.getDWFFacade().removeByCriteria(new FinderWrapper("AD_ITENSLICITACAO", "this.CODLIC = ?", arg0.getEntityProperty("CODLIC")));
		EntityFacadeFactory.getDWFFacade().removeByCriteria(new FinderWrapper("AD_LICITACAOCOMPONENTES", "this.CODLIC = ?", arg0.getEntityProperty("CODLIC")));
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
