package io.preuss.tm;

import com.google.gson.JsonObject;

import java.io.IOException;

/**
 * Created by René Preuß on 7/20/2017.
 */
public class TwitchConversation {
    private final TwitchServer twitchServer;
    private final String conversationId;

    public TwitchConversation(TwitchServer twitchServer, String conversationId) {
        this.twitchServer = twitchServer;
        this.conversationId = conversationId;
    }

    public void sendMessage(String message) throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("ClientID", twitchServer.getMessenger().getClientId());
        jsonObject.addProperty("MachineKey", twitchServer.getMessenger().getMachineKey());
        jsonObject.addProperty("Body", message);

        TwitchRestClient
                .build(twitchServer.getMessenger(), "conversations/%s", conversationId)
                .setBody(jsonObject)
                .execute(TwitchRestClient.POST);
    }

    public String getId() {
        return conversationId;
    }

    public TwitchServer getServer() {
        return twitchServer;
    }
}
