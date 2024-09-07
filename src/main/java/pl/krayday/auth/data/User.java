package pl.krayday.auth.data;


import lombok.Data;
import pl.krayday.auth.Auth;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Data
public class User {

    private int id;

    private UUID uuid;
    private String  lastName, firstIP, lastIP, password;

    private boolean premium;

    public User(String player, UUID uid, boolean premium) {
        uuid = uid;
        lastName = player;
        firstIP = "";
        lastIP = "";
        password = "";
        this.premium = premium;
        insert();
    }

    public User(ResultSet rs)throws SQLException {
        uuid = UUID.fromString(rs.getString("uuid"));
        lastName = rs.getString("lastName");
        firstIP = rs.getString("firstIP");
        lastIP = rs.getString("lastIP");
        password = rs.getString("password");
        premium = rs.getBoolean("premium");
    }


    public void insert() {
        Auth.getStore().update("INSERT INTO `%P%users` (`id`,`uuid`,`firstIP`,`lastIP`,`lastName`,`password`,`premium`) VALUES (NULL,'" + uuid + "', '" + firstIP + "', '" + lastIP + "', '" + lastName + "', '" + password + "', '" + (premium ? 1 : 0) + "');");
    }

    public void update() {
        Auth.getStore().update("UPDATE `%P%users` SET `firstIP`='" + firstIP + "',`lastIP`='" + lastIP + "',`lastName`='" + lastName + "',`password`='" + password + "' WHERE `uuid`='" + uuid + "';");
    }
}
