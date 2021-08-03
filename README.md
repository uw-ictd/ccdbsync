# CCDBSync

## Project Description:

The purpose of this project is to sync a SQL database with data from an [ODK-X Sync-Endpoint][1].  

## Setting Up the Development Environment:
1.  Install [git][2]
2.  Install [maven][3]
3.  Install [openjdk-8][4]
4.  Clone this repository and `cd` into it
5.  Set up your config directory ([See instructions below](#config-values)) 
6.  To install the local ODK-X dependencies:
```
mvn validate
```
7.  To build the executable jar:
```
mvn clean package
```

8.  To execute the jar:
```
java -jar target/ccdbsync-*-SNAPSHOT-jar-with-dependencies.jar
```

## Config Values
The following files must be in the `config` directory.
1.  [blacklisted_tables.txt](#blacklisted_tablestxt)
2.  [config.txt](#configtxt)
3.  [tables_to_convert.csv](#tables_to_convertcsv)

### blacklisted_tables.txt
The `blacklisted_tables.txt` file can be used to ignore tables on the ODK-X Sync-Endpoint server to
sync to the SQL database.  If none of the tables should be ignored, this file should be empty.  To 
ignore a table, put the ODK-X table name on one line of the 
file.  For example, to ignore tables refrigerators and refrigerator_temperature_data the content of 
blacklisted_tables.txt should be:

```
refrigerators
refrigerator_temperature_data
```

### config.txt
The `config.txt` file contains the credentials for the SQL database and ODK-X Sync-Endpoint as 
well as other configuration values.  The order of values is below:

```
jdbc:postgresql://host:port/database
databaseUsername
databasePassword
syncEndpointURL
syncEndpointAppId
syncEndpointUsername
syncEndpointPassword
logsAndDataDir
defaultTimeZone
logTimeZone
```

Example configuration values could be:
```
SQL Server Name: db.windows.net 
SQL Server Port: 5432
SQL Database Name: testdb
SQL Username: testuser
SQL Password: testpass
ODK-X Sync-Endpoint URL: https://testserver.com/odktables
ODK-X Sync-Endpoint Username: testODKUser
ODK-X Sync-Endpoint Password: testODKPass
ODK-X Sync-Endpoint AppID: default
Directory to Store Logs and Data: logsAndData
Default Time Zone: America/Los_Angeles
Log Time Zone: America/Los_Angeles
```
The corresponding config.txt would be:

```
jdbc:postgresql://db.windows.net:5432/testdb
testuser
testpass
https://testserver.com/odktables
default
testODKUser
testODKPass
logsAndData
America/Los_Angeles
America/Los_Angeles
```

NOTE: `odktables` is required at the end of the ODK-X Sync-Endpoint URL.

### tables_to_convert.csv
`tables_to_convert.csv` is a CSV file that maps the ODK-X Sync-Endpoint fields to the SQL 
database fields.  The header for the CSV file should be:  

<table>
  <thead>
    <tr>
      <th>source_table</th>
      <th>destination_table</th>
      <th>source_field</th>
      <th>target_field</th>
      <th>field_type</th>
    </tr>
  </thead>
</table>
<br/>
To sync field `test_data` from `test_odkx_table` ODK-X table to `sql_test_data` in SQL table 
`sql_test_table`, see the example `tables_to_convert.csv` file below:  

<table>
  <thead>
    <tr>
      <th>source_table</th>
      <th>destination_table</th>
      <th>source_field</th>
      <th>target_field</th>
      <th>field_type</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>test_odkx_table</td>
      <td>sql_test_table</td>
      <td>test_data</td>
      <td>sql_test_data</td>
      <td></td>
    </tr>
  </tbody>
</table>  
<br/>
The `field_type` can be left blank if the type in the database is VARCHAR.  
Other valid field types are INT, DOUBLE, DATETIME, and TIME.  Every field that should be synced 
from an ODK-X table to a SQL table should be defined in `tables_to_convert.csv`.

## Execute the Code Via a Cron Job in a Docker Container
Install [docker][5]

To build the docker container
```
docker build -t data-sync .
```

To run the container
```
docker run -p5432:5432 data-sync
```

The `entrypoint.sh` script has the details of the cron job.  

## Integration Tests
In order for the tests to run correctly, a config directory must exist under the test directory 
with a valid ODK-X Sync-Endpoint and SQL database.

To run the tests:
```
mvn test -DskipTests=false
```

[1]: https://github.com/odk-x/sync-endpoint
[2]: https://git-scm.com/book/en/v2/Getting-Started-Installing-Git
[3]: https://maven.apache.org/install.html
[4]: https://openjdk.java.net/install/
[5]: https://docs.docker.com/get-docker/