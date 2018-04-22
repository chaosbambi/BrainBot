import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.telegram.telegrambots.api.objects.Location;

import java.lang.reflect.Type;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class RNVApiHandler {
	
	private static Map<String,RNVHaltestelle> haltestellen;
	
	public RNVApiHandler() {
		if(haltestellen == null) {
			checkUpdate();
		}
	}
	
	public void checkUpdate() {
		File target = new File("rnv/lastUpdate.txt");
		Gson gson = new Gson();
		Map<String, String> dates = null;
		Type type = new TypeToken<Map<String, String>>(){private static final long serialVersionUID = 1L;}.getType();
		if(target.exists()) {
			JsonReader jr = null;
			try {
				jr = new JsonReader(new FileReader(target));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			if(jr != null) {
				dates = gson.fromJson(jr, type);
			}
		}else {
			dates = new HashMap<String, String>();
			dates.put("Haltestellen", "2000-01-01+00:01");
			dates.put("Linien", "2000-01-01+00:01");
			dates.put("Dummy", "2000-01-01+00:01");
		}
		String reqString = "/update?regionID=1&time=" + dates.get("Haltestellen") + "$" + dates.get("Linien") + "$" + dates.get("Dummy");
		System.out.println(reqString);
		String jsonString = getHttpStream(reqString, true);
		RNVUpdateInfo[] udi = null;
		if(jsonString != null && !jsonString.isEmpty()) {
			udi = gson.fromJson(jsonString, RNVUpdateInfo[].class);
			if(udi.length > 0) {
				System.out.println("Description:" + udi[0].description);
				LocalDateTime ldt = LocalDateTime.now();
				DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm");
				String formattedDateTime = ldt.format(dtFormatter);
				//Enter new Date and Time of Update
				dates.replace("Haltestellen", formattedDateTime);
				dates.replace("Linien", formattedDateTime);
				dates.replace("Dummy", formattedDateTime);
				
				String generatedJson = gson.toJson(dates, type);
				try (PrintStream out = new PrintStream(new FileOutputStream(target))) {
				    out.print(generatedJson);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		
		if(udi != null && udi.length > 0) {
			for (RNVUpdateInfo rnvUpdateInfo : udi) {
				if(rnvUpdateInfo.action.equals("CHANGED")) {
					//Load update
				}
			}
		}
		
		reqString = "https://opendata.rnv-online.de/sites/default/files/Haltestellen_mit_Linienreferenz_81.json";
		System.out.println(reqString);
		jsonString = getHttpStream(reqString, false);
		Type datType = new TypeToken<Map<String,RNVHaltestelle>>(){private static final long serialVersionUID = 1L;}.getType();
		RNVApiHandler.haltestellen = gson.fromJson(jsonString, datType);
	}
	
	public RNVHaltestelle getClosestStop(Location loc) {
		HashMap<String, Double> distances = new HashMap<>();
		for(String s1: RNVApiHandler.haltestellen.keySet()){
			for(RNVHaltestellenStop rhs : RNVApiHandler.haltestellen.get(s1).stops) {
				distances.put(s1, Math.pow(rhs.lat - loc.getLatitude(), 2.0) + Math.pow(rhs.lon - loc.getLongitude(), 2.0));
				break;
			}
		}
		double min = Double.MAX_VALUE; //Initialize with max
		String busStop = null;
		for(String s2: distances.keySet()) {
			if(distances.get(s2) < min) {
				min = distances.get(s2);
				busStop = s2;
			}
		}
		if(busStop != null && !busStop.isEmpty()) {
			return RNVApiHandler.haltestellen.get(busStop);
		}else {
			return null;
		}
	}
	
	public RNVHaltestelle getStopByContainedName(String stopName) {
		for(String s : RNVApiHandler.haltestellen.keySet()) {
			if(stopName.toLowerCase().contains(RNVApiHandler.haltestellen.get(s).name.toLowerCase())) {
				return RNVApiHandler.haltestellen.get(s);
			}
		}
		return null;
	}
	
	public RNVHaltestelle getClosestStopWithLine(Location loc, String transportLine) {
		String[] lines = {transportLine};
		return getClosestStopWithLines(loc, lines);
	}
	
	public RNVHaltestelle getClosestStopWithLines(Location loc, String[] transportLines) {
		boolean match = false;
		HashMap<String, Double> distances = new HashMap<>();
		for(String s1: RNVApiHandler.haltestellen.keySet()){
			match = false;
			for(RNVHaltestellenStop rhs : RNVApiHandler.haltestellen.get(s1).stops) {
				if(match) {
					break;
				}
				for(String line1 : rhs.lines) {
					if(match) {
						break;
					}
					for(String line2 : transportLines) {
						if(line1.equals(line2)) {
							distances.put(s1, Math.pow(rhs.lat - loc.getLatitude(), 2.0) + Math.pow(rhs.lon - loc.getLongitude(), 2.0));
							match = true;
							break;
						}
					}
				}
			}
		}
		double min = Double.MAX_VALUE; //Initialize with max
		String busStop = null;
		for(String s2: distances.keySet()) {
			if(distances.get(s2) < min) {
				min = distances.get(s2);
				busStop = s2;
			}
		}
		if(busStop != null && !busStop.isEmpty()) {
			return RNVApiHandler.haltestellen.get(busStop);
		}else {
			return null;
		}	
	}
	
	public void getData(String dataPath, String filename){
		final String urlMain = "http://rnv.the-agent-factory.de:8080/easygo2/api";
		
		try(CloseableHttpClient client = HttpClientBuilder.create().build()){
			HttpGet request = new HttpGet(urlMain + dataPath);
			request.addHeader("RNV_API_TOKEN", Sensitive.getRnvToken());
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			if(entity != null) {
				File target = new File(filename);
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
	
	public String getHttpStream(String dataPath, boolean api){
		final String urlMain = "http://rnv.the-agent-factory.de:8080/easygo2/api";
		
		try(CloseableHttpClient client = HttpClientBuilder.create().build()){
			HttpGet request = new HttpGet(api?(urlMain + dataPath):dataPath);
			request.addHeader("RNV_API_TOKEN", Sensitive.getRnvToken());
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			if(entity != null) {
				InputStream is = entity.getContent();
				String jsonString;
				try {
					jsonString = IOUtils.toString(is, StandardCharsets.UTF_8);
					IOUtils.closeQuietly(is);
					System.out.println(jsonString);
					return jsonString;
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
	}
}
