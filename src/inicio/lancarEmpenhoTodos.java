package inicio;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import consultas.consultasDados;
import processamento.empenhoFuncionalidades;
import save.salvarDados;
import save.salvarDadosEmpenho;

public class lancarEmpenhoTodos implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao arg0) throws Exception {
		// TODO Auto-generated method stub
		
		//String qtd = ""+arg0.getParam("QUANTIDADE");
		String empenho = ""+arg0.getParam("EMPENHO");

		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();
		
		Registro[] linhas = arg0.getLinhas();
        String qtdDisponivel = "0";
        String numContrato = "0";
        String codProd = "0";
        String qtd = "0";
       // String empenho = "";
        
		for (Integer i = 0; i < linhas.length; i++) {
			qtdDisponivel = "" + linhas[i].getCampo("QTDDISPONIVEL");
			numContrato = "" + linhas[i].getCampo("NUMCONTRATO");
			codProd = "" + linhas[i].getCampo("CODPROD");
			qtd = "" + linhas[i].getCampo("QTDLIBERAR");
			//empenho = "" + linhas[i].getCampo("EMPENHO");
			
			if(qtdDisponivel.equalsIgnoreCase("null")) {
				qtdDisponivel = "0";
			}
			//arg0.mostraErro(" qtdDisponivel "+qtdDisponivel+" - "+qtd);

		//arg0.mostraErro(qtdDisponivel+" - "+qtd);

	
			
			String consulta = consultasDados.retornoValidaEmpenho();
			PreparedStatement  updateValidando = jdbcWrapper.getPreparedStatement(consulta);
	  		ResultSet consultaValidando = updateValidando.executeQuery();
 			 
	  	    if(consultaValidando.next()) {
	  	    	
	  	 
		
	  	    		empenhoFuncionalidades.liberarEmpenhoTodos(arg0, new BigDecimal(numContrato), empenho);
		 
	  	    	/*EntityFacade dwf,
	    		ContextoAcao arg0,
	    		BigDecimal codEmp,
	    		BigDecimal codParc,
	    		BigDecimal codTipOper,
	    		BigDecimal codTipVenda,
	    		BigDecimal codNat, 
	    		BigDecimal codCencus,
	    		BigDecimal codProj,
	    		BigDecimal vlrNota*/
	    		
	  	    	}else {
	  	    	
	  	    	arg0.mostraErro("Usuário não tem permissão");
	  	    }
			
		
		}
		arg0.setMensagemRetorno("Empenho liberado Total com sucesso");

	}

}
