package net.mxb_683.dimension_based_shaders.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.config.IrisConfig;
import net.minecraft.util.ActionResult;
import net.mxb_683.dimension_based_shaders.callback.WorldLoadCallback;
import net.mxb_683.dimension_based_shaders.modmenu.SettingsScreen;

import java.io.FileReader;
import java.io.IOException;

public class DimensionBasedShadersClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		WorldLoadCallback.EVENT.register(player -> {
			try {
				String dimensionName = Iris.getCurrentDimension().toString();
				IrisConfig irisConfig = Iris.getIrisConfig();
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				FileReader reader = new FileReader("config/dimension_based_shaders.json");
				SettingsScreen.Config config = gson.fromJson(reader, SettingsScreen.Config.class);
				reader.close();
				switch (dimensionName) {
					case "minecraft:overworld":
						irisConfig.setShadersEnabled(true);
						irisConfig.setShaderPackName(config.overworldShader);
						break;

					case "minecraft:the_nether":
						irisConfig.setShadersEnabled(true);
						irisConfig.setShaderPackName(config.netherShader);
						break;

					case "minecraft:the_end":
						irisConfig.setShadersEnabled(true);
						irisConfig.setShaderPackName(config.endShader);
						break;

					case "":
						irisConfig.setShadersEnabled(false);
						break;

					case null, default:
						break;
				}
				irisConfig.save();
				Iris.reload();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			return ActionResult.PASS;
		});
	}
}
