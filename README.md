# Kooltra Java Sample

## Setting up a local development environment

### System Requirements
- Node (version: 10 LTS) 
    - `brew install node@10 && brew link --force --overwrite node@10`
- Java Development Kit (version: 8) 
    - [AdoptOpenJDK](https://adoptopenjdk.net)
    - [Amazon Corretto](https://aws.amazon.com/corretto/)
- Salesforce Developer Experience (SFDX)
    - `npm install --global sfdx-cli@6.54.4`
    

### Org Setup
1. Connecting to a Salesforce Sandbox Org

    `sfdx force:auth:web:login --setalias my-sandbox --instanceurl https://test.salesforce.com`
    
1. Connecting to a Salesforce Production Org

    `sfdx force:auth:web:login --setalias prod --instanceurl https://login.salesforce.com`
    
1. Push the sample project to your org

    `sfdx force:source:push -u my-sandbox`

### Running the Java App
1. Add your user credentials to `KooltraSample.java`
    ```
        private static final String USERNAME = "YOUR_USERNAME";
        private static final String PASSWORD = "YOUR_PASSWORD";
    ```
1. Set the endpoint URL required for a production or sandbox org
    ```
        // private static final String ENDPOINT = "https://login.salesforce.com"; // for production orgs
        private static final String ENDPOINT = "https://test.salesforce.com"; // for sandbox/scratch orgs
    ```
1. Kooltra data is grouped by Entity. This sample adds data to an entity called `Test Entity 1`. You can either create 
this entity in your org or change the entity name used in the sample
    ```
        private static final String ENTITY_NAME = "Test Entity 1";
    ``` 
1. Run the sample
    ```
        ./gradlew run
    ```
   
   
### What is this sample doing?

This sample shows how to build an integration that queries data from your org, inserts
data to your org, and receives real time events from your org triggered by data changes. 

#### ./sample_package

This subproject is a simple salesforce package that adds code to your salesforce org. It adds a trigger called `TradeTrigger` 
that is invoked when a `Kooltra__FxTrade__c` is inserted or updated. You can put any `Apex` code in this trigger but in 
this sample we publish a Salesforce Platform Event using `EventBus.publish()` for each trade inserted. The event we 
publish is defined in this sample_package and is called `TradeInserted__e` and defined in `TradeInserted__e.object-meta.xml`.
It has one field called `TradeId__c` which is meant to indicate the id of the trade that's inserted. Subscribers to this 
event will receieve all fields populated on the event object.

#### ./sf-ws-client  

This subproject is a simple gradle project that builds a Java library from the Kooltra `wsdl` file. The generated Java 
library contains Java objects for all Kooltra objects on Salesforce and can be used to manage data from a Salesforce Org.

#### ./src/main/java/SoapClient.java

The SoapClient is a simple SOAP client that connects to a Salesforce Org via username and password. It exposes a 
`EnterpriseConnection` instance to interact with an Org. 


#### ./src/main/java/StreamingClient.java

The Streaming client uses the session id from the SoapClient to authenticate and receive streaming events from your Org 
using the Cometd protocol.

#### ./src/main/java/KooltraSample.java
 
This java sample uses `SoapClient` to login into your org based on credentials set in this file. It will insert a sample
account if one does not exist and then inserts a trade every 15 seconds. It adds listener to the `StreamingClient` which
receives the `TradeInserted__e` event and queries back to Salesforce for the trade data. 

The following is an example of output from this process. 

```
    Kooltra__FxTrade__c inserted with id a0J5B000002WtCZUA0
    TradeInserted__e received with id a0J5B000002WtCZUA0
    50.0	Open	Kooltra Sample	10000.0 CAD	-6400.0 EUR
    inserting a Kooltra__FxTrade__c...
    Kooltra__FxTrade__c inserted with id a0J5B000002WtCeUAK
    TradeInserted__e received with id a0J5B000002WtCeUAK
    51.0	Open	Kooltra Sample	10000.0 CAD	-6400.0 EUR
    inserting a Kooltra__FxTrade__c...
    Kooltra__FxTrade__c inserted with id a0J5B000002WtCoUAK
    TradeInserted__e received with id a0J5B000002WtCoUAK
    52.0	Open	Kooltra Sample	10000.0 CAD	-6400.0 EUR
```
