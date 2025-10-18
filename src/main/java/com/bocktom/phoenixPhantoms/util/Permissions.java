package com.bocktom.phoenixPhantoms.util;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.concurrent.CompletableFuture;

import static org.bukkit.Bukkit.getLogger;

public class Permissions {

	private LuckPerms perms;

	public boolean setup() {
		RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
		if (provider == null) {
			getLogger().severe("LuckPerms not found! Disabling plugin...");
			return false;
		}
		perms = provider.getProvider();
		return true;
	}

	public CompletableFuture<Boolean> grant(Player player, String permission) {
		User permUser = perms.getUserManager().getUser(player.getUniqueId());
		if(permUser == null)
			return CompletableFuture.completedFuture(false);

		permUser.data().add(Node.builder(permission).value(true).build());
		CompletableFuture<Void> future = perms.getUserManager().saveUser(permUser);
		return future.thenApply(v -> true);
	}

}
