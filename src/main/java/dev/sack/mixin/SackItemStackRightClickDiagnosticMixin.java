package dev.sack.mixin;

import dev.sack.SackMod;
import dev.sack.SackItems;
import net.minecraft.core.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Diagnostic only: records whether BTA's held ItemStack right-click path is reached.
 * It intentionally does not cancel or modify the call.
 */
@Mixin(value = ItemStack.class, remap = false)
public abstract class SackItemStackRightClickDiagnosticMixin {
    @Inject(method = "useItemRightClick", at = @At("HEAD"), remap = false, require = 0)
    private void sack$diagnoseStackRightClick(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.itemID != SackItems.SACK.id) return;
        SackMod.LOGGER.info("SACK_DIAGNOSTIC: Sack ItemStack.useItemRightClick reached; itemId={}, count={}",
            stack.itemID, stack.stackSize);
    }
}
