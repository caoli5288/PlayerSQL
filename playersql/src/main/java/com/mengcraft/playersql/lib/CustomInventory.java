package com.mengcraft.playersql.lib;

import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
public class CustomInventory implements InventoryHolder {

    private Inventory inventory;
    private Consumer<Inventory> onClose;
    private final Function<InventoryHolder, Inventory> onCreate;

    @Override
    public Inventory getInventory() {
        if (inventory == null) {
            inventory = Objects.requireNonNull(onCreate, "onCreate()").apply(this);
        }
        return inventory;
    }

    public CustomInventory onClose(Consumer<Inventory> onClose) {
        this.onClose = onClose;
        return this;
    }

    public void close() {
        onClose.accept(getInventory());
    }

    public static CustomInventory onCreate(Function<InventoryHolder, Inventory> onCreate) {
        return new CustomInventory(onCreate);
    }

    public static boolean isInstance(Inventory inventory) {
        return inventory.getHolder() instanceof CustomInventory;
    }

    public static void close(Inventory inventory) {
        ((CustomInventory) inventory.getHolder()).close();
    }
}
