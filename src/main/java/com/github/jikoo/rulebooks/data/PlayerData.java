package com.github.jikoo.rulebooks.data;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Data implementation for player data storage.
 *
 * @author Jikoo
 */
public class PlayerData extends Data {

	private static final String RECEIVED_RULES = "receivedRules";

	public PlayerData(File dataFile) {
		super(dataFile);
	}

	public Collection<String> getReceivedRules() {
		return getData().getStringList(RECEIVED_RULES);
	}

	public void markRuleReceived(String bookId, boolean received) {
		List<String> receivedBooks = getData().getStringList(RECEIVED_RULES);
		if (received) {
			if (!receivedBooks.contains(bookId)) {
				receivedBooks.add(bookId);
				getData().set(RECEIVED_RULES, receivedBooks);
				setDirty();
			}
		} else {
			if (receivedBooks.contains(bookId)) {
				receivedBooks.remove(bookId);
				getData().set(RECEIVED_RULES, receivedBooks);
				setDirty();
			}
		}
	}

}
