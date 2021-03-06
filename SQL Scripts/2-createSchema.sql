CREATE TABLE RETAILFOODSTORE (
LICENSE_NUMBER INTEGER NOT NULL PRIMARY KEY,
COUNTY CHAR(100) NOT NULL,
OPERATION_TYPE CHAR(100),
ESTABLISHMENT_TYPE CHAR(10),
ENTITY_NAME CHAR(100) NOT NULL,
DBA_NAME CHAR(100)
);

CREATE TABLE ADDRESS (
LICENSE_NUMBER INTEGER NOT NULL PRIMARY KEY REFERENCES RETAILFOODSTORE(LICENSE_NUMBER),
STREET_NUM CHAR(50),
STREET_NAME CHAR(100),
CITY CHAR(50),
STATE CHAR(10),
ZIP_CODE INTEGER,
SQUARE_FOOTAGE INTEGER,
LOCATION CHAR(100)
);

CREATE TABLE FOODSTOREVIOLATIONS (
id SERIAL PRIMARY KEY,
COUNTY CHAR(100) NOT NULL,
INSPECTIONDATE DATE NOT NULL,
OWNER_NAME CHAR(100),
TRADE_NAME CHAR(100),
STREET_NUM CHAR(50),
STREET_NAME CHAR(100),
CITY CHAR(50),
STATE_CODE CHAR(10),
ZIP_CODE INTEGER,
DEFICIENCY_CODE CHAR(10),
LOCATION CHAR(100)
);