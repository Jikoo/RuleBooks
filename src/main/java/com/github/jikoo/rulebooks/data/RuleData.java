package com.github.jikoo.rulebooks.data;

import com.github.jikoo.rulebooks.util.ItemUtil;
import java.io.File;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Data implementation for item-based rule storage.
 *
 * @author Jikoo
 */
public class RuleData extends Data {

	private static final String PERMISSION = "permission";
	private static final String ITEM = "item";
	private static final String BULK_GIVE = "bulkGive";

	public RuleData(@NotNull File file) {
		super(file);
	}

	public RuleData(@NotNull File file, @NotNull ItemStack itemStack) {
		this(file);
		setItem(itemStack);
	}

	public String getPrettyID() {
		return getID().replace('_', ' ');
	}

	public @NotNull ItemStack getItem() {
		ItemStack itemStack = getData().getItemStack(ITEM);
		if (itemStack == null) {
			throw new IllegalStateException("Rule ItemStack cannot be null!");
		}
		return itemStack.clone();
	}

	public void setItem(@NotNull ItemStack itemStack) {
		itemStack = itemStack.clone();
		itemStack.setAmount(1);

		ItemStack oldItem = getData().getItemStack(ITEM);
		if (itemStack.equals(oldItem)) {
			// Item unchanged, no need to edit rule.
			return;
		}

		// Ensure book is a written book for easy giving later.
		if (itemStack.getType() == Material.WRITABLE_BOOK) {
			itemStack.setType(Material.WRITTEN_BOOK);
		}

		ItemUtil.setRuleID(itemStack, getID());

		getData().set(ITEM, itemStack);
		setDirty();
	}

	public void setBulkGive(boolean givenOnJoin) {
		boolean oldVal = getData().getBoolean(BULK_GIVE);
		if (givenOnJoin == oldVal) {
			return;
		}
		getData().set(BULK_GIVE, givenOnJoin);
		setDirty();
	}

	public boolean isBulkGive() {
		return getData().getBoolean(BULK_GIVE, true);
	}

	public void setPermission(String permission) {
		String oldVal = getData().getString(PERMISSION);
		if (oldVal == null && permission == null || oldVal != null && oldVal.equals(permission)) {
			return;
		}
		getData().set(PERMISSION, permission);
		setDirty();
	}

	public boolean checkPermission(Player player) {
		if (!getData().isSet(PERMISSION)) {
			return true;
		}
		String permission = getData().getString(PERMISSION);
		if (permission == null || permission.isEmpty()) {
			return true;
		}
		return player.hasPermission(permission);
	}

}
