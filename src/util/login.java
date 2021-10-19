package util;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class login {
	
	
	public  String loginNovo1(String url,String usuario,String senha) throws IOException, Error, ParserConfigurationException, SAXException {
		
		OkHttpClient client = new OkHttpClient();

		String retornoNovo = "";
		MediaType mediaType = MediaType.parse("text/xml");
		RequestBody body = RequestBody.create(mediaType, "<serviceRequest serviceName=\"MobileLoginSP.login\">\n<requestBody>\n <NOMUSU>"+usuario+"</NOMUSU>\n <INTERNO>"+senha+"</INTERNO>\n </requestBody>\n</serviceRequest>");
		Request request = new Request.Builder()
		  .url(url+"/mge/service.sbr?serviceName=MobileLoginSP.login")
		  .post(body)
		  .build();

		   Response response = client.newCall(request).execute();
		   // System.out.println(response.body().string());
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			String xml = (response.body().string()).replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");
			InputSource inStream = new InputSource();
			inStream.setCharacterStream(new StringReader(xml));
			Document doc = db.parse(inStream);
			//Criamos um objeto Element que vai receber as informacoes de doc
			Element raiz = doc.getDocumentElement();
			NodeList endList1 = raiz.getElementsByTagName("jsessionid");
			//System.out.println(endList1);
			if((endList1.getLength())>0) {
				Element endElement = (Element) endList1.item(0);
				retornoNovo = endElement.getTextContent();
			}
	
		return retornoNovo;
	}
	
	
}
