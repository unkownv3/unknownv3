package me.bill.fpp.database;

public record BotRecord(
    String botName,
    String botDisplay,
    String botUuid,
    String spawnedBy,
    String spawnedByUuid,
    String worldName,
    double spawnX,
    double spawnY,
    double spawnZ,
    float spawnYaw,
    float spawnPitch
) {
}
