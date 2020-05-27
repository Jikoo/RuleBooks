package com.github.jikoo.rulebooks.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.github.jikoo.rulebooks.RuleBooks;
import com.github.jikoo.rulebooks.data.Data;
import com.github.jikoo.rulebooks.data.RuleData;
import com.github.jikoo.rulebooks.util.ItemUtil;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Commands for giving rules to players.
 *
 * @author Jikoo
 */
@CommandAlias("rulebook|rb")
@CommandPermission("rulebooks.get")
public class GetRules extends BaseCommand {

	@Dependency
	private RuleBooks plugin;

	@Subcommand("give")
	@CommandCompletion("@rules")
	@Description("Get a rule in item form.")
	@CommandPermission("rulebooks.give")
	public void give(CommandSender sender, RuleData ruleData, Player target) {
		if (!ruleData.checkPermission(target)) {
			sender.sendMessage("Target does not have access to this rule.");
			return;
		}

		// Remove existing copies
		for (ItemStack itemStack : target.getInventory().getContents()) {
			String ruleID = ItemUtil.getRuleID(itemStack);
			if (ruleData.getID().equals(ruleID)) {
				target.getInventory().remove(itemStack);
			}
		}

		ItemUtil.giveSafe(target, ruleData.getItem());
		target.sendMessage("You were given a copy of " + ruleData.getPrettyID() + "!");
		sender.sendMessage("Gave " + target.getName() + " a copy of " + ruleData.getPrettyID() + "!");
	}

	@Subcommand("get")
	@CommandCompletion("@rules")
	@Description("Get a rule in item form.")
	public void get(@Conditions("player") CommandSender sender, RuleData ruleData) {
		Player player = (Player) sender;
		if (!ruleData.checkPermission(player)) {
			player.sendMessage("You do not have access to this rule.");
			return;
		}

		// Remove existing copies
		for (ItemStack itemStack : player.getInventory().getContents()) {
			String ruleID = ItemUtil.getRuleID(itemStack);
			if (ruleData.getID().equals(ruleID)) {
				player.getInventory().remove(itemStack);
			}
		}

		ItemUtil.giveSafe(player, ruleData.getItem());
		player.sendMessage("Obtained a copy of " + ruleData.getPrettyID() + "!");
	}

	@Subcommand("list")
	@CommandCompletion("")
	@Description("List all rules.")
	public void list(CommandSender sender) {
		Stream<RuleData> ruleStream = plugin.getRules().stream();
		if (sender instanceof Player) {
			Player player = (Player) sender;
			ruleStream = ruleStream.filter(ruleData -> ruleData.checkPermission(player));
		}
		String rules = ruleStream.map(Data::getID).collect(Collectors.joining(", ", "Rules: ", ""));

		if (rules.length() < 8) {
			sender.sendMessage("No rules are defined!");
		} else {
			sender.sendMessage(rules);
		}
	}

}
