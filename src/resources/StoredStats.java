package resources;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class StoredStats implements Serializable{
	private static final long serialVersionUID = 1L;
	private HashMap<String,Stats> _stats;
	private int _currentLevel;
	private HashMap<Integer,Boolean> _unlockedLevels;
	
	public enum Type{
		MASTERED,
		FAILED,
		FAULTED
	}
	class Stats implements Serializable{
		private static final long serialVersionUID = 1L;
		int mastered;
		int failed;
		int faulted;
		int level;
		public Stats(int lvl){
			mastered=0;failed=0;faulted=0;level=lvl;
		}
		public Stats(Stats __stats, Stats _stats2){
			this.mastered = __stats.mastered + _stats2.mastered;
			this.faulted = __stats.faulted + _stats2.faulted;
			this.failed = __stats.failed + _stats2.failed;
		}
		public void add(Type t, int i){
			switch(t){
			case MASTERED:
				mastered+=i;
				break;
			case FAILED:
				failed+=i;
				break;
			case FAULTED:
				faulted+=i;
				break;
			}
		}
		public void set(Type t, int i){
			switch(t){
			case MASTERED:
				mastered=i;
				break;
			case FAILED:
				failed=i;
				break;
			case FAULTED:
				faulted=i;
				break;
			}
		}
		public Integer get(Type t){
			switch(t){
			case MASTERED:
				return mastered;
			case FAILED:
				return failed;
			case FAULTED:
				return faulted;
			}
			return null;
		}
		public int getLevel(){
			return level;
		}
		public int getTotal(){
			return mastered+failed+faulted;
		}
	}
	/**
	 * Default stats constructor
	 * Initialises at level 0 (global) by default.
	 */
	public StoredStats(){
		clearStats();
		_currentLevel=0;
		_unlockedLevels = new HashMap<Integer,Boolean>();
		_unlockedLevels.put(0, true);
		_unlockedLevels.put(1, true);
	}
	/**
	 * Unlocks a level.
	 * @param level
	 */
	public void unlockLevel(int level){
		_unlockedLevels.put(level, true);
	}
	/**
	 * Get current level
	 * @return
	 */
	public int getCurrentLevel(){
		return _currentLevel;
	}
	/**
	 * Checks if level is locked. If level does not exist, it is locked.
	 * @param level
	 * @return
	 */
	public boolean isLevelLocked(int level){
		if(_unlockedLevels.get(level)!=null&&_unlockedLevels.get(level).booleanValue()==true){
			return false;
		}
		return true;
	}
	/**
	 * Clears stats for all stats.
	 */
	public void clearStats(){
		_stats = new HashMap<String,Stats>();
	}
	/**
	 * Sets stats to all stats, defined by type, word, and occurrences
	 * @param type
	 * @param value
	 * @param number
	 * @param _level 
	 * @return
	 */
	public boolean setStats(Type type, String value, Integer number){
		if(_stats.get(value)==null){return false;}
		_stats.get(value).set(type, number);
		return true;
	}
	/**
	 * Adds stats to all stats, defined by type, word, and occurrences for a level.
	 * @param type type
	 * @param value word
	 * @param n occurrences
	 * @param level level
	 */
	public void addStat(Type type, String value, Integer n, Integer level){
		if(_stats.get(value)==null){
			_stats.put(value, new Stats(level));
		}
		_stats.get(value).add(type, n);
	}
	/**
	 * Adds stats object to existing stats object. Does not return new object.
	 * @param other
	 */
	public void addStats(StoredStats other){
		for(String key : other.getKeys()){
			int level = other.getCurrentLevel();
			this.addStat(Type.MASTERED, key, other.getStat(Type.MASTERED, key), level);
			this.addStat(Type.FAULTED, key, other.getStat(Type.FAULTED, key), level);
			this.addStat(Type.FAILED, key, other.getStat(Type.FAILED, key), level);
		}
	}
	/**
	 * Removes key from all stats.
	 * @param key
	 * @return
	 */
	public boolean removeKey(String key){
		return (_stats.remove(key)!=null);
	}
	/**
	 * Get collection of keys from all stats
	 * @return
	 */
	public Collection<String> getKeys(){
		return _stats.keySet();
	}
	/**
	 * Get collection of keys from all stats, defined by type
	 * @param type
	 * @return
	 */
	public Collection<String> getKeys(Type type){
		HashSet<String> set = new HashSet<String>();
		for(String str : _stats.keySet()){
			if(_stats.get(str).get(type)>0){
				set.add(str);
			}
		}
		return set;
	}
	/**
	 * Get total stats for certain level, defined by type
	 * @param level
	 * @param type
	 * @return
	 */
	public Integer getTotalStatsOfLevel(int level, Type type){
		int num = 0;
		for(String str : _stats.keySet()){
			if(level==_stats.get(str).getLevel()){
			num += _stats.get(str).get(type);
			}
		}
		return num;
	}
	/**
	 * Get total stats for all levels, defined by type
	 * @param type
	 * @return
	 */
	public Integer getTotalStatsOfType(Type type){
		int num = 0;
		for(String str : _stats.keySet()){
			num += _stats.get(str).get(type);
		}
		return num;
	}
	/**
	 * Get stats for all levels, defined by type of statistic and key
	 * @param type
	 * @param key
	 * @return
	 */
	public Integer getStat(Type type, String key){
		if(type == Type.MASTERED){return _stats.get(key).get(type);}
		else if(type == Type.FAILED){return _stats.get(key).get(type);}
		else if(type == Type.FAULTED){return _stats.get(key).get(type);}
		else{
			return null;
		}
	}
}
