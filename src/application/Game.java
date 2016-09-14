package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import controller.QuizController;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import resources.StoredStats;
import resources.StoredStats.Type;

public class Game {
	private List<String> wordList;
	private boolean faulted;
	private boolean prevFaulted;
	private int wordListSize;
	private StoredStats stats;
	private boolean review;
	private boolean gameEnded;
	private MainInterface main;
	
	public Game(MainInterface app){
		main = app;
	}
	
	public List<String> wordList(){
		return wordList;
	}
	/**
	 * Saves statistics
	 */
	public void saveStats(){
		main.writeObjectToFile(MainInterface.STATS_PATH, stats);
	}
	/**
	 * Checks if game has ended
	 * @return true/false
	 */
	public boolean isGameEnded(){
		return gameEnded;
	}
	/**
	 * Helper method that gets stats from the file system path
	 * @return StoredStats
	 */
	private StoredStats getStatsFromFile(){
		//find stored stats
		Object obj = main.loadObjectFromFile(MainInterface.STATS_PATH);
		StoredStats stats = null;
		if(obj instanceof StoredStats) stats = (StoredStats) obj;
		return stats;
	}
	/**
	 * Gets word list from file system path
	 * @return whether the word list has been successfully fetched to the wordList variable
	 */
	private boolean getWordList(){
		try {
			File path = new File(main.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
			File file = new File(path.getParent()+"/wordlist");
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
				wordList.add(line);
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
	 */
	public void startGame(boolean practice){
		gameEnded=false;
		stats = getStatsFromFile();
		if(stats==null){
			stats = new StoredStats();
		}
		main.tell("gameStartConfigure");
		review=false; //assume not reviewing words
		if(practice){
			wordList.addAll(stats.getKeys(Type.FAILED));
			if(wordList.size()==0){
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("");
				alert.setHeaderText("No words to review");
				alert.setContentText("You haven't any words to review!\nDo a spelling quiz first.");
				Optional<ButtonType> response = alert.showAndWait();
				if(response.get()==ButtonType.OK){
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
				wordList = wordList.subList(0, (wordList.size()>=3)?3:wordList.size());
				main.sayWord(new int[]{1},wordList.get(0));
		}
		//set faulted=false for first word
		progress.setProgress(0);
		wordListSize=(wordList.size()!=0)?wordList.size():1;
		faulted=false;
	}
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
		if(!wordList.isEmpty()){
			int speed = 1;
			boolean prev2Faulted = prevFaulted;
			prevFaulted = faulted;
			String testWord = wordList.get(0);
			faulted=!word.toLowerCase().equals(testWord.toLowerCase());
			if(!faulted&&!prevFaulted){
				//mastered
				outputLabel.setText("Well done");
				correctWordLabel.setText("Correct, the word is "+testWord);
				progress.setStyle("-fx-accent: lightgreen;");
				faulted=false;
				stats.addStat(Type.MASTERED,testWord, 1);
				// if review, remove from failedlist
				stats.setStats(Type.FAILED, testWord, 0);
				wordList.remove(0);
			}else if(faulted&&!prevFaulted){
				//faulted once => set faulted
				outputLabel.setText("Try again!");
				correctWordLabel.setText("Sorry, that wasn't quite right");
				progress.setStyle("-fx-accent: #ffbf44;");
				speed = 2;
			}else if(!faulted&&prevFaulted){
				//correct after faulted => store faulted
				outputLabel.setText("Well done");
				correctWordLabel.setText("Correct, the word is "+testWord);
				progress.setStyle("-fx-accent: lightgreen;");
				stats.addStat(Type.FAULTED,testWord, 1);
				wordList.remove(0);
			}else if(review&&!prev2Faulted){
				//give one more chance in review, set speed to very slow
				outputLabel.setText("Last try!");
				correctWordLabel.setText("Let's slow it down...");
				progress.setStyle("-fx-accent: #ffbf44;");
				speed = 3;
			}else{
				//faulted twice => failed
				outputLabel.setText("Incorrect");
				correctWordLabel.setText("The word was "+testWord);
				progress.setStyle("-fx-accent: orangered;");
				faulted=false;
				stats.addStat(Type.FAILED, testWord, 1);
				wordList.remove(0);
			}
			if(wordList.size()!=0){
				application.sayWord(new int[]{speed},wordList.get(0));
			}else{
				wordTextArea.setDisable(true);
				confirm.setText("Restart?");
				gameEnded=true;
			}
			//set progressbars for progress through quiz and also denote additional separation for faulted words
			progress.setProgress((wordListSize-wordList.size()+((faulted)?0.5:0))/(double)wordListSize);
		}
	}
}
