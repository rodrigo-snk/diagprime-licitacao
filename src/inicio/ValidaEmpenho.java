package inicio;

import java.math.BigDecimal;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class ValidaEmpenho implements EventoProgramavelJava {

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

		JdbcWrapper jdbcWrapper = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
		jdbcWrapper.openSession();

		DynamicVO dados = (DynamicVO) arg0.getVo();
		BigDecimal codProd = (dados.asBigDecimalOrZero("CODPROD"));
		BigDecimal qtdliberar = (dados.asBigDecimalOrZero("QTDLIBERAR"));
		BigDecimal adDisponivel = (dados.asBigDecimalOrZero("AD_DISPONIVEL"));
		BigDecimal diferenca = adDisponivel.subtract(qtdliberar);
        
		if((diferenca.intValue())<0) {
			throw new Exception("Quantidade digitada não pode ser maior que a disponivel ! Cód. Prod :"+codProd);
		}

		jdbcWrapper.closeSession();
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
