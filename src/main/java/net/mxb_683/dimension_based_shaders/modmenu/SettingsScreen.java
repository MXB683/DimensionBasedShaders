package net.mxb_683.dimension_based_shaders.modmenu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.irisshaders.iris.Iris;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SettingsScreen extends Screen {
	private static final Component SETTINGS_TITLE = Component.translatable("dimension_based_shaders.settings.title");
	private static final int EDIT_BOX_MAX_LENGTH = 256;

	private final Screen parent;

	// One EditBox per dimension id (e.g. "minecraft:overworld", "modid:my_dim")
	private final Map<String, EditBox> shaderFieldsByDimension = new LinkedHashMap<>();

	protected SettingsScreen(Screen parent) {
		super(SETTINGS_TITLE);
		this.parent = parent;
	}

	@Override
	protected void init() {
		int labelWidth = 220;
		int buttonWidth = 100;
		int centerX = this.width / 2;
		int fieldWidth = this.width - labelWidth - buttonWidth - 60;

		// StringWidget replaces ComponentWidget; this.font replaces this.textRenderer/ComponentRenderer
		this.addRenderableWidget(new StringWidget(centerX - 75, 20, 150, 20, SETTINGS_TITLE, this.font));

		// Build list of dimension ids known to the client right now.
		// This includes modded dimensions as long as they're present in the synced registries.
		Map<String, String> dimensionIdsToLabel = new LinkedHashMap<>();

		// Always include vanilla ids (so the menu still shows something even if registry access fails)
		dimensionIdsToLabel.put("minecraft:overworld", "minecraft:overworld");
		dimensionIdsToLabel.put("minecraft:the_nether", "minecraft:the_nether");
		dimensionIdsToLabel.put("minecraft:the_end", "minecraft:the_end");

		// Always include the current dimension if registry access fails.
		// 26.1: level.dimension() replaces world.getRegistryKey()
		//       ResourceKey.id()    replaces ResourceKey.location() (renamed alongside ResourceLocation -> Identifier)
		if (this.minecraft.level != null) {
			String currentDim = this.minecraft.level.dimension().identifier().toString();
			dimensionIdsToLabel.put(currentDim, currentDim);
		}

		if (this.minecraft.level != null) {
			try {
				// 26.1: registryAccess()  replaces getRegistryManager()
				//       lookupOrThrow()   replaces registryOrThrow() (removed in 1.21.2, absent in 26.1);
				//                         returns HolderLookup.RegistryLookup<T>, not Registry<T>
				//       listElementIds()  streams ResourceKey<T> values from the lookup
				//       ResourceKey.id()  returns Identifier (replaces ResourceKey.location())
				//       Registries.*      replaces RegistryKeys.*
				this.minecraft.level.registryAccess()
						.lookupOrThrow(Registries.DIMENSION_TYPE)
						.listElementIds()
						.filter(key -> !key.identifier().toString().equals("minecraft:overworld_caves"))
						.forEach(key -> {
							String idStr = key.identifier().toString();
							dimensionIdsToLabel.putIfAbsent(idStr, idStr);
						});
			} catch (Throwable ignored) {
				// If anything goes wrong (e.g., API differences), fall back to the vanilla list above.
			}
		}

		// Load config once, then populate fields
		Config config;
		try {
			config = getConfig();
		} catch (FileNotFoundException e) {
			config = new Config();
		}

		int y = 70;
		int rowHeight = 28;

		for (Map.Entry<String, String> entry : dimensionIdsToLabel.entrySet()) {
			String dimId = entry.getKey();
			String label = entry.getValue();

			// Label
			this.addRenderableWidget(new StringWidget(20, y, labelWidth, 20, Component.literal(label), this.font));

			// Text field — EditBox replaces ComponentFieldWidget (Yarn: TextFieldWidget)
			EditBox field = new EditBox(this.font, labelWidth + 30, y, fieldWidth, 20, Component.empty());
			field.setMaxLength(EDIT_BOX_MAX_LENGTH);
			// setValue() / getValue() replace setComponent() / getComponent()
			field.setValue(config.shaders.getOrDefault(dimId, ""));

			this.addRenderableWidget(field);
			shaderFieldsByDimension.put(dimId, field);

			// "Use current pack" button — Button replaces ButtonWidget
			this.addRenderableWidget(Button.builder(
					Component.translatable("dimension_based_shaders.settings.get_current_button"),
                    _ -> field.setValue(getPackName())
			).bounds(labelWidth + fieldWidth + 40, y, buttonWidth, 20).build());

			y += rowHeight;
		}

		// Cancel — this.minecraft.setScreen() replaces this.client.setScreen()
		this.addRenderableWidget(Button.builder(
				Component.translatable("dimension_based_shaders.settings.close_button"),
				_ -> this.minecraft.setScreen(this.parent)
		).bounds(centerX - 155, this.height - 40, 150, 20).build());

		// Save
		this.addRenderableWidget(Button.builder(
				Component.translatable("dimension_based_shaders.settings.save_button"),
				_ -> {
					saveConfig();
					this.minecraft.setScreen(this.parent);
				}
		).bounds(centerX + 5, this.height - 40, 150, 20).build());
	}

	private void saveConfig() {
		try {
			File configDir = new File("config");
			if (!configDir.exists()) {
				//noinspection ResultOfMethodCallIgnored
				configDir.mkdir();
			}

			Config config = new Config();

			for (Map.Entry<String, EditBox> e : shaderFieldsByDimension.entrySet()) {
				String dimId = e.getKey();
				String shaderPack = e.getValue().getValue().trim();
				if (!shaderPack.isEmpty()) {
					config.shaders.put(dimId, shaderPack);
				}
			}

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			try (FileWriter writer = new FileWriter("config/dimension_based_shaders.json")) {
				gson.toJson(config, writer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Config getConfig() throws FileNotFoundException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		FileReader reader = new FileReader("config/dimension_based_shaders.json");
		Config cfg = gson.fromJson(reader, Config.class);
		return (cfg != null) ? cfg : new Config();
	}

	public static class Config {
		/**
		 * Key: dimension id string (e.g. "minecraft:overworld", "mod_id:my_dimension")
		 * Value: shader pack name (empty/missing = off)
		 */
		public Map<String, String> shaders = new HashMap<>();
	}

	private String getPackName() {
		String name = Iris.getCurrentPackName();
		return "(off)".equals(name) ? "" : name;
	}
}