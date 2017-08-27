package io.preuss.tm;

import com.google.gson.JsonElement;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by René Preuß on 7/20/2017.
 */
public class TwitchRestClient {
    public static final String POST = "POST";
    public static final String GET  = "GET";

    private final TwitchMessenger messenger;
    private final String route;
    private JsonElement jsonElement;

    private static final HttpClient client;

    static {
        client = new HttpClient();
        client.getParams().setParameter("http.useragent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
    }

    private TwitchRestClient(TwitchMessenger messenger, String route, Object[] objects) {
        this.messenger = messenger;
        this.route = String.format(route, objects);
    }

    public static TwitchRestClient build(TwitchMessenger messenger, String route, Object... objects) {
        return new TwitchRestClient(messenger, route, objects);
    }

    public TwitchRestClient setBody(JsonElement jsonElement) {
        this.jsonElement = jsonElement;
        return this;
    }

    public InputStream execute(String method) throws IOException {
        if(method.equals(POST)) {
            PostMethod post = new PostMethod(String.format("https://conversations-v1.curseapp.net/%s", route));
            post.setRequestHeader("AuthenticationToken", messenger.getAuthToken());
            post.setRequestHeader("Content-Type", "application/json");
            post.setRequestBody(jsonElement.toString());

            BufferedReader br = null;
            System.out.println("exec post");
            try{
                int returnCode = client.executeMethod(post);

                if(returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
                    System.err.println("The Post method is not implemented by this URI");
                    // still consume the response body
                    post.getResponseBodyAsString();
                } else {
                    br = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
                    String readLine;
                    while(((readLine = br.readLine()) != null)) {
                        System.err.println(readLine);
                    }
                }
                System.out.println(returnCode);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                post.releaseConnection();
                if(br != null) try { br.close(); } catch (Exception ignored) {}
            }
        }
        return null;
    }
}
