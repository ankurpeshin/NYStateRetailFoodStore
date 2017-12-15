 -- WINDOWS PSQL CONFIGURATION SCRIPT
 
 
 --CREATE ROLE
 CREATE USER foodstore with PASSWORD 'foodstore' ;
 
 -- CREATE DATABASE
 CREATE database foodstoredb ;
 
 --GRANT PRIVILEGE
 grant all privileges on database foodstoredb to foodstore ;