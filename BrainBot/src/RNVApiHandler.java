import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.lang.reflect.Type;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class RNVApiHandler {
	public boolean getUpdate() {
		File target = new File("rnv/lastUpdate.txt");
		Map<String, String> dates = null;
		
		if(target.exists()) {
			Gson gson = new Gson();
			JsonReader jr = null;
			try {
				jr = new JsonReader(new FileReader(target));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			Type type = new TypeToken<Map<String, String>>(){private static final long serialVersionUID = 1L;}.getType();
			if(jr != null) {
				dates = gson.fromJson(jr, type);
			}
		}else {
			dates = new HashMap<String, String>();
			dates.put("Haltestellen", "2000-01-01+00:01");
			dates.put("Linien", "2000-01-01+00:01");
			dates.put("Dummy", "2000-01-01+00:01");
		}
		getData("/update?region=1&time=" + dates.get("Haltestellen") + "$" + dates.get("Linien") + "$" + dates.get("Dummy"), "lastUpdate.txt");
		
		return false;
	}
	
	/*
	private String readFile(File file) {
		byte[] encoded;
		try {
			encoded = Files.readAllBytes(file.toPath());
			return new String(encoded, Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	*/
	
	public void getData(String dataPath, String filename){
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
