/**
 * 
 */
package icd2;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of global application preferences that are separate from
 * workspaces.
 * 
 * @author Mark Royer
 *
 */
public class PreferenceUtils {

	private static final Logger logger = LoggerFactory
			.getLogger(PreferenceUtils.class);

	public static class Pair<K, V> {

		private K key;

		private V value;

		public Pair(K key, V value) {
			super();
			this.key = key;
			this.value = value;
		}

		public K getKey() {
			return key;
		}

		public void setKey(K key) {
			this.key = key;
		}

		public V getValue() {
			return value;
		}

		public void setValue(V value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("{ '%s': '%s'}", key, value);
		}
	}

	public Preferences getApplicationPreferences() {
		return Preferences.userNodeForPackage(PreferenceUtils.class);
	}

	public List<Pair<String, String>> getWorkspacesAccessTimes() {

		Preferences prefWorkspaces = getApplicationPreferences()
				.node("workspaces");

		try {

			String[] workspaces = prefWorkspaces.keys();

			final List<Pair<String, String>> wsAccessTimes = new ArrayList<>(
					workspaces.length);
			Arrays.stream(workspaces).forEachOrdered(
					a -> wsAccessTimes.add(new Pair<>(a, prefWorkspaces.get(a,
							String.valueOf(System.currentTimeMillis())))));

			Collections.sort(wsAccessTimes,
					(a, b) -> b.getValue().compareTo(a.getValue()));

			if (workspaces.length == 0) {
				wsAccessTimes.add(new Pair<String, String>(
						System.getProperty("user.home") + File.separator
								+ "icdWorkspace",
						"never used"));
			}

			logger.debug("Known workspaces = {}", (Object) workspaces);

			return wsAccessTimes;

		} catch (BackingStoreException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	public void updateWorkspaceAccessTime(String key, String value) {
		getApplicationPreferences().node("workspaces").put(key, value);
	}
}
