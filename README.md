RuleExporter
===============

F5iRuleExporter connects to a list of F5 Big-IP Local Traffic Manager (LTM) and Global Traffic Manager (GTM) devices, exporting all non system iRules to text files.

**Notes** 
 + Maven currently does not fetch the iControl Java SDK. This needs to be obtained via F5 DevCentral and added manually.

### Configuration

The **f5export.properties** properties file contains the following fields:

 + authserver - Specifies a single device to test initial authentication
    + *Example:* authserver=ltm01a.megacorp.ca
 + username - Username to connect to the F5 devices. Preferably read only, since this is human readable in the config file
    + *Example:* username=f5readonlyuser 
 + password - Acount password
    + *Example:* password=sw33tpwBr0  
 + ltm.list - Comma seperated list of LTM devices
    + *Example:* ltm.list=ltm01a.megacorp.ca,ltm01b.megacorp.ca,ltm02a.megacorp.ca,ltm02b.megacorp.ca
 + gtm.list - Comme seperated list of GTM devices
    + *example:* gtm.list=gtm01.megacorp.ca,gtm02.megacorp.ca
 + export.path
    + *Example:* export.path=/home/nuszkowski/F5iRuleExporter/rules


### Execution

java -jar /home/nuszkowski/F5iRuleExport/F5iRuleExport.jar -config /home/nuszkowski/F5iRuleExport/f5export.properties

**Note:** -Dlog4j.configuration="file:///home/nuszkowski/F5iRuleExport/log4j.properties" can be provided if you would like to externalize the log4j configuration

### Versioning

If you care to version your iRules (which is my primary purpose for this app), you can include the execution as well as
versioning commands inside of a shell script and perhaps schedule it via cron.

