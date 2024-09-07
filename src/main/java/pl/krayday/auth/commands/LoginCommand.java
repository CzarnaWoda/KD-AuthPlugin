package pl.krayday.auth.commands;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import pl.krayday.auth.Auth;
import pl.krayday.auth.data.User;
import pl.krayday.auth.manager.UserManager;
import pl.krayday.auth.utils.Util;

public class LoginCommand extends Command {

    public LoginCommand() {
        super("login", "auth.login", "l");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User u = UserManager.getUser(player);
            if (!player.getServer().getInfo().getName().equalsIgnoreCase("auth")) {
                return;
            }
            if (u == null) {
                Util.sendMessage(sender, Auth.getConfig().getString("messages.register_rightUsage"));
                return;
            }
            if (args.length != 1) {
                Util.sendMessage(player, Auth.getConfig().getString("messages.login_rightUsage"));
                return;
            }
            if (u.getPassword().equals(args[0])) {
                Util.sendMessage(player, Auth.getConfig().getString("messages.logged_in"));
                player.connect(BungeeCord.getInstance().getServerInfo("lobby"));
            }
        }
    }
}
