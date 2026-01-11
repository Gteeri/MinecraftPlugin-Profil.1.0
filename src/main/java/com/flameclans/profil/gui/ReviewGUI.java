package com.flameclans.profil.gui;

import com.flameclans.profil.ProfilePlugin;
import com.flameclans.profil.utils.ItemBuilder;
import com.flameclans.profil.data.Review;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;

import java.util.List;
import java.util.ArrayList;

public class ReviewGUI implements InventoryHolder {

    private final ProfilePlugin plugin;
    private final Player targetPlayer;
    private final Player reviewer;
    private final Inventory inventory;
    private int selectedRating = 0;
    private String reviewComment = "";
    private List<Review> reviews;

    public ReviewGUI(ProfilePlugin plugin, Player reviewer, Player targetPlayer, List<Review> reviews) {
        this.plugin = plugin;
        this.reviewer = reviewer;
        this.targetPlayer = targetPlayer;
        this.reviews = reviews;
        String title = ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfig().getString("gui.review.title", "&8Отзыв для &7%player_name%")
            .replace("%player_name%", targetPlayer.getName()));
        this.inventory = Bukkit.createInventory(this, 27, title);
        initializeItems();
    }

    private void initializeItems() {
        inventory.clear();

        // Background Filler
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        // Text Input
        int textSlot = plugin.getConfig().getInt("gui.review.text_input.slot", 0);
        ItemStack textInput = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta textMeta = textInput.getItemMeta();
        if (textMeta != null) {
            textMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("gui.review.text_input.name", "&bНажмите, чтобы ввести текст")));
            List<String> lore = new ArrayList<>();
            for (String line : plugin.getConfig().getStringList("gui.review.text_input.lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line.replace("%comment%", (reviewComment.isEmpty() ? "Пусто" : reviewComment))));
            }
            textMeta.setLore(lore);
            textInput.setItemMeta(textMeta);
        }
        inventory.setItem(textSlot, textInput);

        // Stars (1-5)
        int starStartSlot = plugin.getConfig().getInt("gui.review.stars.slot_start", 2);
        Material[] ratingMaterials = {Material.RED_DYE, Material.ORANGE_DYE, Material.YELLOW_DYE, Material.LIME_DYE, Material.GREEN_DYE};
        String starNamePattern = plugin.getConfig().getString("gui.review.stars.name", "&e%stars% Звезда");
        String selectedSuffix = plugin.getConfig().getString("gui.review.stars.selected_suffix", " &a(Выбрано)");

        for (int i = 0; i < 5; i++) {
            ItemStack star = new ItemStack(ratingMaterials[i]);
            ItemMeta meta = star.getItemMeta();
            if (meta != null) {
                String starName = ChatColor.translateAlternateColorCodes('&', starNamePattern.replace("%stars%", String.valueOf(i + 1)));
                if (selectedRating == (i + 1)) {
                    starName += ChatColor.translateAlternateColorCodes('&', selectedSuffix);
                }
                meta.setDisplayName(starName);
                star.setItemMeta(meta);
            }
            inventory.setItem(starStartSlot + i, star);
        }

        // Confirm Button
        createConfigItem("gui.review.confirm", inventory);

        // Exit Button
        createConfigItem("gui.review.exit", inventory);

        // Display existing reviews
        int reviewSlot = 9; // Starting slot for reviews
        for (Review review : reviews) {
            if (reviewSlot > 23) break; // Max 15 reviews (slots 9-23)

            ItemStack reviewItem = new ItemStack(Material.PAPER);
            ItemMeta reviewMeta = reviewItem.getItemMeta();
            if (reviewMeta != null) {
                reviewMeta.setDisplayName(ChatColor.YELLOW + "Отзыв от: " + ChatColor.WHITE + review.getReviewerName());
                List<String> lore = new java.util.ArrayList<>();
                lore.add(ChatColor.GRAY + "Рейтинг: " + ChatColor.GOLD + review.getRating() + " Звезд");
                lore.add(ChatColor.GRAY + "Комментарий: " + ChatColor.WHITE + review.getComment());
                reviewMeta.setLore(lore);
                reviewItem.setItemMeta(reviewMeta);
            }
            inventory.setItem(reviewSlot, reviewItem);
            reviewSlot++;
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
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

    public void setSelectedRating(int rating) {
        this.selectedRating = rating;
        initializeItems(); // Refresh GUI
    }

    public void setReviewComment(String comment) {
        this.reviewComment = comment;
        initializeItems(); // Refresh GUI
    }

    public void open() {
        reviewer.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Player getTargetPlayer() {
        return targetPlayer;
    }

    public int getSelectedRating() {
        return selectedRating;
    }

    public String getReviewComment() {
        return reviewComment;
    }
}
