package com.egghunt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class EggHuntProgress {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("egghunt_progress.json");
    private static final Type SET_TYPE = new TypeToken<HashSet<String>>() {}.getType();

    private static final Set<String> finished = new HashSet<>();

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                Set<String> loaded = GSON.fromJson(reader, SET_TYPE);
                if (loaded != null) {
                    finished.clear();
                    finished.addAll(loaded);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(finished, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isFinished(EggLocation loc) {
        return finished.contains(key(loc));
    }

    public static boolean markFinished(EggLocation loc) {
        if (finished.add(key(loc))) {
            save();
            return true;
        }
        return false;
    }

    public static void reset() {
        finished.clear();
        save();
    }

    public static int finishedCount() {
        return finished.size();
    }

    private static String key(EggLocation loc) {
        return loc.x() + "," + loc.y() + "," + loc.z();
    }
}
