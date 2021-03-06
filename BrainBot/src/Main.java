import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

public class Main {

	public static void main(String[] args) {
		System.out.println("Starting Bot.");
		
		RNVApiHandler r = new RNVApiHandler();
		
		//Initialize Api Context
		ApiContextInitializer.init();

        //Instantiate Telegram Bots API
		TelegramBotsApi botsApi = new TelegramBotsApi();
		
		CoreBot t = new CoreBot();
        //Register our bot
		try {
			botsApi.registerBot(t);
			System.out.println("Bot registered!");
		} catch (TelegramApiRequestException e) {
			e.printStackTrace();
		}

	}

}
