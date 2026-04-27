package me.bill.fpp.database;

import me.bill.fpp.FakePlayerMod;
import me.bill.fpp.util.FppLogger;

import java.io.File;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DatabaseManager {
    private static final int SCHEMA_VERSION = 20;
    private Connection connection;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private boolean useMysql = false;

    private static final String CREATE_SESSIONS = """
        CREATE TABLE IF NOT EXISTS fpp_bot_sessions (
            id              INTEGER PRIMARY KEY AUTOINCREMENT,
            bot_name        VARCHAR(16) NOT NULL,
            bot_display     VARCHAR(128) DEFAULT NULL,
            bot_uuid        VARCHAR(36) NOT NULL,
            spawned_by      VARCHAR(16) NOT NULL,
            spawned_by_uuid VARCHAR(36) NOT NULL,
            world_name      VARCHAR(64) NOT NULL,
            spawn_x         DOUBLE NOT NULL,
            spawn_y         DOUBLE NOT NULL,
            spawn_z         DOUBLE NOT NULL,
            spawn_yaw       FLOAT NOT NULL DEFAULT 0,
            spawn_pitch     FLOAT NOT NULL DEFAULT 0,
            last_world      VARCHAR(64),
            last_x          DOUBLE, last_y DOUBLE, last_z DOUBLE,
            last_yaw        FLOAT, last_pitch FLOAT,
            entity_type     VARCHAR(32) NOT NULL DEFAULT 'MANNEQUIN',
            spawned_at      BIGINT NOT NULL,
            removed_at      BIGINT,
            remove_reason   VARCHAR(32),
            server_id       VARCHAR(64) NOT NULL DEFAULT 'default'
        )""";

    private static final String CREATE_ACTIVE = """
        CREATE TABLE IF NOT EXISTS fpp_active_bots (
            bot_uuid        VARCHAR(36) NOT NULL PRIMARY KEY,
            bot_name        VARCHAR(16) NOT NULL,
            bot_display     VARCHAR(128) DEFAULT NULL,
            spawned_by      VARCHAR(16) NOT NULL,
            spawned_by_uuid VARCHAR(36) NOT NULL,
            world_name      VARCHAR(64) NOT NULL,
            pos_x           DOUBLE NOT NULL,
            pos_y           DOUBLE NOT NULL,
            pos_z           DOUBLE NOT NULL,
            pos_yaw         FLOAT NOT NULL DEFAULT 0,
            pos_pitch       FLOAT NOT NULL DEFAULT 0,
            updated_at      BIGINT NOT NULL,
            luckperms_group VARCHAR(64) DEFAULT NULL,
            server_id       VARCHAR(64) NOT NULL DEFAULT 'default',
            frozen          BOOLEAN DEFAULT 0,
            chat_enabled    BOOLEAN DEFAULT 1,
            chat_tier       VARCHAR(16) DEFAULT NULL,
            right_click_cmd VARCHAR(256) DEFAULT NULL,
            ai_personality  VARCHAR(64) DEFAULT NULL,
            pickup_items    BOOLEAN DEFAULT 0,
            pickup_xp       BOOLEAN DEFAULT 1,
            head_ai_enabled BOOLEAN DEFAULT 1,
            nav_parkour     BOOLEAN DEFAULT 0,
            nav_break_blocks BOOLEAN DEFAULT 0,
            nav_place_blocks BOOLEAN DEFAULT 0,
            nav_avoid_water BOOLEAN DEFAULT 0,
            nav_avoid_lava  BOOLEAN DEFAULT 0,
            swim_ai_enabled BOOLEAN DEFAULT 1,
            chunk_load_radius INT DEFAULT -1,
            ping            INT DEFAULT -1,
            pve_enabled     BOOLEAN DEFAULT 0,
            pve_range       DOUBLE DEFAULT 16.0,
            pve_priority    VARCHAR(16) DEFAULT NULL,
            pve_mob_type    VARCHAR(64) DEFAULT NULL,
            pve_smart_attack_mode VARCHAR(16) DEFAULT 'OFF',
            skin_texture    TEXT DEFAULT NULL,
            skin_signature  TEXT DEFAULT NULL
        )""";

    private static final String CREATE_SKIN_CACHE = """
        CREATE TABLE IF NOT EXISTS fpp_skin_cache (
            player_name     VARCHAR(16) NOT NULL PRIMARY KEY,
            texture         TEXT NOT NULL,
            signature       TEXT,
            cached_at       BIGINT NOT NULL
        )""";

    private static final String CREATE_STATS = """
        CREATE TABLE IF NOT EXISTS fpp_stats (
            stat_key        VARCHAR(64) NOT NULL PRIMARY KEY,
            stat_value      BIGINT NOT NULL DEFAULT 0,
            updated_at      BIGINT NOT NULL
        )""";

    public boolean init(File dataFolder) {
        try {
            var config = FakePlayerMod.getInstance().getConfig();
            if (config.mysqlEnabled()) {
                useMysql = true;
                String url = "jdbc:mysql://" + config.mysqlHost() + ":" + config.mysqlPort()
                    + "/" + config.mysqlDatabase() + "?useSSL=false&allowPublicKeyRetrieval=true";
                connection = DriverManager.getConnection(url, config.mysqlUsername(), config.mysqlPassword());
            } else {
                File dbFile = new File(dataFolder, "fpp.db");
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            }
            createTables();
            initialized.set(true);
            FppLogger.info("Database initialized (" + (useMysql ? "MySQL" : "SQLite") + ")");
            return true;
        } catch (Exception e) {
            FppLogger.error("Database init failed: " + e.getMessage(), e);
            return false;
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(CREATE_SESSIONS);
            stmt.execute(CREATE_ACTIVE);
            stmt.execute(CREATE_SKIN_CACHE);
            stmt.execute(CREATE_STATS);
        }
    }

    public void insertSession(BotRecord record) {
        executor.submit(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO fpp_bot_sessions (bot_name, bot_display, bot_uuid, spawned_by, spawned_by_uuid, " +
                "world_name, spawn_x, spawn_y, spawn_z, spawn_yaw, spawn_pitch, spawned_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                ps.setString(1, record.botName());
                ps.setString(2, record.botDisplay());
                ps.setString(3, record.botUuid());
                ps.setString(4, record.spawnedBy());
                ps.setString(5, record.spawnedByUuid());
                ps.setString(6, record.worldName());
                ps.setDouble(7, record.spawnX());
                ps.setDouble(8, record.spawnY());
                ps.setDouble(9, record.spawnZ());
                ps.setFloat(10, record.spawnYaw());
                ps.setFloat(11, record.spawnPitch());
                ps.setLong(12, Instant.now().toEpochMilli());
                ps.executeUpdate();
            } catch (SQLException e) {
                FppLogger.error("Failed to insert session: " + e.getMessage());
            }
        });
    }

    public void markSessionRemoved(String botUuid, String reason) {
        executor.submit(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE fpp_bot_sessions SET removed_at = ?, remove_reason = ? " +
                "WHERE bot_uuid = ? AND removed_at IS NULL")) {
                ps.setLong(1, Instant.now().toEpochMilli());
                ps.setString(2, reason);
                ps.setString(3, botUuid);
                ps.executeUpdate();
            } catch (SQLException e) {
                FppLogger.error("Failed to mark session removed: " + e.getMessage());
            }
        });
    }

    public void cacheSkin(String playerName, String texture, String signature) {
        executor.submit(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO fpp_skin_cache (player_name, texture, signature, cached_at) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, playerName.toLowerCase());
                ps.setString(2, texture);
                ps.setString(3, signature);
                ps.setLong(4, Instant.now().toEpochMilli());
                ps.executeUpdate();
            } catch (SQLException e) {
                FppLogger.error("Failed to cache skin: " + e.getMessage());
            }
        });
    }

    public int getSkinCacheSize() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM fpp_skin_cache")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            FppLogger.error("Failed to get skin cache size: " + e.getMessage());
        }
        return 0;
    }

    public void cleanExpiredSkinCache() {
        long expiry = Instant.now().toEpochMilli() - (7 * 24 * 60 * 60 * 1000L);
        executor.submit(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM fpp_skin_cache WHERE cached_at < ?")) {
                ps.setLong(1, expiry);
                ps.executeUpdate();
            } catch (SQLException e) {
                FppLogger.error("Failed to clean skin cache: " + e.getMessage());
            }
        });
    }

    public long getTotalSessionCount() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM fpp_bot_sessions")) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException ignored) {}
        return 0;
    }

    public void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {}
    }
}
