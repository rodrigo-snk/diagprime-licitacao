package helpper;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.dwfdata.vo.CabecalhoNotaVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
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
		cabVO.setProperty("AD_CODLIC", licitacaoVO.asBigDecimalOrZero("CODLIC"));
		cabVO.setProperty("AD_OBSEDITAL", licitacaoVO.asString("OBSERVACAOORCAMENTO"));
		cabVO.setProperty("AD_LOTE", licitacaoVO.asString("LOTEGRUPO"));
		cabVO.setProperty("AD_VALIDADEPROPOSTA", licitacaoVO.asString("VALIDADAPROPOSTA"));
		cabVO.setProperty("AD_PRAZOENTREGA", licitacaoVO.asString("PRAZOENTREGA"));
		cabVO.setProperty("AD_OBSLOTE", licitacaoVO.asString("OBSERVACAOLOTE"));
		cabVO.setProperty("AD_COMISSAOVENDEDORPERCENTUAL", licitacaoVO.asBigDecimalOrZero("COMISSAO"));
		cabVO.setProperty("AD_SECRETARIA", licitacaoVO.asString("SECRETARIA"));
		cabVO.setProperty("AD_PROCESSO", licitacaoVO.asString("PROCESSO"));
		cabVO.setProperty("AD_MODALIDADE", licitacaoVO.asString("MODALIDADE"));
		cabVO.setProperty("AD_NUMERO", licitacaoVO.asString("NUMERO"));
		cabVO.setProperty("AD_DHABERTURA", licitacaoVO.asTimestamp("DATAHORAABERTURA"));
		//cabVO.setProperty("AD_CODCTABCOINT", licitacaoVO.asBigDecimalOrZero("CODCTABCOINT"));
		cabVO.setProperty("CODCONTATO", licitacaoVO.asBigDecimalOrZero("CODCONTATO"));
		cabVO.setProperty("CODVEND", licitacaoVO.asBigDecimalOrZero("CODVEND"));


		//if(tipo.equalsIgnoreCase("F")) {
		//cabVO.setProperty("STATUSNOTA", "L");
		// }

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
		cabVO.setProperty("AD_CODLIC", licitacaoVO.asBigDecimalOrZero("CODLIC"));
		cabVO.setProperty("AD_OBSEDITAL", licitacaoVO.asString("OBSERVACAOORCAMENTO"));
		cabVO.setProperty("AD_LOTE", licitacaoVO.asString("LOTEGRUPO"));
		cabVO.setProperty("AD_VALIDADEPROPOSTA", licitacaoVO.asString("VALIDADAPROPOSTA"));
		cabVO.setProperty("AD_PRAZOENTREGA", licitacaoVO.asString("PRAZOENTREGA"));
		cabVO.setProperty("AD_OBSLOTE", licitacaoVO.asString("OBSERVACAOLOTE"));
		cabVO.setProperty("AD_COMISSAOVENDEDORPERCENTUAL", licitacaoVO.asBigDecimalOrZero("COMISSAO"));
		cabVO.setProperty("AD_SECRETARIA", licitacaoVO.asString("SECRETARIA"));
		cabVO.setProperty("AD_PROCESSO", licitacaoVO.asString("PROCESSO"));
		cabVO.setProperty("AD_MODALIDADE", licitacaoVO.asString("MODALIDADE"));
		cabVO.setProperty("AD_NUMERO", licitacaoVO.asString("NUMERO"));
		cabVO.setProperty("AD_DHABERTURA", licitacaoVO.asTimestamp("DATAHORAABERTURA"));
		//cabVO.setProperty("AD_CODCTABCOINT", licitacaoVO.asBigDecimalOrZero("CODCTABCOINT"));
		cabVO.setProperty("CODCONTATO", licitacaoVO.asBigDecimalOrZero("CODCONTATO"));
		cabVO.setProperty("CODVEND", licitacaoVO.asBigDecimalOrZero("CODVEND"));


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
