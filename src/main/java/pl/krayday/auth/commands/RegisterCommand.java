package pl.krayday.auth.commands;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import pl.krayday.auth.Auth;
import pl.krayday.auth.data.User;
import pl.krayday.auth.manager.UserManager;
import pl.krayday.auth.utils.Util;

public class RegisterCommand extends Command {

    public RegisterCommand() {
        super("register", "auth.register", "reg", "");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            if (!((ProxiedPlayer) sender).getServer().getInfo().getName().equalsIgnoreCase("auth")) {
                return;
            }
            if (args.length < 2) {
                Util.sendMessage(sender, Auth.getConfig().getString("messages.register_rightUsage"));
                return;
            }
            if (args[0].length() < 6) {
                Util.sendMessage(sender, Auth.getConfig().getString("messages.register_passwordTooWeak"));
            }
            if (!args[0].equals(args[1])) {
                Util.sendMessage(sender, Auth.getConfig().getString("messages.register_passwordNotMach"));
            }
            final ProxiedPlayer player = (ProxiedPlayer) sender;
            if (UserManager.getUser(player) == null) {
                User u = UserManager.createUser(player.getDisplayName(), player.getUniqueId(), player.getAddress().getAddress().getHostAddress(), false);
                Util.sendMessage(sender, Auth.getConfig().getString("messages.register_successfully"));
                u.setPassword(args[1]);
                player.connect(BungeeCord.getInstance().getServerInfo("main"));
            } else {
                Util.sendMessage(sender, Auth.getConfig().getString("messages.register_already"));
            }
        }
    }
}
