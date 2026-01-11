package com.flameclans.profil.listeners;

import com.flameclans.profil.ProfilePlugin;
import com.flameclans.profil.gui.ReviewGUI;
import com.flameclans.profil.gui.ProfileGUI;
import com.flameclans.profil.data.Review;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ReviewGUIListener implements Listener {

    private final ProfilePlugin plugin;

    public ReviewGUIListener(ProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof ReviewGUI) {
            event.setCancelled(true); // Prevent players from taking items

            ReviewGUI reviewGUI = (ReviewGUI) holder;
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

            // Handle star rating clicks
            int starStartSlot = plugin.getConfig().getInt("gui.review.stars.slot_start", 2);
            if (event.getRawSlot() >= starStartSlot && event.getRawSlot() < starStartSlot + 5) {
                int rating = event.getRawSlot() - starStartSlot + 1;
                reviewGUI.setSelectedRating(rating);
            }

            // Handle text input click
            int textSlot = plugin.getConfig().getInt("gui.review.text_input.slot", 0);
            if (event.getRawSlot() == textSlot) {
                player.closeInventory();
                plugin.getPlayersWritingReview().put(player, reviewGUI);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.enter_review")));
            }

            // Handle confirm button click
            int confirmSlot = plugin.getConfig().getInt("gui.review.confirm.slot", 24);
            if (event.getRawSlot() == confirmSlot) {
                if (reviewGUI.getSelectedRating() == 0) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        plugin.getConfig().getString("messages.prefix") + "&cПожалуйста, выберите оценку!"));
                    return;
                }
                if (reviewGUI.getReviewComment().isEmpty()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        plugin.getConfig().getString("messages.prefix") + "&cПожалуйста, введите текст отзыва!"));
                    return;
                }

                // Prevent self-review
                if (player.getUniqueId().equals(reviewGUI.getTargetPlayer().getUniqueId())) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.self_review_error")));
                    return;
                }

                // Submit review
                if (!plugin.getDatabaseManager().isConnected()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.db_error")));
                    return;
                }
                Review newReview = new Review(
                        0,
                        reviewGUI.getTargetPlayer().getUniqueId(),
                        player.getUniqueId(),
                        player.getName(),
                        reviewGUI.getSelectedRating(),
                        reviewGUI.getReviewComment(),
                        new java.sql.Timestamp(System.currentTimeMillis()),
                        plugin.getConfig().getString("server_name", "unknown")
                );
                plugin.getDatabaseManager().addReview(newReview);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.review_success")));
                player.closeInventory();
                new ProfileGUI(plugin, reviewGUI.getTargetPlayer()).open(player);
            }

            // Handle Exit
            int exitSlot = plugin.getConfig().getInt("gui.review.exit.slot", 26);
            if (event.getRawSlot() == exitSlot) {
                player.closeInventory();
                new ProfileGUI(plugin, reviewGUI.getTargetPlayer()).open(player);
            }
        }
    }
}
