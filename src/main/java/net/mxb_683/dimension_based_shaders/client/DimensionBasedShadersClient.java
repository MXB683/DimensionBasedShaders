package net.mxb_683.dimension_based_shaders.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.config.IrisConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.InteractionResult;
import net.mxb_683.dimension_based_shaders.modmenu.SettingsScreen;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class DimensionBasedShadersClient implements ClientModInitializer {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Path.of("config", "dimension_based_shaders.json");

    @Override
    public void onInitializeClient() {

        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register(
                (Minecraft _, ClientLevel _) -> {
                    try {
                        String dimensionId = Iris.getCurrentDimension().toString();
                        IrisConfig irisConfig = Iris.getIrisConfig();
                        SettingsScreen.Config config = loadOrCreateConfig();

                        String pack = (config.shaders != null) ? config.shaders.get(dimensionId) : null;
                        pack = (pack != null) ? pack.trim() : "";

                        irisConfig.setShadersEnabled(!pack.isEmpty());
                        irisConfig.setShaderPackName(pack);

                        irisConfig.save();
                        Iris.reload();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private static SettingsScreen.Config loadOrCreateConfig() throws IOException {
        Files.createDirectories(CONFIG_PATH.getParent());

        if (Files.notExists(CONFIG_PATH)) {
            SettingsScreen.Config defaults = new SettingsScreen.Config();
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(defaults, writer);
            }
            return defaults;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            SettingsScreen.Config cfg = GSON.fromJson(reader, SettingsScreen.Config.class);
            return (cfg != null) ? cfg : new SettingsScreen.Config();
        }
    }
}