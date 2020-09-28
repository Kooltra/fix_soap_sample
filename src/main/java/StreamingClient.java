import org.cometd.bayeux.Channel;
import org.cometd.bayeux.client.ClientSessionChannel.MessageListener;
import org.cometd.client.BayeuxClient;
import org.cometd.client.transport.LongPollingTransport;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.net.URL;
import java.util.HashMap;

public class StreamingClient {
	private static final String STREAMING_ENDPOINT_URI = "/cometd/46.0";

	public static void start(String endpoint, String sessionId, String channel, MessageListener listener) throws Exception {
		HttpClient httpClient = new HttpClient(new SslContextFactory.Client());
		httpClient.start();
		final BayeuxClient client = new BayeuxClient(
				new URL(endpoint + STREAMING_ENDPOINT_URI).toExternalForm(),
				new LongPollingTransport(new HashMap<String, Object>(), httpClient) {
					@Override
					protected void customize(Request request) {
						request.header(HttpHeader.AUTHORIZATION, "OAuth " + sessionId);
						super.customize(request);
					}
				});

		client.getChannel(Channel.META_HANDSHAKE).addListener
				((MessageListener) (inChannel, message) -> {
					System.out.println("[CHANNEL:META_HANDSHAKE]: " + message);
					boolean success = message.isSuccessful();
					if (!success) {
						String error = (String) message.get("error");
						if (error != null) {
							System.out.println("Error during HANDSHAKE: " + error);
						}
						Exception exception = (Exception) message.get("exception");
						if (exception != null) {
							System.out.println("Exception during HANDSHAKE: ");
							exception.printStackTrace();

						}
					}
				});

		client.getChannel(Channel.META_CONNECT).addListener(
				(MessageListener) (inChannel, message) -> {
					System.out.println("[CHANNEL:META_CONNECT]: " + message);
					boolean success = message.isSuccessful();
					if (!success) {
						String error = (String) message.get("error");
						if (error != null) {
							System.out.println("Error during CONNECT: " + error);
						}
					}
				});

		client.getChannel(Channel.META_SUBSCRIBE).addListener(
				(MessageListener) (inChannel, message) -> {
					System.out.println("[CHANNEL:META_SUBSCRIBE]: " + message);
					boolean success = message.isSuccessful();
					if (!success) {
						String error = (String) message.get("error");
						if (error != null) {
							System.out.println("Error during SUBSCRIBE: " + error);
						}
					}
				});


		client.handshake();
		System.out.println("Waiting for handshake");

		boolean handshaken = client.waitFor(10 * 1000, BayeuxClient.State.CONNECTED);
		if (!handshaken) {
			System.out.println("Failed to handshake: " + client);
		}

		System.out.println("Subscribing for channel: " + channel);
		client.getChannel(channel).subscribe(listener);
		System.out.println("Waiting for streamed data from your organization ...");
	}
}
