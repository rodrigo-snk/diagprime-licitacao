package processamento;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import save.salvarDadosEmpenho;

public class gerarOrcamento {

	public static void atualizandoContrato(BigDecimal nuNota, BigDecimal numContrato, BigDecimal codEmp,BigDecimal codNat) throws Exception {
		
		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();
	
		if(numContrato.intValue()>0) {
		String selectItens = "select cab.CODPARC,AD_QTDLIBERAR,\r\n"
				+ "CAB.NUMCONTRATO AS P_CONTRATO,\r\n"
				+ "ITE.CODPROD, ITE.CODVOL\r\n"
				+ "coalesce(itecontrato.CODPROD,0) AS CODPROD_CONTRATO,\r\n"
				+ "'A' AS SITPROD,\r\n"
				+ "ite.QTDNEG+COALESCE(QTDEPREVISTA,0) AS QTDEPREVISTA_NOVO,\r\n"
				+ "ite.VLRUNIT,\r\n"
				+ "ite.QTDNEG,\r\n"
				+ "COALESCE(QTDEPREVISTA,0) AS  QTDEPREVISTA\r\n"
				+ "from\r\n"
				+ "			tgfcab cab inner join \r\n"
				+ "			tgfite ite on ite.nunota = cab.nunota and ite.codemp = cab.codemp left join\r\n"
				+ "			TCSPSC itecontrato on itecontrato.codprod = ite.codprod and cab.numcontrato = itecontrato.numcontrato  LEFT JOIN\r\n"
				+ "			(select count(NUNOTA) as total,numcontrato from TGFCAB group by numcontrato)novo  on novo.numcontrato = cab.numcontrato \r\n"
				+ "			where cab.nunota = "+nuNota+" AND CODTIPOPER IN (SELECT TOP.CODTIPOPER FROM TGFTOP TOP INNER JOIN\r\n"
						+ "					(select max(dhalter) as data,codtipoper from tgftop  group by codtipoper)TIPOPER ON \r\n"
						+ "					TOP.CODTIPOPER = TIPOPER.CODTIPOPER AND TOP.dhalter = TIPOPER.data\r\n"
						+ "					WHERE  ad_GERAR_CONTRATO = 'S') and total>1";
		
		
		PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(selectItens);
		ResultSet rs = pstmt.executeQuery();
		  	while (rs.next()) {

		  		BigDecimal codProd = rs.getBigDecimal("CODPROD_CONTRATO");
		  		BigDecimal codProd1 = rs.getBigDecimal("CODPROD");
				String codVol = rs.getString("CODVOL");
				BigDecimal qtdPrevistaNovo = rs.getBigDecimal("QTDEPREVISTA_NOVO");
		  		BigDecimal AD_QTDLIBERAR = rs.getBigDecimal("AD_QTDLIBERAR");
		  		BigDecimal codParc = rs.getBigDecimal("CODPARC");
		  		BigDecimal qtdNeg = rs.getBigDecimal("QTDNEG");
		  		BigDecimal vlrUnit = rs.getBigDecimal("VLRUNIT");
		  		
		  		String validando = "SELECT * FROM LOG_UPDATE_CONTRATO WHERE NUNOTA = '"+nuNota+"' and CODPROD = "+codProd1;
		  		pstmt = jdbcWrapper.getPreparedStatement(validando);
		  		rs = pstmt.executeQuery();
	  			 
		  	    if (rs.next()) {
		  	    	
		  	    }else{
		  		if (codProd.compareTo(BigDecimal.ZERO) > 0) {

		  			 final String updateCodProd = "update TCSPSC set QTDEPREVISTA="+qtdPrevistaNovo+" where codprod="+codProd1+" and numcontrato="+numContrato;
					 pstmt = jdbcWrapper.getPreparedStatement(updateCodProd);
		  			 pstmt.executeUpdate();
		  			 
		  			 final String updateCodProd1 = "update AD_ITENSEMPENHO set QTDDISPONIVEL="+qtdPrevistaNovo+",AD_DISPONIVEL="+qtdPrevistaNovo+" where codprod="+codProd1+" and numcontrato="+numContrato;
					 pstmt = jdbcWrapper.getPreparedStatement(updateCodProd1);
		  			 pstmt.executeUpdate();
		  			
		  			} else{
		  				
		  			 final String insertCodProd = "insert into TCSPSC (NUMCONTRATO, CODPROD, SITPROD,QTDEPREVISTA,VLRUNIT,DTALTER) \r\n"
		  			 		+ "  values ("+numContrato+", "+codProd1+", 'A',"+qtdNeg+","+vlrUnit+", SYSDATE)";
					 pstmt = jdbcWrapper.getPreparedStatement(insertCodProd);
		  			 pstmt.executeUpdate();
		  	  
		  			salvarDadosEmpenho.gerarEmpenho(
		  					dwf, 
		  					codProd1,
							codVol,
		  					numContrato, 
		  					codParc,
		  					qtdNeg,
		  					qtdNeg,
		  					"", 
		  					qtdNeg
		  					);
		  		}
		  		
		  	//	String del = "DELETE FROM TCSOCC WHERE NUMCONTRATO ="+numContrato+" AND CODPROD ="+codProd1+" AND DTOCOR = TRUNC(SYSDATE)";
		  	//	String insert = "INSERT INTO TCSOCC ( AD_CODPROD,AD_DTLIBERA,AD_QTDNEG,CODCONTATO,CODOCOR,CODPARC,CODPROD,CODUSU,DESCRICAO,DTOCOR,NUMCONTRATO ) VALUES ("+codProd1+",SYSDATE,"+AD_QTDLIBERAR+",1,1,"+CODPARC+","+codProd1+",STP_GET_CODUSULOGADO,'AUTOMATICO',TRUNC(SYSDATE),"+numContrato+" )";
		  		
		  	//	 PreparedStatement  del1 = jdbcWrapper.getPreparedStatement(del);
		  	//	 del1.executeUpdate();
		  	 	 
		  	// 	 PreparedStatement  insert1 = jdbcWrapper.getPreparedStatement(insert);
		  	// 	 insert1.executeUpdate();
		  	 	 
		  	 	 final String insertValidando = "INSERT INTO LOG_UPDATE_CONTRATO(NUNOTA,CODPROD,QTD) VALUES ('"+nuNota+"','"+codProd1+"','"+qtdNeg+"')";
				 pstmt = jdbcWrapper.getPreparedStatement(insertValidando);
		  	 	 pstmt.executeUpdate();
		  	 
		  	}
		  	}
		
	
		
		}
	}
}
