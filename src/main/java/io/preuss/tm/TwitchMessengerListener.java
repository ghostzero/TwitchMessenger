package io.preuss.tm;

/**
 * Created by René Preuß on 7/20/2017.
 */
public interface TwitchMessengerListener {
    public void onMessage(TwitchConversation conversation, String message);
}
