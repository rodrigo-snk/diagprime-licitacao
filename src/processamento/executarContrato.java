package processamento;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.PersistenceException;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import consultas.contratosCons;
import save.contratos;
import save.salvarDadosEmpenho;


public class executarContrato {
	
	
	public static void executarContratosConfirmar(PersistenceEvent arg0) throws Exception {
		
		DynamicVO dados = (DynamicVO) arg0.getVo();
		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();
		
		BigDecimal nuNota = (dados.asBigDecimalOrZero("NUNOTA"));
		BigDecimal numContrato1 = (dados.asBigDecimalOrZero("NUMCONTRATO"));
		Integer codEmp = (dados.asInt("CODEMP"));
		Integer codNat = (dados.asInt("CODNAT"));
		Integer codParc = (dados.asInt("CODPARC"));
		Integer codContato = (dados.asInt("CODCONTATO"));
		Integer codTipVenda = (dados.asInt("CODTIPVENDA"));
		BigDecimal codTipOper = (dados.asBigDecimalOrZero("CODTIPOPER"));
		BigDecimal AD_CODTIPOPERDESTINO = new BigDecimal(0);
		
		String consultaTopDest = contratosCons.retornaTop(codTipOper+"");
		PreparedStatement  updateValidando = jdbcWrapper.getPreparedStatement(consultaTopDest);
  		ResultSet consultaValidando = updateValidando.executeQuery();
			 
  	    while(consultaValidando.next()) {
  	    	AD_CODTIPOPERDESTINO = consultaValidando.getBigDecimal("AD_CODTIPOPERDESTINO");
  	    }
  	    if(AD_CODTIPOPERDESTINO.intValue()>0) {
		if(numContrato1.intValue()>0) {
			
			gerarOrcamento.atualizandoContrato(nuNota, numContrato1, codEmp, codNat);

		}else{
			
		BigDecimal numContrato = contratos.salvarContrato(
				dwf, 
				codEmp+"",
				codParc+"",
				codContato+"",
				codTipVenda+"",
				codNat+"",
				AD_CODTIPOPERDESTINO+"",
				nuNota+"");
		

		String sql = contratosCons.buscarDadosItensContrato(nuNota+"");
		PreparedStatement  selectConta = jdbcWrapper.getPreparedStatement(sql);
		ResultSet consulta = selectConta.executeQuery();

		  	while (consulta.next()) {
		  		
		  		String codProd = consulta.getString("CODPROD");
		  		String qtdNeg = consulta.getString("QTDNEG");
		  		String vlrUnit = consulta.getString("VLRUNIT");
		  		
		  		contratos.salvarContratoItens(
				dwf, 
				numContrato+"",
				codProd, 
				qtdNeg, 
				vlrUnit);
		  		
				String insertValidando = "INSERT INTO LOG_UPDATE_CONTRATO(NUNOTA,CODPROD,QTD) VALUES ('"+nuNota+"','"+codProd+"','"+qtdNeg+"')";
		  	 	PreparedStatement  insertValidando1 = jdbcWrapper.getPreparedStatement(insertValidando);
		  	 	insertValidando1.executeUpdate();
		  	 	
	  			salvarDadosEmpenho.gerarEmpenho(
	  					dwf, 
	  					new BigDecimal(codProd+""), 
	  					numContrato, 
	  					new BigDecimal(codParc+""),
	  					new BigDecimal(qtdNeg), 
	  					new BigDecimal(qtdNeg), 
	  					"", 
	  					new BigDecimal(qtdNeg)
	  					);
		  	 	
		  		}
		  	
			String sql1 = contratosCons.buscarDadosItensContratoComponentes(nuNota+"");
			PreparedStatement selectConta1 = jdbcWrapper.getPreparedStatement(sql1);
			ResultSet consulta1 = selectConta1.executeQuery();

			  	while (consulta1.next()) {
			  		
			  		String codProd = consulta1.getString("CODPROD");
			  		String qtdNeg = consulta1.getString("QTDNEG");
			  		String vlrUnit = consulta1.getString("VLRUNIT");
					  // Componentes já estao no Contrato
			  		/*contratos.salvarContratoItens(
					dwf, 
					numContrato+"",
					codProd, 
					qtdNeg, 
					vlrUnit);*/
			  		
					String insertValidando = "INSERT INTO LOG_UPDATE_CONTRATO(NUNOTA,CODPROD,QTD) VALUES ('"+nuNota+"','"+codProd+"','"+qtdNeg+"')";
			  	 	PreparedStatement insertValidando1 = jdbcWrapper.getPreparedStatement(insertValidando);
			  	 	insertValidando1.executeUpdate();

					// Componentes já estao no Contrato
		  			/*salvarDadosEmpenho.gerarEmpenho(
		  					dwf, 
		  					new BigDecimal(codProd+""), 
		  					numContrato, 
		  					new BigDecimal(codParc+""),
		  					new BigDecimal(qtdNeg), 
		  					new BigDecimal(qtdNeg), 
		  					"", 
		  					new BigDecimal(qtdNeg)
		  					);*/
			  	 	
			  		}
		  	
			String insertValidando = "UPDATE TGFCAB SET NUMCONTRATO = "+numContrato+" where nunota = "+nuNota;
	  	 	PreparedStatement insertValidando1 = jdbcWrapper.getPreparedStatement(insertValidando);
	  	 	insertValidando1.executeUpdate();
	
		
		}
  	    }else{
  	    	throw new PersistenceException("TOP "+codTipOper+" Está sem a Top Destino vinculada o campo é :"+AD_CODTIPOPERDESTINO+" por favor preencha e tente novamente !");
  	    }

  	 	
		jdbcWrapper.closeSession();
	}
	

}
