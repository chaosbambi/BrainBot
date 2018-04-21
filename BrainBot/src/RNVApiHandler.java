import java.io.IOException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class RNVApiHandler {
	public void getData(String dataPath){
		final String urlMain = "http://rnv.the-agent-factory.de:8080/easygo2/api";
		
		try(CloseableHttpClient client = HttpClientBuilder.create().build()){
			HttpGet request = new HttpGet(urlMain + dataPath);
			request.addHeader("RNV_API_TOKEN", Sensitive.getRnvToken());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
