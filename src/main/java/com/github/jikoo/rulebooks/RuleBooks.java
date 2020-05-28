package com.github.jikoo.rulebooks;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import com.github.jikoo.rulebooks.commands.GetRules;
import com.github.jikoo.rulebooks.commands.ModifyRules;
import com.github.jikoo.rulebooks.commands.Rules;
import com.github.jikoo.rulebooks.data.Data;
import com.github.jikoo.rulebooks.data.PlayerData;
import com.github.jikoo.rulebooks.data.RuleData;
import com.github.jikoo.rulebooks.listeners.JoinRulesListener;
import com.github.jikoo.rulebooks.listeners.KeepRulesListener;
import com.github.jikoo.rulebooks.listeners.RuleSellListener;
import com.github.jikoo.rulebooks.listeners.UpdateRuleListener;
import com.github.jikoo.rulebooks.util.Config;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The main plugin class. Handles loading and saving of data.
 *
 * @author Jikoo
 */
public class RuleBooks extends JavaPlugin {

	private final File rulesFolder = new File(getDataFolder(), "rules");
	private final Map<String, RuleData> loadedRules = new HashMap<>();
	private final File playerDataFolder = new File(getDataFolder(), "players");
	private final Map<String, PlayerData> loadedPlayers = new HashMap<>();
	private final Config config = new Config(this);

	@Override
	public void onEnable() {
		saveDefaultConfig();

		load();

		PaperCommandManager commandManager = new PaperCommandManager(this);

		commandManager.enableUnstableAPI("help");

		commandManager.getCommandContexts().registerIssuerOnlyContext(CommandSender.class, BukkitCommandExecutionContext::getSender);
		commandManager.getCommandConditions().addCondition(CommandSender.class, "player", (conditionContext, executionContext, sender) -> {
			if (conditionContext.hasConfig("player") && !(sender instanceof Player)) {
				throw new ConditionFailedException("This command can only be used by players!");
			}
		});

		commandManager.getCommandContexts().registerIssuerAwareContext(RuleData.class, resolver -> {
			RuleData ruleData = getRule(resolver.popFirstArg());
			if (ruleData == null || resolver.getPlayer() != null && !ruleData.checkPermission(resolver.getPlayer())) {
				throw new InvalidCommandArgument("Rule does not exist!");
			}
			return ruleData;
		});

		commandManager.getCommandCompletions().registerCompletion("rules", context -> {
			Stream<RuleData> stream = loadedRules.values().stream();
			if (context.getSender() instanceof Player) {
				stream = stream.filter(ruleData -> ruleData.checkPermission(context.getPlayer()));
			}
			return stream.map(Data::getID).collect(Collectors.toSet());
		});
		commandManager.getCommandCompletions().setDefaultCompletion("rules", RuleData.class);

		commandManager.registerCommand(new Rules());
		commandManager.registerCommand(new GetRules());
		commandManager.registerCommand(new ModifyRules());
	}

	public void load() {
		// In case of a reload, do not register duplicate listeners.
		HandlerList.unregisterAll(this);

		// Load rules.
		String[] files = rulesFolder.list((dir, name) -> name.endsWith(".yml"));
		if (files != null) {
			for (String fileName : files) {
				RuleData ruleData = new RuleData(new File(rulesFolder, fileName));
				try {
					ruleData.getItem();
				} catch (IllegalStateException e) {
					getLogger().warning("Unable to load ItemStack for rule " + ruleData.getID());
				}
				loadedRules.put(ruleData.getID(), ruleData);
			}
		}

		// Enable configured listeners.
		if (config.giveRulesOnJoin()) {
			getServer().getPluginManager().registerEvents(new JoinRulesListener(this), this);
		}
		if (config.keepRulesOnDeath()) {
			getServer().getPluginManager().registerEvents(new KeepRulesListener(this), this);
		}
		if (config.blockSellingRules()) {
			getServer().getPluginManager().registerEvents(new RuleSellListener(), this);
		}
		getServer().getPluginManager().registerEvents(new UpdateRuleListener(this), this);
	}

	public @Nullable RuleData getRule(String ruleID) {
		return loadedRules.get(ruleID);
	}

	public @NotNull RuleData createRule(@NotNull String ruleID, @NotNull ItemStack itemStack) {
		return loadedRules.compute(ruleID, (key, data) -> {
			if (data == null) {
				data = new RuleData(new File(rulesFolder, ruleID + ".yml"), itemStack);
			} else {
				data.setItem(itemStack);
			}
			return data;
		});
	}

	public Collection<RuleData> getRules() {
		return loadedRules.values();
	}

	public @NotNull PlayerData getPlayerData(@NotNull Player player) {
		loadedPlayers.computeIfAbsent(player.getUniqueId().toString(), uuid -> new PlayerData(new File(playerDataFolder, uuid + ".yml")));
		return loadedPlayers.get(player.getUniqueId().toString());
	}

	@Override
	public void onDisable() {
		Stream.concat(loadedRules.values().stream(), loadedPlayers.values().stream()).forEach(data -> {
			try {
				data.save();
			} catch (IOException e) {
				getLogger().log(Level.WARNING, "Caught exception saving data " + data.getID(), e);
			}
		});

		loadedPlayers.clear();
		loadedRules.clear();
	}

	public Config config() {
		return config;
	}

}
