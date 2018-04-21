import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class RNVApiHandler {
	public void getData(String dataPath){
		final String urlMain = "http://rnv.the-agent-factory.de:8080/easygo2/api";
		
		try(CloseableHttpClient client = HttpClientBuilder.create().build()){
			HttpGet request = new HttpGet(urlMain + dataPath);
			request.addHeader("RNV_API_TOKEN", Sensitive.getRnvToken());
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			if(entity != null) {
				File target = new File("rnv/response.txt");
				InputStream inStream = entity.getContent();
				
				if(!target.exists()) {
					target.getParentFile().mkdirs();
					target.createNewFile();
				}
				
				Files.copy(inStream, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
				
				IOUtils.closeQuietly(inStream);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
