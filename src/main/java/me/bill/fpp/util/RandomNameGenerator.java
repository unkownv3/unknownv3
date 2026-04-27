package me.bill.fpp.util;

import java.util.Random;

public final class RandomNameGenerator {
    private static final String[] ADJECTIVES = {
        "Happy", "Brave", "Swift", "Quiet", "Loud", "Dark", "Bright", "Cool",
        "Wild", "Calm", "Bold", "Shy", "Fast", "Slow", "Keen", "Sharp",
        "Tiny", "Big", "Red", "Blue", "Green", "Old", "New", "Hot"
    };
    private static final String[] NOUNS = {
        "Steve", "Alex", "Miner", "Builder", "Crafter", "Digger", "Runner",
        "Guard", "Scout", "Knight", "Farmer", "Fisher", "Hunter", "Ranger",
        "Bard", "Sage", "Cook", "Smith", "Hero", "Ghost", "Ninja", "Fox"
    };
    private static final Random RANDOM = new Random();

    public static String generate() {
        String name = ADJECTIVES[RANDOM.nextInt(ADJECTIVES.length)]
            + NOUNS[RANDOM.nextInt(NOUNS.length)]
            + RANDOM.nextInt(100);
        if (name.length() > 16) name = name.substring(0, 16);
        return name;
    }

    private RandomNameGenerator() {}
}
