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
		
		DynamicVO cabVO = (DynamicVO) arg0.getVo();
		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();
		
		BigDecimal nuNota = cabVO.asBigDecimalOrZero("NUNOTA");
		BigDecimal numContrato1 = cabVO.asBigDecimalOrZero("NUMCONTRATO");
		BigDecimal codEmp = cabVO.asBigDecimalOrZero("CODEMP");
		BigDecimal codNat = cabVO.asBigDecimalOrZero("CODNAT");
		BigDecimal codParc = cabVO.asBigDecimalOrZero("CODPARC");
		BigDecimal codContato = cabVO.asBigDecimalOrZero("CODCONTATO");
		BigDecimal codTipVenda = cabVO.asBigDecimalOrZero("CODTIPVENDA");
		BigDecimal codTipOper = cabVO.asBigDecimalOrZero("CODTIPOPER");
		BigDecimal AD_CODTIPOPERDESTINO = new BigDecimal(0);
		
		final String consultaTopDest = contratosCons.retornaTop(codTipOper.toString());
		PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(consultaTopDest);
  		ResultSet rs = pstmt.executeQuery();
			 
  	    while(rs.next()) {
  	    	AD_CODTIPOPERDESTINO = rs.getBigDecimal("AD_CODTIPOPERDESTINO");
  	    }
  	    if(AD_CODTIPOPERDESTINO.intValue()>0) {
		if(numContrato1.intValue()>0) {
			
			gerarOrcamento.atualizandoContrato(nuNota, numContrato1, codEmp, codNat);

		} else {
			
		BigDecimal numContrato = contratos.salvarContrato(
				dwf, 
				codEmp,
				codParc,
				codContato,
				codTipVenda,
				codNat,
				AD_CODTIPOPERDESTINO,
				nuNota);
		

		final String sql = contratosCons.buscarDadosItensContrato(nuNota.toString());
		pstmt = jdbcWrapper.getPreparedStatement(sql);
		rs = pstmt.executeQuery();

		  	while (rs.next()) {
		  		BigDecimal codProd = rs.getBigDecimal("CODPROD");
		  		BigDecimal qtdNeg = rs.getBigDecimal("QTDNEG");
		  		BigDecimal vlrUnit = rs.getBigDecimal("VLRUNIT");
		  		String codVol = rs.getString("CODVOL");

				contratos.salvarContratoItens(dwf, numContrato, codProd, qtdNeg, vlrUnit);
		  		
				final String insertValidando = "INSERT INTO LOG_UPDATE_CONTRATO(NUNOTA,CODPROD,QTD) VALUES ('"+nuNota+"','"+codProd+"','"+qtdNeg+"')";
		  	 	pstmt = jdbcWrapper.getPreparedStatement(insertValidando);
		  	 	pstmt.executeUpdate();
		  	 	
	  			salvarDadosEmpenho.gerarEmpenho(
	  					dwf, 
	  					codProd,
						codVol,
	  					numContrato, 
	  					codParc,
	  					qtdNeg,
	  					qtdNeg,
	  					"", 
	  					qtdNeg
	  					);
		  		}
		  	
			final String sql1 = contratosCons.buscarDadosItensContratoComponentes(nuNota.toString());
			pstmt = jdbcWrapper.getPreparedStatement(sql1);
			rs = pstmt.executeQuery();

			  	while (rs.next()) {
			  		String codProd = rs.getString("CODPROD");
			  		String qtdNeg = rs.getString("QTDNEG");
			  		String vlrUnit = rs.getString("VLRUNIT");
					  // Componentes já estao no Contrato
			  		/*contratos.salvarContratoItens(
					dwf, 
					numContrato+"",
					codProd, 
					qtdNeg, 
					vlrUnit);*/
			  		
					String insertValidando = "INSERT INTO LOG_UPDATE_CONTRATO(NUNOTA,CODPROD,QTD) VALUES ('"+nuNota+"','"+codProd+"','"+qtdNeg+"')";
			  	 	pstmt = jdbcWrapper.getPreparedStatement(insertValidando);
			  	 	pstmt.executeUpdate();

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
		  	
			final String sql2 = "UPDATE TGFCAB SET NUMCONTRATO = "+numContrato+" where NUNOTA = "+nuNota;
	  	 	pstmt = jdbcWrapper.getPreparedStatement(sql);
	  	 	pstmt.executeUpdate();
	
		
		}
  	    }else{
  	    	throw new PersistenceException("TOP "+codTipOper+" Está sem a Top Destino vinculada o campo é :"+AD_CODTIPOPERDESTINO+" por favor preencha e tente novamente !");
  	    }

  	 	
		jdbcWrapper.closeSession();
	}
	

}
