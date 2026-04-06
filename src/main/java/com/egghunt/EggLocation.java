package com.egghunt;

public record EggLocation(int x, int y, int z, boolean gold, String land, String warp, String hint) {

    public static EggLocation pink(int x, int y, int z, String land, String warp, String hint) {
        return new EggLocation(x, y, z, false, land, warp, hint);
    }

    public static EggLocation gold(int x, int y, int z, String land, String warp, String hint) {
        return new EggLocation(x, y, z, true, land, warp, hint);
    }
}
