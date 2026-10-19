package dev.sack;

import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.ContainerSimple;

/**
 * Six-slot backing inventory for a sack item. Every change is written to that specific item's NBT,
 * rather than to a shared/global inventory, so separate sacks keep separate contents.
 */
public final class SackContainer extends ContainerSimple {
    public static final int SLOT_COUNT = 6;
    public static final int SLOT_STACK_LIMIT = 16;
    private static final String DATA_KEY = "SackItems";
    private static final String SLOT_KEY = "Slot";

    private final ItemStack sackStack;
    private boolean loading;

    public SackContainer(ItemStack sackStack) {
        // Use BTA's built-in chest container identifier so the vanilla chest-style screen is selected.
        super("container.chest", SLOT_COUNT);
        this.sackStack = sackStack;
        loadFromStack();
    }

    @Override
    public int getMaxStackSize() {
        return SLOT_STACK_LIMIT;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOT_COUNT) return;
        // BTA 8's ContainerSimple does not expose canPlaceItem; guard the actual write instead.
        if (stack != null && stack.itemID == SackItems.SACK.id) return;
        if (stack != null && stack.stackSize > SLOT_STACK_LIMIT) stack.stackSize = SLOT_STACK_LIMIT;
        super.setItem(slot, stack);
        saveToStack();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = super.removeItem(slot, amount);
        saveToStack();
        return removed;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        saveToStack();
    }

    private void loadFromStack() {
        loading = true;
        try {
            CompoundTag itemTag = ((SackStackData) (Object) sackStack).sack$getExtraData();
            if (itemTag == null || !itemTag.containsKey(DATA_KEY)) return;

            ListTag items = itemTag.getList(DATA_KEY);
            for (int i = 0; i < items.tagCount(); i++) {
                CompoundTag entry = (CompoundTag) items.tagAt(i);
                int slot = entry.getByte(SLOT_KEY) & 0xFF;
                if (slot >= SLOT_COUNT) continue;

                ItemStack stored = ItemStack.readItemStackFromNbt(entry);
                if (stored == null || stored.itemID == SackItems.SACK.id) continue;
                // Keep the capacity invariant even if the item was edited or imported from bad NBT.
                if (stored.stackSize > SLOT_STACK_LIMIT) stored.stackSize = SLOT_STACK_LIMIT;
                super.setItem(slot, stored);
            }
        } finally {
            loading = false;
        }
        saveToStack();
    }

    private void saveToStack() {
        if (loading || sackStack == null) return;

        SackStackData stackData = (SackStackData) (Object) sackStack;
        CompoundTag itemTag = stackData.sack$getExtraData();
        if (itemTag == null) itemTag = new CompoundTag();

        ListTag items = new ListTag();
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack stored = super.getItem(slot);
            if (stored == null || stored.stackSize <= 0) continue;
            // Defensive check in case another path bypassed setItem.
            if (stored.itemID == SackItems.SACK.id) continue;
            if (stored.stackSize > SLOT_STACK_LIMIT) stored.stackSize = SLOT_STACK_LIMIT;

            CompoundTag entry = new CompoundTag();
            entry.putByte(SLOT_KEY, (byte) slot);
            stored.writeToNBT(entry);
            items.addTag(entry);
        }
        itemTag.put(DATA_KEY, items);
        stackData.sack$setExtraData(itemTag);
    }
}
