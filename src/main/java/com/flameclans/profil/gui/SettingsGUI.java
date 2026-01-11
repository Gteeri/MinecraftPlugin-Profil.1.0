package com.flameclans.profil.gui;

import com.flameclans.profil.ProfilePlugin;
import com.flameclans.profil.data.Profile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.flameclans.profil.utils.ItemBuilder;
import org.bukkit.Material;

import java.util.List;

public class SettingsGUI implements InventoryHolder {

    private final ProfilePlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private Profile playerProfile;

    public SettingsGUI(ProfilePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.playerProfile = plugin.getDatabaseManager().getProfile(player.getUniqueId());
        if (this.playerProfile == null) {
            this.playerProfile = new Profile(player.getUniqueId(), player.getName(), "", "", "", "", 0.0, 0, true);
            plugin.getDatabaseManager().saveProfile(this.playerProfile);
        }
        String title = ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfig().getString("gui.settings.title", "&8Настройки профиля"));
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

        // VK
        createSocialItem("gui.settings.vk", playerProfile.getVkLink(), inventory);
        // Discord
        createSocialItem("gui.settings.discord", playerProfile.getDiscordLink(), inventory);
        // Telegram
        createSocialItem("gui.settings.telegram", playerProfile.getTelegramLink(), inventory);
        // Description
        createSocialItem("gui.settings.description", playerProfile.getDescription(), inventory);

        // Visibility Toggle
        int visSlot = plugin.getConfig().getInt("gui.settings.visibility.slot", 22);
        boolean isPublic = playerProfile.isPublic();
        String path = isPublic ? "gui.settings.visibility.public" : "gui.settings.visibility.private";
        
        String visMatName = plugin.getConfig().getString(path + ".material", isPublic ? "LIME_DYE" : "RED_DYE");
        Material visMat = Material.matchMaterial(visMatName);
        if (visMat == null) visMat = isPublic ? Material.LIME_DYE : Material.RED_DYE;

        ItemStack visibilityToggle = new ItemStack(visMat);
        ItemMeta visibilityMeta = visibilityToggle.getItemMeta();
        if (visibilityMeta != null) {
            visibilityMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfig().getString(path + ".name", isPublic ? "&aПубличный профиль" : "&cСкрытый профиль")));
            
            List<String> lore = new java.util.ArrayList<>();
            for (String line : plugin.getConfig().getStringList("gui.settings.visibility.lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            visibilityMeta.setLore(lore);
            visibilityToggle.setItemMeta(visibilityMeta);
        }
        inventory.setItem(visSlot, visibilityToggle);

        // Exit Button
        createConfigItem("gui.settings.exit", inventory);
    }

    private void createSocialItem(String path, String currentVal, Inventory inv) {
        String matName = plugin.getConfig().getString(path + ".material");
        if (matName == null) return;
        Material mat = Material.matchMaterial(matName);
        if (mat == null) mat = Material.BARRIER;
        
        int slot = plugin.getConfig().getInt(path + ".slot");
        String texture = plugin.getConfig().getString(path + ".texture");
        
        ItemStack item;
        if (mat == Material.PLAYER_HEAD && texture != null && !texture.isEmpty()) {
            item = ItemBuilder.createPlayerHead(texture);
        } else {
            item = new ItemStack(mat);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(path + ".name", "")));
            List<String> lore = new java.util.ArrayList<>();
            lore.add(ChatColor.GRAY + "Текущее значение: " + ChatColor.WHITE + (currentVal == null || currentVal.isEmpty() ? "Не указано" : currentVal));
            lore.add("");
            lore.add(ChatColor.YELLOW + "Нажмите, чтобы изменить");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        inv.setItem(slot, item);
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
            List<String> lore = new java.util.ArrayList<>();
            for (String line : plugin.getConfig().getStringList(path + ".lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        inv.setItem(slot, item);
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Profile getPlayerProfile() {
        return playerProfile;
    }
}
