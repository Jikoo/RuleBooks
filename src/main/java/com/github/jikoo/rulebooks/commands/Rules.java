package com.github.jikoo.rulebooks.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import com.github.jikoo.rulebooks.RuleBooks;
import org.bukkit.command.CommandSender;

/**
 * Basic commands for rules.
 *
 * @author Jikoo
 */
@CommandAlias("rulebook|rb")
@Description("Get or manage rules!")
public class Rules extends BaseCommand {

	@Dependency
	private RuleBooks plugin;

	@Default
	@HelpCommand
	public void noArgs(CommandHelp help) {
		help.showHelp();
	}

	@Subcommand("reload")
	@CommandCompletion("")
	@Description("Reload configuration and rules.")
	@CommandPermission("rulebooks.reload")
	public void reload(CommandSender sender) {
		plugin.reloadConfig();
		plugin.load();
		sender.sendMessage("Reloaded configuration and rules!");
	}

}
