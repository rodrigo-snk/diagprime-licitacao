package helpper;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import consultas.consultasEmpenho;

public class Empenho {

	public static BigDecimal salvaCabecalhoNota(
			EntityFacade dwf,
			BigDecimal codEmp,
			BigDecimal codParc,
			BigDecimal codTipOper,
			BigDecimal codTipVenda,
			BigDecimal codNat,
			BigDecimal codCencus,
			BigDecimal codProj,
			BigDecimal codVend,
			BigDecimal vlrNota,
			BigDecimal numContrato,
			String empenho) throws Exception {

		//SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		//Date parsedDate = dateFormat.parse(dataEntrega);
		// Timestamp timestamp1 = new java.sql.Timestamp(parsedDate.getTime());

		DynamicVO cabVO = (DynamicVO) dwf.getDefaultValueObjectInstance("CabecalhoNota");
		cabVO.setProperty("CODEMP", codEmp);
		cabVO.setProperty("CODPARC", codParc);
		cabVO.setProperty("CODTIPOPER", codTipOper);
		cabVO.setProperty("CODTIPVENDA", codTipVenda);
		cabVO.setProperty("CODNAT", codNat);
		cabVO.setProperty("CODCENCUS", codCencus);
		cabVO.setProperty("CODPROJ", codProj);
		cabVO.setProperty("CODVEND", codVend);
		cabVO.setProperty("VLRNOTA", vlrNota);
		cabVO.setProperty("AD_EMPENHO", empenho);
		cabVO.setProperty("NUMCONTRATO", numContrato);
		cabVO.setProperty("CIF_FOB", "S");
		cabVO.setProperty("NUMNOTA", BigDecimal.ZERO);
		dwf.createEntity("CabecalhoNota", (EntityVO)cabVO);
		return cabVO.asBigDecimal("NUNOTA");
	}

	public static void salvaItemNota(
			EntityFacade dwf,
			BigDecimal codEmp,
			BigDecimal nuNota,
			BigDecimal codProd,
			BigDecimal qtdNeg,
			String codVol,
			BigDecimal vlrUnit,
			BigDecimal vlrTot,
			String atualEst) throws Exception {

		DynamicVO itemVO = (DynamicVO) dwf.getDefaultValueObjectInstance("ItemNota");
		itemVO.setProperty("NUNOTA", nuNota);
		itemVO.setProperty("CODEMP", codEmp);
		itemVO.setProperty("CODPROD", codProd);
		itemVO.setProperty("QTDNEG", qtdNeg);
		itemVO.setProperty("CODVOL", codVol);
		itemVO.setProperty("VLRUNIT", vlrUnit);
		//itemVO.setProperty("CONTROLE", controle);
		itemVO.setProperty("VLRTOT", vlrTot);
		// itemVO.setProperty("NUTAB", nuTab);
		itemVO.setProperty("USOPROD", "P");
		if (atualEst.equalsIgnoreCase("N")) {
			itemVO.setProperty("ATUALESTOQUE", BigDecimal.ZERO);
			itemVO.setProperty("RESERVA", "N");
		} else {
			itemVO.setProperty("ATUALESTOQUE", BigDecimal.ONE);
			itemVO.setProperty("RESERVA", "S");
		}
		//itemVO.setProperty("CODLOCALORIG", codLocal);
		dwf.createEntity("ItemNota", (EntityVO)itemVO);

	}


	public static void salvarDadosPeriodos(
			EntityFacade dwf,
			String mes,
			String referencia,
			String ano,
			String dia,
			String diaSemana) throws Exception {


		DynamicVO itemVO = (DynamicVO)dwf.getDefaultValueObjectInstance("AD_PERIODOANO");
		itemVO.setProperty("MES", mes);
		itemVO.setProperty("REFERENCIA", referencia);
		itemVO.setProperty("ANO", ano);
		itemVO.setProperty("DIA_SEMANA", diaSemana);
		itemVO.setProperty("DIA", dia);
		dwf.createEntity("AD_PERIODOANO", (EntityVO) itemVO);
	}

    /*public static void gerarEmpenho(
    		EntityFacade dwf,
    		BigDecimal CODPROD,
    		BigDecimal NUMCONTRATO,
    		BigDecimal CODPARC,
    		BigDecimal QTDDISPONIVEL,
    		BigDecimal QTDLIBERAR,
    		String EMPENHO,
    		BigDecimal AD_DISPONIVEL) throws Exception {


    	 DynamicVO itemVO = (DynamicVO)dwf.getDefaultValueObjectInstance("AD_EMPENHO");
         itemVO.setProperty("CODPROD", CODPROD);
         itemVO.setProperty("NUMCONTRATO", NUMCONTRATO);
         itemVO.setProperty("CODPARC", CODPARC);
         itemVO.setProperty("QTDDISPONIVEL", QTDDISPONIVEL);
         itemVO.setProperty("QTDLIBERAR", QTDLIBERAR);
         itemVO.setProperty("EMPENHO", EMPENHO);
         itemVO.setProperty("AD_DISPONIVEL", AD_DISPONIVEL);
         dwf.createEntity("AD_EMPENHO", (EntityVO)itemVO);

    }*/


	public static void geraEmpenho(
			EntityFacade dwf,
			BigDecimal codProd,
			String codVol,
			BigDecimal numContrato,
			BigDecimal nuNota,
			BigDecimal sequencia,
			BigDecimal codParc,
			BigDecimal qtdDisponivel,
			String empenho,
			BigDecimal adDisponivel,
			String loteGrupo) throws Exception {

		DynamicVO itemVO = (DynamicVO)dwf.getDefaultValueObjectInstance("AD_ITENSEMPENHO");
		ComercialUtils.MontantesVolumeAlternativo volumeAlternativo = ComercialUtils.calcularVolumeAlternativo(codProd, codVol, " ", qtdDisponivel, BigDecimal.ZERO);
		ComercialUtils.MontantesVolumeAlternativo volumeAlternativo2 = ComercialUtils.calcularVolumeAlternativo(codProd, codVol, " ", adDisponivel, BigDecimal.ZERO);

		itemVO.setProperty("CODPROD", codProd);
		itemVO.setProperty("CODVOL", codVol);
		itemVO.setProperty("NUMCONTRATO", numContrato);
		itemVO.setProperty("NUNOTA", nuNota);
		itemVO.setProperty("SEQUENCIA", sequencia);
		itemVO.setProperty("LOTEGRUPO", loteGrupo);
		// itemVO.setProperty("CODPARC", codParc);
		itemVO.setProperty("QTDDISPONIVEL", volumeAlternativo.getQtdVolAlternativo());
		itemVO.setProperty("QTDLIBERAR", BigDecimal.ZERO);
		itemVO.setProperty("EMPENHO", empenho);
		itemVO.setProperty("AD_DISPONIVEL", volumeAlternativo2.getQtdVolAlternativo());
		dwf.createEntity("AD_ITENSEMPENHO", (EntityVO) itemVO);

	}

	public static void geraEmpenhoConvertido(
			EntityFacade dwf,
			BigDecimal codProd,
			String codVol,
			BigDecimal numContrato,
			BigDecimal qtdDisponivel,
			BigDecimal qtdLiberada,
			String empenho,
			BigDecimal codVend,
			String loteGrupo) throws Exception {

		DynamicVO itemVO = (DynamicVO)dwf.getDefaultValueObjectInstance("AD_CONVERTEREMPENHO");
		ComercialUtils.MontantesVolumeAlternativo volumeAlternativo = ComercialUtils.calcularVolumeAlternativo(codProd, codVol, " ", qtdDisponivel, BigDecimal.ZERO);
		itemVO.setProperty("CODPROD", codProd);
		itemVO.setProperty("CODVOL", codVol);
		itemVO.setProperty("NUMCONTRATO", numContrato);
		itemVO.setProperty("QTDDISPONIVEL",  qtdDisponivel);
		itemVO.setProperty("QTDLIBERAR", BigDecimal.ZERO);
		itemVO.setProperty("EMPENHO", empenho);
		itemVO.setProperty("QTDLIBERADA", qtdLiberada);
		itemVO.setProperty("CODVEND", codVend);
		itemVO.setProperty("LOTEGRUPO", loteGrupo);
		dwf.createEntity("AD_CONVERTEREMPENHO", (EntityVO) itemVO);
	}

	/*public static void liberarEmpenho(BigDecimal numContrato,String empenho) throws Exception {

		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = dwf.getJdbcWrapper();
		jdbc.openSession();
		
		String deleteEmpenho = "delete from AD_EMPENHO where NUMCONTRATO="+numContrato;
				PreparedStatement  consultaParEmepenho = jdbc.getPreparedStatement(deleteEmpenho);
				consultaParEmepenho.execute();

		String consulta = consultasEmpenho.empenhoConsulta(""+numContrato);
		PreparedStatement pstmt = jdbc.getPreparedStatement(consulta);
		ResultSet rs = pstmt.executeQuery();
		while(rs.next()) {
			BigDecimal codProd = rs.getBigDecimal("CODPROD");
			String codVol = rs.getString("CODVOL");
			BigDecimal codParc = rs.getBigDecimal("CODPARC");
			BigDecimal qtdDisponivel = rs.getBigDecimal("QTD_DISPONIVEL");
			BigDecimal adDisponivel = rs.getBigDecimal("AD_DISPONIVEL");

			geraEmpenho(
					dwf, 
					codProd,
					codVol,
					numContrato,
					nuNota,
					sequencia,
					codParc,
					qtdDisponivel,
					BigDecimal.ZERO, 
					empenho,
					adDisponivel);
		}
		
		jdbc.closeSession();
	}*/
	
	public static void liberarEmpenhoTodos(BigDecimal numContrato,String empenho, BigDecimal codVend) throws Exception {

		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = dwf.getJdbcWrapper();
		jdbc.openSession();
		
		/*String deleteEmpenho = "delete from AD_EMPENHO where NUMCONTRATO="+contrato;
				PreparedStatement  consultaParEmepenho = jdbc.getPreparedStatement(deleteEmpenho);
				consultaParEmepenho.execute();*/

		String sql = consultasEmpenho.empenhoConsulta(numContrato.toString());
		PreparedStatement pstmt = jdbc.getPreparedStatement(sql);
		ResultSet rs = pstmt.executeQuery();
		while(rs.next()) {
			
			BigDecimal codProd = rs.getBigDecimal("CODPROD");
			String codVol = rs.getString("CODVOL");
			String loteGrupo = rs.getString("LOTEGRUPO");
			BigDecimal codParc = rs.getBigDecimal("CODPARC");
			BigDecimal qtdDisponivel = rs.getBigDecimal("QTD_DISPONIVEL");
			BigDecimal adDisponivel = rs.getBigDecimal("AD_DISPONIVEL");
			
			
			String update1 = "UPDATE AD_ITENSEMPENHO set QTDLIBERAR=0,AD_DISPONIVEL=AD_DISPONIVEL-"+adDisponivel+"  WHERE NUMCONTRATO = "+numContrato+" AND CODPROD = "+codProd;
			PreparedStatement  preUpdt1 = jdbc.getPreparedStatement(update1);
			preUpdt1.executeUpdate();
			
            geraEmpenhoConvertido(
            		dwf, 
            		codProd,
					codVol,
            		numContrato,
            		adDisponivel,
            		adDisponivel,
            		empenho,
					codVend,
					loteGrupo);
            
			/*salvarDadosEmpenho.gerarEmpenho(
					dwf, 
					CODPROD, 
					contrato, 
					CODPARC, 
					QTDDISPONIVEL, 
					AD_DISPONIVEL, 
					empenho,
					AD_DISPONIVEL);*/
			
		}
		
		jdbc.closeSession();
	}

	public static BigDecimal geraPedido(ContextoAcao arg0, BigDecimal codVend, DynamicVO contratoVO) throws Exception {
		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();

		if (codVend == null) arg0.mostraErro("Vendedor não existe.");

		String codTipOper = (String) arg0.getParam("TIPOPEDIDO");

		Set<String> empenhos = new HashSet<>();
		Set<String> tipo = new HashSet<>();
		BigDecimal nuNota;
		final BigDecimal numContrato = contratoVO.asBigDecimalOrZero("NUMCONTRATO");

		Collection<DynamicVO> empenhosLiberados = dwf.findByDynamicFinderAsVO(new FinderWrapper("AD_CONVERTEREMPENHO", "this.QTDLIBERAR > 0 AND this.CODVEND = ? AND this.NUMCONTRATO = ?", new Object[] {codVend, numContrato}));
		empenhosLiberados.forEach(vo -> empenhos.add(vo.asString("EMPENHO")));
		empenhosLiberados.forEach(vo -> tipo.add(vo.asString("TIPO")));
		if (empenhosLiberados.size() == 0) arg0.mostraErro("Não há empenhos para o vendedor selecionado.");
		if (empenhos.size() > 1) arg0.mostraErro("Mais de um empenho selecionado para o mesmo pedido.");
		if (tipo.size() > 1) arg0.mostraErro("Acessorios e itens nao podem estar no mesmo pedido.");

		DynamicVO cabVO = CabecalhoNota.getOne("this.CODTIPOPER = 1005 AND this.NUMCONTRATO = ?", new Object[] {numContrato});
		if (cabVO == null) throw new MGEModelException("Não foi encontrado nota do contrato.");

		nuNota = Empenho.salvaCabecalhoNota(
				dwf,
				contratoVO.asBigDecimalOrZero("CODEMP"),
				contratoVO.asBigDecimalOrZero("CODPARC"),
				new BigDecimal(codTipOper),
				contratoVO.asBigDecimalOrZero("CODTIPVENDA"),
				contratoVO.asBigDecimalOrZero("CODNAT"),
				contratoVO.asBigDecimalOrZero("CODCENCUS"),
				contratoVO.asBigDecimalOrZero("CODPROJ"),
				codVend,
				BigDecimal.ZERO,
				numContrato,
				empenhos.stream().findFirst().get());


		for (DynamicVO empenhoVO : empenhosLiberados) {

			if (empenhoVO.asBigDecimalOrZero("QTDLIBERAR").compareTo(empenhoVO.asBigDecimalOrZero("QTDDISPONIVEL")) > 0) arg0.mostraErro("Quantidade digitada não pode ser maior que a disponivel ! Cód. Produto : " + empenhoVO.asBigDecimalOrZero("CODPROD"));

			DynamicVO item = ItemNota.getOne("this.NUNOTA = ? AND CODPROD = ? AND CODVOL = ?",
					new Object[] {cabVO.asBigDecimalOrZero("NUNOTA"), empenhoVO.asBigDecimalOrZero("CODPROD"), empenhoVO.asString("CODVOL")});


			if (item != null) {

				String codVol = empenhoVO.asString("CODVOL");
				BigDecimal qtdLiberar = empenhoVO.asBigDecimalOrZero("QTDLIBERAR");
				BigDecimal vlrUnit = item.asBigDecimalOrZero("VLRUNIT");
				BigDecimal vlrTot = vlrUnit.multiply(qtdLiberar);

				final String sqlunidade = "SELECT DIVIDEMULTIPLICA,MULTIPVLR,QUANTIDADE FROM TGFVOA WHERE CODPROD = " + empenhoVO.asBigDecimalOrZero("CODPROD").toString() + " and CODVOL = '" + codVol + "'";
				PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(sqlunidade);
				ResultSet rs = pstmt.executeQuery();
				if (rs.next()) {
					final String divideOuMultiplica = rs.getString("DIVIDEMULTIPLICA");
					BigDecimal quantidade = rs.getBigDecimal("QUANTIDADE").multiply(rs.getBigDecimal("MULTIPVLR"));

					if (divideOuMultiplica.equalsIgnoreCase("M")) {
						qtdLiberar = qtdLiberar.multiply(quantidade);
						vlrUnit = vlrUnit.divide(quantidade, MathContext.DECIMAL128);

					} else if (divideOuMultiplica.equalsIgnoreCase("D")) {
						qtdLiberar = qtdLiberar.divide(quantidade, MathContext.DECIMAL128);
						vlrUnit = vlrUnit.multiply(quantidade);
					}
					vlrTot = vlrUnit.multiply(qtdLiberar);
				}

				// Verifca ATUALEST da TOP para alterar itens
				DynamicVO topVO = TipoOperacaoUtils.getTopVO(BigDecimal.valueOf(Integer.parseInt(codTipOper)));
				String atualEst = (String) topVO.getProperty("ATUALEST");

				Empenho.salvaItemNota(
						dwf,
						contratoVO.asBigDecimal("CODEMP"),
						nuNota,
						empenhoVO.asBigDecimalOrZero("CODPROD"),
						qtdLiberar,
						codVol,
						vlrUnit,
						vlrTot,
						atualEst);
			}

			empenhoVO.setProperty("QTDLIBERAR", BigDecimal.ZERO);
			empenhoVO.setProperty("QTDDISPONIVEL", empenhoVO.asBigDecimalOrZero("QTDDISPONIVEL").subtract(empenhoVO.asBigDecimalOrZero("QTDLIBERAR")));

			dwf.saveEntity("AD_CONVERTEREMPENHO", (EntityVO) empenhoVO);


		}

		jdbcWrapper.closeSession();
		return nuNota;
	}

}
