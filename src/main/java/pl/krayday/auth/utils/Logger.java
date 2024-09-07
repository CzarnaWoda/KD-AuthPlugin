package pl.krayday.auth.utils;

import java.util.logging.Level;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;

import pl.krayday.auth.Auth;

public class Logger
{
	private static CommandSender console = BungeeCord.getInstance().getConsole();
    public static void info(String... logs) {
        for (String s : logs) {
            log(Level.INFO, s);
        }
    }
    
    public static void warning(String... logs) {
        for (String s : logs) {
            log(Level.WARNING, s);
        }
    }
    
    public static void severe(String... logs) {
        for (String s : logs) {
            log(Level.SEVERE, s);
        }
    }
    
    public static void log(Level level, String log) {
        Auth.getAuth().getLogger().log(level, log);
    }
    
    public static void exception(Throwable cause) {
        cause.printStackTrace();
    }
    public static void fixColorSend(String... logs){
    	for(String s : logs){
    		console.sendMessage(Util.fixColor(s));
    	}
    }
}
