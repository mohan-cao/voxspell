package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import controller.QuizController;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import resources.StoredStats.Type;

public class Game {
	public static final int WORDS_NUM = 10;
	private List<String> wordList;
	private boolean faulted;
	private boolean prevFaulted;
	private int wordListSize;
	private StatisticsModel stats;
	private boolean review;
	private boolean gameEnded;
	private int _level;
	private MainInterface main;
	
	public Game(MainInterface app, StatisticsModel statsModel){
		this(app,statsModel,1);
	}
	public Game(MainInterface app, StatisticsModel statsModel, int level){
		main = app;
		stats = statsModel;
		wordList = new LinkedList<String>();
		_level = level;
	}
	/**
	 * Get word list
	 * @return
	 */
	public List<String> wordList(){
		return wordList;
	}
	/**
	 * Checks if game has ended
	 * @return true/false
	 */
	public boolean isGameEnded(){
		return gameEnded;
	}
	/**
	 * Get current level.
	 * @return
	 */
	public int level() {
		return _level;
	}
	/**
	 * Gets word list from file system path
	 * @return whether the word list has been successfully fetched to the wordList variable
	 */
	private boolean getWordList(){
		try {
			File path = new File(main.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
			File file = new File(path.getParent()+"/spelling-lists.txt");
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
			System.out.println(wordList);
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
	 */
	public void startGame(boolean practice){
		gameEnded=false;
		main.tell("gameStartConfigure");
		review=false; //assume not reviewing words
		if(practice){
			wordList.addAll(stats.getGlobalStats().getKeys(Type.FAILED));
			if(wordList.size()==0){
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("");
				alert.setHeaderText("No words to review");
				alert.setContentText("You haven't any words to review!\nDo a spelling quiz first.");
				Optional<ButtonType> response = alert.showAndWait();
				if(response.get()==ButtonType.OK){
					gameEnded=true;
					main.update(new QuizController(), "quitToMainMenu_onClick");
					main.requestSceneChange("mainMenu");
				}
				return;
			}
			Collections.shuffle(wordList);
			review=true; //reviewing words
		}else{
			getWordList();
		}
		if(!wordList.isEmpty()){
				wordList = wordList.subList(0, (wordList.size()>=WORDS_NUM)?WORDS_NUM:wordList.size());
				main.sayWord(new int[]{1},wordList.get(0));
		}
		//set faulted=false for first word
		main.tell("setProgress",0d);
		wordListSize=(wordList.size()!=0)?wordList.size():1;
		faulted=false;
	}
	/**
	 * Called when game is going to exit.
	 * @return true (default) or false to indicate cancellation of exiting
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
        }
        return false;
	}
	/**
	 * Check word against game logic
	 * @param word
	 */
	public void submitWord(String word){
		if(!gameEnded){
			int speed = 1;
			boolean prev2Faulted = prevFaulted;
			prevFaulted = faulted;
			String testWord = wordList.get(0);
			faulted=!word.toLowerCase().equals(testWord.toLowerCase());
			if(!faulted&&!prevFaulted){
				//mastered
				main.tell("masteredWord",testWord);
				faulted=false;
				stats.getSessionStats().addStat(Type.MASTERED,testWord, 1, _level);
				// if review, remove from failedlist
				stats.getGlobalStats().setStats(Type.FAILED, testWord, 0);
				wordList.remove(0);
			}else if(faulted&&!prevFaulted){
				//faulted once => set faulted
				main.tell("faultedWord",testWord);
				speed = 2;
			}else if(!faulted&&prevFaulted){
				//correct after faulted => store faulted
				main.tell("masteredWord",testWord);
				stats.getSessionStats().addStat(Type.FAULTED,testWord, 1, _level);
				wordList.remove(0);
			}else if(review&&!prev2Faulted){
				//give one more chance in review, set speed to very slow
				main.tell("lastChanceWord",testWord);
				speed = 3;
			}else{
				//faulted twice => failed
				main.tell("failedWord",testWord);
				faulted=false;
				stats.getSessionStats().addStat(Type.FAILED, testWord, 1, _level);
				wordList.remove(0);
			}
			if(wordList.size()!=0){
				main.sayWord(new int[]{speed},wordList.get(0));
			}else{
				main.tell("resetGame");
				gameEnded=true;
			}
			//set progressbars for progress through quiz and also denote additional separation for faulted words
			main.tell("setProgress",(wordListSize-wordList.size()+((faulted)?0.5:0))/(double)wordListSize);
		}
	}
	
}
