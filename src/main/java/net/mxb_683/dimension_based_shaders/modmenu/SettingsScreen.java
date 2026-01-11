package net.mxb_683.dimension_based_shaders.modmenu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.irisshaders.iris.Iris;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

import java.io.*;
import java.util.Objects;

public class SettingsScreen extends Screen {
	private static final Text SETTINGS_TITLE = Text.translatable("dimension_based_shaders.settings.title");
	private static final Text OVERWORLD_LABEL = Text.translatable("dimension_based_shaders.settings.overworld_shader");
	private static final Text NETHER_LABEL = Text.translatable("dimension_based_shaders.settings.nether_shader");
	private static final Text END_LABEL = Text.translatable("dimension_based_shaders.settings.end_shader");
	private static final int TEXT_FIELD_MAX_LENGTH = 256;
	private final Screen parent;
	private TextFieldWidget overworldShaderField;
	private TextFieldWidget netherShaderField;
	private TextFieldWidget endShaderField;

	protected SettingsScreen(Screen parent) {
		super(SETTINGS_TITLE);
		this.parent = parent;
	}

	@Override
	protected void init() {
		int labelWidth = 200;
		int buttonWidth = 100;
		int centerX = this.width / 2;
		int fieldWidth = this.width - labelWidth - buttonWidth - 60;

		// title
		this.addDrawableChild(new TextWidget(centerX - 75, 20, 150, 20, SETTINGS_TITLE, this.textRenderer));

		// overworld shader
		this.addDrawableChild(new TextWidget(20, 70, labelWidth, 20, OVERWORLD_LABEL, this.textRenderer));

		overworldShaderField = new TextFieldWidget(this.textRenderer, labelWidth + 30, 70, fieldWidth, 20,
				Text.empty());
		overworldShaderField.setMaxLength(TEXT_FIELD_MAX_LENGTH);
		this.addDrawableChild(overworldShaderField);

		this.addDrawableChild(ButtonWidget.builder(
				Text.translatable("dimension_based_shaders.settings.get_current_button"),
				button -> overworldShaderField.setText(getPackName())
		).dimensions(labelWidth + fieldWidth + 40, 70, buttonWidth, 20).build());


		// nether shader
		this.addDrawableChild(new TextWidget(20, 100, labelWidth, 20, NETHER_LABEL, this.textRenderer));
		netherShaderField = new TextFieldWidget(this.textRenderer, labelWidth + 30, 100, fieldWidth, 20,
				Text.empty());
		netherShaderField.setMaxLength(TEXT_FIELD_MAX_LENGTH);
		this.addDrawableChild(netherShaderField);

		this.addDrawableChild(ButtonWidget.builder(
				Text.translatable("dimension_based_shaders.settings.get_current_button"),
				button -> netherShaderField.setText(getPackName())
		).dimensions(labelWidth + fieldWidth + 40, 100, buttonWidth, 20).build());


		// end shader
		this.addDrawableChild(new TextWidget(20, 130, labelWidth, 20, END_LABEL, this.textRenderer));
		endShaderField = new TextFieldWidget(this.textRenderer, labelWidth + 30, 130, fieldWidth, 20,
				Text.empty());
		endShaderField.setMaxLength(TEXT_FIELD_MAX_LENGTH);
		this.addDrawableChild(endShaderField);

		this.addDrawableChild(ButtonWidget.builder(
				Text.translatable("dimension_based_shaders.settings.get_current_button"),
				button -> endShaderField.setText(getPackName())
		).dimensions(labelWidth + fieldWidth + 40, 130, buttonWidth, 20).build());


		// cancel button
		this.addDrawableChild(ButtonWidget.builder(Text.translatable("dimension_based_shaders.settings.close_button"),
				button -> this.client.setScreen(this.parent)).dimensions(centerX - 155, this.height - 40, 150,
				20).build());
		// save button
		this.addDrawableChild(ButtonWidget.builder(Text.translatable("dimension_based_shaders.settings.save_button"),
				button -> {
					saveConfig();
					this.client.setScreen(this.parent);
				}).dimensions(centerX + 5, this.height - 40, 150,
				20).build());

		// restore
		try {
			Config config = getConfig();
			overworldShaderField.setText(config.overworldShader);
			netherShaderField.setText(config.netherShader);
			endShaderField.setText(config.endShader);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private void saveConfig() {
		try {
			File configDir = new File("config");
			if (!configDir.exists()) {
				configDir.mkdir();
			}

			Config config = new Config(
					overworldShaderField.getText(),
					netherShaderField.getText(),
					endShaderField.getText()
			);

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			FileWriter writer = new FileWriter("config/dimension_based_shaders.json");
			gson.toJson(config, writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Config getConfig() throws FileNotFoundException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		FileReader reader = new FileReader("config/dimension_based_shaders.json");
		return gson.fromJson(reader, Config.class);
	}

	public static class Config {
		public String overworldShader;
		public String netherShader;
		public String endShader;

		public Config(String overworldShader, String netherShader, String endShader) {
			this.overworldShader = overworldShader;
			this.netherShader = netherShader;
			this.endShader = endShader;
		}
	}

	private String getPackName() {
		return Iris.getCurrentPackName().equals("(off)") ? "" : Iris.getCurrentPackName();
	}
}
