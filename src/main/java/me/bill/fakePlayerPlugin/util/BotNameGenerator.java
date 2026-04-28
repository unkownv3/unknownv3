package me.bill.fakePlayerPlugin.util;

import java.util.Random;

public final class BotNameGenerator {
    private static final Random RANDOM = new Random();

    private static final String[] ADJECTIVES = {
        "Cool", "Fast", "Dark", "Blue", "Red", "Gold", "Iron", "Wild", "Epic", "Mega",
        "Tiny", "Big", "Lucky", "Swift", "Brave", "Frost", "Fire", "Storm", "Shadow", "Pixel",
        "Neon", "Cyber", "Mystic", "Ninja", "Ultra", "Pro", "Ace", "Star", "Alpha", "Omega"
    };

    private static final String[] NOUNS = {
        "Player", "Miner", "Steve", "Alex", "Creeper", "Wolf", "Fox", "Cat", "Bear", "Hawk",
        "Dragon", "Knight", "Wizard", "Hunter", "Archer", "Slayer", "Runner", "Builder", "Crafter", "Warrior",
        "Phantom", "Ghost", "Zombie", "Spider", "Blaze", "Golem", "Enderman", "Villager", "Pirate", "Ranger"
    };

    private BotNameGenerator() {}

    public static String generate() {
        String adj = ADJECTIVES[RANDOM.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[RANDOM.nextInt(NOUNS.length)];
        int num = RANDOM.nextInt(1000);
        String name = adj + noun + num;
        if (name.length() > 16) {
            name = name.substring(0, 16);
        }
        return name;
    }
}
