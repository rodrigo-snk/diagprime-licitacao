package inicio;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import consultas.consultasDados;
import processamento.Empenho;

public class LancaEmpenhoTodos implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao arg0) throws Exception {
		//String qtd = ""+arg0.getParam("QUANTIDADE");
		String empenho = (String) arg0.getParam("EMPENHO");

		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();
		
		Registro[] linhas = arg0.getLinhas();
        BigDecimal qtdDisponivel;
        BigDecimal numContrato;
        BigDecimal codProd = BigDecimal.ZERO;
        BigDecimal qtd = BigDecimal.ZERO;

		for (Registro linha : linhas) {
			qtdDisponivel = (BigDecimal) linha.getCampo("QTDDISPONIVEL");
			numContrato = (BigDecimal) linha.getCampo("NUMCONTRATO");
			codProd = (BigDecimal) linha.getCampo("CODPROD");
			qtd = (BigDecimal) linha.getCampo("QTDLIBERAR");
			//empenho = (BigDecimal) linha.getCampo("EMPENHO");

			if (qtdDisponivel == null) {
				qtdDisponivel = BigDecimal.ZERO;
			}
			//arg0.mostraErro("qtdDisponivel " +qtdDisponivel+ " - " +qtd);

			//arg0.mostraErro(qtdDisponivel +" - " +qtd);

			String consulta = consultasDados.validaEmpenho();
			PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(consulta);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				Empenho.liberarEmpenhoTodos(arg0, numContrato, empenho);
		 
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

			} else {

				arg0.mostraErro("Usuário não tem permissão");
			}

		}
		arg0.setMensagemRetorno("Empenho liberado Total com sucesso");

	}

}
