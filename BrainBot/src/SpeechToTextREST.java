
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;




public class SpeechToTextREST {

  private static final String REQUEST_URI = "https://speech.platform.bing.com/speech/recognition/%s/cognitiveservices/v1";
  private static final String PARAMETERS = "language=%s&format=%s";

  
  private SpeechAPI.RecognitionMode mode = SpeechAPI.RecognitionMode.Interactive;
  private SpeechAPI.Language language = SpeechAPI.Language.de_DE;
  private SpeechAPI.OutputFormat format = SpeechAPI.OutputFormat.Simple;

  private final Authentication auth;

  public SpeechToTextREST(Authentication auth){
    this.auth = auth;
  }


  public SpeechAPI.RecognitionMode getMode() {
    return mode;
  }

  public void setMode(SpeechAPI.RecognitionMode mode) {
    this.mode = mode;
  }

  public SpeechAPI.Language getLanguage() {
    return language;
  }

  public void setLanguage(SpeechAPI.Language language) {
    this.language = language;
  }

  public SpeechAPI.OutputFormat getFormat() {
    return format;
  }

  public void setFormat(SpeechAPI.OutputFormat format) {
    this.format = format;
  }

  private URL buildRequestURL() throws MalformedURLException {
    String url = String.format(REQUEST_URI, mode.name().toLowerCase());
    String params = String.format(PARAMETERS, language.name().replace('_', '-'), format.name().toLowerCase());
    return new URL(String.format("%s?%s", url, params));
  }

  private HttpURLConnection connect() throws MalformedURLException, IOException {
    HttpURLConnection connection = (HttpURLConnection) buildRequestURL().openConnection();
    connection.setDoInput(true);
    connection.setDoOutput(true); 
    connection.setRequestMethod("POST");
    connection.setRequestProperty("Content-type", "audio/wav; codec=audio/pcm; samplerate=16000");
    connection.setRequestProperty("Accept", "application/json;text/xml");
    connection.setRequestProperty("Authorization", "Bearer " + auth.getToken());
    connection.setChunkedStreamingMode(0); // 0 == default chunk size
    connection.connect();

    return connection;
  }

  private String getResponse(HttpURLConnection connection) throws IOException {
    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
      throw new RuntimeException(String.format("Something went wrong, server returned: %d (%s)",
          connection.getResponseCode(), connection.getResponseMessage()));
    }

    try (BufferedReader reader = 
        new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
      return reader.lines().collect(Collectors.joining());
    }
  }

  private HttpURLConnection upload(InputStream is, HttpURLConnection connection) throws IOException {
    try (OutputStream output = connection.getOutputStream()) {
      byte[] buffer = new byte[1024];
      int length;
      while ((length = is.read(buffer)) != -1) {
        output.write(buffer, 0, length);
      }
      output.flush();
    }
    return connection;
  }

  private HttpURLConnection upload(Path filepath, HttpURLConnection connection) throws IOException {
    try (OutputStream output = connection.getOutputStream()) {
      Files.copy(filepath, output);
    }
    return connection;
  }

  public String process(InputStream is) throws IOException {
    return getResponse(upload(is, connect()));
  }

  public String process(Path filepath) throws IOException {
    return getResponse(upload(filepath, connect()));
  }
}