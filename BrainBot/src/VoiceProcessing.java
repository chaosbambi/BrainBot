


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;


public class VoiceProcessing {
	
	private String filepath = "/test.wav";
	private File voiceFileOgg = null;
	private File voiceFileWav = null;
	
	
	VoiceProcessing(File voiceFile) throws IOException, UnsupportedAudioFileException{
		this.voiceFileOgg = voiceFile;
		convertToWav();
	}
	
	String process() throws IOException {
		SpeechToTextREST client = new SpeechToTextREST();

		InputStream input = new FileInputStream(voiceFileWav);
		return client.process(input);
	}
	
	void convertToWav() throws IOException, UnsupportedAudioFileException {
			/*
		    AudioInputStream source = AudioSystem.getAudioInputStream(voiceFileOgg);
		    AudioInputStream pcm = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, source);
		    AudioInputStream ulaw = AudioSystem.getAudioInputStream(AudioFormat.Encoding.ULAW, pcm);
		    AudioSystem.write(ulaw, AudioFileFormat.Type.WAVE, voiceFileWav);
		    */
		
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
