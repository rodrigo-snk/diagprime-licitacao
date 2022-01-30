package util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DebugTest {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		//SWServiceInvoker si = new SWServiceInvoker("http://200.251.88.55:9044","SUP","zrPb4${ibk");
		login loginNews = new login();
		String chave = loginNews.loginNovo1("http://200.251.88.55:9044","SUP","zrPb4${ibk");

		Date data1 = new Date();
		SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy");
		String DTNEG = formatador.format(data1);
		
		//x'Retorno ret = CabecalhoNota.incluirNota("http://200.251.88.55:9044", 205+"", "1", DTNEG, 27+"", chave,"1006");
		try {
		//	String chave = si.doLogin();
			System.out.println(chave);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
