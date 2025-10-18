package com.bocktom.phoenixPhantoms;

import com.bocktom.phoenixPhantoms.util.Config;
import com.bocktom.phoenixPhantoms.util.MSG;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.bocktom.phoenixPhantoms.PhoenixPhantoms.plugin;
import static org.bukkit.Bukkit.getLogger;

public class ShopCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String @NotNull [] args) {

		if(args.length == 1) {
			if(args[0].equalsIgnoreCase("resetlimit")) {
				resetLimits();
				return true;
			}
		}

		if(args.length == 2) {
			sellItemsByKey(sender, args[0], args[1]);
			return true;
		}

		sender.sendMessage("§cInkorrekte Nutzung!");

		return true;
	}

	/**
	 * Resets the shop limits for all players asynchronously.
	 * Creates a snapshot of the sold_items.yml file, resets the limits for specific items,
	 * and saves the changes back to the file.
	 * Then reloads the sold items configuration.
	 */
	private void resetLimits() {
		String[] keys = {
				PhantomPlaceholderExpansion.NACHTFRAGMENT,
				PhantomPlaceholderExpansion.NACHTKRISTALL,
				PhantomPlaceholderExpansion.NACHTESSENZ
		};

		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

			FileConfiguration snapshotSold = Config.sold.loadSnapshot();
			try {
				snapshotSold.load(Config.sold.file);
			} catch (IOException | InvalidConfigurationException e) {
				getLogger().severe("Fehler beim Laden der sold_items.yml für das Zurücksetzen der Limits!");
				e.printStackTrace();
				return;
			}
			for (String uuid : snapshotSold.getKeys(false)) {
				for (String key : keys) {
					snapshotSold.set(uuid + "." + key, 0);
				}
			}

			for (String uuid : snapshotSold.getKeys(false)) {
				for (String key : keys) {
					if(snapshotSold.contains(uuid + "." + key)) {
						snapshotSold.set(uuid + "." + key, 0);
					}
				}
			}

			try {
				snapshotSold.save(Config.sold.file);
			} catch (IOException e) {
				getLogger().severe("Fehler beim Speichern der sold_items.yml für das Zurücksetzen der Limits!");
				e.printStackTrace();
				return;
			}
			getLogger().info("Shop Limits wurden zurückgesetzt.");
			Config.sold.reloadFromFile();
		});
	}

	/**
	 * Sells items (based on the key) from a player's inventory in exchange for rubies and/or a title.
	 * @param sender the command sender, can be an NPC
	 * @param key the shop item key <see>config.yml</see>
	 * @param playerName the name of the player who is selling the items
	 */
	private void sellItemsByKey(@NotNull CommandSender sender, @NotNull String key, @NotNull String playerName) {

		// Validation
		Player player = Bukkit.getPlayer(playerName);
		if(player == null) {
			sender.sendMessage("§cDer Spieler §6" + playerName + " §cist nicht online!");
			return;
		}

		int limit = Config.shop.get.getInt(key + ".limit", -1);
		if(limit < 0) {
			player.sendMessage("§cDer Shop Eintrag §6" + key + " §cexistiert nicht!");
			return;
		}
		int anzahl = Config.shop.get.getInt(key + ".anzahl", 1);
		int rubine = Config.shop.get.getInt(key + ".rubine", 0);
		String titel = Config.shop.get.getString(key + ".titel", null);

		// Title items have another item key for the sold item
		String itemKey = Config.shop.get.getString(key + ".item", key);

		ItemStack item = Config.global.getItemStack("loot." + itemKey, null);
		if(item == null) {
			player.sendMessage("§cDer Loot Item Eintrag §6" + key + " §cexistiert nicht!");
			return;
		}

		String uuid = player.getUniqueId().toString();
		int currentSold = Config.sold.get.getInt(uuid + "." + key, 0);

		// Check for limit
		if(currentSold >= limit) {
			player.sendMessage(MSG.get("shop_limit_reached", "%limit%", String.valueOf(limit)));
			return;
		}

		// Purchase
		ItemStack toRemove = item.clone();

		if(!player.getInventory().containsAtLeast(toRemove, anzahl)) {
			player.sendMessage(MSG.get("not_enough_items", Component.text("%item%"), item.displayName().hoverEvent(HoverEvent.showItem(item.asHoverEvent().value()))));
			return;
		}

		toRemove.setAmount(anzahl);

		player.getInventory().removeItemAnySlot(toRemove);

		// What players get
		if(rubine > 0) {
			plugin.economy.deposit(player, rubine);
			player.sendMessage(MSG.get("shop_success_rubine", "%amount%", String.valueOf(rubine)));
		}
		if (titel != null) {
			plugin.perms.grant(player, titel).thenAccept(result -> {
				if(!result) {
					player.sendMessage("§cFehler beim Vergeben des Titels!");
					getLogger().warning("Fehler beim Vergeben des Titels " + titel + " an Spieler " + player.getName());
					return;
				}

				player.sendMessage(MSG.get("shop_success_titel", "%title%", titel));
			});
		}

		// Update sold count
		Config.sold.get.set(uuid + "." + key, currentSold + 1);
		Config.sold.save();
	}

}
