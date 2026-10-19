package dev.sack.mixin;

import dev.sack.SackContainer;
import dev.sack.SackItem;
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
 * BTA 8.0.1 makes Item.onUseItem final, so an Item subclass cannot override it. Inject at the
 * start of this existing interaction method instead. Only SackItem instances are intercepted.
 */
@Mixin(value = Item.class, remap = false)
public abstract class SackItemUseMixin {
    @Inject(
        method = "onUseItem(Lnet/minecraft/core/item/ItemStack;Lnet/minecraft/core/world/World;Lnet/minecraft/core/entity/player/Player;)Lnet/minecraft/core/item/ItemStack;",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void sack$openOnUse(ItemStack stack, World world, Player player, CallbackInfoReturnable<ItemStack> cir) {
        if (!((Object) this instanceof SackItem)) return;

        SackMod.LOGGER.info("Sack use interaction reached Item.onUseItem (clientSide={})", world.isClientSide);
        if (!world.isClientSide) {
            SackMod.LOGGER.info("Opening the six-slot sack inventory.");
            player.displayContainerScreen(new SackContainer(stack));
        }
        // Prevent the regular item-use implementation from running for the Sack. The client/server
        // each process their side of the interaction; the screen is opened by the logical server.
        cir.setReturnValue(stack);
    }
}
