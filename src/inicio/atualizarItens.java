package inicio;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import consultas.consultasDados;
import processamento.atualizarItens1;
import processamento.atualizarItens2;
import processamento.atualizarItens4;
import processamento.deleteItens4;
import processamento.gerarNotas;
import processamento.inserirItens;

public class atualizarItens implements EventoProgramavelJava {

	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {

		deleteItens4.atualizarTotal(arg0);
	}

	@Override
	public void afterInsert(PersistenceEvent arg0) throws Exception {

		inserirItens.atualizarCusto(arg0);
		EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = dwfFacade.getJdbcWrapper();
		jdbc.openSession();

		DynamicVO dados = (DynamicVO) arg0.getVo();
		BigDecimal codIteLic = dados.asBigDecimalOrZero("CODITELIC");
		BigDecimal codLic = dados.asBigDecimalOrZero("CODLIC");
		BigDecimal codProd = dados.asBigDecimalOrZero("CODPROD");
		String sql = consultasDados.retornaDadosItensProdutos(codProd+"");
		PreparedStatement updateValidando = jdbc.getPreparedStatement(sql);
  		ResultSet rset = updateValidando.executeQuery();
			 
  	    while(rset.next()) {
  	    	
  	    	String AD_DESCRITIVO = rset.getString("AD_DESCRITIVO");
  	    	String AD_PROCEDENCIA = rset.getString("AD_PROCEDENCIA");
  	    	String MARCA = rset.getString("MARCA");
  	    	String AD_NRREGISTRO = rset.getString("AD_NRREGISTRO");
  	    	
  	    	String update = "UPDATE AD_ITENSLICITACAO SET "
  	    			+ " DESCRITIVO_PRODUTO='"+AD_DESCRITIVO+"',"
  	    					+ "PROCEDENCIA='"+AD_PROCEDENCIA+"',"
  	    							+ " MARCA='"+MARCA+"',ANVISA='"+AD_NRREGISTRO+"' WHERE CODITELIC="+codIteLic+" AND "
  	    									+ " CODLIC="+codLic;
  	    	
  			PreparedStatement updateValidando1 = jdbc.getPreparedStatement(update);
  	  		updateValidando1.executeUpdate();

				jdbc.closeSession();
  	  		
  	    }

	}

	@Override
	public void afterUpdate(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		if (arg0.getModifingFields().isModifing("UNID")) {
			atualizarItens1.atualizarCustoVolume(arg0);
		}
		
		if (arg0.getModifingFields().isModifing("CUSTO") || arg0.getModifingFields().isModifing("MARKUPFATOR")) {
			atualizarItens2.atualizarCustoProduto(arg0);
		}
		
		if (arg0.getModifingFields().isModifing("QTDE")) {
			atualizarItens2.atualizarCustoProduto(arg0);
		}
		
		if (arg0.getModifingFields().isModifing("VLRUNIT")) {
			atualizarItens4.atualizarCustoProduto(arg0);
		}

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
