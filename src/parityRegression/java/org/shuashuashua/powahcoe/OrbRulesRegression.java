package org.shuashuashua.powahcoe;

import org.shuashuashua.powahcoe.util.OrbRules;
import org.shuashuashua.powahcoe.util.OrbInventoryMigration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import java.util.HashSet;
import java.util.Set;

public final class OrbRulesRegression {
    private static int checks;
    private static void equal(long expected, long actual, String message) {
        checks++;
        if (expected != actual) throw new AssertionError(message + ": expected " + expected + ", got " + actual);
    }
    public static void main(String[] args) {
        equal(19, OrbRules.INVENTORY_SIZE, "inventory size");
        equal(9, OrbRules.INPUT_END - OrbRules.INPUT_START, "input count");
        equal(9, OrbRules.OUTPUT_END, "output count");
        equal(1, OrbRules.targetBatches(false, 7, 1, 64), "no upgrade");
        for (int level = 1; level <= 7; level++) {
            equal(level * 64, OrbRules.targetBatches(true, level, 1, 64), "64-stack target");
            equal(level * 16, OrbRules.targetBatches(true, level, 4, 64), "multi-output recipe");
            equal(level * 22, OrbRules.targetBatches(true, level, 3, 64), "ceiling division");
            equal(level * 16, OrbRules.targetBatches(true, level, 1, 16), "16-stack item");
            equal(level, OrbRules.targetBatches(true, level, 1, 1), "unstackable item");
            equal(200000L * 5 * level, OrbRules.multiplyEnergy(200000L * 5, level), "x5 tier receive boost");
        }
        equal(0, OrbRules.targetBatches(true, 7, 0, 64), "empty result");
        equal(1, OrbRules.upgradeLevel(-1), "invalid upgrade");
        equal(1, OrbRules.upgradeLevel(8), "unknown upgrade");
        equal(Long.MAX_VALUE, OrbRules.multiplyEnergy(Long.MAX_VALUE, 7), "overflow guard");
        equal(0, OrbRules.multiplyEnergy(-1, 7), "negative energy");
        Set<Integer> slots = new HashSet<>();
        for (int old = 0; old < 13; old++) {
            int converted = OrbRules.legacySlot(old);
            equal(old < 6 ? old : old < 12 ? old + 3 : 18, converted, "legacy mapping");
            if (!slots.add(converted)) throw new AssertionError("migration overwrites a slot");
        }
        equal(13, slots.size(), "all old slots preserved");
        equal(-1, OrbRules.legacySlot(-1), "invalid old slot");
        equal(-1, OrbRules.legacySlot(13), "out-of-range old slot");
        equal(64, OrbRules.outputBatchesInSlot(1, 64, 0, true), "empty output");
        equal(4, OrbRules.outputBatchesInSlot(4, 64, 48, true), "partial matching output");
        equal(0, OrbRules.outputBatchesInSlot(4, 64, 62, true), "not enough room");
        equal(0, OrbRules.outputBatchesInSlot(1, 64, 10, false), "different output item");
        equal(0, OrbRules.outputBatchesInSlot(1, 64, 64, true), "full output");
        testMigration();
        System.out.println("PASS: " + checks + " parity regression checks");
    }

    private static void testMigration() {
        CompoundTag old = new CompoundTag();
        old.putInt("Size", 13);
        old.putBoolean("auto_eject", true);
        ListTag items = new ListTag();
        for (int slot = 0; slot < 13; slot++) {
            CompoundTag item = new CompoundTag();
            item.putInt("Slot", slot);
            item.putString("id", "test:item_" + slot);
            item.putByte("Count", (byte) (slot + 1));
            CompoundTag custom = new CompoundTag();
            custom.putString("custom", "preserve-me-" + slot);
            item.put("tag", custom);
            items.add(item);
        }
        old.put("Items", items);
        CompoundTag migrated = OrbInventoryMigration.upgrade(old);
        equal(19, migrated.getInt("Size"), "NBT size");
        equal(13, old.getInt("Size"), "source size untouched");
        equal(1, migrated.getBoolean("auto_eject") ? 1 : 0, "auto-eject preserved");
        ListTag newItems = migrated.getList("Items", Tag.TAG_COMPOUND);
        equal(13, newItems.size(), "all item entries preserved");
        for (int slot = 0; slot < 13; slot++) {
            CompoundTag item = newItems.getCompound(slot);
            equal(OrbRules.legacySlot(slot), item.getInt("Slot"), "NBT slot remap");
            equal(slot, items.getCompound(slot).getInt("Slot"), "source slot untouched");
            equal(slot + 1, item.getByte("Count"), "item count preserved");
            checks++;
            if (!item.getString("id").equals("test:item_" + slot)
                    || !item.getCompound("tag").getString("custom").equals("preserve-me-" + slot)) {
                throw new AssertionError("item identity or custom NBT changed");
            }
        }
        checks++;
        if (!migrated.equals(OrbInventoryMigration.upgrade(migrated))) {
            throw new AssertionError("second load migrates inventory twice");
        }
        CompoundTag off = old.copy();
        off.putBoolean("auto_eject", false);
        equal(0, OrbInventoryMigration.upgrade(off).getBoolean("auto_eject") ? 1 : 0, "OFF preserved");
        CompoundTag empty = new CompoundTag();
        empty.putInt("Size", 13);
        empty.put("Items", new ListTag());
        equal(0, OrbInventoryMigration.upgrade(empty).getList("Items", Tag.TAG_COMPOUND).size(), "empty old inventory");
    }
}
