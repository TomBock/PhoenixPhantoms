package com.bocktom.phoenixPhantoms;

import com.bocktom.phoenixPhantoms.util.Config;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.bocktom.phoenixPhantoms.PhoenixPhantoms.plugin;

public class PhantomPlaceholderExpansion extends PlaceholderExpansion implements Relational {

	public static final String NACHTFRAGMENT = "nachtfragment";
	public static final String NACHTKRISTALL = "nachtkristall";
	public static final String NACHTESSENZ = "essenzdernacht";
	private static final String NACHTFRAGMENT_TITEL = "nachtfragmenttitel";
	private static final String NACHTKRISTALL_TITEL = "nachtkristalltitel";
	private static final String NACHTESSENZ_TITEL = "essenzdernachttitel";

	private final String soldItemsColor;

	public PhantomPlaceholderExpansion() {
		soldItemsColor = plugin.getConfig().getString("placeholder.sold_items_color");
	}

	@Override
	public @NotNull String getIdentifier() {
		return "phoenixphantom";
	}

	@Override
	public @NotNull String getAuthor() {
		return "TomBock";
	}

	@Override
	public @NotNull String getVersion() {
		return "1.0";
	}

	@Override
	public @Nullable String onPlaceholderRequest(Player player, @NotNull String raw) {
		if(player == null) {
			return "";
		}

		return switch (raw) {
			case NACHTFRAGMENT -> getSoldItem(player, NACHTFRAGMENT);
			case NACHTKRISTALL -> getSoldItem(player, NACHTKRISTALL);
			case NACHTESSENZ -> getSoldItem(player, NACHTESSENZ);
			case NACHTFRAGMENT_TITEL -> getSoldItem(player, NACHTFRAGMENT_TITEL);
			case NACHTKRISTALL_TITEL -> getSoldItem(player, NACHTKRISTALL_TITEL);
			case NACHTESSENZ_TITEL -> getSoldItem(player, NACHTESSENZ_TITEL);
			case "awake" -> getAwakeTime(player);
			default -> "<not_found>";
		};
	}

	@Override
	public String onPlaceholderRequest(Player one, Player two, String raw) {
		return switch (raw) {
			case "awake_player" -> getAwakeTime(one);
			default -> "<not_found>";
		};
	}

	private String getSoldItem(Player player, String key) {
		String uuid = player.getUniqueId().toString();
		int amount = Config.sold.get.getInt(uuid + "." + key, 0);
		int max = Config.shop.get.getInt(key + ".limit", 1);
		return soldItemsColor + amount + "/" + max;
	}

	private String getAwakeTime(Player player) {
		// Format with 2 decimal places
		double daysAwake = player.getStatistic(Statistic.TIME_SINCE_REST) / 24000d;
		return String.format("%.2f", daysAwake);
	}
}
