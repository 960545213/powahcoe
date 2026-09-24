package org.shuashuashua.powahcoe.util;

/** Version-independent rules shared by the machine and regression checks. */
public final class OrbRules {
    public static final int OUTPUT_END = 9;
    public static final int INPUT_START = 9;
    public static final int INPUT_END = 18;
    public static final int UPGRADE_SLOT = 18;
    public static final int INVENTORY_SIZE = 19;

    private OrbRules() {}

    public static int upgradeLevel(int level) {
        return level >= 1 && level <= 7 ? level : 1;
    }

    public static int targetBatches(boolean upgraded, int level, int resultCount, int maxStack) {
        if (resultCount <= 0 || maxStack <= 0) return 0;
        if (!upgraded) return 1;
        return (int) Math.min(Integer.MAX_VALUE,
                (long) upgradeLevel(level) * (((long) maxStack + resultCount - 1) / resultCount));
    }

    public static int legacySlot(int slot) {
        if (slot >= 0 && slot < 6) return slot;
        if (slot >= 6 && slot < 12) return slot + 3;
        return slot == 12 ? UPGRADE_SLOT : -1;
    }

    public static int outputBatchesInSlot(int resultCount, int maxStack, int existingCount, boolean matching) {
        if (resultCount <= 0 || maxStack <= 0 || (existingCount > 0 && !matching)) return 0;
        return Math.max(0, maxStack - existingCount) / resultCount;
    }

    public static long multiplyEnergy(long value, int multiplier) {
        if (value <= 0 || multiplier <= 0) return 0;
        return value > Long.MAX_VALUE / multiplier ? Long.MAX_VALUE : value * multiplier;
    }
}
