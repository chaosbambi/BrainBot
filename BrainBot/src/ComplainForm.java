import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

public class ComplainForm {
	private String place;
	private String reason;
	private String line;
	private String direction;
	private String time;
	private String date;
	private String station;
	private String message;
	
	public String getPlace() {
		return place;
	}
	public void setPlace(String place) {
		this.place = place;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getLine() {
		return line;
	}
	public void setLine(String line) {
		this.line = line;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getStation() {
		return station;
	}
	public void setStation(String station) {
		this.station = station;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public void fillHtmlForm(UserData user) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
	    try (final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_52)) {

	        // Get the first page
	        final HtmlPage page1 = webClient.getPage("file:src\\form.html");

	        // Get the form that we are dealing with and within that form, 
	        // find the submit button and the field that we want to change.
	        final HtmlForm form = page1.getFormByName("tx_spbettercontact_pi1-543[form]");

	        //user data
	        HtmlTextInput textField = form.getInputByName("tx_spbettercontact_pi1-543[name]");
	        textField.setValueAttribute(user.getLastName());
	        textField = form.getInputByName("tx_spbettercontact_pi1-543[firstname]");
	        textField.setValueAttribute(user.getFirstName());
	        textField = form.getInputByName("tx_spbettercontact_pi1-543[street]");
	        textField.setValueAttribute(user.getAddress());
	        textField = form.getInputByName("tx_spbettercontact_pi1-543[city]");
	        textField.setValueAttribute(user.getCity());
	        textField = form.getInputByName("tx_spbettercontact_pi1-543[tel]");
	        textField.setValueAttribute(user.getTel());
	        textField = form.getInputByName("tx_spbettercontact_pi1-543[email]");
	        textField.setValueAttribute(user.getMail());
	        //complain data
	        textField = form.getInputByName("tx_spbettercontact_pi1-543[location]");
	        textField.setValueAttribute(place);
	        textField = form.getInputByName("tx_spbettercontact_pi1-543[line]");
	        textField.setValueAttribute(line);
	        textField = form.getInputByName("tx_spbettercontact_pi1-543[direction]");
	        textField.setValueAttribute(direction);
	        textField = form.getInputByName("tx_spbettercontact_pi1-543[time]");
	        textField.setValueAttribute(time);
	        textField = form.getInputByName("tx_spbettercontact_pi1-543[date]");
	        textField.setValueAttribute(date);
	        textField = form.getInputByName("tx_spbettercontact_pi1-543[station]");
	        textField.setValueAttribute(station);
	        HtmlTextArea textArea = form.getTextAreaByName("tx_spbettercontact_pi1-543[message]");
	        textArea.type(message);
	        HtmlSelect hs = form.getSelectByName("tx_spbettercontact_pi1-543[reason]");
	        hs.setSelectedAttribute(reason, true);
	        
	        
	        File fShow = new File("src\\form1\\");
	        page1.save(fShow);
	       
	        // Now submit the form by clicking the button and get back the second page.
	        //final HtmlPage page2 = button.click();*/
	    }
	}
	
	
}
