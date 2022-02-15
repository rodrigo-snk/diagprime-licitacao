package helpper;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.dwfdata.vo.CabecalhoNotaVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;
import consultas.consultasDados;


public class CabecalhoNota {

	public static BigDecimal salvaCabecalhoNota(
			EntityFacade dwf,
			BigDecimal codTipOper,
			DynamicVO licitacaoVO
	) throws Exception {

		DynamicVO cabVO = (DynamicVO) dwf.getDefaultValueObjectInstance("CabecalhoNota");
		cabVO.setProperty("CODEMP", licitacaoVO.asBigDecimalOrZero("CODEMP"));
		cabVO.setProperty("CODPARC", licitacaoVO.asBigDecimalOrZero("CODPARC"));
		cabVO.setProperty("CODTIPOPER", codTipOper);
		cabVO.setProperty("CODTIPVENDA", licitacaoVO.asBigDecimalOrZero("CODTIPVENDA"));
		cabVO.setProperty("CODNAT", licitacaoVO.asBigDecimalOrZero("CODNAT"));
		cabVO.setProperty("CODCENCUS", licitacaoVO.asBigDecimalOrZero("CODCENCUS"));
		cabVO.setProperty("CIF_FOB", "S");
		cabVO.setProperty("NUMNOTA", BigDecimal.ZERO);
		cabVO.setProperty("DTNEG", TimeUtils.getNow());
		cabVO.setProperty("CODCONTATO", licitacaoVO.asBigDecimalOrZero("CODCONTATO"));
		cabVO.setProperty("CODVEND", licitacaoVO.asBigDecimalOrZero("CODVEND"));

		if (cabVO.containsProperty("AD_CODLIC")) cabVO.setProperty("AD_CODLIC", licitacaoVO.asBigDecimalOrZero("CODLIC"));
		if (cabVO.containsProperty("AD_OBSEDITAL")) cabVO.setProperty("AD_OBSEDITAL", licitacaoVO.asString("OBSERVACAOORCAMENTO"));
		if (cabVO.containsProperty("AD_LOTE")) cabVO.setProperty("AD_LOTE", licitacaoVO.asString("LOTEGRUPO"));
		if (cabVO.containsProperty("AD_VALIDADEPROPOSTA")) cabVO.setProperty("AD_VALIDADEPROPOSTA", licitacaoVO.asString("VALIDADAPROPOSTA"));
		if (cabVO.containsProperty("AD_VALPRAZODAPROPOSTA")) cabVO.setProperty("AD_VALPRAZODAPROPOSTA", licitacaoVO.asString("VALIDADAPROPOSTA"));
		if (cabVO.containsProperty("AD_PRAZOENTREGA")) cabVO.setProperty("AD_PRAZOENTREGA", licitacaoVO.asString("PRAZOENTREGA"));
		if (cabVO.containsProperty("AD_PRAZOENT")) cabVO.setProperty("AD_PRAZOENT", licitacaoVO.asString("PRAZOENTREGA"));
		if (cabVO.containsProperty("AD_OBSLOTE")) cabVO.setProperty("AD_OBSLOTE", licitacaoVO.asString("OBSERVACAOLOTE"));
		if (cabVO.containsProperty("AD_COMISSAOVENDEDORPERCENTUAL")) cabVO.setProperty("AD_COMISSAOVENDEDORPERCENTUAL", licitacaoVO.asBigDecimalOrZero("COMISSAO"));
		if (cabVO.containsProperty("AD_SECRETARIA")) cabVO.setProperty("AD_SECRETARIA", licitacaoVO.asString("SECRETARIA"));
		if (cabVO.containsProperty("AD_PROCESSO")) cabVO.setProperty("AD_PROCESSO", licitacaoVO.asString("PROCESSO"));
		if (cabVO.containsProperty("AD_MODALIDADE")) cabVO.setProperty("AD_MODALIDADE", licitacaoVO.asString("MODALIDADE"));
		if (cabVO.containsProperty("AD_NUMERO")) cabVO.setProperty("AD_NUMERO", licitacaoVO.asString("NUMERO"));
		if (cabVO.containsProperty("AD_DHABERTURA")) cabVO.setProperty("AD_DHABERTURA", licitacaoVO.asTimestamp("DATAHORAABERTURA"));
		if (cabVO.containsProperty("AD_CODCTABCOINT")) cabVO.setProperty("AD_CODCTABCOINT", licitacaoVO.asBigDecimalOrZero("CODCTABCOINT"));

		dwf.createEntity("CabecalhoNota", (EntityVO)cabVO);
		return cabVO.asBigDecimal("NUNOTA");
	}

	public static void insereNota(DynamicVO licitacaoVO) throws Exception {

		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();

		final String sql = consultasDados.retornoDados();
		PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(sql);
		ResultSet rs = pstmt.executeQuery();
		while (rs.next()) {

			String url = rs.getString("URL");
			String login11 = rs.getString("LOGIN");
			String senha = rs.getString("SENHA");
			BigDecimal codTipOper = rs.getBigDecimal("TOP");

			BigDecimal nuNota = salvaCabecalhoNota(dwf, codTipOper, licitacaoVO);

			//String chave = login.loginNovo1(url,login11,senha);
			//Retorno1 ret = incluirNota.IncluirNota(url, codParc+"", codEmp+"", DTNEG, codTipVenda+"", chave,codTipOper);
			//nuNota = ret.getNunota();

			licitacaoVO.setProperty("NUNOTA", nuNota);
			dwf.saveEntity("AD_LICITACAO", (EntityVO) licitacaoVO);

		}

		jdbcWrapper.closeSession();

	}

	public static void atualizaCabecalhoNota(EntityFacade dwf, DynamicVO licitacaoVO) throws Exception {

		DynamicVO cabVO = (DynamicVO) dwf.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, licitacaoVO.asBigDecimalOrZero("NUNOTA"));
		cabVO.setProperty("CODEMP", licitacaoVO.asBigDecimalOrZero("CODEMP"));
		cabVO.setProperty("CODPARC", licitacaoVO.asBigDecimalOrZero("CODPARC"));
		cabVO.setProperty("CODTIPVENDA", licitacaoVO.asBigDecimalOrZero("CODTIPVENDA"));
		cabVO.setProperty("CODNAT", licitacaoVO.asBigDecimalOrZero("CODNAT"));
		cabVO.setProperty("CODCENCUS", licitacaoVO.asBigDecimalOrZero("CODCENCUS"));
		cabVO.setProperty("CODCONTATO", licitacaoVO.asBigDecimalOrZero("CODCONTATO"));
		cabVO.setProperty("CODVEND", licitacaoVO.asBigDecimalOrZero("CODVEND"));
		if (cabVO.containsProperty("AD_CODLIC")) cabVO.setProperty("AD_CODLIC", licitacaoVO.asBigDecimalOrZero("CODLIC"));
		if (cabVO.containsProperty("AD_OBSEDITAL")) cabVO.setProperty("AD_OBSEDITAL", licitacaoVO.asString("OBSERVACAOORCAMENTO"));
		if (cabVO.containsProperty("AD_LOTE")) cabVO.setProperty("AD_LOTE", licitacaoVO.asString("LOTEGRUPO"));
		if (cabVO.containsProperty("AD_VALIDADEPROPOSTA")) cabVO.setProperty("AD_VALIDADEPROPOSTA", licitacaoVO.asString("VALIDADAPROPOSTA"));
		if (cabVO.containsProperty("AD_VALPRAZODAPROPOSTA")) cabVO.setProperty("AD_VALPRAZODAPROPOSTA", licitacaoVO.asString("VALIDADAPROPOSTA"));
		if (cabVO.containsProperty("AD_PRAZOENTREGA")) cabVO.setProperty("AD_PRAZOENTREGA", licitacaoVO.asString("PRAZOENTREGA"));
		if (cabVO.containsProperty("AD_PRAZOENT")) cabVO.setProperty("AD_PRAZOENT", licitacaoVO.asString("PRAZOENTREGA"));
		if (cabVO.containsProperty("AD_OBSLOTE")) cabVO.setProperty("AD_OBSLOTE", licitacaoVO.asString("OBSERVACAOLOTE"));
		if (cabVO.containsProperty("AD_COMISSAOVENDEDORPERCENTUAL")) cabVO.setProperty("AD_COMISSAOVENDEDORPERCENTUAL", licitacaoVO.asBigDecimalOrZero("COMISSAO"));
		if (cabVO.containsProperty("AD_SECRETARIA")) cabVO.setProperty("AD_SECRETARIA", licitacaoVO.asString("SECRETARIA"));
		if (cabVO.containsProperty("AD_PROCESSO")) cabVO.setProperty("AD_PROCESSO", licitacaoVO.asString("PROCESSO"));
		if (cabVO.containsProperty("AD_MODALIDADE")) cabVO.setProperty("AD_MODALIDADE", licitacaoVO.asString("MODALIDADE"));
		if (cabVO.containsProperty("AD_NUMERO")) cabVO.setProperty("AD_NUMERO", licitacaoVO.asString("NUMERO"));
		if (cabVO.containsProperty("AD_DHABERTURA")) cabVO.setProperty("AD_DHABERTURA", licitacaoVO.asTimestamp("DATAHORAABERTURA"));
		if (cabVO.containsProperty("AD_CODCTABCOINT")) cabVO.setProperty("AD_CODCTABCOINT", licitacaoVO.asBigDecimalOrZero("CODCTABCOINT"));

		dwf.saveEntity(DynamicEntityNames.CABECALHO_NOTA, (EntityVO)cabVO);
	}

	public static void atualizaParceiro(Object nuNota, Object codParc) throws Exception {
		JapeSession.SessionHandle hnd = null;
		try {
			hnd = JapeSession.open();
			JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA)
					.prepareToUpdateByPK(nuNota)
					.set("CODPARC", codParc)
					.update();
		} catch (Exception e) {
			MGEModelException.throwMe(e);
		} finally {
			JapeSession.close(hnd);
		}

	}

	public static void preencheAdicionaisCabOrig(DynamicVO cabVO) throws Exception {
		EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();

		Collection<DynamicVO> varVOs = dwfEntityFacade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.COMPRA_VENDA_VARIOS_PEDIDO, "this.NUNOTA = ?", new Object[] {cabVO.asBigDecimalOrZero("NUNOTA")}));

		if (varVOs.stream().findFirst().isPresent()) {
			DynamicVO varVO = varVOs.stream().findFirst().get();
			BigDecimal nuNotaOrig = varVO.asBigDecimalOrZero("NUNOTAORIG");

			DynamicVO cabOrigVO = (DynamicVO) dwfEntityFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, new Object[] {nuNotaOrig});

			if (cabVO.containsProperty(("AD_CODAGE"))) cabVO.setProperty("AD_CODAGE", cabOrigVO.asString("AD_CODAGE"));
			if (cabVO.containsProperty(("AD_CODBCO"))) cabVO.setProperty("AD_CODBCO", cabOrigVO.asString("AD_CODBCO"));
			if (cabVO.containsProperty(("AD_CODCTABCOINT"))) cabVO.setProperty("AD_CODCTABCOINT", cabOrigVO.asString("AD_CODCTABCOINT"));
			if (cabVO.containsProperty(("AD_CODLIC"))) cabVO.setProperty("AD_CODLIC", cabOrigVO.asString("AD_CODLIC"));
			if (cabVO.containsProperty(("AD_CODOBSPADRAO"))) cabVO.setProperty("AD_CODOBSPADRAO", cabOrigVO.asString("AD_CODOBSPADRAO"));
			if (cabVO.containsProperty(("AD_COMISSAOVENDEDORPERCENTUAL"))) cabVO.setProperty("AD_COMISSAOVENDEDORPERCENTUAL", cabOrigVO.asString("AD_COMISSAOVENDEDORPERCENTUAL"));
			if (cabVO.containsProperty(("AD_DESPADM"))) cabVO.setProperty("AD_DESPADM", cabOrigVO.asString("AD_DESPADM"));
			if (cabVO.containsProperty(("AD_CUSFORN"))) cabVO.setProperty("AD_CUSFORN", cabOrigVO.asString("AD_CUSFORN"));
			if (cabVO.containsProperty(("AD_DESPESAFIXA"))) cabVO.setProperty("AD_DESPESAFIXA", cabOrigVO.asString("AD_DESPESAFIXA"));
			if (cabVO.containsProperty(("AD_DESPOPER"))) cabVO.setProperty("AD_DESPOPER", cabOrigVO.asString("AD_DESPOPER"));
			if (cabVO.containsProperty(("AD_DTCONTRATO"))) cabVO.setProperty("AD_DTCONTRATO", cabOrigVO.asString("AD_DTCONTRATO"));
			if (cabVO.containsProperty(("AD_EMPENHO"))) cabVO.setProperty("AD_EMPENHO", cabOrigVO.asString("AD_EMPENHO"));
			if (cabVO.containsProperty(("AD_FINALIDADE"))) cabVO.setProperty("AD_FINALIDADE", cabOrigVO.asString("AD_FINALIDADE"));
			if (cabVO.containsProperty(("AD_LOTE"))) cabVO.setProperty("AD_LOTE", cabOrigVO.asString("AD_LOTE"));
			if (cabVO.containsProperty(("AD_LUCROBRU"))) cabVO.setProperty("AD_LUCROBRU", cabOrigVO.asString("AD_LUCROBRU"));
			if (cabVO.containsProperty(("AD_LUCROBRUPERC"))) cabVO.setProperty("AD_LUCROBRUPERC", cabOrigVO.asString("AD_LUCROBRUPERC"));
			if (cabVO.containsProperty(("AD_MODALIDADE"))) cabVO.setProperty("AD_MODALIDADE", cabOrigVO.asString("AD_MODALIDADE"));
			if (cabVO.containsProperty(("AD_NUMERO"))) cabVO.setProperty("AD_NUMERO", cabOrigVO.asString("AD_NUMERO"));
			if (cabVO.containsProperty(("AD_NUMPED"))) cabVO.setProperty("AD_NUMPED", cabOrigVO.asString("AD_NUMPED"));
			//if (cabVO.containsProperty(("AD_OBSLOTE"))) cabVO.setProperty("AD_OBSLOTE", cabOrigVO.asString("AD_OBSLOTE"));
			if (cabVO.containsProperty(("AD_OBSEDITAL"))) cabVO.setProperty("AD_OBSEDITAL", cabOrigVO.asString("AD_OBSEDITAL"));
			if (cabVO.containsProperty(("AD_NUNOTA_NOVO"))) cabVO.setProperty("AD_NUNOTA_NOVO", cabOrigVO.asString("AD_NUNOTA_NOVO"));
			if (cabVO.containsProperty(("AD_NUNOTA_NOVO"))) cabVO.setProperty("AD_NUNOTA_NOVO", cabOrigVO.asString("AD_NUNOTA_NOVO"));
			if (cabVO.containsProperty(("AD_PRAZOENT"))) cabVO.setProperty("AD_PRAZOENT", cabOrigVO.asString("AD_PRAZOENT"));
			if (cabVO.containsProperty(("AD_PRAZOENTREGA"))) cabVO.setProperty("AD_PRAZOENTREGA", cabOrigVO.asString("AD_PRAZOENTREGA"));
			if (cabVO.containsProperty(("AD_PROCESSO"))) cabVO.setProperty("AD_PROCESSO", cabOrigVO.asString("AD_PROCESSO"));
			if (cabVO.containsProperty(("AD_REFERENCIA"))) cabVO.setProperty("AD_REFERENCIA", cabOrigVO.asString("AD_REFERENCIA"));
			if (cabVO.containsProperty(("AD_SECRETARIA"))) cabVO.setProperty("AD_SECRETARIA", cabOrigVO.asString("AD_SECRETARIA "));
			if (cabVO.containsProperty(("AD_SERVDIV"))) cabVO.setProperty("AD_SERVDIV", cabOrigVO.asString("AD_SERVDIV"));
			if (cabVO.containsProperty(("AD_SERVINT"))) cabVO.setProperty("AD_SERVINT", cabOrigVO.asString("AD_SERVINT"));
			if (cabVO.containsProperty(("AD_VALIDADEPROPOSTA"))) cabVO.setProperty("AD_VALIDADEPROPOSTA", cabOrigVO.asString("AD_VALIDADEPROPOSTA"));
			if (cabVO.containsProperty(("AD_VALIDPROP"))) cabVO.setProperty("AD_VALIDPROP", cabOrigVO.asString("AD_VALIDPROP"));
			if (cabVO.containsProperty(("AD_VALIDADEPROPOSTA"))) cabVO.setProperty("AD_VALIDADEPROPOSTA", cabOrigVO.asString("AD_VALIDADEPROPOSTA"));
			if (cabVO.containsProperty(("AD_VALPRAZODAPROPOSTA"))) cabVO.setProperty("AD_VALPRAZODAPROPOSTA", cabOrigVO.asString("AD_VALPRAZODAPROPOSTA"));
		}
	}


	public static void setPendente(BigDecimal nuNota, boolean b) throws Exception {
		CabecalhoNotaVO cabVO = (CabecalhoNotaVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota, CabecalhoNotaVO.class);
		if (b) {
			cabVO.setPENDENTE("S");
		} else {
			cabVO.setPENDENTE("N");
		}
		EntityFacadeFactory.getDWFFacade().saveEntity(DynamicEntityNames.CABECALHO_NOTA, cabVO);
	}

	public static DynamicVO getOne(String where, Object[] param) throws MGEModelException {
		JapeSession.SessionHandle hnd = null;
		try {
			hnd = JapeSession.open();
			JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
			return cabDAO.findOne(where, param);
		} catch (Exception e) {
			MGEModelException.throwMe(e);
		} finally {
			JapeSession.close(hnd);
		}
		return null;
	}
}
