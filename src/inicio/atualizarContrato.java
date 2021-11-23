package inicio;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.PersistenceException;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import consultas.contratosCons;
import processamento.executarContrato;

public class atualizarContrato implements EventoProgramavelJava {

	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterInsert(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterUpdate(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
	
		
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
		boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);
		DynamicVO dados = (DynamicVO) arg0.getVo();
		String status = dados.asString("STATUSNOTA");
		BigDecimal codTipOper = dados.asBigDecimal("CODTIPOPER");
        
		/*if(true) {
			throw new PersistenceException("TESTE status "+status+" confirmando "+confirmando+" nomeEnty"+nomeEnty);
		}*/
		//if (arg0.getModifingFields().isModifing("STATUSNOTA")) {
			if(isConfirmandoNota){
				if(status.equalsIgnoreCase("A")){
					
					EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
					JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
					jdbcWrapper.openSession();
					
					String sql = contratosCons.retornaTopValida(codTipOper+"");
					PreparedStatement  updateValidando = jdbcWrapper.getPreparedStatement(sql);
			  		ResultSet consultaValidando = updateValidando.executeQuery();
						 
			  	    while(consultaValidando.next()) {
					System.out.println("PASSEI AQUI NOVO status"+status+" LEITE"+" confirmando");
					executarContrato.executarContratosConfirmar(arg0);
					System.out.println("PASSEI AQUI NOVO SAINDO"+status+" LEITE" +"confirmando");
			  	    }
				}
			}
		//}
	

	}

}
