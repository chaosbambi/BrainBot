import org.telegram.telegrambots.api.methods.send.SendLocation;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageLiveLocation;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class LocationUpdater implements Runnable{

	private final int mID;
	private final int updates;
	private final long cID;
	private final float lon, lat;
	private CoreBot bot;
	
	/**
	 * 
	 * @param messageID ID of the sent Location message, so it can be updated
	 * @param chatID The chat ID
	 * @param sl the SendLocation (which was sent with the original message - to grab live period and location)
	 * @param bot The bot instance, to execute the updates in it
	 */
	public LocationUpdater(int messageID, long chatID, SendLocation sl, CoreBot bot) {
		this.mID = messageID;
		this.cID = chatID;
		this.updates = sl.getLivePeriod();
		this.lon = sl.getLongitude();
		this.lat = sl.getLatitude();
		this.bot = bot;
	}
	
	@Override
	public void run() {
		float latitude = lat;
		float longitude = lon;
		for(int cnt = 0; cnt < updates; cnt++) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			latitude += 0.0001;
			longitude += 0.0001;
			EditMessageLiveLocation emll = new EditMessageLiveLocation().setChatId(cID).setLatitude(latitude).setLongitud(longitude).setMessageId(mID);
			try {
				bot.execute(emll);
				System.out.println("Location change Nr.: "+cnt);
			} catch (TelegramApiException e) {
				System.out.println("Refresh timeframe expired!");
				break;
			}
		}		
	}

}
