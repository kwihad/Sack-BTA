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
     * BTA routes held-item right clicks through Item.onItemRightClick. Open the container
     * on the logical server, matching the supported pattern used by BTA's own container users.
     */
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, Player player) {
        SackMod.LOGGER.info("Sack right-click callback fired (clientSide={})", world.isClientSide);
        if (!world.isClientSide) {
            SackMod.LOGGER.info("Opening sack inventory on the server.");
            player.displayContainerScreen(new SackContainer(stack));
        }
        return stack;
    }
}
