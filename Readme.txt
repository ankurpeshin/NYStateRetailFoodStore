* DBMS Final Project Java StandAlone *

Contributors: Ankur Peshin

Instructions for running:

1) Make sure that your enviroment variables in My Computer has a PATH ENTRY FOR JDK, pointing to <JDK_Installation Path>/Bin
2) Double click Run.bat or open it using notepad and type each instruction separately on cmd prompt.
3) You will be asked for username, password ( for superuser => usually postgres, postgres ) and port ( usually 5432). If you meet these defaults, 
    you can press 'N' and continue, otherwise enter information.
4) In case scripts don't run through Java because of connectivity issues with super-user, comment line 59 in final.java (createSchema)
   ,run the SQL Scripts 1 and 2 manually, and start Run.bat again.
5) For any other issue, feel free to mail us.
    


Other Info

- Data Used ( Retail Food Store, and Retail Food Store Violations )
- Building ( Simply run from Command Prompt, or run in an IDE by Running Final.java
	In case running from an IDE, please include required JARS in the Project Classpath )
- Application will create the schema, user, tables, and populate the tables on every run, and will drop the schema on normal termination
  In case application terminates abnormally, execute the drop Scripts ( Automatic Loading )
- Application will provide user with choices on what query they want to run out of 7 queries provided.

Work Done on Database:

We have created 3 tables which are normalized.
We have joined 2 tables and the join queries are stored in a materialized view
Any update to FoodStoreViolation Table will update the materialized view, and that function will be executed by a trigger.


Graduate Focus

We broke a part of our relation 'FoodStoreViolation' to an XML file which we are loading on runtime.
It maps deficiency Code Column to Deficiency Description. ( Deficiency desc is removed from food store violation )

For example :

<row>
        <DeficiencyNumber>04H</DeficiencyNumber>
        <DeficiencyDescription>Equipment cleaning or sanitizing facilities inadequate for establishments handling potentially hazardous foods</DeficiencyDescription>
    </row>
    <row>
        <DeficiencyNumber>09F</DeficiencyNumber>
        <DeficiencyDescription>"Cleaning or sanitizing equipment</DeficiencyDescription>
    </row>


Folders:

Lib: Contains JAR file of required runtimes
Files : Files which are loaded into Database at runtime
Backup : Contains source code of all the files 
	- Scripts.java : Class containing queries
	- XMLCreators.java : Class for constructing XML
	- ReadXMLFile.java : Class for reading XML into program
	
These 3 files are packaged into Scripts.jar for functionality.


More information on the dataset/ and its column information may be found at:

https://data.ny.gov/Economic-Development/Retail-Food-Stores/9a8c-vfzj
https://data.ny.gov/Economic-Development/Retail-Food-Store-Inspections-Current-Critical-Vio/d6dy-3h7r


	