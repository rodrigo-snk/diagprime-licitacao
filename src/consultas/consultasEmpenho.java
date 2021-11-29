package consultas;

public class consultasEmpenho {

	public static String empenhoConsulta(String numContrato) {

		return "select \r\n"
				+ "D.CODPROD,\r\n"
				+ "D.CODVOL,\r\n"
				+ "DESCRPROD,A.NUMCONTRATO, \r\n"
				+ "A.CODPARC, NOMEPARC,\r\n"
				+ "C.RAZAOSOCIAL AS EMPRESA,\r\n"
				+ "d.AD_DISPONIVEL AS QTD_DISPONIVEL,coalesce(d.AD_DISPONIVEL,0) AS AD_DISPONIVEL\r\n"
				+ "from TCSCON A INNER JOIN \r\n"
				+ "TGFPAR B ON A.CODPARC = B.CODPARC LEFT JOIN \r\n"
				+ "TSIEMP C ON C.CODEMP = A.CODEMP LEFT JOIN \r\n"
				+ "AD_ITENSEMPENHO D ON D.NUMCONTRATO = A.NUMCONTRATO  LEFT JOIN \r\n"
				+ "TGFPRO E ON E.CODPROD = D.CODPROD\r\n"
				+ "	WHERE (A.NUMCONTRATO ="+numContrato+") and coalesce(d.AD_DISPONIVEL,0)>0";
	}

}
