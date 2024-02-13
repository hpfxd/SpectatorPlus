package com.hpfxd.spectatorplus.fabric.client.sync;

import com.hpfxd.spectatorplus.fabric.sync.PositionEntry;

public class PositionRecord {
    private final long clientTick;
    private final PositionEntry[] entries;

    public PositionRecord(long clientTick, PositionEntry[] entries) {
        this.clientTick = clientTick;
        this.entries = entries;
    }

    public long getClientTick() {
        return this.clientTick;
    }

    public PositionEntry getInterpolatedEntry(float partialTicks) {
        if (this.entries == null || this.entries.length == 0) {
            return null;
        }

        if (this.entries.length == 1) {
            return this.entries[0];
        }

        int lowerIndex = this.getLowerIndex(partialTicks);

        // no higher entry exists, return highest
        if (lowerIndex >= this.entries.length - 1) {
            return this.entries[this.entries.length - 1];
        }

        // make sure index is at least 0
        if (lowerIndex < 0) {
            lowerIndex = 0;
        }

        final PositionEntry lower = this.entries[lowerIndex];
        final PositionEntry higher = this.entries[lowerIndex + 1];
        return PositionEntry.interpolate(partialTicks, lower, higher);
    }

    private int getLowerIndex(float partialTicks) {
        return binarySearch(this.entries, partialTicks);
    }

    /**
     * Adapted from {@link java.util.Arrays#binarySearch(float[], float)} to work on a {@link PositionEntry} array, and
     * ignore checking if values are equal, because they almost never will be.
     *
     * @return The index of the array with the value closest to the supplied partialTicks.
     */
    private static int binarySearch(PositionEntry[] entries, float partialTicks) {
        int low = 0;
        int high = entries.length - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            float midVal = entries[mid].partialTicks();

            if (midVal < partialTicks) {
                low = mid + 1;
            } else if (midVal > partialTicks) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        return low - 1;
    }
}
