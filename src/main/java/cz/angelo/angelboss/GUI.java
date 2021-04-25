package cz.angelo.angelboss;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUI {

	public static void openGUI(Player player){
		Inventory inv = Bukkit.createInventory(null, Config.getConfig().getInt("gui.size"),
				Main.color(Config.getConfig().getString("gui.title")));
		ConfigurationSection arenas = Config.getConfig().getConfigurationSection("gui.items");
		for (String arena : arenas.getKeys(false)){
			Material material = Material.valueOf(Config.getConfig().getString("gui.items." + arena + ".material"));
			ItemStack itemStack = new ItemStack(material);
			ItemMeta itemMeta = itemStack.getItemMeta();
			String name = Config.getConfig().getString("gui.items." + arena + ".name");
			itemMeta.setDisplayName(Main.color(name));
			List<String> lores = Config.getConfig().getStringList("gui.items." + arena + ".lore");
			List<String> lore = new ArrayList<>();
			for (String s : lores){
				lore.add(Main.color(s));
			}
			itemMeta.setLore(lore);
			itemStack.setItemMeta(itemMeta);
			int slot = Config.getConfig().getInt("gui.items." + arena + ".slot");
			inv.setItem(slot, itemStack);
			player.openInventory(inv);
		}
	}

}
