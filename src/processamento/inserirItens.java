package processamento;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.PersistenceException;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import consultas.consultasDados;
import save.salvarDados;

public class inserirItens {
	
	
	
	public static void atualizarCusto(PersistenceEvent arg0) throws Exception {
		
		
		EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
		jdbcWrapper.openSession();
		
		
		DynamicVO itemVO = (DynamicVO) arg0.getVo();
		BigDecimal codIteLic = itemVO.asBigDecimalOrZero("CODITELIC");
		BigDecimal codLic = itemVO.asBigDecimalOrZero("CODLIC");
		BigDecimal codProd = itemVO.asBigDecimalOrZero("CODPROD");
		BigDecimal qtdNeg = itemVO.asBigDecimalOrZero("QTDE");
		BigDecimal vlrTot = itemVO.asBigDecimalOrZero("VLRTOTAL");
		BigDecimal vlrUnit = itemVO.asBigDecimalOrZero("VLRUNIT");
		BigDecimal item = itemVO.asBigDecimalOrZero("ITEM");
		BigDecimal markupFator = itemVO.asBigDecimalOrZero("MARKUPFATOR");
		String codVol = itemVO.asString("UNID");
		BigDecimal replicando = (itemVO.asBigDecimalOrZero("REPLICANDO"));
		
		
		if(!(markupFator.doubleValue()>0)) {
			markupFator = BigDecimal.ONE;
		}
		
		if(!(qtdNeg.doubleValue()>0)) {
			qtdNeg = BigDecimal.ONE;
			
		}
		String consultaProd = consultasDados.retornaDadosItensProdutos(codProd.toString());
		PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(consultaProd);
		ResultSet rset = pstmt.executeQuery();
		boolean isServico = false;
		while(rset.next()) isServico = rset.getString("USOPROD").equals("S");

		
		String  consultaDados = "SELECT coalesce(CUSGER,0) as CUSGER FROM TGFCUS WHERE CODPROD = "+codProd+" AND DTATUAL IN (\r\n"
				+ "select MAX(DTATUAL) AS VALOR from TGFCUS WHERE CODPROD = "+codProd+")";
		
		pstmt = jdbcWrapper.getPreparedStatement(consultaDados);
		ResultSet consulta = pstmt.executeQuery();
		BigDecimal valor = BigDecimal.ZERO;

		while(consulta.next()) valor = (consulta.getBigDecimal("CUSGER") == null) ? BigDecimal.ZERO : consulta.getBigDecimal("CUSGER");

		if(!(valor.intValue() > 0) && !isServico) {
			
			throw new PersistenceException("Custo do produto obrigatório, não encontrado ou está zerado o custo, no cadastro de custo "+consultaDados);
	
		}
		vlrUnit = valor.multiply(markupFator);
		vlrTot = vlrUnit.multiply(qtdNeg);

		String update;
		String update1;
		if(!((replicando.intValue())>0)) {
				update = "UPDATE AD_ITENSLICITACAO SET ITEM=(select COALESCE(max(item),0)+1 item from AD_ITENSLICITACAO WHERE CODLIC="+codLic+"),MARKUPFATOR="+markupFator+",CUSTO="+valor+",VLRTOTAL="+vlrTot+",QTDE="+qtdNeg+",VLRUNIT="+vlrUnit+" "
				+ " where CODITELIC="+codIteLic+" and CODLIC="+codLic;
				
				update1 = "UPDATE TGFITE SET VLRTOT="+vlrTot+",QTDNEG="+qtdNeg+",VLRUNIT="+vlrUnit+" "
						+ " where AD_CODITELIC="+codIteLic+" and AD_CODLIC="+codLic;

				pstmt = jdbcWrapper.getPreparedStatement(update1);
				pstmt.executeUpdate();
				
				
		}else {
				update = "UPDATE AD_ITENSLICITACAO SET REPLICANDO=0 "
						+ "WHERE CODITELIC="+codIteLic+" AND CODLIC="+codLic;
		}

		pstmt = jdbcWrapper.getPreparedStatement(update);
		pstmt.executeUpdate();

		pstmt = jdbcWrapper.getPreparedStatement("select * from AD_LICITACAO where CODLIC="+codLic);
		ResultSet rs = pstmt.executeQuery();

		while(rs.next()){

			BigDecimal nuNota = rs.getBigDecimal("NUNOTA");
			BigDecimal codEmp = rs.getBigDecimal("CODEMP");

			salvarDados.salvarItensDados(dwf,nuNota,codProd,qtdNeg,codVol,vlrUnit,vlrTot,codEmp,codIteLic,codLic);

			ImpostosHelpper impostos = new ImpostosHelpper();
			impostos.setForcarRecalculo(true);
			impostos.calcularImpostos(nuNota);
		}
		

		
		salvarDados.insertComponentes(codLic, jdbcWrapper);
		jdbcWrapper.closeSession();
	}

}
