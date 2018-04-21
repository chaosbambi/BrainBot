
//import java.io.File;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendLocation;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Contact;
import org.telegram.telegrambots.api.objects.File;
import org.telegram.telegrambots.api.objects.Location;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class TestBot extends TelegramLongPollingBot {

	private DialogStates dState = DialogStates.PendingForDialog;
	private WelcomeDialogStates wdState = WelcomeDialogStates.DialogUnfinished;

	private HashMap<Long,UserData> users = new HashMap<>();
	
	private ComplainDialogStates cdState = ComplainDialogStates.NO_COMPLAIN_DIALOG_IN_USE;

	@Override
	public String getBotUsername() {
		return "BrainBot Test";
	}

	@Override
	public String getBotToken() {
		return Sensitive.getKey();
	}

	@Override
	public void onUpdateReceived(Update update) {

		if (update.hasMessage()) {
			if (update.getMessage().hasText()) {
				handleText(update);
			} else if (update.getMessage().getVoice() != null) {
				VoiceProcessing vp;
				try {
					vp = new VoiceProcessing(handleVoice(update));
					String message = vp.process();
					System.out.println(message);
				} catch (IOException | UnsupportedAudioFileException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				handleVoice(update);
			} else if (update.getMessage().hasLocation()) {
				locationTest(update);
			} else if (update.getMessage().hasContact()) {
				handleContact(update);
			}
		}
	}

	/**
	 * Receives text based messages.
	 * 
	 * @param update
	 *            The Update object that triggered the evaluation
	 */
	private void handleText(Update update) {
		SendMessage sendMsg = new SendMessage().setChatId(update.getMessage().getChatId());
		if (update.getMessage().getText().toLowerCase().trim().matches("ping[\\.!]*")) {
			sendMsg.setText("Pong!");
			try {
				execute(sendMsg);
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		} else if (dState == DialogStates.WelcomeDialog) {

			processWelcomeDialog(update.getMessage());


		} else if (update.getMessage().getText().equals("/start")) {

			if(users.containsKey(update.getMessage().getChatId())){
				//breche ab
			} else if(UserData.checkForUser(update.getMessage().getChatId())) {
				users.put(update.getMessage().getChatId(),null);
				//breche ab
			}
			if (wdState == WelcomeDialogStates.DialogUnfinished) {
				dState = DialogStates.WelcomeDialog;

				sendMsg.setText("Hi, ich bin dein BrainGrid-Bot.\n"
						+ "Zu Beginn möchte ich dich nach ein paar Informationen zu deiner Person fragen, "
						+ "damit du in Zukunft alle Funktionen ganz bequem und auf schnellstem Wege nutzen kannst.");
				try {
					execute(sendMsg);
				} catch (TelegramApiException e) {
					e.printStackTrace();
				}

				processWelcomeDialog(update.getMessage());
			}

		} else if (dState == DialogStates.ComplainDialog) {

			processComplainDialog(update.getMessage());

		} else if (update.getMessage().getText().equals("/complain")) {

			if (cdState == ComplainDialogStates.NO_COMPLAIN_DIALOG_IN_USE) {
				dState = DialogStates.ComplainDialog;

				sendMsg.setText("Hi, gerne nehme ich deine Beschwerde auf.");

				try {
					execute(sendMsg);
				} catch (TelegramApiException e) {
					e.printStackTrace();
				}

				processComplainDialog(update.getMessage());
			}

		} else if (update.getMessage().getText().equals("/here")) {
			startLocationDialog(update.getMessage());
		} else if (update.getMessage().getText().equals("Nein, danke.")) {
			sendMsg.setText("Schade. So ist es für mich schwieriger die nächste Haltestelle zu finden \u2639");
			try {
				execute(sendMsg);
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		} else {
			sendMsg.setText("Sorry, das habe ich nicht verstanden...");
			try {
				execute(sendMsg);
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Prints received contact information to console. Can later handle database
	 * entry management.
	 * 
	 * @param update
	 *            The Update that contained the message with the contact info
	 */
	private void handleContact(Update update) {
		User sender = update.getMessage().getFrom();
		Contact cont = update.getMessage().getContact();
		System.out.println("Daten Erhalten:");
		System.out.println("User ID: " + sender.getId()); // In User und Contact enthalten
		System.out.println("Username: " + sender.getUserName()); // May be Null
		System.out.println("Name: " + cont.getFirstName() + " " + cont.getLastName()); // May contain null parts - in
																						// User und Contact enthalten
		System.out.println("Tel.: " + cont.getPhoneNumber());
	}

	/**
	 * Proper location handling TODO
	 * 
	 * @param update
	 */
	private void handleLocation(Update update) {
		Location loc = update.getMessage().getLocation();
		HashMap<String, Location> stops = new HashMap<>();
		HashMap<String, Double> distances = new HashMap<>();
		// Maybe other maps? List of Map.Entry? Will need to be sorted!
		// Query possible Stops, put them in "stops"
		while (stops.entrySet().iterator().hasNext()) {
			Map.Entry<String, Location> kvpair = stops.entrySet().iterator().next();
			distances.put(kvpair.getKey(), Math.pow(kvpair.getValue().getLatitude() - loc.getLatitude(), 2.0)
					+ Math.pow(kvpair.getValue().getLongitude() - loc.getLongitude(), 2.0));
		}
		// Sort by distance
		// Select stop with lowest distance
		// Runtime: O(way too much)
		/*
		 * This is only a basic concept for finding the closest possible stop. More
		 * advanced systems like travel time vs stop distance calculation would require
		 * a more refined algorithm.
		 */

	}

	/**
	 * Speichert eine empfangene Voice Nachricht als ogg datei
	 * 
	 * @param update
	 *            Das Update-Objekt, was die Verarbeitung ausgelöst hat.
	 */
	private java.io.File handleVoice(Update update) {
		java.io.File target = null;
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
		if (url != null && !url.isEmpty()) {
			URL link = null;
			try {
				link = new URL(url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			target = new java.io.File("voice/" + fileID + ".ogg");
			try {
				FileUtils.copyURLToFile(link, target, 1000, 2000);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return target;
	}

	/*
	 * This method gathers the personal information from the User
	 * 
	 * @param message The Message that requested the start dialogue
	 */
	private void processWelcomeDialog(Message message) {
		SendMessage sendMsg = new SendMessage().setChatId(message.getChatId());
		String msgText = "Ups, da ist wohl ein Fehler aufgetreten.";

		switch (wdState) {
		case DialogUnfinished:
			users.put(message.getChatId(), new UserData(message.getChatId()));

			msgText = "Wie lautet dein Nachname?";
			wdState = WelcomeDialogStates.REQUESTED_LAST_NAME;
			break;

		case REQUESTED_LAST_NAME:

			users.get(message.getChatId()).setLastName(message.getText());

			msgText = "Sehr gut. Und dein Vorname?";
			wdState = WelcomeDialogStates.REQUESTED_FIRST_NAME;
			break;

		case REQUESTED_FIRST_NAME:
			users.get(message.getChatId()).setFirstName(message.getText());

			msgText = "Adresse";
			wdState = WelcomeDialogStates.REQUESTED_ADDRESS;
			break;

		case REQUESTED_ADDRESS:
			users.get(message.getChatId()).setAddress(message.getText());

			msgText = "Ort";
			wdState = WelcomeDialogStates.REQUESTED_CITY;
			break;

		case REQUESTED_CITY:
			users.get(message.getChatId()).setCity(message.getText());

			msgText = "Telephonnummer";
			wdState = WelcomeDialogStates.REQUESTED_PHONE;
			break;

		case REQUESTED_PHONE:

			users.get(message.getChatId()).setTel(message.getText());

			msgText = "Email";
			wdState = WelcomeDialogStates.REQUESTED_MAIL;
			break;

		case REQUESTED_MAIL:

			if (message.getText().trim().toLowerCase().equals("abbrechen")) {

				wdState = WelcomeDialogStates.DialogUnfinished;
				dState = DialogStates.PendingForDialog;

				
			}else {
				users.get(message.getChatId()).setMail(message.getText());

				msgText = "Vielen Dank";
				dState = DialogStates.PendingForDialog;
				wdState = WelcomeDialogStates.DialogFinished;
				users.get(message.getChatId()).saveInDb();
			}

			break;

		default:
			break;
		}

		sendMsg.setText(msgText);
		try {
			execute(sendMsg);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}

		User maybeAdmin = message.getFrom();
		/*
		 * KeyboardButton kb = new
		 * KeyboardButton("Darf ich deine Telefonnummer haben?");
		 * kb.setRequestContact(true); KeyboardRow kr = new KeyboardRow(); kr.add(kb);
		 * ArrayList<KeyboardRow> rows = new ArrayList<>(); rows.add(kr);
		 * ReplyKeyboardMarkup rkm = new
		 * ReplyKeyboardMarkup().setKeyboard(rows).setOneTimeKeyboard(true);
		 * 
		 * try {
		 * sendMsg.setChatId(message.getChatId()).setReplyMarkup(rkm).setText("Anfrage:"
		 * ); execute(sendMsg); } catch (TelegramApiException e) { e.printStackTrace();
		 * }
		 */
		System.out.println(maybeAdmin.getId());
	}

	private void processComplainDialog(Message message) {
		SendMessage sendMsg = new SendMessage().setChatId(message.getChatId());
		String msgText = "Ups, da ist wohl ein Fehler aufgetreten.";

		switch (cdState) {
		case COMPLAIN_DIALOG_STARTED:
			
			msgText = "Ort:";
			cdState = ComplainDialogStates.REQUESTED_PLACE;
			break;

		case REQUESTED_PLACE:

			msgText = "Grund:";
			cdState = ComplainDialogStates.REQUESTED_REASON;
			break;

		case REQUESTED_REASON:

			msgText = "Linie:";
			cdState = ComplainDialogStates.REQUESTED_LINE;
			break;

		case REQUESTED_LINE:

			msgText = "Richtung:";
			cdState = ComplainDialogStates.REQUESTED_DIRECTION;
			break;

		case REQUESTED_DIRECTION:

			msgText = "Uhrzeit:";
			cdState = ComplainDialogStates.REQUESTED_TIME;
			break;

		case REQUESTED_TIME:

			msgText = "Datum:";
			cdState = ComplainDialogStates.REQUESTED_DATE;
			break;

		case REQUESTED_DATE:

			msgText = "Haltestelle:";
			cdState = ComplainDialogStates.REQUESTED_STATION;
			break;

		case REQUESTED_STATION:

			msgText = "Nachricht:";
			cdState = ComplainDialogStates.REQUESTED_MESSAGE;
			break;

		case REQUESTED_MESSAGE:

			msgText = "Vielen Dank. Wir werden uns dem Problem schnellstmöglich annehmen.";
			cdState = ComplainDialogStates.COMPLAIN_SEND;
			break;

		case COMPLAIN_SEND:

			cdState = ComplainDialogStates.NO_COMPLAIN_DIALOG_IN_USE;
			break;

		default:
			break;
		}
	}

	/**
	 * Starts the user dialogue requesting a location share.
	 * 
	 * @param message
	 *            Message that requested a location evaluation
	 */
	private void startLocationDialog(Message message) {
		KeyboardButton kbLoc = new KeyboardButton("Standort angeben");
		KeyboardButton kbNo = new KeyboardButton("Nein, danke.");
		kbLoc.setRequestLocation(true);
		KeyboardRow kr = new KeyboardRow();
		kr.add(kbLoc);
		kr.add(kbNo);
		ArrayList<KeyboardRow> rows = new ArrayList<>();
		rows.add(kr);
		ReplyKeyboardMarkup rkm = new ReplyKeyboardMarkup().setKeyboard(rows).setOneTimeKeyboard(true);

		try {
			SendMessage sendMsg = new SendMessage().setChatId(message.getChatId())
					.setText("Möchtest du deinen Standort angeben?").setReplyMarkup(rkm);
			execute(sendMsg);
		} catch (TelegramApiException e) {
			System.err.println("No message sent:");
			e.printStackTrace();
		}
	}

	/**
	 * Location handling proof of concept A received location gets mirrored and then
	 * moves with live updates
	 * 
	 * @param update
	 *            The Update-Object that triggered message processing
	 */
	private void locationTest(Update update) {
		Location loc = update.getMessage().getLocation();
		long cid = update.getMessage().getChatId();
		float lat = loc.getLatitude();
		float lon = loc.getLongitude();
		int period = 120;
		SendLocation sl = new SendLocation(lat, lon).setLivePeriod(period).setChatId(cid);
		Message sent = null;
		try {
			sent = execute(sl);
		} catch (TelegramApiException e1) {
			e1.printStackTrace();
		}
		if (sent != null) {
			Thread updater = new Thread(new LocationUpdater(sent.getMessageId(), cid, sl, this));
			updater.start();
		}
	}

}
