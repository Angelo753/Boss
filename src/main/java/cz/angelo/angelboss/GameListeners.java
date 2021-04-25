package cz.angelo.angelboss;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.Map.Entry;

public class GameListeners implements Listener {

	@EventHandler
	public void invClick(InventoryClickEvent event){
		String title = event.getView().getTitle();
		Player player = (Player) event.getWhoClicked();
		if (title.equals(Main.color(Config.getConfig().getString("gui.title")))){
			event.setCancelled(true);
			int slot = event.getSlot();
			ConfigurationSection arenas = Config.getConfig().getConfigurationSection("gui.items");
			for (String arena : arenas.getKeys(false)){
				if (slot == Config.getConfig().getInt("gui.items." + arena + ".slot")) {
					double price = Config.getConfig().getDouble("arena." + arena + ".price");
					if (Main.instance.getEconomy().getBalance(player) < price) {
						player.sendMessage(Main.color(Config.getConfig().getString("messages.noMoney")));
						return;
					}
					Main.instance.getEconomy().withdrawPlayer(player, price);
					String action = Config.getConfig().getString("gui.items." + arena + ".arena");
					String[] a = Config.getConfig().getString("arena." + action + ".spawn").split(";");
					States state = States.valueOf(Config.getConfig().getString("arena." + action + ".state"));
					if (state == States.INGAME) {
						player.sendMessage(Main.color(Config.getConfig().getString("messages.dungeonInGame")));
						return;
					}
					double x = Double.parseDouble(a[0]);
					double y = Double.parseDouble(a[1]);
					double z = Double.parseDouble(a[2]);
					float yaw = Integer.parseInt(a[3]);
					float pitch = Integer.parseInt(a[4]);
					World world = Bukkit.getWorld(a[5]);
					player.teleport(new Location(world, x, y, z, yaw, pitch));
					player.sendMessage(Main.color(Config.getConfig().getString("messages.teleportedInDungeon")));

					Main.instance.ingamePlayers.put(player.getUniqueId(), action);

					Config.getConfig().set("arena." + action + ".state", States.INGAME.toString());
					Config.save();
				}
			}
		}
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent event){
		Player player = event.getPlayer();
		if (Main.instance.ingamePlayers.containsKey(player.getUniqueId())) {
			Main.instance.teleportPlayerToSpawn(player);
			String arena = Main.instance.ingamePlayers.get(player.getUniqueId());
			Config.getConfig().set("arena." + arena + ".state", States.WAITING.toString());
			Config.save();
			Main.instance.ingamePlayers.remove(player.getUniqueId());
		}
	}

	@EventHandler
	public void onChat(PlayerCommandPreprocessEvent event){
		Player player = event.getPlayer();
		if (Main.instance.ingamePlayers.containsKey(player.getUniqueId())){
			if (event.getMessage().equalsIgnoreCase("/boss") || player.hasPermission("*") || player.hasPermission("angelboss.bypass")) {
				return;
			}
			player.sendMessage(Main.color(Config.getConfig().getString("messages.noCmdsAllowed")));
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onKill(EntityDeathEvent event){
		if (event.getEntity().getKiller() instanceof Player){
			Entity entity = event.getEntity();
			Player player = event.getEntity().getKiller();
			ConfigurationSection configurationSection = Config.getConfig().getConfigurationSection("arena");
			for (String arena : configurationSection.getKeys(false)){
				String name = Config.getConfig().getString("arena." + arena + ".boss.name");
				EntityType entityType = EntityType.valueOf(Config.getConfig().getString("arena." + arena + ".boss.type"));
				if (entity.getName().equals(Main.color(name)) && entity.getType() == entityType){
					for (Entry<UUID, String> s : Main.instance.ingamePlayers.entrySet()){
						if (s.getValue().equals(arena)){
							Bukkit.broadcastMessage(Main.color(Config.getConfig().getString("messages.dungeonEndTeleport")));
							new BukkitRunnable() {
								@Override
								public void run() {
									Main.instance.teleportPlayerToSpawn(player);
									this.cancel();
								}
							}.runTaskLater(Main.instance, 200);
							event.getDrops().clear();
							Config.getConfig().set("arena." + arena + ".state", States.WAITING.toString());
							Config.save();
							Main.instance.ingamePlayers.remove(s.getKey());
						}
					}
					break;
				}
			}
		}
	}

}
