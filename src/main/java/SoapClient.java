import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import org.cometd.bayeux.client.ClientSessionChannel.MessageListener;

import java.net.URI;

public class SoapClient {
	private final String SOAP_ENDPOINT = "/services/Soap/c/46.0/";
	private EnterpriseConnection connection;

	public void login(String username, String password, String endpoint) throws ConnectionException {
		ConnectorConfig config = new ConnectorConfig();
		config.setUsername(username);
		config.setPassword(password);
		config.setAuthEndpoint(endpoint + SOAP_ENDPOINT);
		connection = new EnterpriseConnection(config);
	}

	public void startStreaming(String eventName, MessageListener listener) throws Exception {
		URI uri = new URI(connection.getConfig().getServiceEndpoint());
		String instanceUrl = uri.getScheme() + "://" + uri.getHost();
		StreamingClient.start(
				instanceUrl,
				connection.getSessionHeader().getSessionId(),
				"/event/" + eventName,
				listener);
	}

	public String getObjectIdByName(String object, String name) throws ConnectionException {
		SObject[] records = connection.query(
				"SELECT Id FROM " + object + " WHERE Name='" + name + "'").getRecords();
		if (records.length == 0) {
			return null;
		}
		return records[0].getId();
	}

	public EnterpriseConnection getConnection() {
		return connection;
	}
}