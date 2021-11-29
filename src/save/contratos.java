package save;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import com.sankhya.util.TimeUtils;
import consultas.contratosCons;

public class contratos {

	
    public static BigDecimal salvarContrato(
    		EntityFacade dwf,
    		BigDecimal codEmp,
    		BigDecimal codParc,
    		BigDecimal codContato,
    		BigDecimal codTipVenda,
    		BigDecimal codNat,
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
    	contratoVO.setProperty("CODTIPVENDA", codTipVenda);
    	contratoVO.setProperty("AD_CODTIPOPER", codTipOper);
    	contratoVO.setProperty("ATIVO", "S");
    	contratoVO.setProperty("AD_NUMEROLICITACAO", resumo);
    	contratoVO.setProperty("DTBASEREAJ", dtBaseReaj);
		dwf.createEntity("Contrato", (EntityVO) contratoVO);

		return contratoVO.asBigDecimal("NUMCONTRATO");
    }

    public static void salvarContratoItens(
    		EntityFacade dwf,
    		BigDecimal numContrato,
    		BigDecimal codProd,
    		BigDecimal qtdPrevista,
    		BigDecimal vlrUnit) throws Exception {

    	//Date parsedDate = new Date();
	    //Timestamp dtAlter = new Timestamp(parsedDate.getTime());
	    
	   /* if(true) {
			throw new PersistenceException("CODPROD = "+CODPROD+" - qtdNeg = "+QTDEPREVISTA+" - vlrUnit - "+VLRUNIT+" - NumContrato = "+NUMCONTRATO+" dtneg"+DTALTER);
		}*/

    	DynamicVO produtoContratoVO = (DynamicVO) dwf.getDefaultValueObjectInstance("ProdutoServicoContrato");
    	produtoContratoVO.setProperty("NUMCONTRATO", numContrato);
    	produtoContratoVO.setProperty("CODPROD", codProd);
		produtoContratoVO.setProperty("SITPROD", "A");
    	produtoContratoVO.setProperty("IMPRNOTA", "S");
    	produtoContratoVO.setProperty("LIMITANTE", "N");
    	produtoContratoVO.setProperty("QTDEPREVISTA", qtdPrevista);
    	produtoContratoVO.setProperty("AD_QTDLIBERAR", qtdPrevista);
    	produtoContratoVO.setProperty("VLRUNIT", vlrUnit);
    //	gerarItensContratos.setProperty("DTALTER", dtAlter);
		dwf.createEntity("ProdutoServicoContrato", (EntityVO) produtoContratoVO);
		
    }
	
}
