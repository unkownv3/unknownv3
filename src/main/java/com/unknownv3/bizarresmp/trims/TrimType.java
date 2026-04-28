package com.unknownv3.bizarresmp.trims;

import org.bukkit.inventory.meta.trim.TrimPattern;

public enum TrimType {
    SILENCE(TrimPattern.SILENCE),
    EYE(TrimPattern.EYE),
    SNOUT(TrimPattern.SNOUT),
    RIB(TrimPattern.RIB),
    FLOW(TrimPattern.FLOW),
    SPIRE(TrimPattern.SPIRE),
    BOLT(TrimPattern.BOLT),
    HOST(TrimPattern.HOST),
    RAISER(TrimPattern.RAISER),
    SHAPER(TrimPattern.SHAPER),
    TIDE(TrimPattern.TIDE),
    WARD(TrimPattern.WARD),
    DUNE(TrimPattern.DUNE),
    WAYFINDER(TrimPattern.WAYFINDER),
    COAST(TrimPattern.COAST),
    WILD(TrimPattern.WILD),
    VEX(TrimPattern.VEX),
    SENTRY(TrimPattern.SENTRY);

    private final TrimPattern pattern;

    TrimType(TrimPattern pattern) {
        this.pattern = pattern;
    }

    public TrimPattern getPattern() {
        return pattern;
    }

    public static TrimType fromPattern(TrimPattern pattern) {
        for (TrimType type : values()) {
            if (type.getPattern().equals(pattern)) {
                return type;
            }
        }
        return null;
    }
}
