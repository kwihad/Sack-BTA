package dev.sack;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.HalpLibe;
import turniplabs.halplibe.event.defs.CommonEvents;
import turniplabs.halplibe.helper.RecipeBuilder;
import turniplabs.halplibe.util.dependency.Key;

public final class SackMod implements ModInitializer {
    public static final String MOD_ID = HalpLibe.registerMod("sack", true);
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        CommonEvents.BEFORE_GAME_START.listen(Key.of(MOD_ID), SackItems::initialize);
        CommonEvents.RECIPES_NAMESPACE_INIT.listen(Key.of(MOD_ID), () -> RecipeBuilder.initNameSpace(MOD_ID));
        CommonEvents.RECIPES_READY.listen(Key.of(MOD_ID), SackRecipes::register);
        LOGGER.info("Sack has been initialized.");
    }
}
