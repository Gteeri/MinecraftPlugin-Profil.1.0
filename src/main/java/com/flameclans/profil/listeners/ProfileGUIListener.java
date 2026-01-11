package com.flameclans.profil.listeners;

import com.flameclans.profil.ProfilePlugin;
import com.flameclans.profil.gui.ProfileGUI;
import com.flameclans.profil.gui.ReviewGUI;
import com.flameclans.profil.gui.SettingsGUI;
import com.flameclans.profil.data.Review;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ProfileGUIListener implements Listener {

    private final ProfilePlugin plugin;

    public ProfileGUIListener(ProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof ProfileGUI) {
            event.setCancelled(true); // Prevent players from taking items

            ProfileGUI profileGUI = (ProfileGUI) holder;
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }

            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) {
                return;
            }
            String displayName = meta.getDisplayName();

            // Handle Leave Review
            int leaveReviewSlot = plugin.getConfig().getInt("gui.profile.leave_review.slot", 28);
            if (event.getRawSlot() == leaveReviewSlot) {
                Player targetPlayer = profileGUI.getTargetPlayer();
                if (player.getUniqueId().equals(targetPlayer.getUniqueId())) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.self_review_error")));
                    return;
                }
                List<Review> reviews = plugin.getDatabaseManager().getReviews(targetPlayer.getUniqueId());
                ReviewGUI reviewGUI = new ReviewGUI(plugin, player, targetPlayer, reviews);
                reviewGUI.open();
            }

            // Handle Settings
            int settingsSlot = plugin.getConfig().getInt("gui.profile.settings.slot", 19);
            if (event.getRawSlot() == settingsSlot) {
                SettingsGUI settingsGUI = new SettingsGUI(plugin, player);
                settingsGUI.open();
            }

            // Handle Pagination: Next Page
            int nextSlot = plugin.getConfig().getInt("gui.profile.next_page.slot", 52);
            if (event.getRawSlot() == nextSlot) {
                new ProfileGUI(plugin, player, profileGUI.getTargetPlayer(), profileGUI.getPage() + 1).open(player);
            }

            // Handle Pagination: Previous Page
            int prevSlot = plugin.getConfig().getInt("gui.profile.prev_page.slot", 45);
            if (event.getRawSlot() == prevSlot) {
                new ProfileGUI(plugin, player, profileGUI.getTargetPlayer(), profileGUI.getPage() - 1).open(player);
            }

            // Handle Exit
            int exitSlot = plugin.getConfig().getInt("gui.profile.exit.slot", 53);
            if (event.getRawSlot() == exitSlot) {
                player.closeInventory();
            }
        }
    }
}
