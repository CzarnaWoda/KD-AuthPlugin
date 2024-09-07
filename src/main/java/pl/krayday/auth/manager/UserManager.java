package pl.krayday.auth.manager;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import pl.krayday.auth.Auth;
import pl.krayday.auth.data.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class UserManager {

    private static HashMap<UUID, User> authUsers = new HashMap<>();


    public static User createUser(String player, UUID uuid, String firstip, boolean premium){
        User u = new User(player, uuid, premium);
        u.setFirstIP(firstip);
        authUsers.put(u.getUuid(), u);
        return u;
    }

    public static User getUser(UUID uuid){
        return authUsers.get(uuid);
    }

    public static User getUser(ProxiedPlayer proxiedPlayer){
        return authUsers.get(proxiedPlayer.getUniqueId());
    }

    public static User getUser(String name){
       for(User user : authUsers.values()){
           if(user.getLastName().equalsIgnoreCase(name)){
               return user;
           }
       }
       return null;
    }

    public static void setup(){
        try{
            ResultSet rs = Auth.getStore().query("SELECT * FROM `%P%users`");
            while(rs.next()){
                User u = new User(rs);
                authUsers.put(u.getUuid(), u);
            }
            Auth.sendAuthMessage("Loaded " + authUsers.size() + " accounts!");
        }catch (SQLException e){

        }
    }
}
