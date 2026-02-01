package net.mxb_683.dimension_based_shaders.modmenu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.irisshaders.iris.Iris;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SettingsScreen extends Screen {
	private static final Text SETTINGS_TITLE = Text.translatable("dimension_based_shaders.settings.title");
	private static final int TEXT_FIELD_MAX_LENGTH = 256;

	private final Screen parent;

	// One text field per dimension id (e.g. "minecraft:overworld", "modid:my_dim")
	private final Map<String, TextFieldWidget> shaderFieldsByDimension = new LinkedHashMap<>();

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

		this.addDrawableChild(new TextWidget(centerX - 75, 20, 150, 20, SETTINGS_TITLE, this.textRenderer));

		// Build list of dimension ids known to the client right now.
		// This includes modded dimensions as long as they're present in the synced registries.
		Map<String, String> dimensionIdsToLabel = new LinkedHashMap<>();

		// Always include vanilla ids (so the menu still shows something even if registry access fails)
		dimensionIdsToLabel.put("minecraft:overworld", "minecraft:overworld");
		dimensionIdsToLabel.put("minecraft:the_nether", "minecraft:the_nether");
		dimensionIdsToLabel.put("minecraft:the_end", "minecraft:the_end");

		// Always include the current dimension if registry access fails
		if (this.client.world != null) {
			dimensionIdsToLabel.put(this.client.world.getRegistryKey().getValue().toString(), this.client.world.getRegistryKey().getValue().toString());
		}

		if (this.client.world != null) {
			try {
				for (Identifier id : this.client.world.getRegistryManager().getOrThrow(RegistryKeys.DIMENSION_TYPE).getIds()) {
					if (id.toString().equals("minecraft:overworld_caves")) continue;
					dimensionIdsToLabel.putIfAbsent(id.toString(), id.toString());
				}
			} catch (Throwable ignored) {
				// If anything goes wrong (e.g. API differences), fall back to the vanilla list above.
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

			// label
			this.addDrawableChild(new TextWidget(20, y, labelWidth, 20, Text.literal(label), this.textRenderer));

			// text field
			TextFieldWidget field = new TextFieldWidget(this.textRenderer, labelWidth + 30, y, fieldWidth, 20, Text.empty());
			field.setMaxLength(TEXT_FIELD_MAX_LENGTH);

			String existing = config.shaders.getOrDefault(dimId, "");
			field.setText(existing);

			this.addDrawableChild(field);
			shaderFieldsByDimension.put(dimId, field);

			// "use current pack" button for this row
			this.addDrawableChild(ButtonWidget.builder(
					Text.translatable("dimension_based_shaders.settings.get_current_button"),
					button -> field.setText(getPackName())
			).dimensions(labelWidth + fieldWidth + 40, y, buttonWidth, 20).build());

			y += rowHeight;
		}

		// cancel/save buttons
		this.addDrawableChild(ButtonWidget.builder(
				Text.translatable("dimension_based_shaders.settings.close_button"),
				button -> this.client.setScreen(this.parent)
		).dimensions(centerX - 155, this.height - 40, 150, 20).build());

		this.addDrawableChild(ButtonWidget.builder(
				Text.translatable("dimension_based_shaders.settings.save_button"),
				button -> {
					saveConfig();
					this.client.setScreen(this.parent);
				}
		).dimensions(centerX + 5, this.height - 40, 150, 20).build());
	}

	private void saveConfig() {
		try {
			File configDir = new File("config");
			if (!configDir.exists()) {
				//noinspection ResultOfMethodCallIgnored
				configDir.mkdir();
			}

			Config config = new Config();

			for (Map.Entry<String, TextFieldWidget> e : shaderFieldsByDimension.entrySet()) {
				String dimId = e.getKey();
				String shaderPack = e.getValue().getText().trim();
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
		 * Key: dimension id string (e.g. "minecraft:overworld", "modid:my_dimension")
		 * Value: shader pack name (empty/missing = off)
		 */
		public Map<String, String> shaders = new HashMap<>();
	}

	private String getPackName() {
		String name = Iris.getCurrentPackName();
		return "(off)".equals(name) ? "" : name;
	}
}