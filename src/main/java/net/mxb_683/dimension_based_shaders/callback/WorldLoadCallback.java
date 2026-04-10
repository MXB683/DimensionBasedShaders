package net.mxb_683.dimension_based_shaders.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public interface WorldLoadCallback {
    Event<WorldLoadCallback> EVENT = EventFactory.createArrayBacked(WorldLoadCallback.class,
        (listeners) -> (player) -> {
            for (WorldLoadCallback listener : listeners) {
                InteractionResult result = listener.handle(player);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
            return InteractionResult.PASS;
        });

    InteractionResult handle(Player player);
}