package com.mengcraft.playersql.lib;

import net.jpountz.lz4.LZ4Factory;

public class LZ4 {

    public static byte[] compress(byte[] input) {
        return LZ4Factory.fastestInstance().highCompressor().compress(input);
    }

    public static byte[] decompress(byte[] input, int uncompressedLength) {
        return LZ4Factory.fastestInstance().fastDecompressor().decompress(input, uncompressedLength);
    }
}
