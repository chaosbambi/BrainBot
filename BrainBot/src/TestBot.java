
//import java.io.File;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TestBot extends TelegramLongPollingBot {

	private DialogStates dState = DialogStates.PendingForDialog;
	private WelcomeDialogStates wdState = WelcomeDialogStates.DialogUnfinished;
	private ComplainDialogStates cdState = ComplainDialogStates.NO_COMPLAIN_DIALOG_IN_USE;
	private LocationDialogStates ldState = LocationDialogStates.NO_REQUEST_PENDING;
	
	private HashMap<Long,UserData> users = new HashMap<>();
	private HashMap<Long,ComplainForm> cfs = new HashMap<>();
	private HashMap<Long,RNVHaltestelle> destinations = new HashMap<>();
	private HashMap<Long,RNVHaltestelle> origins = new HashMap<>();
	
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
				handleText(update.getMessage().getChatId(),update.getMessage().getText());
			} else if (update.getMessage().getVoice() != null) {
				VoiceProcessing vp;
				try {
					vp = new VoiceProcessing(handleVoice(update));
					Gson gson = new GsonBuilder().create(); 
					VoiceResponse vr = gson.fromJson(vp.process(), VoiceResponse.class);
					System.out.println(vr.getDisplayText());
					handleText(update.getMessage().getChatId(), vr.getDisplayText());
					
				} catch (IOException | UnsupportedAudioFileException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else if (update.getMessage().hasLocation()) {
				handleLocation(update);
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
	private void handleText(long chatId, String text) {
		SendMessage sendMsg = new SendMessage().setChatId(chatId);
		if (text.toLowerCase().trim().equals("ping")) {
			sendMsg.setText("Pong!");
			try {
				execute(sendMsg);
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		} else if (dState == DialogStates.WelcomeDialog) {

			processWelcomeDialog(chatId, text);


		} else if (text.toLowerCase().trim().contains("start")) {
			UserData newUser = null;
			if(users.containsKey(chatId)){
				//breche ab
			} else {
				newUser = UserData.checkForUser(chatId);
				if( newUser != null) {
					users.put(chatId,newUser);
					//breche ab
				}
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

				processWelcomeDialog(chatId, text);
			}

		} else if (dState == DialogStates.ComplainDialog) {

			processComplainDialog(chatId, text);

		} else if (text.equals("/complain") || text.toLowerCase().trim().contains("beschwer")) {

			if (cdState == ComplainDialogStates.NO_COMPLAIN_DIALOG_IN_USE) {
				dState = DialogStates.ComplainDialog;

				sendMsg.setText("Hi, gerne nehme ich deine Beschwerde auf.");

				try {
					execute(sendMsg);
				} catch (TelegramApiException e) {
					e.printStackTrace();
				}

				cdState = ComplainDialogStates.COMPLAIN_DIALOG_STARTED;
				processComplainDialog(chatId,text);
			}

		} else if (dState == DialogStates.LocationDialog) {

			processLocationDialog(chatId, text);
		 
		}else if (text.toLowerCase().trim().contains("hier")) {
			
			if (ldState == LocationDialogStates.NO_REQUEST_PENDING) {
				dState = DialogStates.LocationDialog;

				sendMsg.setText("Alles klar, gerne schlage ich dir eine Verbindung vor.");

				try {
					execute(sendMsg);
				} catch (TelegramApiException e) {
					e.printStackTrace();
				}

				ldState = LocationDialogStates.LOCATION_DIALOG_STARTED;
				processLocationDialog(chatId, text);
			}

		
		} else if (text.toLowerCase().trim().contains("nein, danke")) {
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
		if(wdState == wdState.REQUESTED_PHONE) {
			processWelcomeDialog(update.getMessage().getChatId(),cont.getPhoneNumber());
		}
	}

	/**
	 * Proper location handling TODO
	 * 
	 * @param update
	 */
	private void handleLocation(Update update) {
		Location loc = update.getMessage().getLocation();
		RNVApiHandler rnvApi = new RNVApiHandler();
		SendMessage sendMsg = new SendMessage().setChatId(update.getMessage().getChatId());
		
		if(dState == DialogStates.LocationDialog && ldState == LocationDialogStates.STARTINGPOINT_REQUESTED) {
			Set<String> lines = new HashSet<>();
			for(RNVHaltestellenStop s : destinations.get(update.getMessage().getChatId()).stops) {
				lines.addAll(Arrays.asList(s.lines));
			}
			String[] stringarr = null;
			origins.put(update.getMessage().getChatId(), rnvApi.getClosestStopWithLines(loc, lines.toArray(stringarr)));
			
			boolean match = false;
			String line = null;
			for(RNVHaltestellenStop rhs1 :this.destinations.get(update.getMessage().getChatId()).stops) {
				if(match) {
					break;
				}
				for(RNVHaltestellenStop rhs2 : this.origins.get(update.getMessage().getChatId()).stops) {
					if(match) {
						break;
					}
					for(String line1 : rhs1.lines) {
						if(match) {
							break;
						}
						for(String line2 : rhs2.lines) {
							if(line1.equals(line2)) {
								line = line1;
								match = true;
								break;
							}
						}
					}
				}
			}
			
			sendMsg.setText("Dies ist deine Verbindung:\r\n"
						+ "Von: " + this.origins.get(update.getMessage().getChatId()).name + "\r\n"
						+ "Nach: " + this.destinations.get(update.getMessage().getChatId()).name + "\r\n"
						+ "Fahrt z.B. mit Linie " + line);
			ldState = LocationDialogStates.CONNECTION_SUGGESTED;
		}else {
			RNVHaltestelle closest = rnvApi.getClosestStop(loc);
			
			
			sendMsg.setText("Nächste Haltestelle: " + closest.name);
		}	
		try {
			execute(sendMsg);
		} catch (TelegramApiException e) {
			System.err.println("Fehler beim Senden der Nachricht: ");
			e.printStackTrace();
		}
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
	private void processWelcomeDialog(long chatId, String text) {
		SendMessage sendMsg = new SendMessage().setChatId(chatId);
		String msgText = "Ups, da ist wohl ein Fehler aufgetreten.";

		switch (wdState) {
		case DialogUnfinished:
			users.put(chatId, new UserData(chatId));

			msgText = "Wie lautet dein Nachname?";
			wdState = WelcomeDialogStates.REQUESTED_LAST_NAME;
			break;

		case REQUESTED_LAST_NAME:

			users.get(chatId).setLastName(text);

			msgText = "Sehr gut. Und dein Vorname?";
			wdState = WelcomeDialogStates.REQUESTED_FIRST_NAME;
			break;

		case REQUESTED_FIRST_NAME:
			users.get(chatId).setFirstName(text);

			msgText = "Um dich eindeutig zu identifizieren bräuchten wir außerdem bitte deine Adresse. Zuerst deine Straße mit Hausnummer:";
			wdState = WelcomeDialogStates.REQUESTED_ADDRESS;
			break;

		case REQUESTED_ADDRESS:
			users.get(chatId).setAddress(text);

			msgText = "In welcher Stadt?";
			wdState = WelcomeDialogStates.REQUESTED_CITY;
			break;

		case REQUESTED_CITY:
			users.get(chatId).setCity(text);

			 KeyboardButton kb = new KeyboardButton("Deinen Kontakt senden");
			  kb.setRequestContact(true); KeyboardRow kr = new KeyboardRow(); kr.add(kb);
			  ArrayList<KeyboardRow> rows = new ArrayList<>(); rows.add(kr);
			 ReplyKeyboardMarkup rkm = new
			 ReplyKeyboardMarkup().setKeyboard(rows).setOneTimeKeyboard(true);
			  
			  try {
			  sendMsg.setChatId(chatId).setReplyMarkup(rkm).setText("Anfrage:"
			  ); execute(sendMsg); } catch (TelegramApiException e) { e.printStackTrace();
			  }
			msgText = "Eine schöne Gegend. Würdest du mir außerdem deine Telefonnummer geben?";
			wdState = WelcomeDialogStates.REQUESTED_PHONE;
			break;

			
		case REQUESTED_PHONE:

			users.get(chatId).setTel(text);

			msgText = "Und zuletzt hätte ich gerne deine Email-Adresse.";
			wdState = WelcomeDialogStates.REQUESTED_MAIL;
			break;

		case REQUESTED_MAIL:

			if (text.trim().toLowerCase().equals("abbrechen")) {

				wdState = WelcomeDialogStates.DialogUnfinished;
				dState = DialogStates.PendingForDialog;

				
			}else {
				users.get(chatId).setMail(text);

				msgText = "Vielen Dank. In Zukunft wirst du deine Daten nicht noch einmal eingeben müssen.";
				dState = DialogStates.PendingForDialog;
				wdState = WelcomeDialogStates.DialogFinished;
				users.get(chatId).saveInDb();
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
	}

	
	private void processComplainDialog(long chatId, String text) {
		SendMessage sendMsg = new SendMessage().setChatId(chatId);
		String msgText = "Ups, da ist wohl ein Fehler aufgetreten.";

		switch (cdState) {
		case COMPLAIN_DIALOG_STARTED:
			cfs.put(chatId,new ComplainForm());
			msgText = "Welchen Ort betrifft deine Beschwerde?";
			cdState = ComplainDialogStates.REQUESTED_PLACE;
			break;

		case REQUESTED_PLACE:
			cfs.get(chatId).setPlace(text);
			msgText = "Und welchem Grund lässt sich deine Beschwerde zuordnen? Bitte wähle aus:";
			cdState = ComplainDialogStates.REQUESTED_REASON;
			 KeyboardButton kb = null;
			 ArrayList<KeyboardRow> rows = new ArrayList<>();
			 KeyboardRow kr = new KeyboardRow();
			 int i = 0;
			 for(Reasons r : Reasons.values()) {
				 System.out.println(r.getMessage());
				 kb = new KeyboardButton(r.getMessage());
				 kr.add(kb);
				 i++;
				 if(i%3 == 0) {
					 rows.add(kr);
					 kr = new KeyboardRow();
				 }
			 }
			  
			 ReplyKeyboardMarkup rkm = new
			 ReplyKeyboardMarkup().setKeyboard(rows).setOneTimeKeyboard(true);
			  
			  try {
			  sendMsg.setChatId(chatId).setReplyMarkup(rkm).setText("Anfrage:"
			  ); execute(sendMsg); } catch (TelegramApiException e) { e.printStackTrace();
			  }
			break;

		case REQUESTED_REASON:
			cfs.get(chatId).setReason(text);
			msgText = "Welche Linie betrifft deine Meldung?";
			cdState = ComplainDialogStates.REQUESTED_LINE;
			break;

		case REQUESTED_LINE:
			cfs.get(chatId).setLine(text);
			msgText = "In welche Richtung?";
			cdState = ComplainDialogStates.REQUESTED_DIRECTION;
			break;

		case REQUESTED_DIRECTION:
			cfs.get(chatId).setDirection(text);
			msgText = "Um wie viel Uhr ist der Mangel aufgetreten?";
			cdState = ComplainDialogStates.REQUESTED_TIME;
			break;

		case REQUESTED_TIME:
			cfs.get(chatId).setTime(text);
			msgText = "Und an welchem Datum?";
			cdState = ComplainDialogStates.REQUESTED_DATE;
			break;

		case REQUESTED_DATE:
			cfs.get(chatId).setDate(text);
			msgText = "Welche Haltestelle ist betroffen gewesen?";
			cdState = ComplainDialogStates.REQUESTED_STATION;
			break;

		case REQUESTED_STATION:
			cfs.get(chatId).setStation(text);
			msgText = "Es tut uns leid, dass nicht alles zu deiner Zufriedenheit gelaufen ist. Bitte hinterlasse uns eine Nachricht.";
			cdState = ComplainDialogStates.REQUESTED_MESSAGE;
			break;

		case REQUESTED_MESSAGE:
			cfs.get(chatId).setMessage(text);
			msgText = "Vielen Dank. Wir werden uns dem Problem schnellstmöglich annehmen.";
			UserData user = null;
			if(users.containsKey(chatId)) {
				user=users.get(chatId);
			} else {
				user = UserData.checkForUser(chatId);
				if(user != null ) {
					users.put(chatId, user);
				}else {
					//TODO abbruch error
				}
			}
			try {
				cfs.get(chatId).fillHtmlForm(user);
			} catch (FailingHttpStatusCodeException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			cdState = ComplainDialogStates.COMPLAIN_SEND;
			break;

		case COMPLAIN_SEND:

			cdState = ComplainDialogStates.NO_COMPLAIN_DIALOG_IN_USE;
			dState = DialogStates.PendingForDialog;
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
	}

	/**
	 * Starts the user dialogue requesting a location share.
	 * 
	 * @param message
	 *            Message that requested a location evaluation
	 */
	private void processLocationDialog(long chatId, String text) {
		SendMessage sendMsg = new SendMessage().setChatId(chatId);
		String msgText = "Ups, da ist wohl ein Fehler aufgetreten.";
		RNVApiHandler rnvApi = new RNVApiHandler();

		switch(ldState) {
		case LOCATION_DIALOG_STARTED:
			msgText = "Bitte teile mir deine Zielhaltestelle mit:";
			ldState = LocationDialogStates.DESTINATION_REQUESTED;
			
			break;
			
		case DESTINATION_REQUESTED:
			RNVHaltestelle destination = rnvApi.getStopByContainedName(text);
			if(destination == null) {
				msgText = "Die Eingabe enthielt leider keine Bekannte Haltestelle. Bitte erneut versuchen:";
			}else {
				destinations.put(chatId, destination);
				msgText = "Wenn du mir deinen Standort mitteilst, werde ich die nächste Haltestelle bestimmen.";
				ldState = LocationDialogStates.STARTINGPOINT_REQUESTED;
				KeyboardButton kbLoc = new KeyboardButton("Standort angeben");
				KeyboardButton kbNo = new KeyboardButton("Nein, danke.");
				kbLoc.setRequestLocation(true);
				KeyboardRow kr = new KeyboardRow();
				kr.add(kbLoc);
				kr.add(kbNo);
				ArrayList<KeyboardRow> rows = new ArrayList<>();
				rows.add(kr);
				ReplyKeyboardMarkup rkm = new ReplyKeyboardMarkup().setKeyboard(rows).setOneTimeKeyboard(true);
				sendMsg.setReplyMarkup(rkm);
			}
			break;
			
		case STARTINGPOINT_REQUESTED:
			break;
			
		case CONNECTION_SUGGESTED:
			msgText = "Gute Fahrt. Möchtest du direkt ein Ticket erwerben?";
			ldState = LocationDialogStates.CONNECTION_PAYMENT;
			KeyboardButton kbYes = new KeyboardButton("Ja, bitte.");
			KeyboardButton kbNope = new KeyboardButton("Nein, danke.");
			KeyboardRow krow = new KeyboardRow();
			krow.add(kbYes);
			krow.add(kbNope);
			ArrayList<KeyboardRow> krows = new ArrayList<>();
			krows.add(krow);
			ReplyKeyboardMarkup rkmu = new ReplyKeyboardMarkup().setKeyboard(krows).setOneTimeKeyboard(true);
			sendMsg.setReplyMarkup(rkmu);
			break;
		case CONNECTION_PAYMENT:
			if(text.equals("Ja, bitte.")) {
				msgText = "Zahlungsvorgang...";
			}else {
				msgText = "Klassisches Ticket gewählt.";
			}
			ldState = LocationDialogStates.NO_REQUEST_PENDING;
			dState = DialogStates.PendingForDialog;
		default:
			break;		
		}


		sendMsg.setText(msgText);
		try {
			execute(sendMsg);
		} catch (TelegramApiException e) {
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
