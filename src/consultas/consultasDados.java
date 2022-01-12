package consultas;

import java.math.BigDecimal;

public class consultasDados {
	
	
	public static String retornoDados() {

		return "SELECT TOP,URL,LOGIN,SENHA\r\n"
				+ "FROM \r\n"
				+ "(select INTEIRO  AS TOP FROM TSIPAR WHERE CHAVE = 'TOPLICITACAO')A,\r\n"
				+ "(select TEXTO  AS URL FROM TSIPAR WHERE CHAVE = 'URLLICITACAO')B,\r\n"
				+ "(select TEXTO  AS LOGIN FROM TSIPAR WHERE CHAVE = 'LOGINLICITACAO')C,\r\n"
				+ "(select TEXTO  AS SENHA FROM TSIPAR WHERE CHAVE = 'SENHALICITACAO')D";
	}
	
	public static String validaEmpenho(){
		return "SELECT AD_LEBERAEMPENHO FROM TSIUSU USU WHERE USU.CODUSU = STP_GET_CODUSULOGADO AND AD_LEBERAEMPENHO='S'";
	}
	
	
	public static String retornaDadosCabecalho(String numContrato) {
		return "select CODEMP,CODPARC,AD_CODTIPOPER,CODTIPVENDA,CODNAT,CODCENCUS,CODPROJ FROM TCSCON WHERE NUMCONTRATO = "+numContrato;
	}
	
	public static String retornaDadosItens(BigDecimal codProd, BigDecimal numContrato, BigDecimal nuNota, BigDecimal sequencia) {

		return "SELECT ITE.CODPROD, SUM(QTDNEG) QTDNEG, PRO.CODLOCALPADRAO, AVG(ITE.VLRUNIT) VLRUNIT, ITE.CODVOL, ITE.AD_LOTEGRUPO\n" +
				"FROM TGFCAB CAB \n" +
				"INNER JOIN TGFITE ITE ON CAB.NUNOTA = ITE.NUNOTA\n" +
				"INNER JOIN TGFPRO PRO ON ITE.CODPROD = PRO.CODPROD\n" +
				"WHERE ITE.CODPROD = "+codProd.toString()+" AND CAB.NUMCONTRATO= "+numContrato.toString()+" AND ITE.NUNOTA="+nuNota.toString()+" AND ITE.SEQUENCIA = "+sequencia.toString()+"\n" +
				"GROUP BY ITE.CODPROD, ITE.CODVOL, PRO.CODLOCALPADRAO";
	}
	
	public static String retornaDadosItensPedidos(String codProd,String numContrato, String codVol) {

		return "SELECT \r\n"
				+ "A.CODPROD,CODLOCALPADRAO, itens.CODVOL,itens.VLRUNIT\r\n"
				+ "FROM \r\n"
				+ "TGFPRO A INNER JOIN \r\n"
				+ "TCSPSC D ON  A.CODPROD = D.CODPROD inner join \r\n"
				+ "(select VLRUNIT,CODPROD,UNID AS CODVOL from AD_ITENSLICITACAO A INNER JOIN \r\n"
				+ "AD_LICITACAO b ON b.codlic = A.codlic \r\n"
				+ "where  b.nunota in (SELECT distinct  a.nunotaorig FROM TGFVAR a inner join \r\n"
				+ "tgfvar b on  a.nunota = b.nunotaorig inner join \r\n"
				+ "tgfcab c on c.nunota = b.nunota \r\n"
				+ "where c.numcontrato="+numContrato+" AND A.UNID = '"+codVol+"') \r\n"
				+ "UNION ALL \r\n"
				+ "select VLRUNIT,CODPROD,CODVOL from AD_LICITACAOCOMPONENTES A INNER JOIN \r\n"
				+ "AD_LICITACAO b ON b.codlic = A.codlic \r\n"
				+ "where b.nunota in (SELECT distinct  a.nunotaorig FROM TGFVAR a inner join \r\n"
				+ "tgfvar b on  a.nunota = b.nunotaorig inner join \r\n"
				+ "tgfcab c on c.nunota = b.nunota\r\n"
				+ "where c.numcontrato="+numContrato+"))itens on itens.CODPROD = D.CODPROD\r\n"
				+ "	WHERE D.CODPROD = "+codProd+"  AND D.NUMCONTRATO="+numContrato;
	}

	public static String retornaDadosItensProdutos(String codProd) {

		return "select * from tgfpro where CODPROD="+codProd;
	}



}
