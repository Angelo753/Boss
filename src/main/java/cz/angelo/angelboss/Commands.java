package cz.angelo.angelboss;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class Commands implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Asi chyba, musis byt hrac"); //toDo
			return false;
		}
		Player player = (Player) sender;
		if (command.getName().equalsIgnoreCase("boss")){
			GUI.openGUI(player);
		}

		return false;
	}
}
