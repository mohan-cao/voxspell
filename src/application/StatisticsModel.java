package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import resources.StoredStats;

public class StatisticsModel {
	public static final String STATS_PATH = System.getProperty("user.home")+"/.user/stats.ser";
	private StoredStats sessionStats;
	private StoredStats globalStats;
	private MainInterface application;
	/**
	 * Create new Statistics model that is not linked to a main interface.
	 * You will have to manually save the session data later, and you cannot use global stats.
	 */
	public StatisticsModel() {
		this(null);
	}
	/**
	 * Create new Statistics model linked to a main interface.
	 * Stats are automatically saved upon exit.
	 * If stats file is corrupted, throws RuntimeException
	 * @param main Main interface
	 */
	public StatisticsModel(MainInterface main) {
		//initiate session stats and main interface
		sessionStats = new StoredStats();
		application = main;
		//create new stats file if it does not exist
		File file = new File(STATS_PATH);
		if(!file.exists()){
			file.getParentFile().mkdirs();
			try {
				FileOutputStream fo = new FileOutputStream(file);
				ObjectOutputStream oos = new ObjectOutputStream(fo);
				oos.writeObject(new StoredStats());
				oos.close();
				fo.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//load global stats using the application model if possible
		if(main!=null){
			Object temp = application.loadObjectFromFile(STATS_PATH);
			if(temp instanceof StoredStats){
				globalStats = (StoredStats)temp;
			}else{
				throw new RuntimeException("Stats file corrupted or outdated, please delete stats file.");
			}
		}
	}
	/**
	 * Ends session.
	 * The current session data is stored to the global data and serialized.
	 */
	public void sessionEnd(){
		if(globalStats==null){return;}
		globalStats.addStats(sessionStats);
		sessionStats.clearStats();
		application.writeObjectToFile(STATS_PATH, globalStats);
	}
	/**
	 * Gets session stats.
	 * @return StoredStats
	 */
	public StoredStats getSessionStats(){
		return sessionStats;
	}
	/**
	 * Store stats for session. The session data will be saved upon exit.
	 * @param stats Stats to store.
	 * @throws Exception when trying to store null session
	 */
	public void storeSessionStats(StoredStats stats) throws Exception {
		if(stats==null){throw new Exception("Trying to reset session stats, not allowed. Use resetSessionStats().");}
		sessionStats = stats;
	}
	/**
	 * Resets session stats.
	 */
	public void resetSessionStats(){
		sessionStats = new StoredStats();
	}
	/**
	 * Gets global stats
	 * @return
	 */
	public StoredStats getGlobalStats(){
		return (globalStats!=null)?globalStats:null;
	}
	/**
	 * Set main application for interaction
	 * @param main Main class
	 */
	public void setMain(MainInterface main){
		application = main;
	}
	
}
