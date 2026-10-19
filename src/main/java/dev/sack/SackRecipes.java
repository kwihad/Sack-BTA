package dev.sack;

import net.minecraft.core.item.Items;
import turniplabs.halplibe.helper.RecipeBuilder;

/** A small, deliberately simple recipe that fits BTA's early-game crafting style. */
public final class SackRecipes {
    private SackRecipes() {}

    public static void register() {
        RecipeBuilder.Shapeless(SackMod.MOD_ID)
            .addInput(Items.LEATHER)
            .addInput(Items.LEATHER)
            .addInput(Items.LEATHER)
            .addInput(Items.STRING)
            .create("sack", SackItems.SACK.getDefaultStack());
    }
}
