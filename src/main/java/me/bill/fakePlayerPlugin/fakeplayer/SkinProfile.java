package me.bill.fakePlayerPlugin.fakeplayer;

public record SkinProfile(String value, String signature) {
    public boolean isValid() {
        return value != null && !value.isEmpty() && signature != null && !signature.isEmpty();
    }
}
