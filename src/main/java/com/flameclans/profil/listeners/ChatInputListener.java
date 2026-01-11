package com.flameclans.profil.listeners;

import com.flameclans.profil.ProfilePlugin;
import com.flameclans.profil.ProfilePlugin.SocialMediaType;
import com.flameclans.profil.data.Profile;
import com.flameclans.profil.gui.ReviewGUI;
import com.flameclans.profil.gui.SettingsGUI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatInputListener implements Listener {

    private final ProfilePlugin plugin;

    public ChatInputListener(ProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (plugin.getPlayersWritingReview().containsKey(player)) {
            event.setCancelled(true);

            ReviewGUI reviewGUI = plugin.getPlayersWritingReview().get(player);
            String message = event.getMessage();

            if (message.equalsIgnoreCase("отмена") || message.equalsIgnoreCase("cancel")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.review_cancel")));
            } else {
                reviewGUI.setReviewComment(message);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.review_success")));
            }

            plugin.getPlayersWritingReview().remove(player);
            
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                reviewGUI.open();
            });
            return;
        }

        if (plugin.getPlayersAwaitingSocialLinkInput().containsKey(player.getUniqueId())) {
            event.setCancelled(true);

            SocialMediaType socialMediaType = plugin.getPlayersAwaitingSocialLinkInput().get(player.getUniqueId());
            String message = event.getMessage();

            if (message.equalsIgnoreCase("отмена") || message.equalsIgnoreCase("cancel")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.social_cancel")));
            } else {
                if (!plugin.getDatabaseManager().isConnected()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.db_error")));
                    plugin.getPlayersAwaitingSocialLinkInput().remove(player.getUniqueId());
                    return;
                }
                Profile playerProfile = plugin.getDatabaseManager().getProfile(player.getUniqueId());
                if (playerProfile == null) {
                    playerProfile = new Profile(player.getUniqueId(), player.getName());
                }

                String typeStr = "";
                switch (socialMediaType) {
                    case DISCORD:
                        playerProfile.setDiscordLink(message);
                        typeStr = "Discord";
                        break;
                    case TELEGRAM:
                        playerProfile.setTelegramLink(message);
                        typeStr = "Telegram";
                        break;
                    case VK:
                        playerProfile.setVkLink(message);
                        typeStr = "VK";
                        break;
                    case DESCRIPTION:
                        playerProfile.setDescription(message);
                        typeStr = "Описание";
                        break;
                }
                plugin.getDatabaseManager().saveProfile(playerProfile);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.social_updated").replace("%type%", typeStr)));
            }

            plugin.getPlayersAwaitingSocialLinkInput().remove(player.getUniqueId());

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                new SettingsGUI(plugin, player).open();
            });
        }
    }
}
