package processamento;



import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.codec.binary.Base64;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class incluirNota {


	public static Retorno1 IncluirNota(String url,String CODPARC,String CODEMP,String DTNEG,String CODTIPVENDA,String jsessionid,String codTipOper) throws  Exception{
		

		OkHttpClient client = new OkHttpClient().newBuilder()
				  .connectTimeout(7200, TimeUnit.SECONDS)
				  .writeTimeout(7200, TimeUnit.SECONDS)
				  .readTimeout(7200, TimeUnit.SECONDS)
				  .build();
				MediaType mediaType = MediaType.parse("application/xml");
				String novo = "<serviceRequest serviceName=\"CACSP.incluirAlterarCabecalhoNota\">\n\t<requestBody>\n\t<nota>\n  <cabecalho>\n   <NUNOTA/>\n    <NUMNOTA>0</NUMNOTA>\n    <TIPMOV>P</TIPMOV>\n    <CODTIPVENDA>"+CODTIPVENDA+"</CODTIPVENDA>\n    <CODPARC>"+CODPARC+"</CODPARC>\n     <DTNEG>"+DTNEG+"</DTNEG>\n    <CODTIPOPER>"+codTipOper+"</CODTIPOPER>\n    <CODEMP>"+CODEMP+"</CODEMP>\n    <SERIENOTA>1</SERIENOTA>\n </cabecalho>\n</nota>\n</requestBody></serviceRequest>";
				System.out.println(novo);
				System.out.println(url+"/mgecom/service.sbr?serviceName=CACSP.incluirAlterarCabecalhoNota&mgeSession="+jsessionid);
				RequestBody body = RequestBody.create(mediaType, "<serviceRequest serviceName=\"CACSP.incluirAlterarCabecalhoNota\">\n\t<requestBody>\n\t<nota>\n  <cabecalho>\n   <NUNOTA/>\n    <NUMNOTA>0</NUMNOTA>\n    <TIPMOV>P</TIPMOV>\n    <CODTIPVENDA>"+CODTIPVENDA+"</CODTIPVENDA>\n    <CODPARC>"+CODPARC+"</CODPARC>\n     <DTNEG>"+DTNEG+"</DTNEG>\n    <CODTIPOPER>"+codTipOper+"</CODTIPOPER>\n    <CODEMP>"+CODEMP+"</CODEMP>\n    <SERIENOTA>1</SERIENOTA>\n </cabecalho>\n</nota>\n</requestBody></serviceRequest>");
				Request request = new Request.Builder()
				  .url(url+"/mgecom/service.sbr?serviceName=CACSP.incluirAlterarCabecalhoNota&mgeSession="+jsessionid)
				  .method("POST", body)
				  .addHeader("Cookie", jsessionid)
				  .addHeader("Content-Type", "application/xml")
				  .build();
				Retorno1 endereco = new Retorno1();
				try {
					Response response = client.newCall(request).execute();
					//System.out.println(response.body().string());
					if(response.isSuccessful()) {
						
						DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
						DocumentBuilder db = dbf.newDocumentBuilder();
						String xml = (response.body().string()).replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");
						System.out.println(xml);
						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();						
						InputSource inStream = new InputSource();
						inStream.setCharacterStream(new StringReader(xml));
						Document doc = db.parse(inStream);
						//Criamos um objeto Element que vai receber as informacoes de doc
						Element raiz = doc.getDocumentElement();
					
						
						NodeList endList = raiz.getElementsByTagName("statusMessage");
						if((endList.getLength())>0) {
						Element endElement = (Element) endList.item(0);
			            
						 endereco.setStatusMessage(endElement.getTextContent());
						 Base64 base64 = new Base64();

						String mensagem = endereco.getStatusMessage();
						byte[] bytes = mensagem.getBytes(StandardCharsets.UTF_8);
						//System.out.println(new String(base64.decodeBase64(bytes)));
						endereco.setRetorno(false);

						}else {
							NodeList endList1 = raiz.getElementsByTagName("NUNOTA");
							if((endList1.getLength())>0) {
								Element endElement = (Element) endList1.item(0);
								endereco.setNunota(endElement.getTextContent());
							}
							 endereco.setStatusMessage("IMPORTADO OK");
							endereco.setRetorno(true);

						}
						return endereco;
						//String retorno = new String(base64.decode(mensagem));
						//System.out.println(retorno);
			
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//contexto.mostraErro("ERRO : "+e.toString());
					throw new Error("ERRO : "+e);
					//e.printStackTrace();
				}
				endereco.setRetorno(false);
				return endereco;
				
	}
	

	public static String getChildTagValue(Element elem, String tagName) throws Exception {
		NodeList children = elem.getElementsByTagName(tagName);
		String result = null;
		 //children, a tag pai que estamos lendo,
		 // por exemplo a tag 
		if (children == null) {
			return result;
		}
		 //child, a tag que queremos recuperar o valor, por exemplo
		 //a tag 
		Element child = (Element) children.item(0);

		if (child == null) {
			return result;
		}
		 //recuperamos o texto contido na tagName   
		result = child.getTextContent();

		return result;
	}
	
	
}
