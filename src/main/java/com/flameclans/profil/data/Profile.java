package com.flameclans.profil.data;

import java.util.UUID;

public class Profile {

    private final UUID uuid;
    private final String playerName;
    private String description;
    private String vkLink;
    private String discordLink;
    private String telegramLink;
    private double averageRating;
    private int totalReviews;
    private boolean isPublic; // New field for profile visibility

    public Profile(UUID uuid, String playerName, String description, String vkLink, String discordLink, String telegramLink, double averageRating, int totalReviews, boolean isPublic) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.description = description;
        this.vkLink = vkLink;
        this.discordLink = discordLink;
        this.telegramLink = telegramLink;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.isPublic = isPublic;
    }

    // New constructor for creating a basic profile
    public Profile(UUID uuid, String playerName) {
        this(uuid, playerName, null, null, null, null, 0.0, 0, true);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVkLink() {
        return vkLink;
    }

    public void setVkLink(String vkLink) {
        this.vkLink = vkLink;
    }

    public String getDiscordLink() {
        return discordLink;
    }

    public void setDiscordLink(String discordLink) {
        this.discordLink = discordLink;
    }

    public String getTelegramLink() {
        return telegramLink;
    }

    public void setTelegramLink(String telegramLink) {
        this.telegramLink = telegramLink;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }
}
