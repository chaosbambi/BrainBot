import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserData {
	
	private static final String dbUser = Sensitive.getDbuser();
	private static final String dbPw = Sensitive.getDbpw();
	private static final String dbServer = Sensitive.getDbserver();
	private static final String hostName = dbServer + ".database.windows.net";
	
	private Long personId;
	private String lastName;
	private String firstName;
	private String address;
	private String city;
	private String tel;
	private String mail;

	public UserData(Long personId) {
		this.personId = personId;
	}
	
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public Long getPersonId() {
		return personId;
	}

	public void saveInDb() {
		String url = String.format("jdbc:sqlserver://%s:1433;database=%s;user=%s;password=%s;encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=30;", hostName, dbServer, dbUser, dbPw);
        Connection connection = null;

        try {
                connection = DriverManager.getConnection(url);
                String schema = connection.getSchema();
                System.out.println("Successful connection - Schema: " + schema);


                // Create and execute a SELECT SQL statement.
                PreparedStatement insertion = connection.prepareStatement("INSERT INTO Users VALUES(?,?,?,?,?,?,?)");
                insertion.setString(1, personId.toString());
                insertion.setString(2, lastName);
                insertion.setString(3, firstName);
                insertion.setString(4, address);
                insertion.setString(5, city);
                insertion.setString(6, tel);
                insertion.setString(7, mail);
                
                insertion.executeUpdate();
                connection.close();
                      
                }
                catch (Exception e) {
                	e.printStackTrace();
                }
                finally {
                	if(connection != null) {
                		try {
							connection.close();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                	}
                }
		
	}

	static boolean checkForUser(Long chatId) {
		String url = String.format("jdbc:sqlserver://%s:1433;database=%s;user=%s;password=%s;encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=30;", hostName, dbServer, dbUser, dbPw);
        Connection connection = null;
        boolean userExists = false;
        try {
                connection = DriverManager.getConnection(url);
                String schema = connection.getSchema();
                System.out.println("Successful connection - Schema: " + schema);


                // Create and execute a SELECT SQL statement.
                PreparedStatement query = connection.prepareStatement("SELECT LASTNAME FROM Users WHERE PERSONID = ?");
                query.setString(1, chatId.toString());

                ResultSet rs = query.executeQuery();
                userExists = rs.next();
                connection.close();
                      
                }
                catch (Exception e) {
                	e.printStackTrace();
                }
                finally {
                	if(connection != null) {
                		try {
							connection.close();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                	}
                }
        return userExists;
	}
	
	
	
	
	
	
}
