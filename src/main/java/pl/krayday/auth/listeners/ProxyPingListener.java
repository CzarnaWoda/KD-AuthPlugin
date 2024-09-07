package pl.krayday.auth.listeners;

import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pl.krayday.auth.Auth;
import pl.krayday.auth.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class ProxyPingListener implements Listener {

    @EventHandler(priority = 1)
    public void onProxyPing(ProxyPingEvent e){
        final ServerPing serverPing = e.getResponse();
        final List<ServerPing.PlayerInfo> customPlayerInfo = new ArrayList<>();
        Auth.getConfig().getStringList("motd.playersinfo").forEach(s -> customPlayerInfo.add(new ServerPing.PlayerInfo(Util.fixColor(s),"1")));
        serverPing.getPlayers().setSample(customPlayerInfo.toArray((new ServerPing.PlayerInfo[0])));
        final String protocol = Auth.getConfig().getString("motd.protocol");
        final double multipler = Auth.getConfig().getDouble("motd.multipler");
        final String motd = Util.fixColor(Auth.getConfig().getString("motd.description.1") + "\n" + Auth.getConfig().getString("motd.description.2"));
        serverPing.setDescriptionComponent(new TextComponent(motd));
        serverPing.getPlayers().setOnline((int)(serverPing.getPlayers().getOnline() * multipler));
        serverPing.getPlayers().setMax(serverPing.getPlayers().getOnline() + 1);
        if(!protocol.equalsIgnoreCase("none")){
            serverPing.setVersion(new ServerPing.Protocol(Util.fixColor(protocol.replace("{ONLINE}",String.valueOf(serverPing.getPlayers().getOnline())).replace("{MAX}",String.valueOf(serverPing.getPlayers().getMax()))),1337));
        }

    }
}
