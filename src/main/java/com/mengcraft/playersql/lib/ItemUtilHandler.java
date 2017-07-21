package com.mengcraft.playersql.lib;

import com.mengcraft.playersql.lib.ItemUtil.NMS;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

import static com.mengcraft.playersql.PluginMain.nil;
import static com.mengcraft.playersql.PluginMain.thr;


public class ItemUtilHandler {

    private static ItemUtil util;
    private final Plugin plugin;
    private String version;
    private ItemStack item;

    public ItemUtilHandler(Plugin plugin) {
        thr(nil(plugin), "null");
        this.plugin = plugin;
    }

    public ItemUtil handle() {
        if (nil(util)) {
            util = validNMS(version());
            if (nil(util)) {
                Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin("ProtocolLib");
                if (!nil(plugin)) {
                    util = validPLib();
                }
            }
            thr(nil(util), "Hasn't compatible util! Update PlayerSQL or ProtocolLib");
            plugin.getLogger().info("Bukkit " + version());
            plugin.getLogger().info("Item util " + util.id());
        }
        return util;
    }

    private ItemUtil validPLib() {
        ItemUtil util = new ItemUtil.PLib();
        try {
            if (item().equals(util.convert(util.convert(item())))) {
                return util;
            }
        } catch (Exception e) {
            plugin.getLogger().warning(e.toString());
        }
        return null;
    }

    private ItemUtil validNMS(String version) {
        ItemUtil util = new NMS(version);
        try {
            if (item().equals(util.convert(util.convert(item())))) {
                return util;
            }
        } catch (Exception e) {
            plugin.getLogger().warning(e.toString());
        }
        return null;
    }

    private ItemStack item() {
        if (item == null) {
            item = new ItemStack(Material.DIAMOND_SWORD);

            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("valid");
            meta.setLore(Arrays.asList("a", "b", "c"));

            item.setItemMeta(meta);
            item.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 0);
        }
        return item;
    }

    private String version() {
        if (nil(version)) {
            version = Bukkit.getServer().getClass().getName().split("\\.")[3];
        }
        return version;
    }

}
