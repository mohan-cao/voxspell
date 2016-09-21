package application;
import java.util.Collection;

import javafx.stage.Stage;
/**
 * Interface representing the main application model
 * @author Mohan Cao
 *
 */
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
	 * Request a scene change in the application in the Stage
	 * @param key
	 * @param newWindow
	 * @param data
	 * @return
	 */
	public boolean requestSceneChange(String key, Stage newWindow, String... data);
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
	 * Called by controllers to update model
	 * @param mUpdateEvent
	 */
	public void update(ModelUpdateEvent mUpdateEvent);
	/**
	 * Notifies current controller of update with message.
	 * @param msg
	 * @param objects object parameters
	 */
	public void tell(String msg, Object... objects);
	/**
	 * Says word in festival
	 * @param i speed
	 * @param string Sequence of strings
	 */
	public void sayWord(int i, String voice, String... string);
	
}
