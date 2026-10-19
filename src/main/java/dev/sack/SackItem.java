package dev.sack;

import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.World;

/** A portable six-slot inventory, with contents stored on each individual item stack. */
public final class SackItem extends Item {
    public SackItem(int id) {
        super("sack", SackMod.MOD_ID + ":item/sack", id);
        // A sack must be unique per stack; otherwise inventory contents could be merged ambiguously.
        setMaxStackSize(1);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, Player player) {
        // Open only on the logical server, matching BTA's existing portable-container patterns.
        if (!world.isClientSide) {
            player.displayContainerScreen(new SackContainer(stack));
        }
        return stack;
    }
}
