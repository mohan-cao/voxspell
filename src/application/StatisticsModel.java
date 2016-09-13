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
	
	public StatisticsModel() {
		File file = new File(STATS_PATH);
		if(file.exists()){return;}
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
	 * Set main application for interaction
	 * @param main Main class
	 */
	public void setMain(MainInterface main){
		application = main;
	}
}
