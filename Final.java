/*
 * DBMS : Final Project Stand-alone Java Interface
 * Author : Ankur Peshin | Ritu Chandwani
 * 
 */

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

import com.opencsv.CSVReader;

public class Final {

	private static String POSTGRES_SUPERUSER = "postgres";
	
	private static String POSTGRES_SUPERPASS = "postgres";
	
	private static String POSTGRES_SUPERPORT = "5432";
	
	

	
	//Super Script if superuser Database params not given.
	@SuppressWarnings("unused")
	private static String SUPERCONNECT = "psql.exe -U postgres -p 5432 -f D:\\Script.sql";
	
	private static Connection conn =null;
	private static Connection superConn = null;
	
	static Scripts scripts= null;
	
	static Map<String, String> def_map=new HashMap<String, String>();
	
	static Scanner sc = new Scanner(System.in);
	
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
		ReadXMLFile readFileObj=new ReadXMLFile();
		def_map=readFileObj.getDeficiencyMapFromXML(Scripts.desc_xml);
		
		//Use this method to do the looping operations and display results:
		try {
			opInstance.doOperations();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch(Exception ex){
			ex.printStackTrace();
			conn.close();
			dropSchema();
			superConn.close();
			return;
		}
		
		// Drops the schema and user. Comment everything above and uncomment this program, and run to drop schema
		//Or maybe just uncomment this one, and the method will wrap up unnecessary DB that you won't ever need
		conn.close();
		dropSchema();
		
		superConn.close();
	}
	
	@SuppressWarnings("resource")
	public void doOperations() throws SQLException, FileNotFoundException, ParseException{
		
		boolean continuechoice = true;
		
		if(conn ==null){
			conn= getPostgresConnection();
		}
		
		do{
			printOperations();
			String userInput=sc.next();
			if(isAlpha(userInput)) {
				System.out.println("Program exited successfully");
				System.exit(1);
			}
			Integer input=Integer.valueOf(userInput);
			switch(input){
			
			case 1:
				System.out.println("Enter the DBA Name");
				String dba_name = sc.next();
				dba_name += sc.nextLine();  //"101 corner food mart";sc.nextLine();
				if(!dba_name.isEmpty()){
					List<String> deficiencyCodeList=getDeficiencyListByDbaName(dba_name);
					System.out.println(" FOR DBA NAME >>>> "+ dba_name);
					if(deficiencyCodeList.isEmpty()){
						System.out.println("No deficiencies exist");
					}
					else{
					System.out.println("Defciency code		Inspection Date								Deficiency Description");
					for(String defCode : deficiencyCodeList){
						String def_desc=def_map.get(new StringTokenizer(defCode).nextToken().trim());
						System.out.println(defCode+"							"+def_desc);
						
						}
					}
				}
				break;
			
			case 2:
				System.out.println("Enter the deficiency code!");
				String def_code = sc.next();
				if(!def_code.isEmpty()){
					List<String> storesList=getStoresWithDefCode(def_code);
					if(storesList.isEmpty()){
						System.out.println("No stores have this deficiency");
					}
					else{
						String def_desc=def_map.get(def_code.trim());
						System.out.println(" FOLLOWING ARE THE STORES with DEFCIENCY "+def_desc+" : "+ def_code);
						for(String store : storesList){
							System.out.println(store);
						}
						System.out.println("--------------------------------------------------------------------------------------------");
					}
				}	
				break;
				
			case 3:
				System.out.println("Enter License Number");
				Integer license_number=sc.nextInt();
				
				List<String> deficiency_code=getDeficiencyCodeByLicenseNumber(license_number);
				
				if(deficiency_code.isEmpty()){
					System.out.println("No deficiencies exists for this license number");
				}else{
					System.out.println(" For store with License Number "+license_number+" following deficiencies are found");
					System.out.println("Deficiency Code        Deficiency Description");
					for(String defCode : deficiency_code){
						String def_desc=def_map.get(defCode.trim());
						System.out.println(defCode+"        "+def_desc);
					}
					System.out.println("--------------------------------------------------------------------------------------------");
				}
				break;
				
			case 4:
				List<String> countyList = getDeficiencyFreeCounties();
				if(countyList!=null && !countyList.isEmpty()){
					System.out.println("Following are the deficiency free Counties");
					for(String county : countyList){
						System.out.println(county); 
					}
				}else{
					System.out.println("No Result found");
				}
				
				break;
				
			case 5:
				Map<String,Integer> topDefCountyMap = getTopCountyListWithMaxDeficiency();
				if(null!=topDefCountyMap && !topDefCountyMap.isEmpty()){
					System.out.println("Following are the top 10 counties with maximum deficiencies");
					System.out.println("County Name               Count ");
					for(String county:topDefCountyMap.keySet()){
						Integer count =topDefCountyMap.get(county);
						System.out.println(county+"               "+count);
					}
					System.out.println("--------------------------------------------------------------------------------------------");
				}else{
						System.out.println("No results found");
				}
				break;
				
			case 6:
				System.out.println("Enter the DATE in YYYY-MM-DD Format");
				String date = sc.next();
				
				if(!date.isEmpty()){
					List<LicenseView> list=getReportByDate(date);
					System.out.println(" FOR Date >>>> "+ date);
					if(list.isEmpty()){
						System.out.println("No reports for the date. Try using double digit MM YY like => 2005-03-03");
					}
					else{
					System.out.println("OWNER 	|  DEF CODE  |	INSPECTDATE	|	LICENSE NUMBER	 |	Deficiency Description");
					for(LicenseView lic : list){
						String def_desc=def_map.get(lic.getDeficiencyCode().trim());
						System.out.println(lic.getOwnerName() + "		"  + lic.getDeficiencyCode()  + "		"  + 
						lic.getInspectionDate() + "		"  + lic.getLicenseNumber() + "		"  + def_desc);
						
						}
					}
				}
				break;
				
			case 7:
				System.out.println("Enter License Number");
				Integer lic_number=sc.nextInt();
				System.out.println(" Enter Deficiency Code");
				String defcode =sc.next();
				
				if(def_map.containsKey(defcode)){
					
				String addressDetails = getAddressDetailsByLicense(lic_number);
				OwnerView own = getOwnerInfoByLicense(lic_number);
				
				if(addressDetails.isEmpty() || addressDetails.equalsIgnoreCase("") || addressDetails ==null || own == null){
					System.out.println("License info incorrect");
					break;
				}
				Boolean success = insertNewDeficiencyRecord(own.getCounty(),own.getEntity_name(),own.getDba_name(),addressDetails,defcode);
				if(success)
					System.out.println("Record inserted successfully with refreshing materized view using trigger");
				}
				else
					System.out.println("Deficiency Code not valid");
				
				break;
			
			case 0:
				continuechoice = false;
				
			default:
				
				
			}
			
			
		}
		while(continuechoice);
		
	}	
	
	public void printOperations(){
		
		System.out.println("==============================================");
		System.out.println("1. For a given DBA, get all deficiency reports");
		System.out.println("2. For a given deficiency code, get all stores with this deficiency");
		System.out.println("3. For a give store license_number, get all the deficiencies associated with the store ");
		System.out.println("4. Get a list of counties which have deficiency free till date");
		System.out.println("5. Get a list of top 10 counties with maximum deficiency in Stores");
		System.out.println("6. Get a list of reports on a specific Date");
		System.out.println("7. File a new deficiency report against a store");
		System.out.println("==============================================");
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			
		}
		System.out.println();
		System.out.println("Make your choice and press enter OR press 0 and then hit enter and escape");
	}

	public static void createSchema() throws SQLException {
			
			GetSuperUserPass();
			superConn = getPostgresSuperConnection();
			
			runSQL(scripts.initScripts, superConn);
			System.out.println("User foodstore Created and DB FoodStoreDB created");
			
			conn = getPostgresConnection();

			runSQL(scripts.schemaScripts, conn);		
			System.out.println("Tables Created");
		
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
	
		String username = sc.nextLine();
		if(!username.equalsIgnoreCase("n")){
			POSTGRES_SUPERUSER = username;
		}
				
		System.out.println("Enter your SERVER PASSWORD Or Enter 'N' if PASSWORD is postgres ");
		String password = sc.nextLine();
        if(!password.equalsIgnoreCase("n")){
			POSTGRES_SUPERPASS = password;
		}
        
        System.out.println("Enter your SERVER PORT Or Enter 'N' if PORT is 5432 ");
		String port = sc.nextLine();
        if(!password.equalsIgnoreCase("n")){
			POSTGRES_SUPERPORT = port;
		}
	}
	
	
	public static void populateSchema(){
		
		try{
			
			conn.setAutoCommit(false);
			
			insertIntoFoodStoreTable(conn, Scripts.FOOD_STORE_INSERT, Scripts.FILE_PATH);
			System.out.println("Inserted rows to Food Store Table");
			
			insertIntoViolationTable(conn, Scripts.FOOD_STORE_VIOLATE, Scripts.FILE_PATH_2);
			System.out.println("Inserted rows to Violation Table");
			
			insertIntoAddressTable(conn,Scripts.ADDRESS,Scripts.FILE_PATH_ADDRESS);
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
			food_store_insert.setString(11, nextline[10]);
			
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
			System.out.println("Materialized View, Function & Trigger Created");
	}
	
	
	public static void dropSchema() {
			runSQL(scripts.dropScripts,superConn);
			System.out.println("Schema and User Dropped");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
	
	// Operations Methods
	private Boolean insertNewDeficiencyRecord(String county, String owner_name,
			String trade_name, String addressDetails,String def_code) throws SQLException {
		
		StringTokenizer st=new StringTokenizer(addressDetails,";");
		String street_num=st.nextToken();
		String street_name=st.nextToken();
		String city=st.nextToken();
		String state = st.nextToken();
		Integer zipcode = Integer.valueOf(st.nextToken());
		String location = st.nextToken();
		@SuppressWarnings("unused")
		DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
		PreparedStatement food_store_insert = conn.prepareStatement(Scripts.FOOD_STORE_VIOLATE);
		
		food_store_insert.setString(1,county);
		Calendar cal=Calendar.getInstance();
		food_store_insert.setDate(2,  new Date(cal.getTimeInMillis()));
		food_store_insert.setString(3, owner_name);
		food_store_insert.setString(4, trade_name);
		food_store_insert.setString(5, street_num);
		food_store_insert.setString(6, street_name);
		food_store_insert.setString(7, city);
		food_store_insert.setString(8, state);
		food_store_insert.setInt(9, zipcode);
		food_store_insert.setString(10, def_code);
		food_store_insert.setString(11, location);
		food_store_insert.executeUpdate();
		conn.commit();
		return Boolean.TRUE;
	}

		private String getAddressDetailsByLicense(Integer lic_number) throws SQLException {
			StringBuffer addressDetails = new StringBuffer();
			String address_query ="select street_num, street_name,city,state,zip_code,location from address where license_number= ?";
			PreparedStatement pstmt = conn.prepareStatement(address_query);
			pstmt.setInt(1, lic_number);
			ResultSet rs=null;
			boolean isResult= pstmt.execute();
			if(isResult){
				rs=pstmt.getResultSet();
				while(rs.next()){
					String street_num=rs.getString(1);
					String street_name = rs.getString(2);
					String city =rs.getString(3);
					String state = rs.getString(4);
					Integer zipCode=rs.getInt(5);
					String location = rs.getString(6);
					addressDetails.append(street_num.trim()).append(";").append(street_name.trim()).append(";").append(city.trim()).
					append(";").append(state.trim()).append(";").append(zipCode.toString().trim()).append(";").append(location.trim());
				}
			}
			return addressDetails.toString();
		}

	private Map<String, Integer> getTopCountyListWithMaxDeficiency() throws SQLException {
		Map<String, Integer> countyMap=new HashMap<String, Integer>();
		String query = " select county , count (*) defCount from foodstoreviolations group by county order by defCount desc LIMIT 10";
		PreparedStatement pstmt = conn.prepareStatement(query);
		ResultSet rs=null;
		boolean isResult= pstmt.execute();
		if(isResult){
			rs=pstmt.getResultSet();
			while(rs.next()){
				String county_name=rs.getString(1);
				Integer defCount = rs.getInt(2);
				countyMap.put(county_name.trim(),defCount);
			}
		}
		return countyMap;
	}

	private List<String> getDeficiencyFreeCounties() throws SQLException {
		List<String> countyList=new ArrayList<String>();
		String query = " select distinct county from retailfoodstore where county not in(select distinct county from foodstoreviolations)";
		PreparedStatement pstmt = conn.prepareStatement(query);
		ResultSet rs=null;
		boolean isResult= pstmt.execute();
		if(isResult){
			rs=pstmt.getResultSet();
			while(rs.next()){
				String owner_name=rs.getString(1);
				countyList.add(owner_name.trim());
			}
		}
		return countyList;
	}

	private List<String> getDeficiencyCodeByLicenseNumber(Integer license_number) throws SQLException {
		
		List<String> defCodeList=new ArrayList<String>();
		String query = " select deficiency_code  from license_deficiency_view where license_number= ?";
		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.setInt(1, license_number);
		ResultSet rs=null;
		boolean isResult= pstmt.execute();
		if(isResult){
			rs=pstmt.getResultSet();
			while(rs.next()){
				String owner_name=rs.getString(1);
				defCodeList.add(owner_name);
			}
		}
		return defCodeList;
	}

	private List<String> getStoresWithDefCode(String def_code) throws SQLException {
		List<String> storeList=new ArrayList<String>();
		String query = "select distinct owner_name from license_deficiency_view where deficiency_code = ?";
		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.setString(1, def_code);
		ResultSet rs=null;
		boolean isResult= pstmt.execute();
		if(isResult){
			rs=pstmt.getResultSet();
			while(rs.next()){
				String owner_name=rs.getString(1);
				storeList.add(owner_name.trim());
			}
		}
		return storeList;
	}

	private List<String> getDeficiencyListByDbaName(String dba_name) throws SQLException, FileNotFoundException {
		
		List<String> defCodes=new ArrayList<String>();
		String query="select deficiency_code, inspectiondate from license_deficiency_view where lower(dba_name) =lower(?)";
		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.setString(1, dba_name);
		ResultSet rs=null;
		pstmt.execute();
		
			rs=pstmt.getResultSet();
			while(rs.next()){
				String def_code=rs.getString(1) + "			" +rs.getDate(2);
				defCodes.add(def_code);
				System.out.println("Code >> "+def_code);
			}
		
		return defCodes;
	}
	
	
	
	private List<LicenseView> getReportByDate(String date) throws SQLException, FileNotFoundException, ParseException {
		
		List<LicenseView> defCodes=new ArrayList<LicenseView>();
		String query="select * from license_deficiency_view where INSPECTIONDATE = to_date(?,?)";
		PreparedStatement pstmt = conn.prepareStatement(query);
		SimpleDateFormat sm = new SimpleDateFormat("yyyy-mm-dd");
		DateFormat format = new SimpleDateFormat("YYYY-MM-DD", Locale.ENGLISH);
		//pstmt.setDate(1, new java.sql.Date((format.parse(date)).getTime()));
		pstmt.setString(1, date);
		pstmt.setString(2, "YYYY-MM-DD");
		ResultSet rs=null;
		pstmt.execute();
		
			rs=pstmt.getResultSet();
			while(rs.next()){
				defCodes.add(new LicenseView(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)));
			}
		
		return defCodes;
	}
	
private OwnerView getOwnerInfoByLicense(int license) throws SQLException, FileNotFoundException, ParseException {
		
		OwnerView own=null;
		String query="select * from retailfoodstore where LICENSE_NUMBER = ?";
		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.setInt(1, license);
		ResultSet rs=null;
		pstmt.execute();
		
			rs=pstmt.getResultSet();
			if(rs.next()){
				own = new OwnerView(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6));
			}
			
			
		
		return own;
	}
	
	
	public boolean isAlpha(String name) {
	    char[] chars = name.toCharArray();

	    for (char c : chars) {
	        if(!Character.isLetter(c)) {
	            return false;
	        }
	    }

	    return true;
	}

}



class LicenseView{
	String ownerName;
	String deficiencyCode;
	String inspectionDate;
	String dbaName;
	String licenseNumber;
	public String getOwnerName() {
		return ownerName;
	}
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	public String getDeficiencyCode() {
		return deficiencyCode;
	}
	public void setDeficiencyCode(String deficiencyCode) {
		this.deficiencyCode = deficiencyCode;
	}
	public String getInspectionDate() {
		return inspectionDate;
	}
	public void setInspectionDate(String inspectionDate) {
		this.inspectionDate = inspectionDate;
	}
	public String getDbaName() {
		return dbaName;
	}
	public void setDbaName(String dbaName) {
		this.dbaName = dbaName;
	}
	public String getLicenseNumber() {
		return licenseNumber;
	}
	public void setLicenseNumber(String licenseNumber) {
		this.licenseNumber = licenseNumber;
	}
	public LicenseView(String ownerName, String deficiencyCode, String inspectionDate, String dbaName,
			String licenseNumber) {
		super();
		this.ownerName = ownerName;
		this.deficiencyCode = deficiencyCode;
		this.inspectionDate = inspectionDate;
		this.dbaName = dbaName;
		this.licenseNumber = licenseNumber;
	}
	
	
}


class OwnerView{
	String licenseNumber;
	String county;
	String operation_type;
	String establishment_type;
	String entity_name;
	String dba_name;
	
	public OwnerView(String licenseNumber, String county, String operation_type, String establishment_type,
			String entity_name, String dba_name) {
		super();
		this.licenseNumber = licenseNumber;
		this.county = county;
		this.operation_type = operation_type;
		this.establishment_type = establishment_type;
		this.entity_name = entity_name;
		this.dba_name = dba_name;
	}
	
	public String getLicenseNumber() {
		return licenseNumber;
	}
	public void setLicenseNumber(String licenseNumber) {
		this.licenseNumber = licenseNumber;
	}
	public String getCounty() {
		return county;
	}
	public void setCounty(String county) {
		this.county = county;
	}
	public String getOperation_type() {
		return operation_type;
	}
	public void setOperation_type(String operation_type) {
		this.operation_type = operation_type;
	}
	public String getEstablishment_type() {
		return establishment_type;
	}
	public void setEstablishment_type(String establishment_type) {
		this.establishment_type = establishment_type;
	}
	public String getEntity_name() {
		return entity_name;
	}
	public void setEntity_name(String entity_name) {
		this.entity_name = entity_name;
	}
	public String getDba_name() {
		return dba_name;
	}
	public void setDba_name(String dba_name) {
		this.dba_name = dba_name;
	}
	
	
}
