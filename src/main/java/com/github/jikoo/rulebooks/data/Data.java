package com.github.jikoo.rulebooks.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Class for data storage wrapping a YamlConfiguration.
 *
 * @author Jikoo
 */
public abstract class Data {

	private final String identifier;
	private final File file;
	private final YamlConfiguration configuration;
	private boolean dirty = false;

	public Data(@NotNull File file) {
		String name = file.getName();
		this.identifier = name.substring(0, name.length() - 4);
		this.file = file;
		this.configuration = YamlConfiguration.loadConfiguration(file);
	}

	public String getID() {
		return identifier;
	}

	protected YamlConfiguration getData() {
		return configuration;
	}

	protected void setDirty() {
		this.dirty = true;
	}

	public void save() throws IOException {
		if (dirty) {
			configuration.save(file);
			dirty = false;
		}
	}

	public void delete() throws IOException {
		Files.delete(file.toPath());
	}

}
