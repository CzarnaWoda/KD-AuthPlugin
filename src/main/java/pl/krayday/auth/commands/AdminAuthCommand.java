package pl.krayday.auth.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import pl.krayday.auth.data.User;
import pl.krayday.auth.manager.UserManager;
import pl.krayday.auth.utils.Util;

public class AdminAuthCommand extends Command  {

    public AdminAuthCommand() {
        super("adminAuth", "auth.admin");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args[0].equalsIgnoreCase("unregister")){
            User u = UserManager.getUser(args[1]);
            if(u == null){
                Util.sendMessage(sender, "Nie odnaleziono podanego gracza w bazie danych!");
            }

        }
    }
}
