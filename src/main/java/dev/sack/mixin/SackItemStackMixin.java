package dev.sack.mixin;

import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.sack.SackContainer;
import dev.sack.SackMod;
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
    @Unique private static final String SACK_ITEMS_KEY = "SackItems";
    @Unique private static final String SACK_SLOT_KEY = "Slot";
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

        // Do not cancel the client-side use call. In BTA, cancelling the local ItemStack
        // callback can prevent the normal item-use request from reaching the logical server,
        // where displayContainerScreen must be opened and synchronized.
        if (world.isClientSide) return;

        SackMod.LOGGER.info("Opening sack inventory on the server.");
        player.displayContainerScreen(new SackContainer(self));
        cir.setReturnValue(self);
    }

    @Inject(method = "copy", at = @At("RETURN"), remap = false, require = 0)
    private void sack$copyExtraData(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack copy = cir.getReturnValue();
        if (copy != null && sack$extraData != null) {
            ((SackStackData) (Object) copy).sack$setExtraData(sack$copyExtraData(sack$extraData));
        }
    }

    @Inject(method = "copyItemStack", at = @At("RETURN"), remap = false, require = 0)
    private static void sack$copyExtraDataLegacy(ItemStack original, CallbackInfoReturnable<ItemStack> cir) {
        // ItemStack.copyItemStack is static in BTA 8.0.1, so its injection callback must be static too.
        // The normal instance copy() hook handles most copies; this covers callers of the static helper.
        if (original == null || original.itemID != SackItems.SACK.id) return;

        ItemStack copy = cir.getReturnValue();
        if (copy == null) return;

        CompoundTag originalData = ((SackStackData) (Object) original).sack$getExtraData();
        if (originalData != null) {
            // Do not share the same mutable NBT object between two sack stacks.
            ((SackStackData) (Object) copy).sack$setExtraData(sack$copyExtraData(originalData));
        }
    }


    /**
     * Deep-copies this mod's payload without relying on CompoundTag.copy(), which is not available
     * in the BTA 8.0.1 NBT API. Each saved item is round-tripped through ItemStack's NBT methods.
     */
    @Unique
    private static CompoundTag sack$copyExtraData(CompoundTag source) {
        if (source == null) return null;

        CompoundTag result = new CompoundTag();
        if (!source.containsKey(SACK_ITEMS_KEY)) return result;

        ListTag sourceItems = source.getList(SACK_ITEMS_KEY);
        ListTag copiedItems = new ListTag();
        for (int i = 0; i < sourceItems.tagCount(); i++) {
            Object rawEntry = sourceItems.tagAt(i);
            if (!(rawEntry instanceof CompoundTag)) continue;

            CompoundTag entry = (CompoundTag) rawEntry;
            ItemStack stored = ItemStack.readItemStackFromNbt(entry);
            if (stored == null || stored.stackSize <= 0) continue;

            CompoundTag copiedEntry = new CompoundTag();
            copiedEntry.putByte(SACK_SLOT_KEY, entry.getByte(SACK_SLOT_KEY));
            stored.writeToNBT(copiedEntry);
            copiedItems.addTag(copiedEntry);
        }
        result.put(SACK_ITEMS_KEY, copiedItems);
        return result;
    }
}
