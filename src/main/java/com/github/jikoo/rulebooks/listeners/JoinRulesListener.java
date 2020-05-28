package com.github.jikoo.rulebooks.listeners;

import com.github.jikoo.rulebooks.RuleBooks;
import com.github.jikoo.rulebooks.data.PlayerData;
import com.github.jikoo.rulebooks.data.RuleData;
import com.github.jikoo.rulebooks.util.ItemUtil;
import java.util.Collection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Listener for giving players rules on join.
 *
 * @author Jikoo
 */
public class JoinRulesListener implements Listener {

	private final RuleBooks plugin;

	public JoinRulesListener(RuleBooks plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
			Player player = event.getPlayer();
			if (!player.isOnline()) {
				return;
			}

			PlayerData playerData = plugin.getPlayerData(player);
			Collection<String> receivedRules = playerData.getReceivedRules();
			for (RuleData rule : plugin.getRules()) {
				if (!rule.isBulkGive() || receivedRules.contains(rule.getID())) {
					continue;
				}
				ItemUtil.giveSafe(player, rule);
				playerData.markRuleReceived(rule.getID(), true);
			}
			for (String ruleName : receivedRules) {
				RuleData ruleData = plugin.getRule(ruleName);
				if (ruleData == null || !ruleData.checkPermission(player)) {
					playerData.markRuleReceived(ruleName, false);
				}
			}
		}, plugin.config().getGiveDelayTicks());
	}

}
