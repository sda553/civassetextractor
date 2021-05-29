package ru.disasm.civ.lzw;

public class VgaPaletteEntry {
    private byte[] entry;

    VgaPaletteEntry(byte[] entry) {
        this.entry = new byte[3];
        this.entry[0] = (byte)((entry[0] << 2) | (entry[0] >> 4));
        this.entry[1] = (byte)((entry[1] << 2) | (entry[1] >> 4));
        this.entry[2] = (byte)((entry[2] << 2) | (entry[2] >> 4));
    }

    public byte[] getBytes() {
        return entry;
    }
}
