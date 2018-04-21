
public enum Reasons {

	Ausfall("Ausfall von Fahrten"),
	Frueh("Abfahrt zu fr�h"),
	Spaet("Abfahrt zu sp�t"),
	Anschluss("Anschl�sse"),
	Fahrplan("Fahrplan"),
	Fahrzeug("Fahrzeuge"),
	Haltestelle("Haltestellen"),
	Uebersetzung("�bersetzung"),
	Linien("Linienf�hrung"),
	Info("Information bei St�rungen"),
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
