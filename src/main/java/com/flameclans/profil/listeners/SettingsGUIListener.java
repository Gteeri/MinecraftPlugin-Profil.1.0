package com.flameclans.profil.listeners;

import com.flameclans.profil.ProfilePlugin;
import com.flameclans.profil.data.Profile;
import com.flameclans.profil.gui.SettingsGUI;
import com.flameclans.profil.gui.ProfileGUI;
import com.flameclans.profil.ProfilePlugin.SocialMediaType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SettingsGUIListener implements Listener {

    private final ProfilePlugin plugin;

    public SettingsGUIListener(ProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof SettingsGUI) {
            event.setCancelled(true); // Prevent players from taking items

            SettingsGUI settingsGUI = (SettingsGUI) holder;
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }

            ItemMeta itemMeta = clickedItem.getItemMeta();
            if (itemMeta == null || !itemMeta.hasDisplayName()) {
                return;
            }
            String displayName = itemMeta.getDisplayName();

            // Handle Exit button click (using display name now as it's a custom head)
            if (displayName.equals(ChatColor.RED + "Закрыть")) {
                player.closeInventory();
                return;
            }

            // Handle Profile Visibility Toggle
            if (displayName.equals(ChatColor.YELLOW + "Видимость профиля")) {
                if (!plugin.getDatabaseManager().isConnected()) {
                    player.sendMessage(ChatColor.RED + "База данных недоступна. Настройка не сохранена.");
                    return;
                }
                Profile profile = settingsGUI.getPlayerProfile();
                boolean newVisibility = !profile.isPublic();
                profile.setPublic(newVisibility);
                plugin.getDatabaseManager().updateProfileVisibility(player.getUniqueId(), newVisibility);
                
                String statusMsg = newVisibility ? 
                    plugin.getConfig().getString("messages.visibility_on") : 
                    plugin.getConfig().getString("messages.visibility_off");
                
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.prefix") + statusMsg));
                settingsGUI.open(); // Refresh the GUI
                return;
            }

            // Handle Social Link Inputs
            int vkSlot = plugin.getConfig().getInt("gui.settings.vk.slot", 10);
            int discordSlot = plugin.getConfig().getInt("gui.settings.discord.slot", 12);
            int telegramSlot = plugin.getConfig().getInt("gui.settings.telegram.slot", 14);
            int descSlot = plugin.getConfig().getInt("gui.settings.description.slot", 16);

            if (event.getRawSlot() == vkSlot) {
                plugin.getPlayersAwaitingSocialLinkInput().put(player.getUniqueId(), SocialMediaType.VK);
                player.closeInventory();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.enter_social").replace("%type%", "VK")));
            } else if (event.getRawSlot() == discordSlot) {
                plugin.getPlayersAwaitingSocialLinkInput().put(player.getUniqueId(), SocialMediaType.DISCORD);
                player.closeInventory();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.enter_social").replace("%type%", "Discord")));
            } else if (event.getRawSlot() == telegramSlot) {
                plugin.getPlayersAwaitingSocialLinkInput().put(player.getUniqueId(), SocialMediaType.TELEGRAM);
                player.closeInventory();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.enter_social").replace("%type%", "Telegram")));
            } else if (event.getRawSlot() == descSlot) {
                plugin.getPlayersAwaitingSocialLinkInput().put(player.getUniqueId(), SocialMediaType.DESCRIPTION);
                player.closeInventory();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.enter_social").replace("%type%", "Описание")));
            }

            // Handle Visibility Toggle
            int visibilitySlot = plugin.getConfig().getInt("gui.settings.visibility.slot", 22);
            if (event.getRawSlot() == visibilitySlot) {
                Profile profile = plugin.getDatabaseManager().getProfile(player.getUniqueId());
                if (profile == null) {
                    profile = new Profile(player.getUniqueId(), player.getName());
                }
                boolean newVisibility = !profile.isPublic();
                profile.setPublic(newVisibility);
                plugin.getDatabaseManager().updateProfileVisibility(player.getUniqueId(), newVisibility);
                
                String statusMsg = newVisibility ? 
                    plugin.getConfig().getString("messages.visibility_on") : 
                    plugin.getConfig().getString("messages.visibility_off");
                
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.prefix") + statusMsg));
                settingsGUI.open(); // Refresh
            }

            // Handle Exit
            int exitSlot = plugin.getConfig().getInt("gui.settings.exit.slot", 26);
            if (event.getRawSlot() == exitSlot) {
                new ProfileGUI(plugin, player).open();
            }
        }
    }
}
