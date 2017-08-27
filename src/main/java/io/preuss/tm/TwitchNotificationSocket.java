package io.preuss.tm;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by René Preuß on 7/20/2017.
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class TwitchNotificationSocket {

    private final static long TYPE_ID_SIGNAL_PING = -476754606;
    private final static long TYPE_ID_MESSAGE_RECEIVE = -635182161;
    private final static long TYPE_ID_SERVER_INFO = -815187584;
    private final static long TYPE_ID_FRIENDS_STATUS_CHANGE = 580569888;

    private final CountDownLatch closeLatch;
    private final TwitchMessenger messenger;
    @SuppressWarnings("unused")
    private Session session;

    public TwitchNotificationSocket(TwitchMessenger messenger) {
        this.messenger = messenger;
        this.closeLatch = new CountDownLatch(1);
    }

    public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
        return this.closeLatch.await(duration,unit);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.printf("Connection closed: %d - %s%n",statusCode,reason);
        this.session = null;
        this.closeLatch.countDown(); // trigger latch
    }

    @OnWebSocketConnect
    public void onConnect(final Session session) {
        this.session = session;
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("TypeID", -2101997347);

            JsonObject jsonBodyObject = new JsonObject();
            jsonBodyObject.addProperty("CipherAlgorithm", 0);
            jsonBodyObject.addProperty("CipherStrength", 0);
            jsonBodyObject.addProperty("ClientVersion", messenger.getClientVersion());
            jsonBodyObject.add("PublicKey", JsonNull.INSTANCE);
            jsonBodyObject.addProperty("MachineKey", messenger.getMachineKey());
            jsonBodyObject.addProperty("UserID", messenger.getUserId());
            jsonBodyObject.addProperty("SessionID", messenger.getSessionId());
            jsonBodyObject.addProperty("Status", 1);
            jsonObject.add("Body", jsonBodyObject);

            session.getRemote().sendString(jsonObject.toString());
        } catch (Throwable t) {
            t.printStackTrace();
        }

        Thread signalThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("TypeID", TYPE_ID_SIGNAL_PING);
                        JsonObject jsonBodyObject = new JsonObject();
                        jsonBodyObject.addProperty("Signal", true);
                        jsonObject.add("Body", jsonBodyObject);
                        session.getRemote().sendString(jsonObject.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        signalThread.start();
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        JsonObject messageObject = new JsonParser().parse(msg).getAsJsonObject();
        long typeId = messageObject.get("TypeID").getAsLong();

        if(typeId == TYPE_ID_MESSAGE_RECEIVE) {
            JsonObject bodyObject = messageObject.getAsJsonObject("Body");
            System.out.println(bodyObject.get("Body").getAsString());
            if(!bodyObject.get("ClientID").getAsString().equals(messenger.getClientId())) {
                for(TwitchMessengerListener listener : messenger.getListeners()) {
                    listener.onMessage(messenger
                            .getServer(bodyObject.get("ServerID").getAsString())
                            .getConversation(bodyObject.get("ConversationID").getAsString()), bodyObject.get("Body").getAsString());
                }
            }
        } else if(typeId == TYPE_ID_SIGNAL_PING) {
            // got pong
        } else if(typeId == TYPE_ID_SERVER_INFO) {
            // got server infos
        } else if(typeId == TYPE_ID_FRIENDS_STATUS_CHANGE) {
            // got friends status change events
        } else {
            System.out.println(String.format("Unhandled Web Socket Packet: %s, Message: %s", typeId, msg));
        }
    }
}
