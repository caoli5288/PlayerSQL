package com.mengcraft.playersql.lib;

import io.netty.util.Recycler;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class Compressor implements Closeable {

    private static final Recycler<Compressor> RECYCLER = new Recycler<Compressor>() {
        @Override
        protected Compressor newObject(Handle<Compressor> handle) {
            return new Compressor(handle);
        }
    };

    private final Deflater deflater = new Deflater(3);
    private final ByteArrayOutputStream buf = new ByteArrayOutputStream();
    private final DeflaterOutputStream compressor = new DeflaterOutputStream(buf, deflater);
    private final Recycler.Handle<Compressor> handle;

    public Compressor(Recycler.Handle<Compressor> handle) {
        this.handle = handle;
    }

    public static byte[] compress(byte[] input) {
        try (Compressor compressor = RECYCLER.get()) {
            return compressor._compress(input);
        }
    }

    @SneakyThrows
    private byte[] _compress(byte[] input) {
        compressor.write(input);
        compressor.finish();
        return buf.toByteArray();
    }

    @Override
    public void close() {
        buf.reset();
        deflater.reset();
        handle.recycle(this);
    }
}
