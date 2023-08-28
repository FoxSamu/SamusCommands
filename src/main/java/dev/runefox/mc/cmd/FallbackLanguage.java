package dev.runefox.mc.cmd;

import com.google.gson.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class FallbackLanguage {
    private static final Map<String, String> storage = new HashMap<>();

    public static String get(String key) {
        return storage.get(key);
    }

    public static void load() {
        storage.clear();

        InputStream stream = FallbackLanguage.class.getResourceAsStream("/assets/rfx-cmd/lang/en_us.json");
        if (stream == null) {
            CommandsMod.LOGGER.error("Fallback language not found");
            return;
        }

        try (InputStreamReader reader = new InputStreamReader(stream)) {
            JsonElement elem = JsonParser.parseReader(reader);
            JsonObject obj = elem.getAsJsonObject();

            for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
                storage.put(e.getKey(), e.getValue().getAsString());
            }
        } catch (Exception exc) {
            storage.clear();
            CommandsMod.LOGGER.error("Fallback failed loading", exc);
        }
    }
}
