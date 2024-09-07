package pl.krayday.auth.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class Util {
    public static boolean isAlphanumeric(String str)
    {
        char[] charArray = str.toCharArray();
        for(char c:charArray)
        {
            if (!Character.isLetterOrDigit(c))
                return false;
        }
        return true;
    }

    public static void sendMessage(CommandSender sender, String text){
        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', text)));
    }

    public static TextComponent fixColors(String text){
        return new TextComponent(ChatColor.translateAlternateColorCodes('&', text));
    }
    public static String fixColor(String text){
        return ChatColor.translateAlternateColorCodes('&',text);
    }
}
