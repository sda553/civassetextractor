package ru.disasm.civ.lzw;

class LzwEntry {
    private LzwEntry prefix;
    private byte suffix;

    LzwEntry getPrefix() {
        return prefix;
    }

    void setPrefix(LzwEntry prefix) {
        this.prefix = prefix;
    }

    byte getSuffix() {
        return suffix;
    }

    void setSuffix(byte suffix) {
        this.suffix = suffix;
    }
}
