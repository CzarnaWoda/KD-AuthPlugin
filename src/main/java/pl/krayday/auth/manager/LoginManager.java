package pl.krayday.auth.manager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Data;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;
import pl.krayday.auth.Auth;
import pl.krayday.auth.data.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Data
public class LoginManager {

    private int currentJoints;
    private Map<String, PreLoginEvent> waiting;
    private Cache<String, Boolean> premium;
    public void registerEvent(String name, PreLoginEvent event){
        registerIntent(name,event);
        //final String ip = event.getConnection().getAddress().getAddress().getHostAddress();
        final User user = UserManager.getUser(name);
        if(user != null){
            getPremium().put(name.toLowerCase(),user.isPremium());
        }
        final Boolean ifPresent = premium.getIfPresent(name.toLowerCase());
        if(ifPresent != null){
            this.unRegisterIntent(name, ifPresent);
            return;
        }
        try {
            final String html = getHTML("http://someapiadress/haspaid/?name=" + name + "&auth=apikey");
            if(html.equalsIgnoreCase("error")){
                Auth.sendAuthMessage("REQUEST->HASHPAID->" + name + "->ERROR->result:[html: ERROR, REASON: TOMANYREQUEST]");
                unRegisterIntent(name, Auth.getConfig().getString("messages.html_error"));
                return;
            }
            Auth.sendAuthMessage("REQUEST->HASHPAID->" + name + "->FINISH->result:[html:" + html.toUpperCase()+ "]");
            final boolean premium = Boolean.parseBoolean(html);
            getPremium().put(name.toLowerCase(),premium);
            this.unRegisterIntent(name, premium);
        } catch (Exception e) {
            e.printStackTrace();
            Auth.sendAuthMessage("REQUEST->HASHPAID->" + name + "->ERROR->result:[html: ERROR , REASON: EXCEPTIONERROR]");
            unRegisterIntent(name, Auth.getConfig().getString("messages.html_exception"));
        }
    }
    public LoginManager(){
        currentJoints = 0;
        waiting = new HashMap<>();
        premium = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.HOURS).build();
    }

    public void addJoin(int index){
        currentJoints += index;
    }
    private void registerIntent(String name, PreLoginEvent event){
        event.registerIntent(Auth.getAuth());
        waiting.put(name.toLowerCase(), event);
        Auth.sendAuthMessage("AUTH->startIntent->" + name + "->registerToWaiting");
    }
    private void unRegisterIntent(final String name, final String reason) {
        final PreLoginEvent event = waiting.get(name.toLowerCase());
        event.setCancelReason(new TextComponent(reason));
        event.setCancelled(true);
        event.completeIntent(Auth.getAuth());
        waiting.remove(name.toLowerCase());
        Auth.sendAuthMessage("AUTH->endIntent-> + " + name + "->unregisterToWaiting" + "->result:[PREMIUM:" + " none" + ", REASON: " + reason + "]");
    }
    private void unRegisterIntent(final String name, final boolean premium) {
        final PreLoginEvent event = this.waiting.get(name.toLowerCase());
        event.getConnection().setOnlineMode(premium);
        event.completeIntent(Auth.getAuth());
        waiting.remove(name.toLowerCase());
        Auth.sendAuthMessage("AUTH->endIntent-> + " + name + "->unregisterToWaiting" + "->result:[PREMIUM: " + premium + "]");
    }
    public static String getHTML(final String urlToRead) throws Exception {
        final StringBuilder result = new StringBuilder();
        final URL url = new URL(urlToRead);
        final HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(4000);
        conn.setReadTimeout(4000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("charset", "utf-8");
        final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }
}
