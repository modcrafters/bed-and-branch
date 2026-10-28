package net.modcrafters.minecraft.bedandbranch.config;

import net.modcrafters.minecraft.bedandbranch.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BedAndBranchConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static BedAndBranchConfig current = defaults();

    private final boolean vanillaCropOverride;

    private BedAndBranchConfig(boolean vanillaCropOverride) {
        this.vanillaCropOverride = vanillaCropOverride;
    }

    public static BedAndBranchConfig defaults() {
        return new BedAndBranchConfig(true);
    }

    public static BedAndBranchConfig current() {
        return current;
    }

    public boolean vanillaCropOverride() {
        return vanillaCropOverride;
    }

    public static void load(Path path) {
        try {
            if (Files.notExists(path)) {
                Files.createDirectories(path.getParent());
                BedAndBranchConfig created = defaults();
                Files.writeString(path, created.toJson());
                current = created;
                return;
            }
            current = parse(Files.readString(path));
        } catch (IOException | IllegalArgumentException exception) {
            Constants.LOG.error("Failed to load {}, using defaults", path, exception);
            current = defaults();
        }
    }

    public static BedAndBranchConfig parse(String json) {
        try {
            JsonObject object = JsonParser.parseString(json).getAsJsonObject();
            boolean override = !object.has("vanillaCropOverride") || object.get("vanillaCropOverride").getAsBoolean();
            return new BedAndBranchConfig(override);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    public String toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("vanillaCropOverride", vanillaCropOverride);
        return GSON.toJson(object);
    }
}
