import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
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

	@Override
	public String getBotToken() {
		return "516826600:AAHwrHY_yNOGbCc_CuSYo7C3KfdtL0KSu0E";
	}

}
