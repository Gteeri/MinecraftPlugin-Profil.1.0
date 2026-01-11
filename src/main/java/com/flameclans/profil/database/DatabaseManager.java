package com.flameclans.profil.database;

import com.flameclans.profil.ProfilePlugin;
import com.flameclans.profil.data.Profile;
import com.flameclans.profil.data.Review;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {

    private final ProfilePlugin plugin;
    private Connection connection;

    public DatabaseManager(ProfilePlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        String type = plugin.getConfig().getString("database.type", "SQLITE").toUpperCase();
        try {
            if (type.equals("MYSQL")) {
                String host = plugin.getConfig().getString("database.mysql.host");
                int port = plugin.getConfig().getInt("database.mysql.port");
                String database = plugin.getConfig().getString("database.mysql.database");
                String username = plugin.getConfig().getString("database.mysql.username");
                String password = plugin.getConfig().getString("database.mysql.password");
                boolean useSSL = plugin.getConfig().getBoolean("database.mysql.useSSL");

                String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL + "&autoReconnect=true";
                connection = DriverManager.getConnection(url, username, password);
                plugin.getLogger().info("Successfully connected to MySQL database.");
            } else {
                Class.forName("org.sqlite.JDBC");
                File dataFolder = plugin.getDataFolder();
                if (!dataFolder.exists()) {
                    dataFolder.mkdirs();
                }
                String filename = plugin.getConfig().getString("database.sqlite.filename", "profiles.db");
                String url = "jdbc:sqlite:" + new File(dataFolder, filename).getAbsolutePath();
                connection = DriverManager.getConnection(url);
                plugin.getLogger().info("Successfully connected to SQLite database.");
            }
            createTables(type);
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().severe("Could not connect to database (" + type + "): " + e.getMessage());
        }
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("Disconnected from database.");
            } catch (SQLException e) {
                plugin.getLogger().severe("Error disconnecting from database: " + e.getMessage());
            }
        }
    }

    private void createTables(String type) {
        try (Statement statement = connection.createStatement()) {
            // Table for player profiles
            String createProfilesTable = "CREATE TABLE IF NOT EXISTS player_profiles (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "player_name VARCHAR(16) NOT NULL," +
                    "description TEXT," +
                    "vk_link TEXT," +
                    "discord_link TEXT," +
                    "telegram_link TEXT," +
                    "average_rating REAL DEFAULT 0.0," +
                    "total_reviews INTEGER DEFAULT 0," +
                    "is_public BOOLEAN DEFAULT TRUE" +
                    ");";
            statement.execute(createProfilesTable);

            // Table for player reviews
            String autoIncrement = type.equals("MYSQL") ? "AUTO_INCREMENT" : "AUTOINCREMENT";
            String createReviewsTable = "CREATE TABLE IF NOT EXISTS player_reviews (" +
                    "review_id INTEGER PRIMARY KEY " + autoIncrement + "," +
                    "target_uuid VARCHAR(36) NOT NULL," +
                    "reviewer_uuid VARCHAR(36) NOT NULL," +
                    "reviewer_name VARCHAR(16) NOT NULL," +
                    "rating INTEGER NOT NULL," +
                    "comment TEXT," +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "server_name VARCHAR(64) NOT NULL," +
                    "INDEX (target_uuid)" + // MySQL needs this for foreign keys or just for performance
                    ");";
            
            if (type.equals("SQLITE")) {
                // SQLite doesn't support INDEX inside CREATE TABLE like that
                createReviewsTable = "CREATE TABLE IF NOT EXISTS player_reviews (" +
                        "review_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "target_uuid VARCHAR(36) NOT NULL," +
                        "reviewer_uuid VARCHAR(36) NOT NULL," +
                        "reviewer_name VARCHAR(16) NOT NULL," +
                        "rating INTEGER NOT NULL," +
                        "comment TEXT," +
                        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP," +
                        "server_name VARCHAR(64) NOT NULL" +
                        ");";
            }
            
            statement.execute(createReviewsTable);
            plugin.getLogger().info("Database tables created or already exist.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error creating database tables: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public Connection getConnection() {
        if (!isConnected()) {
            connect();
        }
        return connection;
    }

    public Profile getProfile(UUID uuid) {
        Connection conn = getConnection();
        if (conn == null) return null;

        String sql = "SELECT * FROM player_profiles WHERE uuid = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Profile(
                            UUID.fromString(resultSet.getString("uuid")),
                            resultSet.getString("player_name"),
                            resultSet.getString("description"),
                            resultSet.getString("vk_link"),
                            resultSet.getString("discord_link"),
                            resultSet.getString("telegram_link"),
                            resultSet.getDouble("average_rating"),
                            resultSet.getInt("total_reviews"),
                            resultSet.getBoolean("is_public")
                    );
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting profile: " + e.getMessage());
        }
        return null;
    }

    public void saveProfile(Profile profile) {
        Connection conn = getConnection();
        if (conn == null) return;

        String sql = "INSERT INTO player_profiles (uuid, player_name, description, vk_link, discord_link, telegram_link, average_rating, total_reviews, is_public) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE player_name = ?, description = ?, vk_link = ?, discord_link = ?, telegram_link = ?, average_rating = ?, total_reviews = ?, is_public = ?";
        
        String type = plugin.getConfig().getString("database.type", "SQLITE").toUpperCase();
        if (type.equals("SQLITE")) {
            sql = "INSERT OR REPLACE INTO player_profiles (uuid, player_name, description, vk_link, discord_link, telegram_link, average_rating, total_reviews, is_public) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, profile.getUuid().toString());
            statement.setString(2, profile.getPlayerName());
            statement.setString(3, profile.getDescription());
            statement.setString(4, profile.getVkLink());
            statement.setString(5, profile.getDiscordLink());
            statement.setString(6, profile.getTelegramLink());
            statement.setDouble(7, profile.getAverageRating());
            statement.setInt(8, profile.getTotalReviews());
            statement.setBoolean(9, profile.isPublic());
            
            if (type.equals("MYSQL")) {
                statement.setString(10, profile.getPlayerName());
                statement.setString(11, profile.getDescription());
                statement.setString(12, profile.getVkLink());
                statement.setString(13, profile.getDiscordLink());
                statement.setString(14, profile.getTelegramLink());
                statement.setDouble(15, profile.getAverageRating());
                statement.setInt(16, profile.getTotalReviews());
                statement.setBoolean(17, profile.isPublic());
            }
            
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error saving profile: " + e.getMessage());
        }
    }

    public List<Review> getReviews(UUID targetUuid) {
        List<Review> reviews = new ArrayList<>();
        Connection conn = getConnection();
        if (conn == null) return reviews;

        String sql = "SELECT * FROM player_reviews WHERE target_uuid = ? ORDER BY timestamp DESC";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, targetUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reviews.add(new Review(
                            resultSet.getInt("review_id"),
                            UUID.fromString(resultSet.getString("target_uuid")),
                            UUID.fromString(resultSet.getString("reviewer_uuid")),
                            resultSet.getString("reviewer_name"),
                            resultSet.getInt("rating"),
                            resultSet.getString("comment"),
                            resultSet.getTimestamp("timestamp"),
                            resultSet.getString("server_name")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting reviews: " + e.getMessage());
        }
        return reviews;
    }

    public void addReview(Review review) {
        Connection conn = getConnection();
        if (conn == null) return;

        String sql = "INSERT INTO player_reviews (target_uuid, reviewer_uuid, reviewer_name, rating, comment, server_name) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, review.getTargetUuid().toString());
            statement.setString(2, review.getReviewerUuid().toString());
            statement.setString(3, review.getReviewerName());
            statement.setInt(4, review.getRating());
            statement.setString(5, review.getComment());
            statement.setString(6, review.getServerName());
            statement.executeUpdate();
            
            updateProfileStats(review.getTargetUuid());
        } catch (SQLException e) {
            plugin.getLogger().severe("Error adding review: " + e.getMessage());
        }
    }

    public void updateProfileVisibility(UUID uuid, boolean isPublic) {
        Connection conn = getConnection();
        if (conn == null) return;

        String sql = "UPDATE player_profiles SET is_public = ? WHERE uuid = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setBoolean(1, isPublic);
            statement.setString(2, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error updating profile visibility: " + e.getMessage());
        }
    }

    private void updateProfileStats(UUID targetUuid) {
        Connection conn = getConnection();
        if (conn == null) return;

        String sql = "SELECT AVG(rating) as avg_rating, COUNT(*) as total_reviews FROM player_reviews WHERE target_uuid = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, targetUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    double avg = resultSet.getDouble("avg_rating");
                    int total = resultSet.getInt("total_reviews");
                    
                    String updateSql = "UPDATE player_profiles SET average_rating = ?, total_reviews = ? WHERE uuid = ?";
                    try (PreparedStatement updateStatement = conn.prepareStatement(updateSql)) {
                        updateStatement.setDouble(1, avg);
                        updateStatement.setInt(2, total);
                        updateStatement.setString(3, targetUuid.toString());
                        updateStatement.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error updating profile stats: " + e.getMessage());
        }
    }
}
