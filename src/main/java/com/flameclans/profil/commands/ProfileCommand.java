package com.flameclans.profil.commands;

import com.flameclans.profil.ProfilePlugin;
import com.flameclans.profil.gui.ProfileGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProfileCommand implements CommandExecutor {

    private final ProfilePlugin plugin;

    public ProfileCommand(ProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эту команду могут использовать только игроки.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("profil.command.profile")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no_permission")));
            return true;
        }

        if (!plugin.getDatabaseManager().isConnected()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.db_error")));
            return true;
        }

        Player targetPlayer = player; // Default to self

        if (args.length > 0) {
            // Attempt to find the target player by name
            Player foundPlayer = Bukkit.getPlayer(args[0]);
            if (foundPlayer == null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.player_not_found")));
                return true;
            }
            targetPlayer = foundPlayer;

            // Check profile visibility
            if (!targetPlayer.getUniqueId().equals(player.getUniqueId())) {
                com.flameclans.profil.data.Profile targetProfile = plugin.getDatabaseManager().getProfile(targetPlayer.getUniqueId());
                if (targetProfile != null && !targetProfile.isPublic() && !player.hasPermission("profil.admin.manage")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.profile_hidden")));
                    return true;
                }
            }
        }

        ProfileGUI profileGUI = new ProfileGUI(plugin, targetPlayer);
        profileGUI.open(player);

        return true;
    }
}
