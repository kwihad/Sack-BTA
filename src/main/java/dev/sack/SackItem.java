package dev.sack;

import net.minecraft.core.item.Item;

/** A sack's interaction and per-stack data hooks are supplied by SackItemStackMixin. */
public final class SackItem extends Item {
    public SackItem(int id) {
        super("sack", SackMod.MOD_ID + ":item/sack", id);
        // A sack must be unique per stack; otherwise contents could be merged ambiguously.
        setMaxStackSize(1);
    }
}
