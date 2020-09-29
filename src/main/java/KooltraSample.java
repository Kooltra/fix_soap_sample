import com.sforce.soap.enterprise.UpsertResult;
import com.sforce.soap.enterprise.sobject.Account;
import com.sforce.soap.enterprise.sobject.Kooltra__FxTrade__c;
import com.sforce.soap.enterprise.sobject.SObject;
import com.sforce.ws.ConnectionException;

import java.util.Calendar;
import java.util.Map;

public class KooltraSample {
	private static final String USERNAME = "YOUR_USERNAME";
	private static final String PASSWORD = "YOUR_PASSWORD";
	//private static final String ENDPOINT = "https://login.salesforce.com"; // for production orgs
	private static final String ENDPOINT = "https://test.salesforce.com"; // for sandbox/scratch orgs

	private static final String ENTITY_NAME = "Test Entity 1";
	private static final String ACCOUNT_NAME = "Kooltra Sample";
	private static final String EVENT_NAME = "TradeInserted__e";
	private static final String EVENT_FIELD_NAME = "TradeId__c";
	private static final long TRADE_INSERT_PERIOD = 15000;

	public static void main(String[] args) throws Exception {
		SoapClient client = new SoapClient();
		client.login(USERNAME, PASSWORD, ENDPOINT);
		client.startStreaming(EVENT_NAME, (channel, message) -> {
			Map<String, String> payload = (Map<String, String>) message.getDataAsMap().get("payload");
			String tradeId = payload.get(EVENT_FIELD_NAME);
			System.out.println(EVENT_NAME + " received with id " + tradeId);
			queryTrade(client, tradeId);
		});

		String accountId = client.getObjectIdByName("Account", ACCOUNT_NAME);
		if (accountId == null) {
			System.out.println("creating account " + ACCOUNT_NAME);
			accountId = insertAccount(client, ACCOUNT_NAME);
		}

		do {
			insertTrade(client, accountId);
			Thread.sleep(TRADE_INSERT_PERIOD);
		} while (true);
	}

	private static String insertAccount(SoapClient client, String accountName) throws ConnectionException {
		String entityId = client.getObjectIdByName("Kooltra__Entity__c", ENTITY_NAME);
		if (entityId == null) {
			throw new RuntimeException("Entity not found " + ENTITY_NAME);
		}

		Account account = new Account();
		account.setName(accountName);
		account.setKooltra__Code__c("KSAMPLE");
		account.setKooltra__Entity__c(entityId);
		account.setKooltra__Status__c("Active");
		account.setKooltra__BaseCurrency__c("CAD");
		account.setKooltra__Currencies__c("CAD;EUR;USD;");
		account.setKooltra__SettlementType__c("PAYMENTS");
		account.setKooltra__Type__c("Company");
		return client.getConnection().upsert("Id", new SObject[]{account})[0].getId();
	}

	private static void insertTrade(SoapClient client, String accountId) throws ConnectionException {
		System.out.println("inserting a Kooltra__FxTrade__c...");
		Kooltra__FxTrade__c trade = new Kooltra__FxTrade__c();
		trade.setKooltra__Counterparty__c(accountId);
		trade.setKooltra__DealtCurrency__c("CAD");
		trade.setKooltra__Amount1__c(10000.0);
		trade.setKooltra__Currency1__c("CAD");
		trade.setKooltra__Amount2__c(6400.0);
		trade.setKooltra__Currency2__c("EUR");
		trade.setKooltra__Rate__c(6400.0/10000.0);
		trade.setKooltra__Action__c("BUY");
		trade.setKooltra__Status__c("Open");
		trade.setKooltra__TradeDate__c(Calendar.getInstance());

		Calendar valueDate = Calendar.getInstance();
		valueDate.add(Calendar.DATE, 2);
		trade.setKooltra__ValueDate__c(valueDate);

		UpsertResult result = client.getConnection().upsert("Id", new SObject[]{trade})[0];
		if (!result.isSuccess()) {
			throw new RuntimeException(result.getErrors()[0].getMessage());
		}

		System.out.println("Kooltra__FxTrade__c inserted with id " + result.getId());
	}

	private static void queryTrade(SoapClient client, String tradeId) {
		try {
			for (SObject record : client.getConnection().query("SELECT " +
					"Name, Kooltra__Counterparty__r.Name, " +
					"Kooltra__Amount1__c, Kooltra__Currency1__c, " +
					"Kooltra__Amount2__c, Kooltra__Currency2__c, " +
					"Kooltra__Status__c " +
					"FROM Kooltra__FxTrade__c WHERE Id='" + tradeId + "'").getRecords()) {
				Kooltra__FxTrade__c trade = (Kooltra__FxTrade__c) record;
				System.out.println(
						trade.getName() + "\t" +
						trade.getKooltra__Status__c() + "\t" +
						trade.getKooltra__Counterparty__r().getName() + "\t" +
						trade.getKooltra__Amount1__c() + " " + trade.getKooltra__Currency1__c() + "\t" +
						trade.getKooltra__Amount2__c() + " " + trade.getKooltra__Currency2__c()
				);
			}
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
	}
}
