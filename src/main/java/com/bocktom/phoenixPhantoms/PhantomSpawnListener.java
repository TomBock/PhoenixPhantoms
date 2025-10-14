package com.bocktom.phoenixPhantoms;

import com.destroystokyo.paper.event.entity.PhantomPreSpawnEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Statistic;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

import static com.bocktom.phoenixPhantoms.PhoenixPhantoms.plugin;

public class PhantomSpawnListener implements Listener {

	private final int _phantomSpawnThreshold;

	public PhantomSpawnListener() {
		_phantomSpawnThreshold = plugin.getConfig().getInt("spawn_start", 3) * 24000; // 24000 ticks = one full day
	}

	@EventHandler
	public void onPhantomSpawn(PhantomPreSpawnEvent event) {
		if(!(event.getSpawningEntity() instanceof Player player)) {
			return;
		}

		plugin.debug("§7SPAWNING PHANTOM FOR " + player.getName() + ", time since rest: " + player.getStatistic(Statistic.TIME_SINCE_REST)/24000);

		int timeSinceRest = player.getStatistic(Statistic.TIME_SINCE_REST) * 24000;
		if(timeSinceRest < _phantomSpawnThreshold) {
			event.setShouldAbortSpawn(true);
		}
	}

	@EventHandler
	public void onPhantomSpawned(EntitySpawnEvent event) {
		if(event.isCancelled() ||
				!(event.getEntity() instanceof Phantom phantom) ||
				phantom.getEntitySpawnReason() != CreatureSpawnEvent.SpawnReason.PATROL) {
			return;
		}

		// Delay upgrade application to ensure target is set
		Bukkit.getScheduler().runTaskLater(plugin, () -> applyPhantomUpgrades(phantom), 40);
	}

	private void applyPhantomUpgrades(Phantom phantom) {
		if(!(phantom.getTarget() instanceof Player player))
			return;

		plugin.debug("§aSPAWNED PHANTOM FOR " + player.getName() + ", time since rest: " + (player.getStatistic(Statistic.TIME_SINCE_REST)/24000));

		int timeSinceRest = player.getStatistic(Statistic.TIME_SINCE_REST);
		int daysSinceRest = timeSinceRest / 24000;

		int daysAboveThreshold = daysSinceRest - (_phantomSpawnThreshold / 24000);

		ConfigurationSection upgrades = plugin.getConfig().getConfigurationSection("upgrades");
		List<Integer> dayKeys = upgrades.getKeys(false).stream().map(Integer::parseInt).sorted().toList();

		int dayKey = dayKeys.contains(daysAboveThreshold) ? daysAboveThreshold : dayKeys.getLast();

		ConfigurationSection upgrade = upgrades.getConfigurationSection(String.valueOf(dayKey));

		// Basic Ugprades
		String customName = upgrade.getString("name");
		if(customName != null && !customName.isEmpty()) {
			phantom.customName(Component.text(customName));
			phantom.setCustomNameVisible(true);
		}

		double speed = upgrade.getDouble("speed", -1);
		if(speed != -1) {
			phantom.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
		}

		double health = upgrade.getDouble("health", -1);
		if(health != -1) {
			phantom.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
			phantom.setHealth(health);
		}

		double damage = upgrade.getDouble("damage", -1);
		if(damage != -1) {
			phantom.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
		}

		double knockback = upgrade.getDouble("knockback", -1);
		if(knockback != -1) {
			phantom.getAttribute(Attribute.ATTACK_KNOCKBACK).setBaseValue(knockback);
		}

		double size = upgrade.getDouble("size", -1);
		if(size != -1) {
			phantom.getAttribute(Attribute.SCALE).setBaseValue(size);
			phantom.setSize((int) size);
		}

		// Effects
		ConfigurationSection effects = upgrade.getConfigurationSection("effects");
		if(effects != null) {
			for (String effectName : effects.getKeys(false).stream().toList()) {
				int effectLevel = effects.getInt(effectName);

				try {
					PotionEffectType type = Registry.POTION_EFFECT_TYPE.get(new NamespacedKey("minecraft", effectName));
					if (type != null) {
						phantom.addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, effectLevel - 1, false, false));
					} else {
						Bukkit.getLogger().warning("Effect not found in registry: " + effectName);
					}

				} catch (IllegalArgumentException e) {
					Bukkit.getLogger().warning("Invalid effect name in config: " + effectName);
				}
			};
		}

		// Loot
		phantom.getPersistentDataContainer().set(plugin.phantomKey, PersistentDataType.SHORT, (short)dayKey);
	}

	@EventHandler
	public void onPhantomDeath(EntityDeathEvent event) {
		if(!(event.getEntity() instanceof Phantom phantom))
			return;

		if(!phantom.getPersistentDataContainer().has(plugin.phantomKey, PersistentDataType.SHORT))
			return;

		short upgradeLevel = phantom.getPersistentDataContainer().get(plugin.phantomKey, PersistentDataType.SHORT).shortValue();

		List<String> cfgLoot = plugin.getConfig().getStringList("upgrades." + upgradeLevel + ".loot");
		if(cfgLoot.isEmpty())
			return;

		List<ItemStack> loot = event.getDrops();

		if(cfgLoot.contains("default")) {
			// Keep default loot in
			cfgLoot.remove("default");
		} else {
			// Remove all default loot
			loot.clear();
		}

		double chance = 1.0;
		for (String itemName : cfgLoot) {
			chance = 1.0;

			if(itemName.contains(":")) {
				// Split item name and chance
				String[] parts = itemName.split(":");
				if(parts.length != 2) {
					Bukkit.getLogger().warning("Invalid loot item format in config: " + itemName);
					continue;
				}
				itemName = parts[0];
				chance = Double.parseDouble(parts[1]);
			}

			ItemStack item = plugin.getConfig().getItemStack("loot." + itemName);
			if(item == null || item.getType().isAir()) {
				Bukkit.getLogger().warning("Loot item not found in config: " + itemName);
				continue;
			}

			if(Math.random() > chance)
				continue;
			loot.add(item.clone());
		}
	}

}
