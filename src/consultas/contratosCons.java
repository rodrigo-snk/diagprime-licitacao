package consultas;

import java.math.BigDecimal;

public class contratosCons {

	public static String buscarDadosItensContrato(String nuNota) {
		return "SELECT CODPROD, QTDNEG, VLRUNIT, CODVOL\r\n"
				+ "FROM TGFITE WHERE NUNOTA = "+nuNota;
	}
	
	public static String buscarDadosItensContratoComponentes(String nuNota) {
		return "SELECT CODPROD,QTDNEG,VLRUNIT,CODVOL FROM AD_LICITACAOCOMPONENTES WHERE CODLIC IN (select b.CODLIC from AD_LICITACAO b\r\n"
				+ "where b.nunota in (SELECT distinct  a.nunotaorig FROM TGFVAR a inner join \r\n"
				+ "tgfvar b on  a.nunota = b.nunotaorig inner join \r\n"
				+ "tgfcab c on c.nunota = b.nunota\r\n"
				+ "where c.nunota = "+nuNota+"))";
	}
	
	public static String buscarDadosResumo(BigDecimal nuNota) {
		return  "select NUMERO  from AD_LICITACAO b \r\n"
				+ "	where b.nunota in (SELECT distinct  a.nunotaorig FROM TGFVAR a inner join \r\n"
				+ "	tgfvar b on  a.nunota = b.nunotaorig inner join \r\n"
				+ "	tgfcab c on c.nunota = b.nunota\r\n"
				+ "	where  c.NUNOTA="+nuNota.toString()+") ";
	}

	
	public static String buscarDadosItensContratoContrato(String nuNota,String numContrato) {
		return "SELECT  CODPROD, QTDNEG,VLRUNIT\r\n"
				+ "FROM TGFITE WHERE NUNOTA = "+nuNota+" and \r\n"
				+ "CODPROD NOT IN (select codprod from TCSPSC where numcontrato = "+numContrato+")";
	}

	public static String retornoValidaEmpenho(){
		return "SELECT AD_LEBERAEMPENHO FROM TSIUSU USU WHERE USU.CODUSU = STP_GET_CODUSULOGADO AND AD_LEBERAEMPENHO='S'";
	}
	
	public static String retornaTop(String codTipOper){
		
		return "SELECT COALESCE(AD_CODTIPOPERDESTINO,0) AS AD_CODTIPOPERDESTINO FROM TGFTOP TOP INNER JOIN\r\n"
				+ "(select max(dhalter) as data,codtipoper from tgftop  group by codtipoper)TIPOPER ON \r\n"
				+ "TOP.CODTIPOPER = TIPOPER.CODTIPOPER AND TOP.dhalter = TIPOPER.data\r\n"
				+ "WHERE TOP.CODTIPOPER= "+codTipOper;
	}
	
	public static String retornaTopValida(String codTipOper){
		return "SELECT COALESCE(AD_CODTIPOPERDESTINO,0) AS AD_CODTIPOPERDESTINO FROM TGFTOP TOP INNER JOIN\r\n"
				+ "	(select max(dhalter) as data,codtipoper from tgftop  group by codtipoper)TIPOPER ON \r\n"
				+ "	TOP.CODTIPOPER = TIPOPER.CODTIPOPER AND TOP.dhalter = TIPOPER.data\r\n"
				+ "	WHERE TOP.CODTIPOPER= "+codTipOper+" and ad_GERAR_CONTRATO = 'S'";
	}

	public static String retornaTopValidaParaLicitacao(BigDecimal nuNota){
		
		return "select * from \r\n"
				+ "tgfcab where codtipoper in (SELECT TOP.codtipoper  FROM TGFTOP TOP INNER JOIN\r\n"
				+ "					(select max(dhalter) as data,codtipoper from tgftop  group by codtipoper)TIPOPER ON \r\n"
				+ "					TOP.CODTIPOPER = TIPOPER.CODTIPOPER AND TOP.dhalter = TIPOPER.data\r\n"
				+ "					WHERE AD_LICITACAO = 'S') \r\n"
				+ "					and nunota = "+nuNota;
	}

	public static String retornaDadosCabecalho(String numContrato) {
		
		return "select CODEMP,CODPARC,AD_CODTIPOPER,CODTIPVENDA,CODNAT,CODCENCUS,CODPROJ FROM TCSCON WHERE NUMCONTRATO = "+numContrato;
		
	}
	
	public static String retornaDadosItens(String codProd,String numContrato) {
		
		return "SELECT \r\n"
				+ "A.CODPROD,CODLOCALPADRAO, CODVOL,VLRUNIT\r\n"
				+ "FROM \r\n"
				+ "TGFPRO A INNER JOIN \r\n"
				+ "TCSPSC D ON  A.CODPROD = D.CODPROD\r\n"
				+ "WHERE D.CODPROD = "+codProd+"  AND D.NUMCONTRATO="+numContrato;
	}
}
