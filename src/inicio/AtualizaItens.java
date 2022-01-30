package inicio;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import consultas.consultasDados;
import helpper.*;

import static helpper.Licitacao.excluiReferencias;

public class AtualizaItens implements EventoProgramavelJava {

	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {
		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = dwf.getJdbcWrapper();
		jdbc.openSession();

		ItensLicitacao.atualizaTotal(arg0);

		ItemNota.insereAcessorios((BigDecimal) arg0.getEntityProperty("CODLIC"), jdbc);

		DynamicVO licitacaoVO = (DynamicVO) dwf.findEntityByPrimaryKeyAsVO("AD_LICITACAO", arg0.getEntityProperty("CODLIC"));
		Licitacao.atualizaImpostosFederais(licitacaoVO);

		jdbc.closeSession();

	}

	@Override
	public void afterInsert(PersistenceEvent arg0) throws Exception {

		ItensLicitacao.insereItem(arg0);
		EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = dwfFacade.getJdbcWrapper();
		jdbc.openSession();

		DynamicVO dados = (DynamicVO) arg0.getVo();
		BigDecimal codIteLic = dados.asBigDecimalOrZero("CODITELIC");
		BigDecimal codLic = dados.asBigDecimalOrZero("CODLIC");
		BigDecimal codProd = dados.asBigDecimalOrZero("CODPROD");
		final String sql = consultasDados.retornaDadosItensProdutos(codProd.toString());
		PreparedStatement pstmt = jdbc.getPreparedStatement(sql);
  		ResultSet rs = pstmt.executeQuery();
			 
  	    while(rs.next()) {
  	    	final String AD_DESCRITIVO = rs.getString("AD_DESCRITIVO");
			final String AD_PROCEDENCIA = rs.getString("AD_PROCEDENCIA");
			final String MARCA = rs.getString("MARCA");
			final String AD_NRREGISTRO = rs.getString("AD_NRREGISTRO");
  	    	
  	    	final String updateItens = "UPDATE AD_ITENSLICITACAO SET "
							+ " DESCRITIVO_PRODUTO='"+AD_DESCRITIVO+"',"
							+ "PROCEDENCIA='"+AD_PROCEDENCIA+"',"
							+ " MARCA='"+MARCA+"',ANVISA='"+AD_NRREGISTRO+"' WHERE CODITELIC="+codIteLic+" AND "
							+ " CODLIC="+codLic;
  			pstmt = jdbc.getPreparedStatement(updateItens);
  	  		pstmt.executeUpdate();
		}
		jdbc.closeSession();
	}

	@Override
	public void afterUpdate(PersistenceEvent arg0) throws Exception {
		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = dwf.getJdbcWrapper();
		jdbc.openSession();

		if (!dwf.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.ITEM_COMPOSICAO_PRODUTO, "this.CODPROD = ?", arg0.getEntityProperty("CODPROD"))).isEmpty()) {
			ItemNota.insereAcessorios((BigDecimal) arg0.getEntityProperty("CODLIC"), jdbc);
		}

		Impostos.recalculaImpostos((BigDecimal) arg0.getEntityProperty("CODLIC"));
		DynamicVO licitacaoVO = (DynamicVO) dwf.findEntityByPrimaryKeyAsVO("AD_LICITACAO", arg0.getEntityProperty("CODLIC"));
		Licitacao.atualizaImpostosFederais(licitacaoVO);

		jdbc.closeSession();

	}

	@Override
	public void beforeCommit(TransactionContext arg0) throws Exception {

	}

	@Override
	public void beforeDelete(PersistenceEvent arg0) throws Exception {
		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = dwf.getJdbcWrapper();
		jdbc.openSession();

		DynamicVO licitacaoVO = (DynamicVO) dwf.findEntityByPrimaryKeyAsVO("AD_LICITACAO", arg0.getEntityProperty("CODLIC"));

		// Deleta as referencias da pré-proposta
		excluiReferencias(jdbc, licitacaoVO.asBigDecimalOrZero("NUNOTA"));

		jdbc.closeSession();
	}



	@Override
	public void beforeInsert(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeUpdate(PersistenceEvent arg0) throws Exception {

		if (arg0.getModifingFields().isModifing("QTDE")) {
			ItensLicitacao.atualizaQtdNeg(arg0);
		}

		if (arg0.getModifingFields().isModifing("CUSTO") || arg0.getModifingFields().isModifing("MARKUPFATOR")) {
			ItensLicitacao.atualizaCusto(arg0);
		}

		if (arg0.getModifingFields().isModifing("VLRUNIT")) {
			ItensLicitacao.atualizaVlrUnit(arg0);
		}

		if (arg0.getModifingFields().isModifing("UNID")) {
			ItensLicitacao.atualizaCustoVolume(arg0);
		}


	}


}
