import java.util.ArrayList;
import java.util.List;

public class Scripts {
	
	public Scripts(){
		
		initScripts.add(CREATE_ROLE);
		initScripts.add(CREATE_DB);
		initScripts.add(GRANT_PRIVILEGE);
		
		schemaScripts.add(CREATE_RFS);
		schemaScripts.add(ADDRESSTABLE);
		schemaScripts.add(CREATE_FSV);
		
		postScripts.add(MAT_VIEW);
		postScripts.add(FUNCTION);
		postScripts.add(TRIGGER_1);
		postScripts.add(TRIGGER_2);
		postScripts.add(TRIGGER_3);
		
		dropScripts.add(DROP_DB);
		dropScripts.add(DROP_ROLE);
		
		
	}

	
	public  List<String> initScripts = new ArrayList<String>();
	public  List<String> schemaScripts = new ArrayList<String>();
	public  List<String> postScripts = new ArrayList<String>();
	public  List<String> dropScripts = new ArrayList<String>();
	
	//New Database Scripts
	public static final String CREATE_ROLE = "CREATE USER foodstore with PASSWORD 'foodstore' ;";
	public static final String CREATE_DB = " CREATE database foodstoredb ;";
	public static final String GRANT_PRIVILEGE = "grant all privileges on database foodstoredb to foodstore ;";
	
	
	
	//Create Database Objects
	public static final String CREATE_RFS = "CREATE TABLE RETAILFOODSTORE (\r\n" + 
			"LICENSE_NUMBER INTEGER NOT NULL PRIMARY KEY,\r\n" + 
			"COUNTY CHAR(100) NOT NULL,\r\n" + 
			"OPERATION_TYPE CHAR(100),\r\n" + 
			"ESTABLISHMENT_TYPE CHAR(10),\r\n" + 
			"ENTITY_NAME CHAR(100) NOT NULL,\r\n" + 
			"DBA_NAME CHAR(100)\r\n" + 
			");";
	
	public static final String CREATE_FSV = "CREATE TABLE FOODSTOREVIOLATIONS (\r\n" + 
			"id SERIAL PRIMARY KEY,\r\n" + 
			"COUNTY CHAR(100) NOT NULL,\r\n" + 
			"INSPECTIONDATE DATE NOT NULL,\r\n" + 
			"OWNER_NAME CHAR(100),\r\n" + 
			"TRADE_NAME CHAR(100),\r\n" + 
			"STREET_NUM CHAR(50),\r\n" + 
			"STREET_NAME CHAR(100),\r\n" + 
			"CITY CHAR(50),\r\n" + 
			"STATE_CODE CHAR(10),\r\n" + 
			"ZIP_CODE INTEGER,\r\n" + 
			"DEFICIENCY_CODE CHAR(10),\r\n" + 
			"LOCATION CHAR(100)\r\n" + 
			");";
	
	public static final String ADDRESSTABLE = "CREATE TABLE ADDRESS (\r\n" + 
			"LICENSE_NUMBER INTEGER NOT NULL PRIMARY KEY REFERENCES RETAILFOODSTORE(LICENSE_NUMBER),\r\n" + 
			"STREET_NUM CHAR(50),\r\n" + 
			"STREET_NAME CHAR(100),\r\n" + 
			"CITY CHAR(50),\r\n" + 
			"STATE CHAR(10),\r\n" + 
			"ZIP_CODE INTEGER,\r\n" + 
			"SQUARE_FOOTAGE INTEGER,\r\n" + 
			"LOCATION CHAR(100)\r\n" + 
			");";
	
	
	//Post Scripts
	public static final String MAT_VIEW ="create materialized view license_deficiency_view as\r\n" + 
			"select fsv.owner_name,fsv.deficiency_code,fsv.inspectiondate,temp2.dba_name,temp2.license_number from\r\n" + 
			"foodstoreviolations fsv \r\n" + 
			"inner join\r\n" + 
			"(\r\n" + 
			"select rfs.license_number,rfs.entity_name,rfs.dba_name,addr.street_num,addr.street_name,addr.zip_code,rfs.county from\r\n" + 
			"RetailFoodStore rfs inner join address addr on rfs.license_number=addr.license_number\r\n" + 
			") temp2\r\n" + 
			"on fsv.street_num=temp2.street_num and \r\n" + 
			"fsv.street_name = temp2.street_name;";
	
	
	public static final String  FUNCTION = "create or replace function refresh_mat_view()\r\n" + 
			"returns trigger language plpgsql\r\n" + 
			"as $$\r\n" + 
			"begin\r\n" + 
			"    refresh materialized view license_deficiency_view;\r\n" + 
			"    return null;\r\n" + 
			"end $$;";
	
	public static final String TRIGGER_1 = "create trigger refresh_mat_view\r\n" + 
			"after insert or update or delete or truncate\r\n" + 
			"on retailfoodstore for each statement \r\n" + 
			"execute procedure refresh_mat_view();";
	
	public static final String TRIGGER_2 = "create trigger refresh_mat_view\r\n" + 
			"after insert or update or delete or truncate\r\n" + 
			"on address for each statement \r\n" + 
			"execute procedure refresh_mat_view();";
	
	public static final String TRIGGER_3 = "create trigger refresh_mat_view\r\n" + 
			"after insert or update or delete or truncate\r\n" + 
			"on foodstoreviolations for each statement \r\n" + 
			"execute procedure refresh_mat_view();";
	
	
	//Drop Statements
	public static final String DROP_DB = "drop database foodstoredb;";
	public static final String DROP_ROLE= "drop role foodstore;";
	
	
	
	public static final String DIRECTORY = "D:\\DBProject\\Material\\";
	
	
	//
	//File Path Retail Food Stores
	public static String FILE_PATH = DIRECTORY+"Retail_Food_Stores.csv";
	
	//Violations CSV Path
	public static String FILE_PATH_2 =DIRECTORY+"Retail_Food_Store_Inspections___Current_Critical_Violations.csv";
	
	//Food Store Address CSV PATH
	public static String FILE_PATH_ADDRESS=DIRECTORY+"FoodStoreAddress.csv";
	
	//Food Inspection Address CSV PATH
	public static String food_ins_path=DIRECTORY+"Food_Inspection_Address.csv";
	
	public static String desc_xml = DIRECTORY+ "Deficiency_Desc.xml";
	
	//Food Store Insert Statement
	public static String FOOD_STORE_INSERT = "INSERT INTO retailfoodstore(license_number, "
			+ "county, operation_type, establishment_type,entity_name, dba_name)"
			+ "VALUES ( ?, ?, ?, ?, ?, ?);";
	
	/*public static String FOOD_STORE_VIOLATE = "INSERT INTO public.foodinspection( county,"
			+ " inspectiondate, owner_name, trade_name,deficiency_code)VALUES (?, ?, ?, ?, ?);";*/
	
	
	//Food Store Violate Insert Statement
	public static String FOOD_STORE_VIOLATE ="INSERT INTO public.foodstoreviolations( county,"
			+ " inspectiondate, owner_name, trade_name, street_num, street_name, city, state_code,"
			+ " zip_code, deficiency_code,location)VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	
	// Food Store Address Insert Statement
	public static String ADDRESS = "INSERT INTO address(license_number,street_num,street_name"+
			",city,state,zip_code,square_footage,location) VALUES (?,?,?,?,?,?,?,?)";
	
	// Food Inspection Address Insert Statement
	public static String food_ins_address = "INSERT INTO public.FOOD_INSPECTION_ADDRESS(owner_name,street_num,street_name"+
			",city,state_code,zip_code) VALUES (?,?,?,?,?,?)";
	

/*	
	private static String OPERATING_SYS = System.getProperty("os.name").toLowerCase();

	private static String PORT = "5432";
	
	private static String createScript = "D:\\DBProject\\Material\\Script.sql";
	private static String constructSchema = "D:\\DBProject\\Material\\createSchema.sql";
	private static String dropScript = "D:\\DBProject\\Material\\dropScript.sql";
	private static String postSchemaScript = "D:\\DBProject\\Material\\postSchemaScript.sql";
	
	private static String POSTGRES_SUPERUSER = "postgres";
	
	private static String POSTGRES_SUPERPASS = "postgres";
	
	//Script for creating DB and User
	private static String INIT_DBANDUSER = "psql.exe -U " +POSTGRES_SUPERUSER + " -p " + PORT +" -f  " + createScript;
	
	//Script for creating Tables etc
	private static String CREATESCHEMAOBJ = "psql.exe -U foodstore -d foodstoredb -f " + constructSchema ;
	
	//Script for dropping DB and User
	private static String DROP_DBANDUSER = "psql.exe -U " +POSTGRES_SUPERUSER + " -p " + PORT +" -f  " + dropScript;
	
	//Contains script for Generating Triggers, Views etc
	private static String MODIFY_SCHEMA = "psql.exe -U foodstore -d foodstoredb -f  " + postSchemaScript;	

*/	
	
	
	
	
}
