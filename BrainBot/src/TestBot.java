//import java.io.File;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.File;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class TestBot extends TelegramLongPollingBot{

	@Override
	public String getBotUsername() {
		return "BrainBot Test";
	}

	@Override
	public void onUpdateReceived(Update update) {


		if(update.hasMessage()) {
			if(update.getMessage().hasText()) {
				receiveText(update);
			}else if(update.getMessage().getVoice() != null) {
				saveVoice(update);
			}
		}
	}

	/*
	 * This method gathers the personal information from the User
	 */
	private void startWelcomeDialog(Message message) {
		User maybeAdmin = message.getFrom();
		System.out.println(maybeAdmin.getId());
	}

	@Override
	public String getBotToken() {
		return Sensitive.getKey();
	}
	
	/**
	 * Empfängt text aus einer nachricht.
	 * @param update Das Update-Objekt, was die Verarbeitung ausgelöst hat.
	 */
	private void receiveText(Update update) {
		SendMessage sendMsg = new SendMessage().setChatId(update.getMessage().getChatId());
		if(update.getMessage().getText().toLowerCase().trim().matches("ping[\\.!]*")) {
			sendMsg.setText("Pong!");
		}else if(update.getMessage().getText().equals("/start")){
			startWelcomeDialog(update.getMessage());
		}else {
			sendMsg.setText("Sorry, das habe ich nicht verstanden...");
		}
		try {
			execute(sendMsg);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Speichert eine empfangene Voice Nachricht als ogg datei
	 * @param update Das Update-Objekt, was die Verarbeitung ausgelöst hat.
	 */
	private void saveVoice(Update update) {
		String fileID = update.getMessage().getVoice().getFileId();
		GetFile getfile = new GetFile().setFileId(fileID);
		File tf;
		String url = null;
		try {
			tf = this.execute(getfile);
			url = tf.getFileUrl(Sensitive.getKey());
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
		if(url != null && !url.isEmpty()) {
			URL link = null;
			try {
				link = new URL(url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			java.io.File target = new java.io.File("voice/"+fileID+".ogg");
			try {
				FileUtils.copyURLToFile(link, target, 1000, 2000);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
