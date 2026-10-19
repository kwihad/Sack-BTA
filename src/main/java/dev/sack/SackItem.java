package dev.sack;

import net.minecraft.core.item.Item;

/** A sack with its own six-slot portable inventory. Use handling is injected into Item.onUseItem,
 * because that BTA 8.0.1 method is final and cannot be overridden by an Item subclass. */
public final class SackItem extends Item {
    public SackItem(int id) {
        super("sack", SackMod.MOD_ID + ":item/sack", id);
        // A sack must be unique per stack; otherwise contents could merge ambiguously.
        setMaxStackSize(1);
    }
}
