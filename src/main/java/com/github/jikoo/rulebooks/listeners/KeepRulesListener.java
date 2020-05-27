package com.github.jikoo.rulebooks.listeners;

import com.github.jikoo.rulebooks.util.ItemUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for keeping rule items on death.
 *
 * @author Jikoo
 */
public class KeepRulesListener implements Listener {

	private final Map<UUID, List<ItemStack>> rulesOnRespawn = new HashMap<>();

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (!event.getKeepInventory()) {
			List<ItemStack> rules = new ArrayList<>();
			event.getDrops().removeIf(itemStack -> {
				if (ItemUtil.isTagged(itemStack)) {
					rules.add(itemStack);
					return true;
				}
				return false;
			});

			if (rules.isEmpty()) {
				return;
			}

			rulesOnRespawn.put(event.getEntity().getUniqueId(), rules);
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		List<ItemStack> rules = rulesOnRespawn.remove(event.getPlayer().getUniqueId());
		if (rules != null) {
			rules.forEach(rule -> ItemUtil.giveSafe(event.getPlayer(), rule));
		}
	}

}
