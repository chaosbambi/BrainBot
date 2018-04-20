import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

public class Main {

	public static void main(String[] args) {
		TestBot tst = new TestBot();
		
		//Initialize Api Context
		ApiContextInitializer.init();

        //Instantiate Telegram Bots API
		TelegramBotsApi botsApi = new TelegramBotsApi();

        //Register our bot
		try {
			botsApi.registerBot(tst);
		} catch (TelegramApiRequestException e) {
			e.printStackTrace();
		}

	}

}
