package net.mxb_683.dimension_based_shaders.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public interface WorldLoadCallback {
    Event<WorldLoadCallback> EVENT = EventFactory.createArrayBacked(WorldLoadCallback.class,
        (listeners) -> (player) -> {
            for (WorldLoadCallback listener : listeners) {
                ActionResult result = listener.handle(player);
                if (result != ActionResult.PASS) {
                    return result;
                }
            }
            return ActionResult.PASS;
        });

    ActionResult handle(PlayerEntity player);
}