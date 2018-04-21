

import java.io.File;

import java.io.IOException;


import javax.sound.sampled.UnsupportedAudioFileException;


public class VoiceProcessing {

	private File voiceFileOgg = null;
	private File voiceFileWav = null;
	
	
	VoiceProcessing(File voiceFile) throws IOException, UnsupportedAudioFileException{
		this.voiceFileOgg = voiceFile;
		convertToWav();
	}
	
	String process() throws IOException {
		SpeechToTextREST client = new SpeechToTextREST(new Authentication(Sensitive.getToken()));

		return client.process(voiceFileWav.toPath());
	}
	
	void convertToWav() throws IOException, UnsupportedAudioFileException {
			
		
			/*
			 * TODO Make this async
			 */
		
		    String filePath = voiceFileOgg.getPath().substring(0, voiceFileOgg.getPath().length() - 4);
		    try {
		    	System.out.println("FFMpeg conversion start:");
		    	String command[] = {"lib/ffmpeg", "-i",voiceFileOgg.getPath(), filePath + ".wav"};
		    	Process p = Runtime.getRuntime().exec(command);
		    	p.waitFor();
		    	System.out.println("FFMpeg conversion done!");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		    
		    this.voiceFileWav = new File(voiceFileOgg.getPath().substring(0, voiceFileOgg.getPath().length() - 4) + ".wav"); 
	}
}
