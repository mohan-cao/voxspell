package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import resources.StoredStats.Type;
/**
 * Game model class
 * Contains the logic for evaluating words and storing game state, and acts as the Game data model.
 * Created on demand for "review" and "new" games.
 * 
 * Requires a MainInterface and StatisticsModel to function properly.
 * 
 * @author Mohan Cao
 * @author Ryan Macmillan
 *
 */
public class Game {
	public static final int WORDS_NUM = 10;
	public static final String WORDLIST = "spelling-lists.txt";
	public static final int SAY_SPEED_INTRO = 1;
	public static final int SAY_SPEED_DEFAULT = 1;
	public static final int SAY_SPEED_SLOW = 2;
	public static final int SAY_SPEED_VERYSLOW = 3;
	private StatisticsModel stats;
	private MainInterface main;
	private List<String> wordList;
	
	private boolean faulted;
	private boolean prevFaulted;
	private boolean review;
	private boolean gameEnded;
	
	private int wordListSize;
	private int _level;
	private int _correct;
	private int _incorrect;
	
	private String voiceType;
	
	public Game(MainInterface app, StatisticsModel statsModel){
		this(app,statsModel,1);
	}
	public Game(MainInterface app, StatisticsModel statsModel, int level){
		main = app;
		stats = statsModel;
		wordList = new LinkedList<String>();
		voiceType = "kal_diphone"; //FIXME: CHECK WHICH VOICES ARE AVAILABLE ON ECE MACHINE
		_level = level;
	}
	
	/**
	 * Get word list
	 * @return
	 * @author Mohan Cao
	 */
	public List<String> wordList(){
		return wordList;
	}
	/**
	 * Checks if game has ended
	 * @return true/false
	 * @author Mohan Cao
	 */
	public boolean isGameEnded(){
		return gameEnded;
	}
	public boolean isReview(){
		return review;
	}
	/**
	 * Toggles from kal_diphone voice to akl_nz_jdt_diphone or vice versa
	 * @author Ryan Macmillan
	 */
	public void changeVoice(){
		if(voiceType.equals("kal_diphone")){
			voiceType = "akl_nz_jdt_diphone";
		}
		else {
			voiceType = "kal_diphone";
		}
	}
	/**
	 * Returns voice type
	 * @return
	 */
	public String getVoice(){
		final String voice = voiceType;
		return voice;
	}
	/**
	 * Get current level.
	 * @return Mohan Cao
	 */
	public int level() {
		return _level;
	}
	/**
	 * Gets word list from file system path
	 * @return whether the word list has been successfully fetched to the wordList variable
	 * @author Mohan Cao
	 * @author Ryan MacMillan
	 */
	private boolean getWordList(){
		try {
			File path = new File(main.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
			File file = new File(path.getParent()+"/"+WORDLIST);
			if(!file.exists()){
				Alert alert = new Alert(AlertType.ERROR);
				alert.setContentText("You don't have a word list!\nPlease put one in the folder that you ran the spelling app from.");
				alert.showAndWait();
				return false;
			}
			FileReader fi = new FileReader(file);
			BufferedReader br = new BufferedReader(fi);
			String line = null;
			while((line= br.readLine())!=null){
				if(line.contains("%Level "+_level)){
					line = line.split("%Level ")[1];
					_level = Integer.parseInt(line);
					line = br.readLine();
					while(!line.startsWith("%Level ")){
						wordList.add(line);
						line = br.readLine();
					}
					break;
				}
			}
			Collections.shuffle(wordList);
			br.close();
			return true;
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * Starts game in previous mode
	 */
	public void startGame(){
		startGame(review);
	}
	/**
	 * Starts the game with option for practice mode (review)
	 * @param practice review -> true
	 * @author Mohan Cao (Assignment 2)
	 */
	public void startGame(boolean practice){
		_correct=0;
		_incorrect=0;
		gameEnded=false;
		main.tell("gameStartConfigure", _level);
		review=false; //assume not reviewing words
		if(practice){
			HashSet<String> set = new HashSet<String>();
			set.addAll(stats.getGlobalStats().getKeys(Type.FAILED,_level));
			set.addAll(stats.getSessionStats().getKeys(Type.FAILED,_level));
			wordList.addAll(set);
			if(wordList.size()==0){
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("");
				alert.setHeaderText("No words to review");
				alert.setContentText("You haven't any words to review!\nDo a spelling quiz first.");
				alert.showAndWait();
				gameEnded=true;
				main.requestSceneChange("levelMenu","failed");
				return;
			}
			Collections.shuffle(wordList);
			review=true; //reviewing words
		}else{
			getWordList();
		}
		if(!wordList.isEmpty()){
				wordList = wordList.subList(0, (wordList.size()>=WORDS_NUM)?WORDS_NUM:wordList.size());
				main.sayWord(new int[]{SAY_SPEED_INTRO,SAY_SPEED_DEFAULT},voiceType,"Please spell the spoken words.",wordList.get(0));
		}
		//set faulted=false for first word
		main.tell("setProgress",0d);
		wordListSize=(wordList.size()!=0)?wordList.size():1;
		faulted=false;
	}
	
	/**
	 * Repeat the word using festival
	 * @author Ryan Macmillan
	 */
	public void repeatWord(){
		if(!gameEnded){
		main.sayWord(new int[]{SAY_SPEED_DEFAULT}, voiceType, wordList.get(0));
		}
	}
	
	/**
	 * Called when game is going to exit.
	 * @return true (default) or false to indicate cancellation of exiting
	 * @author Mohan Cao
	 */
	public boolean onExit(){
		if(gameEnded){
			return true;
		}
		Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Are you sure you want to quit?");
        alert.setContentText("You will lose progress\nIf you are in the middle of a word,\nit will be incorrect");
        Optional<ButtonType> response = alert.showAndWait();
        if(response.get()==ButtonType.OK){
        	return true;
        }else{
        	return false;
        }
	}
	/**
	 * Check word against game logic
	 * @param word
	 * @author Mohan Cao (Assignment 2)
	 */
	public void submitWord(String word){
		if(!gameEnded){
			int speed = SAY_SPEED_DEFAULT;
			boolean prev2Faulted = prevFaulted;
			prevFaulted = faulted;
			String testWord = wordList.get(0);
			faulted=!word.toLowerCase().equals(testWord.toLowerCase());
			if(!faulted&&!prevFaulted){
				//mastered
				main.tell("masteredWord",testWord);
				faulted=false;
				stats.getSessionStats().addStat(Type.MASTERED,testWord, 1, _level);
				_correct++;
				// if review, remove from failedlist
				stats.getGlobalStats().setStats(Type.FAILED, testWord, 0);
				stats.getSessionStats().setStats(Type.FAILED, testWord, 0);
				wordList.remove(0);
			}else if(faulted&&!prevFaulted){
				//faulted once => set faulted
				main.tell("faultedWord",testWord);
				speed = SAY_SPEED_SLOW;
				main.sayWord(new int[]{speed},voiceType, testWord);
				main.sayWord(new int[]{speed},voiceType, "...");
			}else if(!faulted&&prevFaulted){
				//correct after faulted => store faulted
				main.tell("masteredWord",testWord);
				stats.getSessionStats().addStat(Type.FAULTED,testWord, 1, _level);
				_incorrect++;
				wordList.remove(0);
			}else if(review&&!prev2Faulted){
				//give one more chance in review, set speed to very slow
				main.tell("lastChanceWord",testWord);
				speed = SAY_SPEED_VERYSLOW;
				main.sayWord(new int[]{speed},voiceType, testWord);
				main.sayWord(new int[]{speed},voiceType, "...");
			}else{
				//faulted twice => failed
				main.tell("failedWord",testWord);
				faulted=false;
				stats.getSessionStats().addStat(Type.FAILED, testWord, 1, _level);
				wordList.remove(0);
				_incorrect++;
			}
			if(wordList.size()!=0){
				main.sayWord(new int[]{speed},voiceType, wordList.get(0));
			}else{
				//end game
				if(prevFaulted||faulted||prev2Faulted){
					main.tell("resetGame",_correct,(_correct+_incorrect),testWord);
				}else{
					main.tell("resetGame",_correct,(_correct+_incorrect));
				}
				
				if((_correct/(double)(_incorrect+_correct))>=0.9){
					stats.getSessionStats().unlockLevel(_level+1);
					main.tell("showRewards");
				}
				gameEnded=true;
			}
			//set progressbars for progress through quiz and also denote additional separation for faulted words
			main.tell("setProgress",(wordListSize-wordList.size()+((faulted)?0.5:0))/(double)wordListSize);
		}
	}
	
	
}
