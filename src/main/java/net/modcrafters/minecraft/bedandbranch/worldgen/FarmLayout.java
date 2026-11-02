package net.modcrafters.minecraft.bedandbranch.worldgen;

/** Small, rotatable farm footprints. R = rice, E = empty support, M = mud,
 * I = irrigation, L = loam, P = path, F = broken fence, C = ruin, O = post, W = creek, B = bridge. */
public enum FarmLayout {
    POCKET_PADDY("pocket_paddy", 400,
            ".FF..", ".RMR.", ".EIM.", ".MRL.", "..P.."),
    SPLIT_BEDS("split_beds", 800,
            ".FF.F..", ".RMPMR.", ".ERPMRM", ".MIPIR.",
            ".RMPMR.", ".L.P.L.", "...P..."),
    CREEKSIDE_PLOT("creekside_plot", 1200,
            "W..FFF...", "W.PRMRMR.", "W.PMMMIE.", "W.P...MR.",
            "W.P.L.MR.", "BPPPPPME.", "W.....FF."),
    TERRACED_PATCH("terraced_patch", 1000,
            ".FF....F.", ".RRMRR.P.", ".MRIRM.P.", ".RRMRM.P.",
            ".CCCCCCP.", ".RMRRM.P.", ".REIRE.P.", ".RMRML.P.", ".FF.PPP.."),
    RUINED_FARMSTEAD("ruined_farmstead", 1600,
            ".FFF...F.", ".RMRMRME.", ".MEMIMRR.", ".RM.OCC..", ".ER.C.P..",
            ".RI.C.O..", ".MR.P.LL.", ".EM.P.LL.", ".FF.P....");

    private final String id;
    private final int rarity;
    private final String[] rows;

    FarmLayout(String id, int rarity, String... rows) {
        this.id = id;
        this.rarity = rarity;
        this.rows = rows;
        for (String row : rows) {
            if (row.length() != rows[0].length()) throw new IllegalArgumentException(id);
        }
    }

    public String id() { return id; }
    public int rarity() { return rarity; }
    public int width() { return rows[0].length(); }
    public int depth() { return rows.length; }
    public char cell(int x, int z) { return rows[z].charAt(x); }
    public int elevation(int x, int z) { return this == TERRACED_PATCH && z < 4 ? 1 : 0; }
    public boolean creek(int x, int z) { return cell(x, z) == 'W' || cell(x, z) == 'B'; }

    /** Integer quarter turns about the footprint center; all layouts have odd dimensions. */
    public int worldX(int x, int z, int rotation) {
        int dx = x - width() / 2, dz = z - depth() / 2;
        return switch (rotation & 3) { case 0 -> dx; case 1 -> -dz; case 2 -> -dx; default -> dz; };
    }

    public int worldZ(int x, int z, int rotation) {
        int dx = x - width() / 2, dz = z - depth() / 2;
        return switch (rotation & 3) { case 0 -> dz; case 1 -> dx; case 2 -> -dz; default -> -dx; };
    }
}
