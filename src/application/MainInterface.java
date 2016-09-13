package application;
import java.util.Collection;

import resources.StoredStats;

public interface MainInterface {
	public static final String STATS_PATH = System.getProperty("user.home")+"/.user/stats.ser";
	/**
	 * Request a scene change in the application.
	 * @param key
	 * @param data
	 * @return
	 */
	public boolean requestSceneChange(String key, String... data);
	/**
	 * Gets all available scene keys. Controllers can request keys for model changes
	 * @return
	 */
	public Collection<String> getAvailableSceneKeys();
	/**
	 * Loads object from path to file
	 * @param path
	 * @return
	 */
	public Object loadObjectFromFile(String path);
	/**
	 * Writes object to path
	 * @param path
	 * @param obj
	 * @return
	 */
	public boolean writeObjectToFile(String path, Object obj);
	
	/**
	 * Notifies main of a change.
	 * Main notification then propagates to controller.
	 */
	public void tell();
}
