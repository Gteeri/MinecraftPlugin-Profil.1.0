package com.flameclans.profil;

import com.flameclans.profil.commands.ProfileCommand;
import com.flameclans.profil.database.DatabaseManager;
import com.flameclans.profil.listeners.ChatInputListener;
import com.flameclans.profil.listeners.ProfileGUIListener;
import com.flameclans.profil.listeners.ReviewGUIListener;
import com.flameclans.profil.listeners.SettingsGUIListener;
import com.flameclans.profil.gui.ReviewGUI;
import com.flameclans.profil.placeholders.ProfilePlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public final class ProfilePlugin extends JavaPlugin {

    private DatabaseManager databaseManager;
    private Map<Player, ReviewGUI> playersWritingReview;
    private Map<UUID, SocialMediaType> playersAwaitingSocialLinkInput;
    private FileConfiguration config;

    public enum SocialMediaType {
        DISCORD,
        TELEGRAM,
        VK,
        DESCRIPTION
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        reloadConfig();
        
        getLogger().info("Profil has been enabled!");

        databaseManager = new DatabaseManager(this);
        databaseManager.connect();
        playersWritingReview = new HashMap<>();
        playersAwaitingSocialLinkInput = new HashMap<>();

        // Register dynamic command
        registerProfileCommand();

        // Register event listeners
        getServer().getPluginManager().registerEvents(new ReviewGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatInputListener(this), this);
        getServer().getPluginManager().registerEvents(new ProfileGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new SettingsGUIListener(this), this);

        // Register PlaceholderAPI expansion
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ProfilePlaceholderExpansion(this).register();
            getLogger().info("PlaceholderAPI expansion registered!");
        } else {
            getLogger().warning("PlaceholderAPI not found! Some placeholders will not work.");
        }
    }

    private void registerProfileCommand() {
        String cmdName = getConfig().getString("command.name", "profile");
        List<String> aliases = getConfig().getStringList("command.aliases");
        
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, org.bukkit.plugin.Plugin.class);
            constructor.setAccessible(true);
            PluginCommand command = constructor.newInstance(cmdName, this);
            
            command.setExecutor(new ProfileCommand(this));
            if (aliases != null && !aliases.isEmpty()) {
                command.setAliases(aliases);
            }
            command.setPermission("profil.command.profile");
            command.setPermissionMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.no_permission")));

            commandMap.register(getName(), command);
            getLogger().info("Registered command /" + cmdName + (aliases.isEmpty() ? "" : " with aliases " + aliases));
        } catch (Exception e) {
            getLogger().severe("Could not register command dynamically: " + e.getMessage());
            // Fallback to default if something goes wrong
            getCommand("profile").setExecutor(new ProfileCommand(this));
        }
    }

    public Map<Player, ReviewGUI> getPlayersWritingReview() {
        return playersWritingReview;
    }

    public Map<UUID, SocialMediaType> getPlayersAwaitingSocialLinkInput() {
        return playersAwaitingSocialLinkInput;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Profil has been disabled!");
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    @Override
    public void reloadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        // Look for defaults in the jar
        InputStream defaultStream = getResource("config.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            config.setDefaults(defaultConfig);
        }
    }

    @Override
    public void saveDefaultConfig() {
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveResource("config.yml", false);
        }
    }
}
