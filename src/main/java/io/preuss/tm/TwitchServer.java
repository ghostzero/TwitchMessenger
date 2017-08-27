package io.preuss.tm;

/**
 * Created by René Preuß on 7/20/2017.
 */
public class TwitchServer {
    private final TwitchMessenger twitchMessenger;
    private final String serverId;

    public TwitchServer(TwitchMessenger twitchMessenger, String serverId) {
        this.twitchMessenger = twitchMessenger;
        this.serverId = serverId;
    }

    public TwitchConversation getConversation(String conversationId) {
        return new TwitchConversation(this, conversationId);
    }

    public TwitchMessenger getMessenger() {
        return twitchMessenger;
    }

    public String getId() {
        return serverId;
    }
}
