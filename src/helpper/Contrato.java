package helpper;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.PersistenceException;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.TimeUtils;
import consultas.contratosCons;


public class Contrato {

	public static void confirma(PersistenceEvent arg0) throws Exception {
		
		DynamicVO cabVO = (DynamicVO) arg0.getVo();
		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();
		
		BigDecimal nuNota = cabVO.asBigDecimalOrZero("NUNOTA");
		BigDecimal numContrato1 = cabVO.asBigDecimalOrZero("NUMCONTRATO");
		BigDecimal codEmp = cabVO.asBigDecimalOrZero("CODEMP");
		BigDecimal codNat = cabVO.asBigDecimalOrZero("CODNAT");
		BigDecimal codCenCus = cabVO.asBigDecimalOrZero("CODCENCUS");
		BigDecimal codParc = cabVO.asBigDecimalOrZero("CODPARC");
		BigDecimal codContato = cabVO.asBigDecimalOrZero("CODCONTATO");
		BigDecimal codTipVenda = cabVO.asBigDecimalOrZero("CODTIPVENDA");
		BigDecimal codTipOper = cabVO.asBigDecimalOrZero("CODTIPOPER");
		BigDecimal AD_CODTIPOPERDESTINO = BigDecimal.ZERO;
		
		final String consultaTopDest = contratosCons.retornaTop(codTipOper.toString());
		PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(consultaTopDest);
  		ResultSet rs = pstmt.executeQuery();

  	    while(rs.next()) {
  	    	AD_CODTIPOPERDESTINO = rs.getBigDecimal("AD_CODTIPOPERDESTINO");
  	    }
  	    if (AD_CODTIPOPERDESTINO.intValue()>0) {
			if (numContrato1.intValue()>0) {
				atualizandoContrato(nuNota, numContrato1, codEmp, codNat);
			} else {

				BigDecimal numContrato = salvaContrato(
						dwf,
						codEmp,
						codParc,
						codContato,
						codTipVenda,
						codNat,
						codCenCus,
						AD_CODTIPOPERDESTINO,
						nuNota);

				// Adiciona os itens na ProdutoServicoContrato
				salvaItensContrato(dwf, nuNota, numContrato);


				String sql = contratosCons.buscarDadosItensContrato(nuNota);
				pstmt = jdbcWrapper.getPreparedStatement(sql);
				rs = pstmt.executeQuery();


				while (rs.next()) {
					BigDecimal codProd = rs.getBigDecimal("CODPROD");
					BigDecimal qtdNeg = rs.getBigDecimal("QTDNEG");
					BigDecimal vlrUnit = rs.getBigDecimal("VLRUNIT");
					BigDecimal sequencia = rs.getBigDecimal("SEQUENCIA");
					String codVol = rs.getString("CODVOL");
					String loteGrupo = rs.getString("LOTEGRUPO");



					pstmt = jdbcWrapper.getPreparedStatement("INSERT INTO LOG_UPDATE_CONTRATO(NUNOTA,CODPROD,QTD) VALUES ('"+nuNota+"','"+codProd+"','"+qtdNeg+"')");
					pstmt.executeUpdate();

					Empenho.geraEmpenho(
							dwf,
							codProd,
							codVol,
							numContrato,
							nuNota,
							sequencia,
							codParc,
							qtdNeg,
							"",
							qtdNeg,
							loteGrupo
							);
				}

				// Componentes já estao no Contrato
				/*final String sql1 = contratosCons.buscarDadosItensContratoComponentes(nuNota.toString());
				pstmt = jdbcWrapper.getPreparedStatement(sql1);
				rs = pstmt.executeQuery();

					while (rs.next()) {
						String codProd = rs.getString("CODPROD");
						String qtdNeg = rs.getString("QTDNEG");
						String vlrUnit = rs.getString("VLRUNIT");
						  // Componentes já estao no Contrato
						*//*contratos.salvarContratoItens(
						dwf,
						numContrato+"",
						codProd,
						qtdNeg,
						vlrUnit);*//*

						String insertValidando = "INSERT INTO LOG_UPDATE_CONTRATO(NUNOTA,CODPROD,QTD) VALUES ('"+nuNota+"','"+codProd+"','"+qtdNeg+"')";
						pstmt = jdbcWrapper.getPreparedStatement(insertValidando);
						pstmt.executeUpdate();

						Empenho.gerarEmpenho(
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
					}*/

				// Atualiza número do contrato na nota de TOP 1005 - Contrato
				pstmt = jdbcWrapper.getPreparedStatement("UPDATE TGFCAB SET NUMCONTRATO = "+numContrato+" where NUNOTA = "+nuNota);
				pstmt.executeUpdate();

			}
  	    }else{
  	    	throw new PersistenceException("TOP "+codTipOper+" Está sem a Top Destino vinculada o campo é :"+AD_CODTIPOPERDESTINO+" por favor preencha e tente novamente !");
  	    }
  	 	
		jdbcWrapper.closeSession();
	}

	public static BigDecimal salvaContrato(
			EntityFacade dwf,
			BigDecimal codEmp,
			BigDecimal codParc,
			BigDecimal codContato,
			BigDecimal codTipVenda,
			BigDecimal codNat,
			BigDecimal codCenCus,
			BigDecimal codTipOper,
			BigDecimal nuNota) throws Exception {

		Date parsedDate = new Date();
		Timestamp dtContrato = TimeUtils.getNow();
		Timestamp dtBaseReaj = TimeUtils.getNow();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();
		String resumo = "";
		String sqlConsulta = contratosCons.buscarDadosResumo(nuNota);
		PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(sqlConsulta);
		ResultSet rs = pstmt.executeQuery();

		while(rs.next()) {
			resumo = rs.getString("NUMERO");
		}

		BigDecimal codUsuLogado = (BigDecimal) JapeSessionContext.getRequiredProperty("usuario_logado");

		DynamicVO contratoVO = (DynamicVO) dwf.getDefaultValueObjectInstance("Contrato");
		contratoVO.setProperty("DTCONTRATO", dtContrato);
		contratoVO.setProperty("CODEMP", codEmp);
		contratoVO.setProperty("CODPARC", codParc);
		contratoVO.setProperty("CODCONTATO", codContato);
		contratoVO.setProperty("CODUSU", codUsuLogado);
		contratoVO.setProperty("CODNAT", codNat);
		contratoVO.setProperty("CODCENCUS", codCenCus);
		contratoVO.setProperty("CODTIPVENDA", codTipVenda);
		contratoVO.setProperty("AD_CODTIPOPER", codTipOper);
		contratoVO.setProperty("ATIVO", "S");
		contratoVO.setProperty("AD_NUMEROLICITACAO", resumo);
		contratoVO.setProperty("DTBASEREAJ", dtBaseReaj);
		dwf.createEntity("Contrato", (EntityVO) contratoVO);

		return contratoVO.asBigDecimal("NUMCONTRATO");
	}

	public static void salvaItensContrato(
			EntityFacade dwf,
			BigDecimal nuNota,
			BigDecimal numContrato
			) throws Exception {

		//Date parsedDate = new Date();
		//Timestamp dtAlter = new Timestamp(parsedDate.getTime());

	   /* if(true) {
			throw new PersistenceException("CODPROD = "+CODPROD+" - qtdNeg = "+QTDEPREVISTA+" - vlrUnit - "+VLRUNIT+" - NumContrato = "+NUMCONTRATO+" dtneg"+DTALTER);
		}*/
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();
		final String sql = "SELECT CODPROD, SUM(QTDNEG) QTDNEG, AVG(VLRUNIT) VLRUNIT\n" +
				"FROM TGFITE WHERE NUNOTA = "+nuNota+"\n" +
				"GROUP BY CODPROD";

		PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(sql);
		ResultSet rs = pstmt.executeQuery();


		while (rs.next()) {
			DynamicVO produtoContratoVO = (DynamicVO) dwf.getDefaultValueObjectInstance("ProdutoServicoContrato");
			produtoContratoVO.setProperty("NUMCONTRATO", numContrato);
			produtoContratoVO.setProperty("CODPROD", rs.getBigDecimal("CODPROD"));
			produtoContratoVO.setProperty("SITPROD", "A");
			produtoContratoVO.setProperty("IMPRNOTA", "S");
			produtoContratoVO.setProperty("LIMITANTE", "N");
			produtoContratoVO.setProperty("QTDEPREVISTA", rs.getBigDecimal("QTDNEG"));
			produtoContratoVO.setProperty("AD_QTDLIBERAR", rs.getBigDecimal("QTDNEG"));
			produtoContratoVO.setProperty("VLRUNIT", rs.getBigDecimal("VLRUNIT"));
			//produtoContratoVO.setProperty("DTALTER", dtAlter);
			dwf.createEntity("ProdutoServicoContrato", (EntityVO) produtoContratoVO);
		}


		jdbcWrapper.closeSession();

	}

	public static void atualizandoContrato(BigDecimal nuNota, BigDecimal numContrato, BigDecimal codEmp,BigDecimal codNat) throws Exception {

		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();

		if (numContrato.intValue()>0) {
			String selectItens = "select cab.CODPARC,AD_QTDLIBERAR,\r\n"
					+ "CAB.NUMCONTRATO AS P_CONTRATO,\r\n"
					+ "ITE.CODPROD, ITE.CODVOL, ITE.SEQUENCIA,\r\n"
					+ "coalesce(itecontrato.CODPROD,0) AS CODPROD_CONTRATO,\r\n"
					+ "'A' AS SITPROD,\r\n"
					+ "ite.QTDNEG+COALESCE(QTDEPREVISTA,0) AS QTDEPREVISTA_NOVO,\r\n"
					+ "ite.VLRUNIT,\r\n"
					+ "ite.QTDNEG,\r\n"
					+ "ite.AD_LOTEGRUPO,\r\n"
					+ "COALESCE(QTDEPREVISTA,0) AS  QTDEPREVISTA\r\n"
					+ "from\r\n"
					+ "tgfcab cab inner join \r\n"
					+ "tgfite ite on ite.nunota = cab.nunota and ite.codemp = cab.codemp left join\r\n"
					+ "TCSPSC itecontrato on itecontrato.codprod = ite.codprod and cab.numcontrato = itecontrato.numcontrato  LEFT JOIN\r\n"
					+ "(select count(NUNOTA) as total,numcontrato from TGFCAB group by numcontrato)novo  on novo.numcontrato = cab.numcontrato \r\n"
					+ "where cab.nunota = "+nuNota+" AND CODTIPOPER IN (SELECT TOP.CODTIPOPER FROM TGFTOP TOP INNER JOIN\r\n"
					+ "(select max(dhalter) as data,codtipoper from tgftop  group by codtipoper)TIPOPER ON \r\n"
					+ "TOP.CODTIPOPER = TIPOPER.CODTIPOPER AND TOP.dhalter = TIPOPER.data\r\n"
					+ "WHERE  ad_GERAR_CONTRATO = 'S') and total>1";


			PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(selectItens);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {

				BigDecimal codProd = rs.getBigDecimal("CODPROD_CONTRATO");
				BigDecimal codProd1 = rs.getBigDecimal("CODPROD");
				String codVol = rs.getString("CODVOL");
				String loteGrupo = rs.getString("AD_LOTEGRUPO");
				BigDecimal qtdPrevistaNovo = rs.getBigDecimal("QTDEPREVISTA_NOVO");
				BigDecimal qtdLiberar = rs.getBigDecimal("AD_QTDLIBERAR");
				BigDecimal codParc = rs.getBigDecimal("CODPARC");
				BigDecimal qtdNeg = rs.getBigDecimal("QTDNEG");
				BigDecimal vlrUnit = rs.getBigDecimal("VLRUNIT");
				BigDecimal sequencia = rs.getBigDecimal("SEQUENCIA");

				String validando = "SELECT * FROM LOG_UPDATE_CONTRATO WHERE NUNOTA = '"+nuNota+"' and CODPROD = "+codProd1;
				pstmt = jdbcWrapper.getPreparedStatement(validando);
				rs = pstmt.executeQuery();

				if (rs.next()) {

				} else {
					if (codProd.compareTo(BigDecimal.ZERO) > 0) {

						final String updateCodProd = "update TCSPSC set QTDEPREVISTA="+qtdPrevistaNovo+" where codprod="+codProd1+" and numcontrato="+numContrato;
						pstmt = jdbcWrapper.getPreparedStatement(updateCodProd);
						pstmt.executeUpdate();

						final String updateCodProd1 = "update AD_ITENSEMPENHO set QTDDISPONIVEL="+qtdPrevistaNovo+",AD_DISPONIVEL="+qtdPrevistaNovo+" where CODPROD="+codProd1+" and NUMCONTRATO="+numContrato;
						pstmt = jdbcWrapper.getPreparedStatement(updateCodProd1);
						pstmt.executeUpdate();

					} else{

						final String insertCodProd = "insert into TCSPSC (NUMCONTRATO, CODPROD, SITPROD,QTDEPREVISTA,VLRUNIT,DTALTER) \r\n"
								+ "  values ("+numContrato+", "+codProd1+", 'A',"+qtdNeg+","+vlrUnit+", SYSDATE)";
						pstmt = jdbcWrapper.getPreparedStatement(insertCodProd);
						pstmt.executeUpdate();

						Empenho.geraEmpenho(
								dwf,
								codProd1,
								codVol,
								numContrato,
								nuNota,
								sequencia,
								codParc,
								qtdNeg,
								"",
								qtdNeg,
								loteGrupo
						);
					}

					//	String del = "DELETE FROM TCSOCC WHERE NUMCONTRATO ="+numContrato+" AND CODPROD ="+codProd1+" AND DTOCOR = TRUNC(SYSDATE)";
					//	String insert = "INSERT INTO TCSOCC ( AD_CODPROD,AD_DTLIBERA,AD_QTDNEG,CODCONTATO,CODOCOR,CODPARC,CODPROD,CODUSU,DESCRICAO,DTOCOR,NUMCONTRATO ) VALUES ("+codProd1+",SYSDATE,"+AD_QTDLIBERAR+",1,1,"+CODPARC+","+codProd1+",STP_GET_CODUSULOGADO,'AUTOMATICO',TRUNC(SYSDATE),"+numContrato+" )";

					//	 PreparedStatement  del1 = jdbcWrapper.getPreparedStatement(del);
					//	 del1.executeUpdate();

					// 	 PreparedStatement  insert1 = jdbcWrapper.getPreparedStatement(insert);
					// 	 insert1.executeUpdate();

					final String insertValidando = "INSERT INTO LOG_UPDATE_CONTRATO (NUNOTA,CODPROD,QTD) VALUES ('"+nuNota+"','"+codProd1+"','"+qtdNeg+"')";
					pstmt = jdbcWrapper.getPreparedStatement(insertValidando);
					pstmt.executeUpdate();

				}
			}



		}
	}
	

}
