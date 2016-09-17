package application;
import java.util.Collection;

import controller.SceneController;

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
	 * Notifies main for update
	 * @param sc SceneController that has changed
	 */
	public void update(SceneController sc, String msg);
	/**
	 * Notifies current controller of update with message.
	 * @param msg
	 * @param objects object parameters
	 */
	public void tell(String msg, Object... objects);
	/**
	 * Says word in festival
	 * @param is Sequence of speeds
	 * @param string Sequence of strings
	 */
	public void sayWord(int[] is, String voice, String... string);
}
