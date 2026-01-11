package com.flameclans.profil.gui;

import com.flameclans.profil.ProfilePlugin;
import com.flameclans.profil.data.Profile;
import com.flameclans.profil.data.Review;
import com.flameclans.profil.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import me.clip.placeholderapi.PlaceholderAPI;

import java.util.ArrayList;
import java.util.List;

public class ProfileGUI implements InventoryHolder {

    private final ProfilePlugin plugin;
    private final Player viewer;
    private final Player targetPlayer;
    private final Inventory inventory;
    private final int page;

    public ProfileGUI(ProfilePlugin plugin, Player viewer, Player targetPlayer, int page) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.targetPlayer = targetPlayer;
        this.page = page;
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("gui.profile_menu_title", "&8Профиль %player_name%").replace("%player_name%", targetPlayer.getName()));
        if (page > 1) title += " (Стр. " + page + ")";
        this.inventory = Bukkit.createInventory(this, 54, title);
        initializeItems();
    }

    public ProfileGUI(ProfilePlugin plugin, Player targetPlayer) {
        this(plugin, targetPlayer, targetPlayer, 1);
    }

    private void initializeItems() {
        inventory.clear();

        // Fill background
        String fillerMatName = plugin.getConfig().getString("gui.profile.filler.material", "GRAY_STAINED_GLASS_PANE");
        Material fillerMat = Material.matchMaterial(fillerMatName);
        if (fillerMat == null) fillerMat = Material.GRAY_STAINED_GLASS_PANE;
        
        ItemStack filler = new ItemStack(fillerMat);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("gui.profile.filler.name", " ")));
            filler.setItemMeta(fillerMeta);
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        // Clear slots for reviews (3x6 area in the middle)
        for (int row = 1; row <= 4; row++) {
            for (int col = 3; col <= 7; col++) {
                inventory.setItem(row * 9 + col, null);
            }
        }

        Profile profile = plugin.getDatabaseManager().getProfile(targetPlayer.getUniqueId());
        List<Review> reviews = plugin.getDatabaseManager().getReviews(targetPlayer.getUniqueId());

        // Slot 10: Player Head
        int headSlot = plugin.getConfig().getInt("gui.profile.head.slot", 10);
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(targetPlayer);
            skullMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfig().getString("gui.profile.head.name", "&eПрофиль &6%player_name%")
                .replace("%player_name%", targetPlayer.getName())));
            
            List<String> lore = new ArrayList<>();
            for (String line : plugin.getConfig().getStringList("gui.profile.head.lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line
                    .replace("%description%", (profile != null && !profile.getDescription().isEmpty() ? profile.getDescription() : "Пусто"))
                    .replace("%vk%", (profile != null && !profile.getVkLink().isEmpty() ? profile.getVkLink() : "Не указан"))
                    .replace("%discord%", (profile != null && !profile.getDiscordLink().isEmpty() ? profile.getDiscordLink() : "Не указан"))
                    .replace("%telegram%", (profile != null && !profile.getTelegramLink().isEmpty() ? profile.getTelegramLink() : "Не указан"))
                    .replace("%total_reviews%", String.valueOf(profile != null ? profile.getTotalReviews() : 0))
                    .replace("%average_rating%", String.format("%.1f", profile != null ? profile.getAverageRating() : 0.0))
                    .replace("%rating_stars%", getStars(profile))
                ));
            }
            skullMeta.setLore(lore);
            playerHead.setItemMeta(skullMeta);
        }
        inventory.setItem(headSlot, playerHead);

        // Settings Button
        if (viewer.getUniqueId().equals(targetPlayer.getUniqueId())) {
            createConfigItem("gui.profile.settings", inventory);
        }

        // Leave Review Button
        if (!viewer.getUniqueId().equals(targetPlayer.getUniqueId())) {
            createConfigItem("gui.profile.leave_review", inventory);
        }

        // Stats Button
        int statsSlot = plugin.getConfig().getInt("gui.profile.stats.slot", 37);
        String statsMatName = plugin.getConfig().getString("gui.profile.stats.material", "BOOK");
        Material statsMat = Material.matchMaterial(statsMatName);
        if (statsMat == null) statsMat = Material.BOOK;

        ItemStack statsItem = new ItemStack(statsMat);
        ItemMeta statsMeta = statsItem.getItemMeta();
        if (statsMeta != null) {
            String name = plugin.getConfig().getString("gui.profile.stats.name", "&eСтатистика игрока &6%player_name%")
                .replace("%player_name%", targetPlayer.getName());
            statsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            
            List<String> lore = new ArrayList<>();
            String status = targetPlayer.isOnline() ? 
                plugin.getConfig().getString("messages.status_online", "&aОнлайн") : 
                plugin.getConfig().getString("messages.status_offline", "&7Оффлайн");
            
            // Default playtime placeholder if PAPI is missing
            String playtime = "N/A";
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                // Try to get playtime from common PAPI expansions like %statistic_time_played% or %vault_playtime%
                // We'll use a generic one and let the user change it in config if needed
                playtime = PlaceholderAPI.setPlaceholders(targetPlayer, "%statistic_time_played%");
            }

            for (String line : plugin.getConfig().getStringList("gui.profile.stats.lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line
                    .replace("%playtime%", playtime)
                    .replace("%status%", status)
                    .replace("%player_name%", targetPlayer.getName())
                ));
            }
            statsMeta.setLore(lore);
            statsItem.setItemMeta(statsMeta);
        }
        inventory.setItem(statsSlot, statsItem);

        // Exit Button
        createConfigItem("gui.profile.exit", inventory);

        // Navigation
        if (page > 1) {
            createConfigItem("gui.profile.prev_page", inventory);
        }
        if (reviews.size() > page * 20) {
            createConfigItem("gui.profile.next_page", inventory);
        }

        // Reviews logic
        if (reviews.isEmpty()) {
            createConfigItem("gui.profile.no_reviews", inventory);
        } else {
            int start = (page - 1) * 20;
            int end = Math.min(start + 20, reviews.size());
            int[] reviewSlots = {
                12, 13, 14, 15, 16,
                21, 22, 23, 24, 25,
                30, 31, 32, 33, 34,
                39, 40, 41, 42, 43
            };
            
            for (int i = start; i < end; i++) {
                int slotIndex = i - start;
                if (slotIndex >= reviewSlots.length) break;

                Review review = reviews.get(i);
                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.YELLOW + "Отзыв от " + ChatColor.WHITE + review.getReviewerName());
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Оценка: " + ChatColor.GOLD + review.getRating() + "/5");
                    lore.add(ChatColor.GRAY + "Комментарий:");
                    lore.add(ChatColor.WHITE + review.getComment());
                    lore.add("");
                    lore.add(ChatColor.DARK_GRAY + "Дата: " + review.getTimestamp().toString());
                    lore.add(ChatColor.DARK_GRAY + "Сервер: " + review.getServerName());
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                inventory.setItem(reviewSlots[slotIndex], item);
            }
        }
    }

    private void createConfigItem(String path, Inventory inv) {
        String matName = plugin.getConfig().getString(path + ".material");
        if (matName == null) return;
        Material mat = Material.matchMaterial(matName);
        if (mat == null) mat = Material.BARRIER;
        
        int slot = plugin.getConfig().getInt(path + ".slot");
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(path + ".name", "")));
            List<String> lore = new ArrayList<>();
            for (String line : plugin.getConfig().getStringList(path + ".lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        inv.setItem(slot, item);
    }

    private String getStars(Profile profile) {
        if (profile == null) return "☆☆☆☆☆";
        int fullStars = (int) Math.round(profile.getAverageRating());
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < fullStars; i++) stars.append("⭐");
        for (int i = fullStars; i < 5; i++) stars.append("☆");
        return stars.toString();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void open() {
        viewer.openInventory(inventory);
    }

    public Player getTargetPlayer() {
        return targetPlayer;
    }

    public int getPage() {
        return page;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
