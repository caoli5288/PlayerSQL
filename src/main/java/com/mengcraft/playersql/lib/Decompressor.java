package com.mengcraft.playersql.lib;

import io.netty.util.Recycler;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

public class Decompressor implements Closeable {

    private static final Recycler<Decompressor> RECYCLER = new Recycler<Decompressor>() {
        @Override
        protected Decompressor newObject(Handle<Decompressor> handle) {
            return new Decompressor(handle);
        }
    };

    private final Recycler.Handle<Decompressor> handle;
    private final Inflater inflater = new Inflater();
    private final ByteArrayOutputStream buf = new ByteArrayOutputStream();
    private final InflaterOutputStream decompressor = new InflaterOutputStream(buf, inflater);

    private Decompressor(Recycler.Handle<Decompressor> handle) {
        this.handle = handle;
    }

    @SneakyThrows
    public static byte[] decompress(byte[] input) {
        try (Decompressor decompressor = RECYCLER.get()) {
            return decompressor._decompress(input);
        }
    }

    @SneakyThrows
    private byte[] _decompress(byte[] input) {
        decompressor.write(input);
        decompressor.finish();
        return buf.toByteArray();
    }

    @Override
    public void close() {
        buf.reset();
        inflater.reset();
        handle.recycle(this);
    }

}
