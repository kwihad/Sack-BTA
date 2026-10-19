package dev.sack;

import net.minecraft.core.item.Item;
import turniplabs.halplibe.helper.ItemBuilder;
import turniplabs.halplibe.helper.creativeInventory.CreativeInventoryCategory;
import turniplabs.halplibe.helper.creativeInventory.CreativeInventoryPlacement;

/** Item registration for Sack. */
public final class SackItems {
    private SackItems() {}

    private static final int SACK_ITEM_ID = 18000;

    // Register the Sack in BTA's creative inventory under Drops, following the same
    // HalpLibe placement API used by Mo' Creatures BTA 8.0.1.
    private static final ItemBuilder SACK_BUILDER = new ItemBuilder(SackMod.MOD_ID)
        .setCreativeInventoryPlacement(
            new CreativeInventoryPlacement.Category(CreativeInventoryCategory.DROPS)
        );

    public static final Item SACK = SACK_BUILDER.build(new SackItem(SACK_ITEM_ID));

    /** Referenced by the early-game event to ensure this class is initialized at the right time. */
    public static void initialize() {
        // Static registration above is the complete initialization.
    }
}
