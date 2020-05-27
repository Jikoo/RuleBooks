package com.github.jikoo.rulebooks.listeners;

import com.github.jikoo.rulebooks.util.ItemUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

/**
 * Listener preventing users from selling rule books to villagers.
 * 
 * @author Jikoo
 */
public class RuleSellListener implements Listener {

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getInventory().getType() != InventoryType.MERCHANT) {
			return;
		}

		ItemStack interacted;
		if (event.getClick() == ClickType.NUMBER_KEY) {
			interacted = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
		} else if (event.isShiftClick()) {
			interacted = event.getCurrentItem();
		} else {
			interacted = event.getCursor();
		}

		if (ItemUtil.isTagged(interacted)) {
			event.setCancelled(true);
			// TODO notify?
		}
	}

}
