package dev.sack;

import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.World;

/** A sack with its own six-slot portable inventory. */
public final class SackItem extends Item {
    public SackItem(int id) {
        super("sack", SackMod.MOD_ID + ":item/sack", id);
        // A sack must be unique per stack; otherwise contents could merge ambiguously.
        setMaxStackSize(1);
    }

    /**
     * BTA 8.0.1 routes use-in-air interactions through Item.onUseItem.
     * Open the container on the logical server, following BTA's portable-container pattern.
     */
    @Override
    public ItemStack onUseItem(ItemStack stack, World world, Player player) {
        SackMod.LOGGER.info("Sack onUseItem callback fired (clientSide={})", world.isClientSide);
        if (!world.isClientSide) {
            SackMod.LOGGER.info("Opening sack inventory on the server.");
            player.displayContainerScreen(new SackContainer(stack));
        }
        return stack;
    }
}
