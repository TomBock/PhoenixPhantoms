package com.bocktom.phoenixPhantoms;

import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AwakeCommand implements CommandExecutor {
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

		if(!(sender instanceof Player player)) {
			sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
			return true;
		}

		if(args.length == 1) {
			try {
				int awakeTime = Integer.parseInt(args[0]);
				player.setStatistic(Statistic.TIME_SINCE_REST, awakeTime * 24000);
				player.sendMessage("§7Deine Zeit ohne Schlaf wurde auf §a" + player.getStatistic(Statistic.TIME_SINCE_REST)/24000 + " Tage §7gesetzt.");
			} catch (NumberFormatException e) {
				player.sendMessage("§7Bitte gib eine gültige Zahl ein.");
			}
		} else {
			player.sendMessage("§7Bitte gib die Anzahl der Tage ohne Schlaf an.");
		}
		return true;
	}
}
