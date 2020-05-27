package com.github.jikoo.rulebooks.commands;

import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import com.github.jikoo.rulebooks.RuleBooks;
import com.github.jikoo.rulebooks.commands.enums.RuleProperty;
import com.github.jikoo.rulebooks.data.RuleData;
import com.github.jikoo.rulebooks.util.ItemUtil;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Commands for creating, modifying, and removing rules.
 *
 * @author Jikoo
 */
@CommandAlias("rulebook|rb")
@CommandPermission("rulebooks.edit")
public class ModifyRules extends BaseCommand {

	@Dependency
	private RuleBooks plugin;

	@Subcommand("edit")
	@CommandCompletion("@rules")
	@Description("Get a writable rule. Changes save.")
	public void edit(@Conditions("player") CommandSender sender, RuleData ruleData) {
		Player player = (Player) sender;
		ItemStack item = ruleData.getItem();

		if (item.getType() == Material.WRITTEN_BOOK) {
			item.setType(Material.WRITABLE_BOOK);
		}

		ItemUtil.giveSafe(player, item);
		sender.sendMessage("Obtained writable copy of " + ruleData.getPrettyID() + "!");
	}

	@Subcommand("edit")
	@CommandCompletion("@rules")
	@Description("Edit a rule's properties.")
	public void edit(RuleData ruleData, RuleProperty property, @Single String value) {
		switch (property) {
			case PERMISSION:
				if (getCurrentCommandIssuer().isPlayer() && getCurrentCommandIssuer().hasPermission(value)) {
					getCurrentCommandIssuer().sendMessage("You cannot set a rule's permission to one that you do not have!");
					return;
				}
				ruleData.setPermission(value);
				getCurrentCommandIssuer().sendMessage("Set required permission to " + value);
				break;
			case BULK_GIVE:
				ruleData.setBulkGive(ACFUtil.isTruthy(value));
				getCurrentCommandIssuer().sendMessage("Set bulk give " + ruleData.isBulkGive());
				break;
		}

		try {
			ruleData.save();
		} catch (IOException e) {
			getCurrentCommandIssuer().sendMessage("Caught exception saving rule. Please check console!");
			plugin.getLogger().log(Level.WARNING, "Caught exception saving data " + ruleData.getID(), e);
		}
	}

	@Subcommand("add")
	@CommandCompletion("rule_name")
	@Description("Add a rule item.")
	public void add(@Conditions("player") CommandSender sender, @Single String ruleName, @Default("false") boolean force) {
		Player player = (Player) sender;
		ItemStack itemInMainHand = player.getInventory().getItemInMainHand();

		if (itemInMainHand.getType().isAir()) {
			sender.sendMessage("You must be holding the new rule item in your main hand to replace a rule.");
			return;
		}

		if (itemInMainHand.getType() != Material.WRITABLE_BOOK) {
			if (!force) {
				sender.sendMessage("Setting rule items other than written books is inadvisable. Please use a kit plugin.");
				sender.sendMessage("If you are very sure that this is what you want to do, please add the parameter \"true\" to the end of your command.");
				return;
			}
		}

		if (!ruleName.matches("\\w+")) {
			sender.sendMessage("Rule names must be alphanumeric only. Underscores will be displayed to users as spaces in feedback.");
			return;
		}

		RuleData ruleData = plugin.createRule(ruleName, itemInMainHand);

		try {
			ruleData.save();
			sender.sendMessage("Saved rule!");
		} catch (IOException e) {
			sender.sendMessage("Caught exception saving rule. Please check console!");
			plugin.getLogger().log(Level.WARNING, "Caught exception saving data " + ruleData.getID(), e);
		}
	}

	@Subcommand("remove")
	@CommandCompletion("@rule")
	@Description("Delete a rule item.")
	public void remove(CommandSender sender, RuleData ruleData) {
		try {
			ruleData.delete();
			plugin.getRules().remove(ruleData);
			sender.sendMessage("Deleted rule!");
		} catch (IOException e) {
			sender.sendMessage("Caught exception deleting rule. Please check console!");
			plugin.getLogger().log(Level.WARNING, "Caught exception deleting data " + ruleData.getID(), e);
		}
	}

	@Subcommand("replace")
	@CommandCompletion("@rules")
	@Description("Replace a rule item.")
	public void replace(@Conditions("player") CommandSender sender, RuleData ruleData, @Default("false") boolean force) {
		Player player = (Player) sender;
		ItemStack itemInMainHand = player.getInventory().getItemInMainHand();

		if (itemInMainHand.getType().isAir()) {
			sender.sendMessage("You must be holding the new rule item in your main hand to replace a rule.");
			return;
		}

		if (itemInMainHand.getType() != Material.WRITABLE_BOOK && !force) {
			sender.sendMessage("Setting rule items other than written books is inadvisable. Please use a kit plugin.");
			sender.sendMessage("If you are very sure that this is what you want to do, please add the parameter \"true\" to the end of your command.");
			return;
		}

		ruleData.setItem(itemInMainHand);

		try {
			ruleData.save();
			sender.sendMessage("Saved rule!");
		} catch (IOException e) {
			sender.sendMessage("Caught exception saving rule. Please check console!");
			plugin.getLogger().log(Level.WARNING, "Caught exception saving data " + ruleData.getID(), e);
		}
	}

}
