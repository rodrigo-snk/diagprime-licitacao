package util;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class consulta {
	
	public consulta(String url,String select) throws IOException {
		
		OkHttpClient client = new OkHttpClient();
		MediaType mediaType = MediaType.parse("text/json");
		RequestBody body = RequestBody.create(mediaType, "{\"serviceName\":\"DbExplorerSP.executeQuery\",\"requestBody\":{\"sql\":\""+select+"\"}}");
		Request request = new Request.Builder()
		  .url(url+"/mge/service.sbr?serviceName=DbExplorerSP.executeQuery&outputType=json&preventTransform=false&mgeSession=PWJ0eWadFOZ7d6eng-yFwBH_pL-fHdKtpOnuC6Tg")
		  .post(body)
		  .addHeader("Cookie", "PWJ0eWadFOZ7d6eng-yFwBH_pL-fHdKtpOnuC6Tg,PWJ0eWadFOZ7d6eng-yFwBH_pL-fHdKtpOnuC6Tg; JSESSIONID=PWJ0eWadFOZ7d6eng-yFwBH_pL-fHdKtpOnuC6Tg.centos")

		//  .addHeader("Content-Type", "charset=utf-8")
		  .addHeader("Cache-Control", "no-cache")
		  .addHeader("Accept-Encoding", "charset=utf-8")
		  .addHeader("Connection", "keep-alive")
		  .addHeader("cache-control", "no-cache")
		  .build();

			Response response = client.newCall(request).execute();
			

	}

}
