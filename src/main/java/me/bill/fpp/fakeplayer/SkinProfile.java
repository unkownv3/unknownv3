package me.bill.fpp.fakeplayer;

public record SkinProfile(String texture, String signature) {
    public boolean isValid() {
        return texture != null && !texture.isEmpty();
    }
}
