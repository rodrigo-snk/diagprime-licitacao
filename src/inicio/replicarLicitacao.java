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
		BigDecimal CODLIC = null;
		
		for (Integer i = 0; i < registros.length; i++) {
			
			CODLIC = (BigDecimal) registros[i].getCampo("CODLIC");
			BigDecimal CODITELIC = (BigDecimal) registros[i].getCampo("CODITELIC");
			BigDecimal CODPROD = (BigDecimal) registros[i].getCampo("CODPROD");
			BigDecimal QTDE = (BigDecimal) registros[i].getCampo("QTDE");
			BigDecimal VLRTOTAL = (BigDecimal) registros[i].getCampo("VLRTOTAL");
			BigDecimal VLRUNIT = (BigDecimal) registros[i].getCampo("VLRUNIT");
			BigDecimal MARKUPFATOR = (BigDecimal) registros[i].getCampo("MARKUPFATOR");
			String CODVOL = ""+registros[i].getCampo("UNID");
			
			
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
		DynamicVO financeiroVO = financeiroDAO.findOne("CODLIC = ?", CODLIC);
		Map<String, Object> fieldsFin = new HashMap<>();
		fieldsFin.put("NUNOTA", new BigDecimal(0));
		
		Map<String, Object> PK = util.CentralNotasUtils.duplicaRegistro(financeiroVO, "AD_LICITACAO", fieldsFin);
		BigDecimal codLicNovo = (BigDecimal) PK.get("CODLIC");

		JapeWrapper licitacaoDAO = JapeFactory.dao("AD_ITENSLICITACAO");
		
		String sql = "select CODITELIC from AD_ITENSLICITACAO where codlic="+CODLIC;
		PreparedStatement  consultaValidando2 = jdbcWrapper.getPreparedStatement(sql);
		ResultSet rset = consultaValidando2.executeQuery();

		while(rset.next()){
			
		BigDecimal CODITELIC = rset.getBigDecimal("CODITELIC");
		DynamicVO licitacaoVO = licitacaoDAO.findOne("CODLIC = ? AND CODITELIC="+CODITELIC, CODLIC);
		Map<String, Object> licitacaoFin = new HashMap<>();
		licitacaoFin.put("CODLIC", codLicNovo);
		licitacaoFin.put("REPLICANDO", new BigDecimal(1));
		licitacaoFin.put("CODITELIC_ANTERIOR", CODITELIC);
		licitacaoFin.put("CODLIC_ANTERIOR", CODLIC);
		
		Map<String, Object> PK1 = util.CentralNotasUtils.duplicaRegistro(licitacaoVO, "AD_ITENSLICITACAO", licitacaoFin);
		//BigDecimal codLicNovo = (BigDecimal) PK1.get("CODLIC");
		}
		arg0.setMensagemRetorno("Duplicado com sucesso codlic:"+codLicNovo);
		jdbcWrapper.closeSession();
	}

}
