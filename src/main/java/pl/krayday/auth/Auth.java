package pl.krayday.auth;

import lombok.Getter;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import pl.krayday.auth.commands.AdminAuthCommand;
import pl.krayday.auth.commands.LoginCommand;
import pl.krayday.auth.commands.RegisterCommand;
import pl.krayday.auth.listeners.PreLoginListener;
import pl.krayday.auth.listeners.ProxyPingListener;
import pl.krayday.auth.listeners.ServerConnectedListener;
import pl.krayday.auth.manager.LoginManager;
import pl.krayday.auth.manager.UserManager;
import pl.krayday.auth.store.Store;
import pl.krayday.auth.store.modes.StoreMySQL;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public class Auth extends Plugin {
    @Getter
    private static Auth auth;
    @Getter
    private static Store store;
    @Getter
    private static LoginManager loginManager;
    @Getter
    private static File fileConfig;
    @Getter
    private static Configuration config;
    @Getter
    private static PluginManager pluginManager;

    @Override
    public void onLoad() {
        sendAuthMessage("Auth plugin loading...");
        auth=this;
    }

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        sendAuthMessage("Enable configs...");
        saveDefaultConfig();
        sendAuthMessage("Configs enabled...");

        sendAuthMessage("Enable managers...");
        store = new StoreMySQL(getConfig().getString("mysql.host"), getConfig().getInt("mysql.port"), getConfig().getString("mysql.user"), getConfig().getString("mysql.password"), getConfig().getString("mysql.name"), getConfig().getString("mysql.prefix"));
        if(store.connect()){
            store.update("CREATE TABLE IF NOT EXISTS `%P%users`(`ID` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT, `uuid` varchar(36) NOT NULL, `firstIP` varchar(100) NOT NULL, `lastIP` varchar(100) NOT NULL, `lastName` varchar(16) NOT NULL, password varchar(48) NOT NULL, `premium` tinyint(1) NOT NULL);");
        }
        loginManager = new LoginManager();
        sendAuthMessage("Managers enabled");

        sendAuthMessage("Enable loading data from MYSQL...");
        UserManager.setup();
        sendAuthMessage("Data loaded from MYSQL");

        sendAuthMessage("Register listeners...");
        pluginManager = getProxy().getPluginManager();
        registerListeners(new PreLoginListener(), new ProxyPingListener(), new ServerConnectedListener());
        sendAuthMessage("Listeners registered...");

        sendAuthMessage("Register commands...");
        this.getProxy().getPluginManager().registerCommand(this, new RegisterCommand());
        this.getProxy().getPluginManager().registerCommand(this, new LoginCommand());
        this.getProxy().getPluginManager().registerCommand(this, new AdminAuthCommand());
        sendAuthMessage("Commands registered...");

        sendAuthMessage("Register tasks...");
        this.getProxy().getScheduler().schedule(this, () -> getLoginManager().setCurrentJoints(0), 2L, 2L, TimeUnit.SECONDS);
        sendAuthMessage("Tasks registered");

        sendAuthMessage("Check information about servers...");
        if(getProxy().getServerInfo("auth") == null){
            sendAuthMessage("Auth server doesn't exists...");
        }else{
            sendAuthMessage("Auth server is enable");
        }
        if(getProxy().getServerInfo("main") == null){
            sendAuthMessage("Main server doesn't exists...");
        }else{
            sendAuthMessage("Main server is enable");
        }
        sendAuthMessage("Information about servers checked");

        sendAuthMessage("Plugin loaded in " + (System.currentTimeMillis()-start) + "ms");
    }
    public void registerListeners(Listener... listeners){
        for(Listener listener : listeners){
            pluginManager.registerListener(getAuth(), listener);
        }
        sendAuthMessage("Registered " + listeners.length + " listeners!");
    }
    public void saveDefaultConfig(){
        fileConfig = new File(getDataFolder(), "config.yml");
        if(!getDataFolder().exists()){
            getDataFolder().mkdirs();
        }
        if(!fileConfig.exists()){
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, fileConfig.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(fileConfig, config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig(){
        if(fileConfig.exists()){
            try {
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, fileConfig);
                config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(fileConfig, config);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, fileConfig.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveConfig(){
        if(fileConfig.exists()){
            try {
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, fileConfig);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public static void sendAuthMessage(String message){
        System.out.println("KRAYDAY:AUTHPLUGIN [" + message + "]");
    }
}
