//import java.io.File;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.StopMessageLiveLocation;
import org.telegram.telegrambots.api.methods.send.SendChatAction;
import org.telegram.telegrambots.api.methods.send.SendLocation;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageLiveLocation;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.File;
import org.telegram.telegrambots.api.objects.Location;
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
			}else if(update.getMessage().hasLocation()) {
				handleLocation(update);
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
	
	private void handleLocation(Update update) {
		Location loc = update.getMessage().getLocation();
		long cid = update.getMessage().getChatId();
		float lat = loc.getLatitude();
		float lon = loc.getLongitude();
		int period = 100;
		SendLocation sl = new SendLocation(lat, lon).setLivePeriod(period);
		try {
			execute(sl);
		} catch (TelegramApiException e1) {
			e1.printStackTrace();
		}
		Runnable updater = () -> {
			float latitude = lat;
			float longitude = lon;
			for(int cnt = 0; cnt < period; cnt++) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				latitude += 0.0001;
				longitude += 0.0001;
				EditMessageLiveLocation emll = new EditMessageLiveLocation().setChatId(cid).setLatitude(latitude).setLongitud(longitude);
				try {
					execute(emll);
				} catch (TelegramApiException e) {
					e.printStackTrace();
				}
			}
			StopMessageLiveLocation smll = new StopMessageLiveLocation().setChatId(cid);
			try {
				execute(smll);
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		};
		updater.run();
	}

}
