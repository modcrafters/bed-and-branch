package net.modcrafters.minecraft.bedandbranch.growth;

import net.modcrafters.minecraft.bedandbranch.Constants;
import net.modcrafters.minecraft.bedandbranch.soil.SoilKind;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntUnaryOperator;

/**
 * Cross recipes from {@code data/<namespace>/bed_and_branch/crop_cross/*.json}.
 * Parents are plant ids. Order does not matter. The offspring is a seed item id.
 */
public final class CrossBook {
    private static final String FOLDER = "bed_and_branch/crop_cross";
    private static final String BUILTIN = "/data/bed_and_branch/" + FOLDER + "/corn.json";

    private static volatile List<Recipe> recipes = List.of();
    private static ResourceManager loadedFrom;

    static {
        loadBuiltin();
    }

    private CrossBook() {
    }

    /** The built-in file, so a cross works before the server resource manager is scanned. */
    public static void loadBuiltin() {
        try (InputStream in = CrossBook.class.getResourceAsStream(BUILTIN)) {
            if (in == null) {
                Constants.LOG.warn("Missing built-in crop cross {}", BUILTIN);
                return;
            }
            Recipe recipe = parse(new String(in.readAllBytes(), StandardCharsets.UTF_8), BUILTIN);
            if (recipe != null) {
                recipes = List.of(recipe);
            }
        } catch (IOException e) {
            Constants.LOG.warn("Could not read {}", BUILTIN, e);
        }
    }

    /** Datapacks included. Called from the server when a plant tries to spread. */
    public static void use(ResourceManager manager) {
        if (manager == null || manager == loadedFrom) {
            return;
        }
        try {
            List<Recipe> parsed = new ArrayList<>();
            Map<Identifier, Resource> files = manager.listResources(FOLDER, id -> id.getPath().endsWith(".json"));
            for (Map.Entry<Identifier, Resource> entry : files.entrySet()) {
                try (InputStream in = entry.getValue().open()) {
                    Recipe recipe = parse(new String(in.readAllBytes(), StandardCharsets.UTF_8), entry.getKey().toString());
                    if (recipe != null) {
                        parsed.add(recipe);
                    }
                }
            }
            if (!parsed.isEmpty()) {
                recipes = List.copyOf(parsed);
                loadedFrom = manager;
            }
        } catch (IOException e) {
            Constants.LOG.warn("Could not read crop crosses", e);
        }
    }

    /**
     * A cross that suits the empty stick, or null when this pair has none.
     * {@code nextInt} receives the total weight and returns a value in that range.
     */
    public static String pick(String spreading, List<String> partners, SoilKind soil, IntUnaryOperator nextInt) {
        if (spreading == null || partners == null || soil == null || partners.isEmpty()) {
            return null;
        }
        List<Recipe> matches = new ArrayList<>();
        int weight = 0;
        for (String partner : partners) {
            for (Recipe recipe : recipes) {
                if (!recipe.matches(spreading, partner) || !SpreadRules.accepts(recipe.offspring, soil)) {
                    continue;
                }
                matches.add(recipe);
                weight += recipe.weight;
            }
        }
        if (matches.isEmpty() || weight <= 0) {
            return null;
        }
        int roll = matches.size() == 1 ? 0 : nextInt.applyAsInt(weight);
        int cursor = 0;
        for (Recipe recipe : matches) {
            cursor += recipe.weight;
            if (roll < cursor) {
                return recipe.offspring;
            }
        }
        return matches.get(matches.size() - 1).offspring;
    }

    static Recipe parse(String json, String source) {
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        JsonArray parents = object.getAsJsonArray("parents");
        if (parents == null || parents.size() != 2) {
            Constants.LOG.warn("Crop cross {} needs two parents", source);
            return null;
        }
        String offspringId = object.get("offspring").getAsString();
        String offspring = seedPlant(offspringId);
        if (offspring == null) {
            Constants.LOG.warn("Crop cross {} has an unknown seed {}", source, offspringId);
            return null;
        }
        int weight = object.has("weight") ? object.get("weight").getAsInt() : 1;
        if (weight <= 0) {
            return null;
        }
        return new Recipe(parents.get(0).getAsString(), parents.get(1).getAsString(), offspring, weight);
    }

    /** Seed item id to the plant name spread rules already use. */
    static String seedPlant(String seedId) {
        return switch (seedId) {
            case "bed_and_branch:corn_seeds" -> "corn";
            case "bed_and_branch:rice_seeds" -> "rice";
            case "bed_and_branch:tomato_seeds" -> "tomato";
            case "minecraft:wheat_seeds" -> "wheat";
            case "minecraft:beetroot_seeds" -> "beetroot";
            case "minecraft:torchflower_seeds" -> "torchflower";
            case "minecraft:melon_seeds" -> "melon";
            case "minecraft:pumpkin_seeds" -> "pumpkin";
            case "minecraft:carrot" -> "carrot";
            case "minecraft:potato" -> "potato";
            default -> null;
        };
    }

    record Recipe(String parentA, String parentB, String offspring, int weight) {
        boolean matches(String spreading, String partner) {
            return (parentA.equals(spreading) && parentB.equals(partner))
                    || (parentB.equals(spreading) && parentA.equals(partner));
        }
    }
}
