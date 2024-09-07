package pl.krayday.auth.listeners;

import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pl.krayday.auth.Auth;
import pl.krayday.auth.utils.Util;

public class PreLoginListener implements Listener {

    @EventHandler(priority = 96)
    public void onPreLoginEvent(PreLoginEvent e){
        if(e.getConnection().getVersion() != 47){
            e.setCancelReason(Util.fixColors(Auth.getConfig().getString("messages.preLogin_wrongVersion")));
            e.setCancelled(true);
            return;
        }
        final String name = e.getConnection().getName();
        if(!Util.isAlphanumeric(name)){
            e.setCancelReason(Util.fixColors(Auth.getConfig().getString("messages.preLogin_errorAlphanumeric")));
            e.setCancelled(true);
            return;
        }
        if(Auth.getLoginManager().getCurrentJoints() > 9){
            e.setCancelReason(Util.fixColors(Auth.getConfig().getString("messages.preLogin_tooMuchJoins")));
            e.setCancelled(true);
            return;
        }
        Auth.getLoginManager().addJoin(1);
        Auth.getLoginManager().registerEvent(name.toLowerCase(),e);
    }
}
