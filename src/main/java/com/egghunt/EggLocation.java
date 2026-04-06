package com.egghunt;

public record EggLocation(int x, int y, int z, boolean gold, String warp, String hint) {

    public static EggLocation pink(int x, int y, int z, String warp, String hint) {
        return new EggLocation(x, y, z, false, warp, hint);
    }

    public static EggLocation gold(int x, int y, int z, String warp, String hint) {
        return new EggLocation(x, y, z, true, warp, hint);
    }
}
