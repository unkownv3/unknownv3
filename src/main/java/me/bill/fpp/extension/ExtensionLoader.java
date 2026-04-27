package me.bill.fpp.extension;

import me.bill.fpp.FakePlayerMod;
import me.bill.fpp.util.FppLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExtensionLoader {
    private final File extensionDir;
    private final List<Object> loadedExtensions = new ArrayList<>();

    public ExtensionLoader(File configDir) {
        this.extensionDir = new File(configDir, "extensions");
        if (!extensionDir.exists()) extensionDir.mkdirs();
    }

    public void loadAll() {
        File[] files = extensionDir.listFiles((d, n) -> n.endsWith(".jar"));
        if (files == null) return;
        FppLogger.info("Found " + files.length + " extension(s) to load.");
    }

    public List<Object> getLoadedExtensions() {
        return loadedExtensions;
    }
}
