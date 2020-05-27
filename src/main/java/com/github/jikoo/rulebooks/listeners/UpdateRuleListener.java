package com.github.jikoo.rulebooks.listeners;

import com.github.jikoo.rulebooks.RuleBooks;
import com.github.jikoo.rulebooks.data.RuleData;
import com.github.jikoo.rulebooks.util.ItemUtil;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for dynamically updating existing rules.
 *
 * @author Jikoo
 */
public class UpdateRuleListener implements Listener {

	private final RuleBooks plugin;

	public UpdateRuleListener(RuleBooks plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerEditBook(PlayerEditBookEvent event) {
		String ruleID = ItemUtil.getRuleID(event.getPreviousBookMeta());

		if (ruleID == null) {
			return;
		}

		RuleData rule = plugin.getRule(ruleID);

		if (rule == null) {
			return;
		}

		if (!event.getPlayer().hasPermission("rulebooks.edit")) {
			// Make book un-editable by forcing signing.
			event.setNewBookMeta(event.getPreviousBookMeta());
			event.setSigning(true);
			return;
		}

		// Preserve old author
		if (event.getPreviousBookMeta().getAuthor() != null) {
			event.getNewBookMeta().setAuthor(event.getPreviousBookMeta().getAuthor());
		}

		ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK);
		itemStack.setItemMeta(event.getNewBookMeta());
		rule.setItem(itemStack);

		try {
			rule.save();
		} catch (IOException e) {
			plugin.getLogger().log(Level.WARNING, "Error saving rule " + ruleID, e);
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		String ruleID = ItemUtil.getRuleID(event.getItem());

		if (ruleID == null) {
			return;
		}

		RuleData rule = plugin.getRule(ruleID);

		if (rule == null) {
			return;
		}

		if (event.getHand() == null) {
			// Shouldn't be possible
			event.setCancelled(true);
			return;
		}

		if (rule.checkPermission(event.getPlayer())) {
			if (event.getItem().getType() == Material.WRITABLE_BOOK) {
				// User is modifying existing rule.
				return;
			}
			ItemStack ruleItem = rule.getItem();
			if (!ruleItem.isSimilar(event.getItem())) {
				// Rule is not up to date.
				event.getPlayer().getInventory().setItem(Objects.requireNonNull(event.getHand()), ruleItem);
			}
		} else {
			// User does not have permission to access rule.
			event.getPlayer().getInventory().setItem(Objects.requireNonNull(event.getHand()), null);
		}
	}

}
