package pl.krayday.auth.listeners;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pl.krayday.auth.Auth;
import pl.krayday.auth.data.User;
import pl.krayday.auth.manager.UserManager;
import pl.krayday.auth.utils.Util;

public class ServerConnectedListener implements Listener {

    @EventHandler
    public void onServerConnectedEvent(ServerConnectedEvent e){
        final Server server = e.getServer();
        final ProxiedPlayer player = e.getPlayer();
        final String serverName = server.getInfo().getName();
        final String name = player.getName().toLowerCase();
        Boolean ifPresent = Auth.getLoginManager().getPremium().getIfPresent(name);
        if (ifPresent == null) {
            player.disconnect(Util.fixColors(Auth.getConfig().getString("messages.connected_wrongPresent")));
            return;
        }
        if (ifPresent) {
            User user = UserManager.getUser(player.getUniqueId());
            if (user == null) {
                user = UserManager.createUser(player.getDisplayName(), player.getUniqueId(), player.getAddress().getAddress().getHostAddress(), true);
                user.setPassword("PREMIUM");
            }
            user.setLastName(player.getDisplayName());
            user.setLastIP(player.getAddress().getAddress().getHostAddress());
            player.connect(Auth.getAuth().getProxy().getServerInfo("lobby"));
            Util.sendMessage(player, Auth.getConfig().getString("messages.login_withPremium"));
            user.update();
        }else{
            User user = UserManager.getUser(player.getUniqueId());
            if (user == null) {
                Util.sendMessage(player, Auth.getConfig().getString("messages.register_rightUsage"));
            } else {
                user.setLastName(player.getDisplayName());
                user.setLastIP(player.getAddress().getAddress().getHostAddress());
                Util.sendMessage(player, Auth.getConfig().getString("messages.login_rightUsage"));
            }
            player.connect(Auth.getAuth().getProxy().getServerInfo("auth"));
            Util.sendMessage(player, Auth.getConfig().getString("messages.login_withNoPremium"));
        }
        /*Auth.getAuth().getProxy().getScheduler().schedule(Auth.getAuth(), () -> {
            if (player != null && !player.getServer().getInfo().getName().equalsIgnoreCase("main")) {
                player.disconnect(Util.fixColors(Auth.getConfig().getString("messages.login_tooMuchTime")));
            }
        }, 30L, TimeUnit.SECONDS);*/
        Util.sendMessage(player, "CONFIG -> zostales polaczony z  " + serverName);
    }
}
