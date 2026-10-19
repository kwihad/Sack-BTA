package dev.sack;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.item.model.ItemModelDispatcher;
import net.minecraft.client.render.item.model.ItemModelStandard;
import turniplabs.halplibe.event.defs.ClientEvents;
import turniplabs.halplibe.util.dependency.Key;

public final class SackClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientEvents.ITEM_MODEL_RELOAD.listen(Key.of(SackMod.MOD_ID), this::registerModels);
    }

    private void registerModels(ItemModelDispatcher dispatcher) {
        dispatcher.addDispatch(new ItemModelStandard(SackItems.SACK));
    }
}
