package resources;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * The serializable spelling statistics class that stores all statistics for VoxSpell
 * @author Mohan Cao
 *
 */
public class StoredStats implements Serializable{
	private static final long serialVersionUID = 3L;
	private HashMap<String,Stats> _stats;
	private HashMap<Integer,Boolean> _unlockedLevels;
	/**
	 * Defines type of statistic being stored.
	 * @author Mohan Cao
	 *
	 */
	public enum Type{
		MASTERED,
		FAILED,
		FAULTED
	}
	/**
	 * Objects instantiated from this class represent raw statistics
	 * @author Mohan Cao
	 *
	 */
	class Stats implements Serializable{
		private static final long serialVersionUID = 3L;
		int mastered;
		int failed;
		int faulted;
		int level;
		/**
		 * Initialise stats at a certain level.
		 * @param lvl
		 */
		public Stats(int lvl){
			mastered=0;failed=0;faulted=0;level=lvl;
		}
		/**
		 * Initialise stats by combining 2 stats objects
		 * @param __stats
		 * @param _stats2
		 */
		public Stats(Stats _stats, Stats _stats2){
			this.mastered = _stats.mastered + _stats2.mastered;
			this.faulted = _stats.faulted + _stats2.faulted;
			this.failed = _stats.failed + _stats2.failed;
			this.level = _stats2.level;
		}
		/**
		 * Adds stats of type t to current stats object
		 * @param t Type
		 * @param i Stats
		 */
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
		/**
		 * Sets stats of type t to current stats object
		 * @param t Type
		 * @param i Stats
		 */
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
		/**
		 * Get stats of type t
		 * @param t Type
		 */
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
		/**
		 * Get level of current stats object
		 * @param t Type
		 */
		public int getLevel(){
			return level;
		}
		/**
		 * Get total sum of mastered+failed+faulted stats
		 * @return
		 */
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
		resetLevelProgress();
	}
	/**
	 * Unlocks a level.
	 * @param level
	 */
	public void unlockLevel(int level){
		_unlockedLevels.put(level, true);
	}
	/**
	 * Get a set of all unlocked levels
	 * @return
	 */
	public Set<Integer> getUnlockedLevelSet(){
		return _unlockedLevels.keySet();
	}
	/**
	 * Resets all level progress
	 */
	public void resetLevelProgress(){
		_unlockedLevels = new HashMap<Integer,Boolean>();
		_unlockedLevels.put(0, true);
		_unlockedLevels.put(1, true);
	}
	/**
	 * Gets the level of a word
	 * @param key
	 * @return
	 */
	public Integer getLevel(String key){
		return _stats.get(key).getLevel();
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
		resetLevelProgress();
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
			this.addStat(Type.MASTERED, key, other.getStat(Type.MASTERED, key), other.getLevel(key));
			this.addStat(Type.FAULTED, key, other.getStat(Type.FAULTED, key), other.getLevel(key));
			this.addStat(Type.FAILED, key, other.getStat(Type.FAILED, key), other.getLevel(key));
			this._unlockedLevels.putAll(other._unlockedLevels);
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
	 * Get collection of keys from stats of a certain level, defined by type
	 * @param type
	 * @return
	 */
	public Collection<String> getKeys(Type type, int level){
		HashSet<String> set = new HashSet<String>();
		for(String str : _stats.keySet()){
			if(_stats.get(str).get(type)>0 && _stats.get(str).getLevel()==level){
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
