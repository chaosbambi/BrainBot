
public enum Reasons {

	Ausfall("Ausfall von Fahrten"),
	Frueh("Abfahrt zu früh"),
	Spaet("Abfahrt zu spät"),
	Anschluss("Anschlüsse"),
	Fahrplan("Fahrplan"),
	Fahrzeug("Fahrzeuge"),
	Haltestelle("Haltestellen"),
	Uebersetzung("Übersetzung"),
	Linien("Linienführung"),
	Info("Information bei Störungen"),
	Fahrpersonal("Verhalten Fahrpersonal"),
	ServicePersonal("Verhalten Servicepersonal"),
	Automat("Automaten"),
	Tarife("Tarife"),
	Sonstiges("Sonstiges");
	
	private String message;
	
	Reasons(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
}
