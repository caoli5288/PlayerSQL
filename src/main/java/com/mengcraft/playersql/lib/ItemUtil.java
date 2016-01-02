package com.mengcraft.playersql.lib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public interface ItemUtil {

    String convert(ItemStack in) throws Exception;

    ItemStack convert(String in) throws Exception;

    class WarpedItemUtil implements ItemUtil {
        
        private final String path = "com.comphenix.protocol.utility.StreamSerializer";
        
        private Object handle;
        
        private Class<?> serializer;

        private Method serializeItemStack;
        private Method deserializeItemStack;

        @Override
        public String convert(ItemStack in) throws Exception {
            if (handle == null) {
                serializer = getClass().getClassLoader().loadClass(path);
                handle = serializer.newInstance();
            }
            return serializeItemStack(in);
        }

        private String serializeItemStack(ItemStack in) throws Exception {
            if (serializeItemStack == null) {
                serializeItemStack = serializer.getMethod(
                        "serializeItemStack", ItemStack.class
                        );
            }
            return (String) serializeItemStack.invoke(handle, in);
        }

        @Override
        public ItemStack convert(String in) throws Exception {
            if (handle == null) {
                serializer = getClass().getClassLoader().loadClass(path);
                handle = serializer.newInstance();
            }
            return deserializeItemStack(in);
        }

        private ItemStack deserializeItemStack(String in) throws Exception {
            if (deserializeItemStack == null) {
                deserializeItemStack = serializer.getMethod(
                        "deserializeItemStack", String.class
                        );
            }
            return (ItemStack) deserializeItemStack.invoke(handle, in);
        }

    }
    
    @SuppressWarnings("all")
    public static class SimpleItemUtil implements ItemUtil {

        private final String cb;
        private final String nms;

        private Method save;
        private Method write;
        private Method load;
        private Method create;

        private Class craftItemStack;
        private Class nbtTagCompound;
        private Class nbtReadLimiter;
        private Class itemStack;

        private Object a;
        
        private Constructor copy;
        private Constructor mirror;
        
        private Field handle;

        @Override
        public String convert(ItemStack in) throws Exception {
            if (craftItemStack == null) {
                initialize();
            }
            return write(save(handle(copy(in))));
        }

        private Object handle(Object stack) throws Exception {
            if (handle == null) {
                handle = craftItemStack.getDeclaredField("handle");
                handle.setAccessible(true);
            }
            return handle.get(stack);
        }

        @Override
        public ItemStack convert(String in) throws Exception {
            if (craftItemStack == null) {
                initialize();
            }
            return mirror(create(load(in)));
        }

        private ItemStack mirror(Object proto) throws Exception {
            if (mirror == null) {
                mirror = craftItemStack.getDeclaredConstructor(itemStack);
                mirror.setAccessible(true);
            }
            return (ItemStack) mirror.newInstance(proto);
        }

        private void initialize() throws Exception {
            nbtTagCompound = find(nms + ".NBTTagCompound");
            nbtReadLimiter = find(nms + ".NBTReadLimiter");
            itemStack = find(nms + ".ItemStack");
            craftItemStack = find(cb + ".inventory.CraftItemStack");
        }

        private Object copy(ItemStack in) throws Exception {
            if (copy == null) {
                copy = craftItemStack.getDeclaredConstructor(ItemStack.class);
                copy.setAccessible(true);
            }
            return craftItemStack.isInstance(in) ? in : copy.newInstance(in);
        }

        private Object create(Object tag) throws Exception {
            if (create == null) {
                create = itemStack.getMethod("createStack", nbtTagCompound);
            }
            return create.invoke(itemStack, tag);
        }

        private Object load(String in) throws Exception {
            if (load == null) {
                load = nbtTagCompound.getDeclaredMethod("load",
                        DataInput.class, int.class, nbtReadLimiter
                        );
                load.setAccessible(true);
            }
            DataInput input = new DataInputStream(
                    new ByteArrayInputStream(Base64Coder.decode(in))
                    );
            Object tag = nbtTagCompound.newInstance();
            load.invoke(tag, input, 0, a());
            return tag;
        }

        private Object a() throws Exception {
            if (a == null) {
                a = nbtReadLimiter.getField("a").get(nbtReadLimiter);
            }
            return a;
        }

        private String write(Object tags) throws Exception {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (write == null) {
                write = nbtTagCompound.getDeclaredMethod("write",
                        DataOutput.class
                        );
                write.setAccessible(true);
            }
            write.invoke(tags, new DataOutputStream(out));
            return new String(Base64Coder.encode(out.toByteArray()));
        }

        private Object save(Object item) throws Exception {
            Object output = nbtTagCompound.newInstance();
            if (save == null) {
                save = itemStack.getMethod("save", nbtTagCompound);
            }
            save.invoke(item, output);
            return output;
        }

        private Class find(String path) throws Exception {
            return getClass().getClassLoader().loadClass(path);
        }

        SimpleItemUtil(String version) {
            this.cb = "org.bukkit.craftbukkit." + version;
            this.nms = "net.minecraft.server." + version;
        }

    }
}
