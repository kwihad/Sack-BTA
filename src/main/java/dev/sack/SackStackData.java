package dev.sack;

import com.mojang.nbt.tags.CompoundTag;

/** Extra per-stack data used to persist the Sack inventory on BTA versions without ItemStack tag accessors. */
public interface SackStackData {
    CompoundTag sack$getExtraData();
    void sack$setExtraData(CompoundTag data);
}
