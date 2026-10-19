package dev.sack.mixin;

import dev.sack.SackContainer;
import dev.sack.SackItems;
import dev.sack.SackMod;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * BTA 8.0.1's Item.onUseItem(ItemStack, World, Player) is final, so a custom Item subclass
 * cannot override it. Inject at the actual item-use entry point instead of ItemStack's
 * useItemRightClick wrapper (which the user's in-game test showed was not reached).
 */
@Mixin(value = Item.class, remap = false)
public abstract class SackItemUseMixin {
    @Inject(method = "onUseItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void sack$openInventoryOnUse(ItemStack stack, World world, Player player,
                                          CallbackInfoReturnable<ItemStack> cir) {
        if (stack == null || stack.itemID != SackItems.SACK.id) {
            return;
        }

        SackMod.LOGGER.info("Sack: Item.onUseItem reached (clientSide={})", world.isClientSide);

        // Opening a container must be initiated by the logical server so slot clicks and
        // stack changes are synchronized using BTA's normal container protocol.
        if (world.isClientSide) {
            return;
        }

        SackMod.LOGGER.info("Sack: opening six-slot container on server.");
        player.displayContainerScreen(new SackContainer(stack));
        cir.setReturnValue(stack);
    }
}
