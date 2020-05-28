package com.github.jikoo.rulebooks.listeners;

import com.github.jikoo.rulebooks.RuleBooks;
import com.github.jikoo.rulebooks.data.RuleData;
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

/**
 * Listener for keeping rule items on death.
 *
 * @author Jikoo
 */
public class KeepRulesListener implements Listener {

	private final Map<UUID, List<RuleData>> rulesOnRespawn = new HashMap<>();
	private final RuleBooks plugin;

	public KeepRulesListener(RuleBooks plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (!event.getKeepInventory()) {
			List<RuleData> rules = new ArrayList<>();
			event.getDrops().removeIf(itemStack -> {
				String ruleID = ItemUtil.getRuleID(itemStack);
				if (ruleID == null) {
					return false;
				}
				RuleData ruleData = plugin.getRule(ruleID);
				if (ruleData != null) {
					rules.add(ruleData);
				}
				return true;
			});

			if (rules.isEmpty()) {
				return;
			}

			rulesOnRespawn.put(event.getEntity().getUniqueId(), rules);
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		List<RuleData> rules = rulesOnRespawn.remove(event.getPlayer().getUniqueId());
		if (rules != null) {
			rules.forEach(ruleData -> ItemUtil.giveSafe(event.getPlayer(), ruleData));
		}
	}

}
