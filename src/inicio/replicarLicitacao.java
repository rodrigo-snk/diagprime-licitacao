package inicio;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class replicarLicitacao implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao arg0) throws Exception {

		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();
		Registro[] registros = arg0.getLinhas();
		Integer usuario = Integer.parseInt(""+arg0.getUsuarioLogado());
		BigDecimal codLic = null;

		for (Registro registro : registros) {

			codLic = (BigDecimal) registro.getCampo("CODLIC");
			BigDecimal codIteLic = (BigDecimal) registro.getCampo("CODITELIC");
			BigDecimal codProd = (BigDecimal) registro.getCampo("CODPROD");
			BigDecimal qtde = (BigDecimal) registro.getCampo("QTDE");
			BigDecimal vlrTotal = (BigDecimal) registro.getCampo("VLRTOTAL");
			BigDecimal vlrUnit = (BigDecimal) registro.getCampo("VLRUNIT");
			BigDecimal markUpFator = (BigDecimal) registro.getCampo("MARKUPFATOR");
			String codVol = (String) registro.getCampo("UNID");

			/*recalcularItens.atualizarCusto(
					CODITELIC, 
					CODLIC, 
					CODPROD, 
					QTDE, 
					VLRTOTAL, 
					VLRUNIT, 
					MARKUPFATOR, 
					CODVOL);*/

		}
		
		JapeWrapper financeiroDAO = JapeFactory.dao("AD_LICITACAO");
		DynamicVO financeiroVO = financeiroDAO.findOne("CODLIC = ?", codLic);
		Map<String, Object> fieldsFin = new HashMap<>();
		fieldsFin.put("NUNOTA", BigDecimal.ZERO);
		
		Map<String, Object> PK = util.CentralNotasUtils.duplicaRegistro(financeiroVO, "AD_LICITACAO", fieldsFin);
		BigDecimal codLicNovo = (BigDecimal) PK.get("CODLIC");

		JapeWrapper licitacaoDAO = JapeFactory.dao("AD_ITENSLICITACAO");
		
		String sql = "select CODITELIC from AD_ITENSLICITACAO where codlic="+codLic;
		PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(sql);
		ResultSet rs = pstmt.executeQuery();

		while(rs.next()){
			
		BigDecimal codIteLic = rs.getBigDecimal("CODITELIC");
		DynamicVO licitacaoVO = licitacaoDAO.findOne("CODLIC = ? AND CODITELIC="+codIteLic, codLic);
		Map<String, Object> licitacaoFin = new HashMap<>();
		licitacaoFin.put("CODLIC", codLicNovo);
		licitacaoFin.put("REPLICANDO", new BigDecimal(1));
		licitacaoFin.put("CODITELIC_ANTERIOR", codIteLic);
		licitacaoFin.put("CODLIC_ANTERIOR", codLic);
		
		Map<String, Object> PK1 = util.CentralNotasUtils.duplicaRegistro(licitacaoVO, "AD_ITENSLICITACAO", licitacaoFin);
		//BigDecimal codLicNovo = (BigDecimal) PK1.get("CODLIC");
		}
		arg0.setMensagemRetorno("Duplicado com sucesso codlic:"+codLicNovo);
		jdbcWrapper.closeSession();
	}

}
