package cz.angelo.angelboss;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public final class Main extends JavaPlugin {

	public static Main instance;

	public HashMap<UUID, String> ingamePlayers = new HashMap<>();

	private Economy econ;

	@Override
	public void onEnable() {
		instance = this;
		Config.registerConfig();
		this.registerCommands();
		this.registerEvents();
		this.setupEconomy();
	}

	@Override
	public void onDisable() {
		for (Player player : Bukkit.getOnlinePlayers()){
			if (this.ingamePlayers.containsKey(player.getUniqueId())){
				Config.getConfig().set("arena." + this.ingamePlayers.get(player.getUniqueId()) + ".state", States.WAITING.toString());
				Config.save();
				this.teleportPlayerToSpawn(player);
				this.ingamePlayers.remove(player.getUniqueId());
			}
		}
		this.ingamePlayers.clear();
	}

	public void registerEvents(){
		this.getServer().getPluginManager().registerEvents(new GameListeners(), this);
	}

	public void registerCommands(){
		this.getCommand("boss").setExecutor(new Commands());
	}

	public static String color(String s){
		return ChatColor.translateAlternateColorCodes('&', s);
	}

	public Economy getEconomy() {
		return econ;
	}

	private boolean setupEconomy() {
		if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
			return false;
		}

		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	public void teleportPlayerToSpawn(Player player){
		String[] a = Config.getConfig().getString("spawn").split(";");
		double x = Double.parseDouble(a[0]);
		double y = Double.parseDouble(a[1]);
		double z = Double.parseDouble(a[2]);
		float yaw = Float.parseFloat(a[3]);
		float pitch = Float.parseFloat(a[4]);
		World world = Bukkit.getWorld(a[5]);
		player.teleport(new Location(world, x, y, z, yaw, pitch));
	}

}
