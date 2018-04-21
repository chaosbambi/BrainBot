import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
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
		if(update.hasMessage() && update.getMessage().hasText()) {
			SendMessage sendMsg = new SendMessage().setChatId(update.getMessage().getChatId());
			if(update.getMessage().getText().toLowerCase().trim().matches("ping[\\.!]*")) {
				sendMsg.setText("Pong!");
			}
			else if(update.getMessage().getText().matches("/start")){
				startWelcomeDialog(update.getMessage());
				sendMsg.setText("Welcome process started.");
			}else {
				sendMsg.setText("Sorry, das habe ich nicht verstanden...");
			}
			try {
				execute(sendMsg);
			} catch (TelegramApiException e) {
				e.printStackTrace();
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

}
