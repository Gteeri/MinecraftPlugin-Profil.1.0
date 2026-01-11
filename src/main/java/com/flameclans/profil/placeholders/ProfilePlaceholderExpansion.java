package com.flameclans.profil.placeholders;

import com.flameclans.profil.ProfilePlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProfilePlaceholderExpansion extends PlaceholderExpansion {

    private final ProfilePlugin plugin;

    public ProfilePlaceholderExpansion(ProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "profil";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // This is required or the expansion will end up deregistering itself.
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        com.flameclans.profil.data.Profile profile = plugin.getDatabaseManager().getProfile(player.getUniqueId());

        if (identifier.equalsIgnoreCase("average_rating")) {
            return profile != null ? String.format("%.1f", profile.getAverageRating()) : "0.0";
        }

        if (identifier.equalsIgnoreCase("total_reviews")) {
            return profile != null ? String.valueOf(profile.getTotalReviews()) : "0";
        }

        if (identifier.equalsIgnoreCase("rating_stars")) {
            if (profile == null) return "☆☆☆☆☆";
            int fullStars = (int) Math.round(profile.getAverageRating());
            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < fullStars; i++) stars.append("⭐");
            for (int i = fullStars; i < 5; i++) stars.append("☆");
            return stars.toString();
        }

        return null; // Placeholder is invalid
    }
}
