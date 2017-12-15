/*
 * DBMS : Final Project Stand-alone Java Interface
 * Author : Ankur Peshin | Ritu Chandwani
 * 
 */

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import com.opencsv.CSVReader;

public class Final {

	private static String POSTGRES_SUPERUSER = "postgres";
	
	private static String POSTGRES_SUPERPASS = "postgres";
	
	private static String POSTGRES_SUPERPORT = "5432";
	
	
	//File Path Retail Food Stores
	public static String FILE_PATH = "Files\\Retail_Food_Stores.csv";
	
	//Violations CSV Path
	public static String FILE_PATH_2 ="Files\\Retail_Food_Store_Inspections___Current_Critical_Violations.csv";
	
	//Food Store Address CSV PATH
	public static String FILE_PATH_ADDRESS="Files\\FoodStoreAddress.csv";
	
	//Food Inspection Address CSV PATH
	public static String food_ins_path="Files\\Food_Inspection_Address.csv";
	
	//Food Store Insert Statement
	public static String FOOD_STORE_INSERT = "INSERT INTO retailfoodstore(license_number, "
			+ "county, operation_type, establishment_type,entity_name, dba_name)"
			+ "VALUES ( ?, ?, ?, ?, ?, ?);";
	
	/*public static String FOOD_STORE_VIOLATE = "INSERT INTO public.foodinspection( county,"
			+ " inspectiondate, owner_name, trade_name,deficiency_code)VALUES (?, ?, ?, ?, ?);";*/
	
	
	//Food Store Violate Insert Statement
	public static String FOOD_STORE_VIOLATE = "INSERT INTO 	foodstoreviolations( county,"
			+ " inspectiondate, owner_name, trade_name, street_num, street_name, city, state_code,"
			+ " zip_code, deficiency_code, deficiency_desc, location)VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	
	// Food Store Address Insert Statement
	public static String ADDRESS = "INSERT INTO address(license_number,street_num,street_name"+
			",city,state,zip_code,square_footage,location) VALUES (?,?,?,?,?,?,?,?)";
	
	// Food Inspection Address Insert Statement
	public static String food_ins_address = "INSERT INTO public.FOOD_INSPECTION_ADDRESS(owner_name,street_num,street_name"+
			",city,state_code,zip_code) VALUES (?,?,?,?,?,?)";
	
	//Super Script if superuser Database params not given.
	@SuppressWarnings("unused")
	private static String SUPERCONNECT = "psql.exe -U postgres -p 5432 -f D:\\Script.sql";
	
	private static Connection conn =null;
	private static Connection superConn = null;
	
	static Scripts scripts= null;
	
	public static void main(String[] args) throws SQLException {
		
		//Create User,Database, Tables
		scripts = new Scripts();
		
		createSchema();
		
		//Populate Tables with Data
		populateSchema();
		
		//Post Populate operations like Triggers, Materialized View
		postSchema();
		
		Final opInstance = new Final();
		
		//Use this method to do the looping operations and display results:
		//opInstance.doOperations();
		
		// Drops the schema and user. Comment everything above and uncomment this program, and run to drop schema
		//Or maybe just uncomment this one, and the method will wrap up unnecessary DB that you won't ever need
		//dropSchema();
		conn.close();
		superConn.close();
	}
	
	public void doOperations(){
		if(conn ==null){
			conn= getPostgresConnection();
		}
		
		do{
			printOperations();
			
			
			
			
		}
		while(true);
		
	}
	
	public void printOperations(){
		
		System.out.println("==============================================");
		System.out.println("1. For a given DBA, get all deficiency reports");
		System.out.println("2. For a given deficiency code, get all stores with this deficiency");
		System.out.println("3. For a give store license_number, get all the deficiencies associated with the store ");
		System.out.println("4. Get a list of counties which have deficiency free till date");
		System.out.println("5. Get a list of top 10 counties with maximum deficiency in Stores");
		System.out.println("6. Get a city-wise count of deficiency along with entity name ");
		System.out.println("7. File a new deficiency report against a store");
		System.out.println("==============================================");
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			
		}
		System.out.println();
		System.out.println("Make your choice and press enter OR press 'q' and then hit enter and escape");
	}

	public static void createSchema() throws SQLException {
			
			//GetSuperUserPass();
			superConn = getPostgresSuperConnection();
			
			runSQL(scripts.initScripts, superConn);
			
			conn = getPostgresConnection();

			runSQL(scripts.schemaScripts, conn);		
		
	}
	
	public static void runSQL(List<String> list, Connection pgConn) {
		String query= null;
		for(int i=0; i<list.size();i++){
			try {
				query = (String) list.get(i);
				PreparedStatement statement = pgConn.prepareStatement(query);
				statement.executeUpdate();
				//System.out.println(statement.getResultSet().getString(0));
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	
/*	public static void WindowsrunCommand(String command){
		
		try {		
			String line;
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}
			input.close();
		} catch (Exception err) {
			err.printStackTrace();
		}
		
	}
*/	
	
	
	public static void GetSuperUserPass(){
		System.out.println("Enter your username Or Enter 'N' if username is postgres ");
		
		Scanner scanner = new Scanner(System.in);
		String username = scanner.nextLine();
		if(!username.equalsIgnoreCase("n")){
			POSTGRES_SUPERUSER = username;
		}
				
		System.out.println("Enter your SERVER PASSWORD Or Enter 'N' if PASSWORD is postgres ");
		String password = scanner.nextLine();
        if(!password.equalsIgnoreCase("n")){
			POSTGRES_SUPERPASS = password;
		}
        
        System.out.println("Enter your SERVER PORT Or Enter 'N' if PORT is 5432 ");
		String port = scanner.nextLine();
        if(!password.equalsIgnoreCase("n")){
			POSTGRES_SUPERPORT = port;
		}
        
        scanner.close();
	}
	
	
	public static void populateSchema(){
		
		try{
			
			conn.setAutoCommit(false);
			
			insertIntoFoodStoreTable(conn, FOOD_STORE_INSERT, FILE_PATH);
			System.out.println("Inserted rows to Food Store Table");
			
			insertIntoViolationTable(conn, FOOD_STORE_VIOLATE, FILE_PATH_2);
			System.out.println("Inserted rows to Violation Table");
			
			insertIntoAddressTable(conn,ADDRESS,FILE_PATH_ADDRESS);
			System.out.println("Inserted rows to Address Table");
			//insertIntoFoodInsAddr(conn,food_ins_address,food_ins_path);
			//conn.close();
		} 
		catch(IOException ex ){
			ex.printStackTrace();
		}
		catch(SQLException ex ){
			ex.printStackTrace();
		}
		catch(ParseException ex ){
			ex.printStackTrace();
		}
		
	}
	
	
	@SuppressWarnings("unused")
	private static void insertIntoFoodInsAddr(Connection conn, String insertQuery, String filePath)
			throws SQLException, NumberFormatException, IOException {
		PreparedStatement address_insert = conn.prepareStatement(insertQuery);
		@SuppressWarnings("resource")
		CSVReader reader = new CSVReader(new FileReader(filePath));

		String[] nextline = null;
		reader.readNext();
		while ((nextline = reader.readNext()) != null) {
			address_insert.setString(1, nextline[0]);
			if (!nextline[1].isEmpty() && nextline[1] != null) {
				address_insert.setString(2, nextline[1].substring(0, nextline[1].indexOf(' ')));
				address_insert.setString(3, nextline[1].substring(nextline[1].indexOf(' ') + 1));
			} else {
				address_insert.setString(2, nextline[1]);
				address_insert.setString(3, nextline[1]);
			}
			/*
			 * address_insert.setString(2, nextline[1]);
			 * address_insert.setString(3, nextline[2]);
			 */
			address_insert.setString(4, nextline[2]);
			address_insert.setString(5, nextline[3]);
			address_insert.setInt(6, Integer.parseInt(nextline[4]));

			System.out.println("Line >>> " + nextline[0]);
			address_insert.executeUpdate();
		}
		conn.commit();

	}

	public static Connection getPostgresConnection() {
		try {
			
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
			return null;
		}
		Connection connection = null;
		try {
			connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/foodstoredb",
					"foodstore", "foodstore");
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return null;
		}

		if (connection != null) {
			return connection;
		} else {
			return null;
		}
	}
	
	public static Connection getPostgresSuperConnection() {
		try {
			
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
			return null;
		}
		Connection connection = null;
		try {
			String URL ="jdbc:postgresql://127.0.0.1:"+ POSTGRES_SUPERPORT+ "/";
			connection = DriverManager.getConnection(URL,
				POSTGRES_SUPERUSER, POSTGRES_SUPERPASS);
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return null;
		}

		if (connection != null) {
			return connection;
		} else {
			return null;
		}
	}

	public static void insertIntoFoodStoreTable(Connection conn, String insertQuery, String filePath)
			throws SQLException, IOException {
		PreparedStatement food_store_insert = conn.prepareStatement(insertQuery);
		@SuppressWarnings("resource")
		CSVReader reader = new CSVReader(new FileReader(filePath));

		String[] nextline = null;
		reader.readNext();
		while ((nextline = reader.readNext()) != null) {
			food_store_insert.setInt(1, Integer.parseInt(nextline[0]));
			food_store_insert.setString(2, nextline[1]);
			food_store_insert.setString(3, nextline[2]);
			food_store_insert.setString(4, nextline[3]);
			food_store_insert.setString(5, nextline[4]);
			food_store_insert.setString(6, nextline[5]);
			food_store_insert.executeUpdate();
		}
		conn.commit();
	}


	public static void insertIntoViolationTable(Connection conn, String insertQuery, String filePath)
			throws SQLException, IOException, ParseException {
		PreparedStatement food_store_insert = conn.prepareStatement(insertQuery);
		@SuppressWarnings("resource")
		CSVReader reader = new CSVReader(new FileReader(filePath));
		DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
		String[] nextline = null;
		reader.readNext();
		while ((nextline = reader.readNext()) != null) {
			food_store_insert.setString(1, nextline[0]);
			food_store_insert.setDate(2, new java.sql.Date((format.parse(nextline[1])).getTime()));
			food_store_insert.setString(3, nextline[2]);
			food_store_insert.setString(4, nextline[3]);
			if (!nextline[4].isEmpty() && nextline[4] != null) {
				food_store_insert.setString(5, nextline[4].substring(0, nextline[4].indexOf(' ')));
				food_store_insert.setString(6, nextline[4].substring(nextline[4].indexOf(' ') + 1));
			} else {
				food_store_insert.setString(5, nextline[4]);
				food_store_insert.setString(6, nextline[4]);
			}

			food_store_insert.setString(7, nextline[5]);
			food_store_insert.setString(8, nextline[6]);
			food_store_insert.setInt(9, Integer.parseInt(nextline[7]));
			food_store_insert.setString(10, nextline[8]);
			food_store_insert.setString(11, nextline[9]);
			food_store_insert.setString(12, nextline[10]);
			food_store_insert.executeUpdate();
		}
		conn.commit();
	}

	public static void insertIntoAddressTable(Connection conn, String insertQuery, String filePath)
			throws SQLException, IOException {
		PreparedStatement address_insert = conn.prepareStatement(insertQuery);
		@SuppressWarnings("resource")
		CSVReader reader = new CSVReader(new FileReader(filePath));

		String[] nextline = null;
		reader.readNext();
		while ((nextline = reader.readNext()) != null) {
			address_insert.setInt(1, Integer.parseInt(nextline[0]));
			address_insert.setString(2, nextline[1]);
			address_insert.setString(3, nextline[2]);
			address_insert.setString(4, nextline[3]);
			address_insert.setString(5, nextline[4]);
			address_insert.setInt(6, Integer.parseInt(nextline[5]));
			address_insert.setInt(7, Integer.parseInt(nextline[6]));
			address_insert.setString(8, "Random");

			address_insert.executeUpdate();
		}
		conn.commit();
	}

	
	public static void postSchema() throws SQLException{
			runSQL(scripts.postScripts, conn);
			conn.commit();
	}
	
	
	public static void dropSchema() {
			runSQL(scripts.dropScripts,superConn);
	}

}
