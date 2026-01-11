package com.flameclans.profil.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

public class ItemBuilder {

    public static ItemStack createPlayerHead(String base64Texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (base64Texture == null || base64Texture.isEmpty()) {
            return head;
        }

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) {
            return head;
        }

        try {
            // Создаем профиль с рандомным UUID
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            
            // Декодируем Base64, чтобы получить URL текстуры
            String decoded = new String(Base64.getDecoder().decode(base64Texture));
            // Извлекаем URL из JSON (обычно {"textures":{"SKIN":{"url":"http://..."}}})
            String urlString = "";
            if (decoded.contains("url\":\"")) {
                urlString = decoded.split("url\":\"")[1].split("\"")[0];
            }

            if (!urlString.isEmpty()) {
                textures.setSkin(new URL(urlString));
                profile.setTextures(textures);
                meta.setOwnerProfile(profile);
            }
        } catch (Exception e) {
            // Если не удалось через URL, пробуем оставить как есть или логируем
            // В Paper 1.21.1+ это самый надежный способ
        }

        head.setItemMeta(meta);
        return head;
    }
}
