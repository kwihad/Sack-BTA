package dev.sack.mixin;

import com.mojang.nbt.tags.CompoundTag;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.sack.SackContainer;
import dev.sack.SackItems;
import dev.sack.SackStackData;

/**
 * BTA 8 does not expose the older ItemStack getTagCompound/setTagCompound API and the Item callback
 * signature differs from legacy Minecraft. Add a small payload to ItemStack's existing NBT read/write
 * paths and intercept the stack's right-click entry point instead.
 */
@Mixin(value = ItemStack.class, remap = false)
public abstract class SackItemStackMixin implements SackStackData {
    @Unique private static final String SACK_DATA_KEY = "SackModData";
    @Unique private CompoundTag sack$extraData;

    @Override
    public CompoundTag sack$getExtraData() {
        return sack$extraData;
    }

    @Override
    public void sack$setExtraData(CompoundTag data) {
        sack$extraData = data;
    }

    @Inject(method = "writeToNBT", at = @At("RETURN"), remap = false)
    private void sack$writeExtraData(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.itemID == SackItems.SACK.id && sack$extraData != null) {
            tag.put(SACK_DATA_KEY, sack$extraData);
        }
    }

    @Inject(method = "readItemStackFromNbt", at = @At("RETURN"), remap = false)
    private static void sack$readExtraData(CompoundTag tag, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = cir.getReturnValue();
        if (stack != null && stack.itemID == SackItems.SACK.id && tag.containsKey(SACK_DATA_KEY)) {
            ((SackStackData) (Object) stack).sack$setExtraData(tag.getCompound(SACK_DATA_KEY));
        }
    }

    @Inject(method = "useItemRightClick", at = @At("HEAD"), cancellable = true, remap = false)
    private void sack$openScreen(World world, Player player, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.itemID != SackItems.SACK.id) return;

        if (!world.isClientSide) {
            player.displayContainerScreen(new SackContainer(self));
        }
        cir.setReturnValue(self);
    }

    @Inject(method = "copy", at = @At("RETURN"), remap = false, require = 0)
    private void sack$copyExtraData(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack copy = cir.getReturnValue();
        if (copy != null && sack$extraData != null) {
            ((SackStackData) (Object) copy).sack$setExtraData(sack$extraData);
        }
    }

    @Inject(method = "copyItemStack", at = @At("RETURN"), remap = false, require = 0)
    private void sack$copyExtraDataLegacy(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack copy = cir.getReturnValue();
        if (copy != null && sack$extraData != null) {
            ((SackStackData) (Object) copy).sack$setExtraData(sack$extraData);
        }
    }
}
