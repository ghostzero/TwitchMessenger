package io.preuss.tm;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by René Preuß on 7/20/2017.
 */
public class TwitchMessenger {

    private final String clientId;
    private final String authToken;
    private final String sessionId;
    private List<TwitchMessengerListener> listeners = new ArrayList<TwitchMessengerListener>();
    private long userId;
    private String machineKey;

    public void connect() throws Exception {
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        request.setHeader("Cookie", "CurseAuthToken=" + URLEncoder.encode(getAuthToken()));
        WebSocketClient client = new WebSocketClient(new SslContextFactory());
        TwitchNotificationSocket socket = new TwitchNotificationSocket(this);
        client.start();
        URI destUri = new URI("wss://notifications-eu-v1.curseapp.net/");
        System.out.println("Connecting to " + destUri);
        client.connect(socket, destUri, request);
    }

    public TwitchMessenger(long userId, String clientId, String authToken, String machineKey, String sessionId) {
        this.userId = userId;
        this.clientId = clientId;
        this.authToken = authToken;
        this.machineKey = machineKey;
        this.sessionId = sessionId;
    }

    public TwitchServer getServer(String serverId) {
        return new TwitchServer(this, serverId);
    }

    public String getMachineKey() {
        return machineKey;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientVersion() {
        return "7.5.8";
    }

    public long getUserId() {
        return userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setListener(TwitchMessengerListener listener) {
        listeners.add(listener);
    }

    public List<TwitchMessengerListener> getListeners() {
        return listeners;
    }
}
