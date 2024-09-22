package org.mythofy.chatcolors;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class DatabaseManager {
    private final JavaPlugin plugin;

    private Connection connection;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        ConfigurationSection dbConfig = this.plugin.getConfig().getConfigurationSection("database");
        if (dbConfig == null) {
            this.plugin.getLogger().severe("No database configuration found in config.yml!");
            return;
        }
        String host = dbConfig.getString("host");
        int port = dbConfig.getInt("port");
        String database = dbConfig.getString("database");
        String username = dbConfig.getString("username");
        String password = dbConfig.getString("password");
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        try {
            this.connection = DriverManager.getConnection(url, username, password);
            this.plugin.getLogger().info("Connected to the MySQL database successfully.");
            String createTableSQL = "CREATE TABLE IF NOT EXISTS player_colors (uuid VARCHAR(36) PRIMARY KEY,color_key VARCHAR(50) NOT NULL);";
            Statement stmt = this.connection.createStatement();
            stmt.execute(createTableSQL);
            stmt.close();
        } catch (SQLException e) {
            this.plugin.getLogger().severe("Failed to connect to the MySQL database.");
            e.printStackTrace();
        }
    }

    public void setPlayerColor(UUID uuid, String colorKey) {
        if (this.connection == null)
            return;

        String sql;
        if (colorKey == null) {
            sql = "DELETE FROM player_colors WHERE uuid = ?";
        } else {
            sql = "INSERT INTO player_colors (uuid, color_key) VALUES (?, ?) ON DUPLICATE KEY UPDATE color_key = VALUES(color_key)";
        }

        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            if (colorKey != null) {
                pstmt.setString(2, colorKey);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            this.plugin.getLogger().severe("Failed to set player color for UUID: " + uuid);
            e.printStackTrace();
        }
    }

    public String getPlayerColor(UUID uuid) {
        if (this.connection == null)
            return null;
        String selectSQL = "SELECT color_key FROM player_colors WHERE uuid = ?;";
        try (PreparedStatement pstmt = this.connection.prepareStatement(selectSQL)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("color_key");
            }
        } catch (SQLException e) {
            this.plugin.getLogger().severe("Failed to get player color for UUID: " + uuid);
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        if (this.connection == null)
            return;
        try {
            this.connection.close();
            this.plugin.getLogger().info("Database connection closed.");
        } catch (SQLException e) {
            this.plugin.getLogger().severe("Failed to close the database connection.");
            e.printStackTrace();
        }
    }
}
