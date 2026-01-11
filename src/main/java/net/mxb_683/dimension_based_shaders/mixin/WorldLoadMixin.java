package net.mxb_683.dimension_based_shaders.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientChunkLoadProgress;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ActionResult;
import net.mxb_683.dimension_based_shaders.callback.WorldLoadCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientChunkLoadProgress.class)
public class WorldLoadMixin {
	@Inject(method = "startWorldLoading", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientChunkLoadProgress$Start;<init>(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/render/WorldRenderer;J)V"), cancellable = true)
	private void onWorldStartLoading(ClientPlayerEntity player, ClientWorld world, WorldRenderer renderer,
																	 CallbackInfo ci) {
		ActionResult result = WorldLoadCallback.EVENT.invoker().handle(player);
		if (result == ActionResult.FAIL) {
			ci.cancel();
		}
	}
}
