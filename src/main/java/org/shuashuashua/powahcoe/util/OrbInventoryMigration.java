package org.shuashuashua.powahcoe.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public final class OrbInventoryMigration {
    public static final String VERSION_KEY = "powahcoe_inventory_version";

    private OrbInventoryMigration() {}

    /** Migrate old Powah Inventory NBT once, without changing the caller's tag. */
    public static CompoundTag upgrade(CompoundTag source) {
        CompoundTag tag = source.copy();
        if (tag.getInt(VERSION_KEY) == 0 && tag.getInt("Size") == 13) {
            ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
            for (int i = 0; i < items.size(); i++) {
                CompoundTag item = items.getCompound(i);
                item.putInt("Slot", OrbRules.legacySlot(item.getInt("Slot")));
            }
        }
        tag.putInt("Size", OrbRules.INVENTORY_SIZE);
        tag.putInt(VERSION_KEY, 1);
        return tag;
    }
}
