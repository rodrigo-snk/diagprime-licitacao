package consultas;

public class consultasDados {
	
	
	public static String retornoDados() {
		
		String sql = "SELECT TOP,URL,LOGIN,SENHA\r\n"
				+ "FROM \r\n"
				+ "(select INTEIRO  AS TOP FROM TSIPAR WHERE CHAVE = 'TOPLICITACAO')A,\r\n"
				+ "(select TEXTO  AS URL FROM TSIPAR WHERE CHAVE = 'URLLICITACAO')B,\r\n"
				+ "(select TEXTO  AS LOGIN FROM TSIPAR WHERE CHAVE = 'LOGINLICITACAO')C,\r\n"
				+ "(select TEXTO  AS SENHA FROM TSIPAR WHERE CHAVE = 'SENHALICITACAO')D";
		
		return sql;
	
	}
	
	public static String retornoValidaEmpenho(){
		
		String sql = "SELECT AD_LEBERAEMPENHO FROM TSIUSU USU WHERE USU.CODUSU = STP_GET_CODUSULOGADO AND AD_LEBERAEMPENHO='S'";
		return sql;
		
	}
	
	
	public static String retornaDadosCabecalho(String numContrato) {
		
		String sql = "select CODEMP,CODPARC,AD_CODTIPOPER,CODTIPVENDA,CODNAT,CODCENCUS,CODPROJ FROM TCSCON WHERE NUMCONTRATO = "+numContrato;
		return sql;
		
	}
	
	public static String retornaDadosItens(String codProd,String numContrato) {
		
		String sql = "SELECT \r\n"
				+ "A.CODPROD,CODLOCALPADRAO, CODVOL,VLRUNIT\r\n"
				+ "FROM \r\n"
				+ "TGFPRO A INNER JOIN \r\n"
				+ "TCSPSC D ON  A.CODPROD = D.CODPROD\r\n"
				+ "WHERE D.CODPROD = "+codProd+"  AND D.NUMCONTRATO="+numContrato;
		return sql;
		
	}
	
	public static String retornaDadosItensPedidos(String codProd,String numContrato) {
		
		String sql = "SELECT \r\n"
				+ "				A.CODPROD,CODLOCALPADRAO, itens.CODVOL,itens.VLRUNIT\r\n"
				+ "				FROM \r\n"
				+ "				TGFPRO A INNER JOIN \r\n"
				+ "				TCSPSC D ON  A.CODPROD = D.CODPROD inner join \r\n"
				+ "				(select VLRUNIT,CODPROD,UNID AS CODVOL     from AD_ITENSLICITACAO A INNER JOIN \r\n"
				+ "AD_LICITACAO b ON b.codlic = A.codlic \r\n"
				+ "where  b.nunota in (SELECT distinct  a.nunotaorig FROM TGFVAR a inner join \r\n"
				+ "tgfvar b on  a.nunota = b.nunotaorig inner join \r\n"
				+ "tgfcab c on c.nunota = b.nunota \r\n"
				+ "where c.numcontrato="+numContrato+") \r\n"
				+ "UNION ALL \r\n"
				+ "select VLRUNIT,CODPROD,CODVOL from AD_LICITACAOCOMPONENTES A INNER JOIN \r\n"
				+ "AD_LICITACAO b ON b.codlic = A.codlic \r\n"
				+ "where b.nunota in (SELECT distinct  a.nunotaorig FROM TGFVAR a inner join \r\n"
				+ "tgfvar b on  a.nunota = b.nunotaorig inner join \r\n"
				+ "tgfcab c on c.nunota = b.nunota\r\n"
				+ "where c.numcontrato="+numContrato+"))itens on itens.CODPROD = D.CODPROD\r\n"
				+ "				WHERE D.CODPROD = "+codProd+"  AND D.NUMCONTRATO="+numContrato;
		return sql;
		
	}

	public static String retornaDadosItensProdutos(String codProd) {
		
		String sql = "select \r\n"
				+ "	AD_DESCRITIVO,\r\n"
				+ "	AD_PROCEDENCIA,\r\n"
				+ "	MARCA,\r\n"
				+ "	AD_NRREGISTRO\r\n"
				+ "	USOPROD\r\n"
				+ "	from tgfpro where CODPROD="+codProd;
		return sql;
		
	}



}