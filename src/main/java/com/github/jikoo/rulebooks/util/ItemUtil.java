package com.github.jikoo.rulebooks.util;

import com.github.jikoo.rulebooks.data.RuleData;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class containing utility methods for handling or checking ItemStacks.
 *
 * @author Jikoo
 */
public final class ItemUtil {

	@SuppressWarnings("deprecation") // Namespaced key constructor using raw name is deprecated, but ours is constant.
	private static final NamespacedKey key = new NamespacedKey("rulebooks", "rule_id");

	public static @Nullable String getRuleID(@Nullable ItemStack itemStack) {
		if (itemStack == null || !itemStack.hasItemMeta()) {
			return null;
		}

		return getRuleID(itemStack.getItemMeta());
	}

	public static @Nullable String getRuleID(@Nullable ItemMeta itemMeta) {
		if (itemMeta == null) {
			return null;
		}

		PersistentDataContainer container = itemMeta.getPersistentDataContainer();
		return container.get(key, PersistentDataType.STRING);
	}

	public static boolean isTagged(@Nullable ItemStack itemStack) {
		return getRuleID(itemStack) != null;
	}

	public static void setRuleID(@NotNull ItemStack itemStack, String id) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (itemMeta == null) {
			return;
		}
		itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
		itemStack.setItemMeta(itemMeta);
	}

	public static boolean giveSafe(@NotNull Player player, @NotNull RuleData ruleData) {
		// Remove existing copies
		for (ItemStack itemStack : player.getInventory().getContents()) {
			String ruleID = ItemUtil.getRuleID(itemStack);
			if (ruleData.getID().equals(ruleID)) {
				player.getInventory().remove(itemStack);
			}
		}

		if (ruleData.checkPermission(player)) {
			giveSafe(player, ruleData.getItem());
			return true;
		}
		return false;
	}

	public static void giveSafe(@NotNull Player player, @Nullable ItemStack itemStack) {
		if (itemStack == null) {
			return;
		}
		HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(itemStack);

		// Drop anything that didn't fit at player's location
		for (Map.Entry<Integer, ItemStack> entry : leftovers.entrySet()) {
			player.getWorld().dropItem(player.getLocation(), entry.getValue()).setPickupDelay(0);
		}
	}

	public static BiConsumer<PlayerInventory, ItemStack> getSlotSetter(EquipmentSlot slot) {
		switch (slot) {
			case HAND:
				return PlayerInventory::setItemInMainHand;
			case OFF_HAND:
				return PlayerInventory::setItemInOffHand;
			case FEET:
				return PlayerInventory::setBoots;
			case LEGS:
				return PlayerInventory::setLeggings;
			case CHEST:
				return PlayerInventory::setChestplate;
			case HEAD:
				return PlayerInventory::setHelmet;
			default:
				throw new IllegalArgumentException(String.format("Unsupported EquipmentSlot %s", slot));
		}
	}

	private ItemUtil() {}

}
