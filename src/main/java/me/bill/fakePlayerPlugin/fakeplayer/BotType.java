package me.bill.fakePlayerPlugin.fakeplayer;

public enum BotType {
    AFK,
    MINING,
    PLACING,
    ATTACKING,
    FOLLOWING,
    MOVING,
    PATROLLING,
    SLEEPING,
    FINDING;

    public String displayName() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
