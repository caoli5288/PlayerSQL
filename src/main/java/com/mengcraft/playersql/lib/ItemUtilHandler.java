package com.mengcraft.playersql.lib;

import com.mengcraft.playersql.PluginMain;
import com.mengcraft.playersql.lib.ItemUtil.Simple;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

import static com.mengcraft.playersql.PluginMain.nil;


public class ItemUtilHandler {

    private final Plugin proxy;
    private final Server server;

    private ItemUtil util;
    private String version;
    private ItemStack item;

    public ItemUtilHandler(Plugin in) {
        if (in == null) {
            throw new NullPointerException();
        }
        this.proxy = in;
        this.server = in.getServer();
    }

    public ItemUtil handle() {
        if (util == null) {
            Plugin plugin = proxy.getServer().getPluginManager().getPlugin("ProtocolLib");
            if (!nil(plugin) && testPLib()) {
                util = new ItemUtil.PLib();
            }
            if (nil(util)) {
                if (testBuildIn(version())) {
                    util = new Simple(version());
                } else {
                    throw new RuntimeException("Hasn't compatible util! Update plugin or use compatible ProtocolLib");
                }
            }
        }
        return util;
    }

    private boolean testPLib() {
        try {
            ItemUtil util = new ItemUtil.PLib();

            if (item().equals(util.convert(util.convert(item())))) {
                proxy.getLogger().info("Server version: " + version + '.');
                proxy.getLogger().info("ProtocolLib item util work well!");
                return true;
            }
        } catch (Exception e) {
            proxy.getLogger().warning(e.toString());
            PluginMain.bug.notify(e);
        }
        return false;
    }

    private boolean testBuildIn(String version) {
        try {
            ItemUtil util = new Simple(version);

            if (item().equals(util.convert(util.convert(item())))) {
                proxy.getLogger().info("Server version: " + version + '.');
                proxy.getLogger().info("Build-in item util work well!");

                return true;
            }
        } catch (Exception e) {
            proxy.getLogger().warning(e.toString());
            PluginMain.bug.notify(e);
        }
        return false;
    }

    private ItemStack item() {
        if (item == null) {
            item = new ItemStack(Material.DIAMOND_SWORD);

            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("test");
            meta.setLore(Arrays.asList("a", "b", "c"));

            item.setItemMeta(meta);
            item.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 0);
        }
        return item;
    }

    private String version() {
        if (version == null) {
            version = server.getClass().getName().split("\\.")[3];
        }
        return version;
    }

}
