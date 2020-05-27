package com.github.jikoo.rulebooks.util;

import com.github.jikoo.rulebooks.RuleBooks;

/**
 * Wrapper adding easy-to-use methods for accessing particular configuration values.
 *
 * @author Jikoo
 */
public class Config {

	private final RuleBooks plugin;

	public Config(RuleBooks plugin) {
		this.plugin = plugin;
	}

	public boolean blockSellingRules() {
		return plugin.getConfig().getBoolean("block-selling-rules", true);
	}

	public boolean giveRulesOnJoin() {
		return plugin.getConfig().getBoolean("give-rules-on-join", true);
	}

	public int getGiveDelayTicks() {
		return Math.max(0, plugin.getConfig().getInt("give-delay-seconds", 5)) * 20;
	}

	public boolean keepRulesOnDeath() {
		return plugin.getConfig().getBoolean("keep-rules-on-death", true);
	}

}
