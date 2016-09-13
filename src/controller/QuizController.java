package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import application.MainInterface;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import resources.StoredStats;
import resources.StoredStats.Type;

public class QuizController extends SceneController{
	@FXML private Label outputLabel;
	@FXML private Label correctWordLabel;
	@FXML private TextArea wordTextArea;
	@FXML private Button confirm;
	@FXML private ProgressBar progress;
	@FXML private List<String> wordList;
	@FXML private boolean faulted;
	@FXML private boolean prevFaulted;
	@FXML private int wordListSize;
	@FXML private StoredStats stats;
	@FXML private Task<Void> festivalTask;
	@FXML private boolean review;
	@FXML private boolean gameEnded;
	
	
	@FXML
	public void initialize(){
		//init
		wordList = new LinkedList<String>();
	}
	/**
	 * Listener for quit to main menu navigation button
	 * @param me MouseEvent
	 */
	@FXML
	public void quitToMainMenu(MouseEvent me){
		//save and quit to main menu
		if(wordList.size()!=0){
			if(!onExit()){return;}
			String testWord = wordList.get(0);
			stats.addStat(Type.FAILED, testWord, 1);
		}
		saveStats();
		application.requestSceneChange("mainMenu");
	}
	/**
	 * Listener for text area key entered
	 * Prevents enter from entering a newline character
	 * @param ke KeyEvent from textArea
	 */
	@FXML
	public void textAreaEnter(KeyEvent ke){
		if(ke.getCode()==KeyCode.ENTER){
			ke.consume();
			validateAndSubmitInput();
		}
	}
	/**
	 * Listener for text area character typed (after being typed)
	 * @param ke KeyEvent from textArea
	 */
	@FXML
	public void textAreaType(KeyEvent ke){
		if(ke.getCharacter().matches("[^A-Za-z]")){
			ke.consume();
		}
	}
	/**
	 * Listener for confirmation button (for marking of the word)
	 * @param me MouseEvent
	 */
	@FXML
	public void btnConfirm(MouseEvent me){
		if(!gameEnded){
			validateAndSubmitInput();
		}else{
			saveStats();
			startGame(review);
		}
	}
	/**
	 * Validates input before sending it to the marking algorithm
	 */
	private void validateAndSubmitInput(){
		if(wordTextArea.getText().isEmpty()){
			//prevent accidental empty string submission for user acceptance, show brief tooltip
			Tooltip tip = new Tooltip("Please enter a word!");
			tip.setAutoHide(true);
			tip.show(wordTextArea, wordTextArea.localToScreen(wordTextArea.getBoundsInLocal()).getMaxX(), wordTextArea.localToScreen(wordTextArea.getBoundsInLocal()).getMinY());
			new Thread(new Task<Void>(){
				@Override
				protected Void call() throws Exception {
					Thread.sleep(1000);
					return null;
				}
				public void succeeded(){
					tip.hide();
					Tooltip.uninstall(wordTextArea, tip);
				}
			}).start();
			return;
		}
		if(wordTextArea.getText().length()>50){
			//prevent overflow, show tooltip
			Tooltip tip = new Tooltip("Word is far too long!");
			tip.setAutoHide(true);
			tip.show(wordTextArea, wordTextArea.localToScreen(wordTextArea.getBoundsInLocal()).getMaxX(), wordTextArea.localToScreen(wordTextArea.getBoundsInLocal()).getMinY());
			new Thread(new Task<Void>(){
				@Override
				protected Void call() throws Exception {
					Thread.sleep(1000);
					return null;
				}
				public void succeeded(){
					tip.hide();
					Tooltip.uninstall(wordTextArea, tip);
				}
			}).start();
			return;
		}
		submitWord(wordTextArea.getText());
		wordTextArea.setText("");
		wordTextArea.requestFocus();
	}
	/**
	 * Helper method that gets stats from the file system path
	 * @return StoredStats
	 */
	private StoredStats getStatsFromFile(){
		//find stored stats
		Object obj = application.loadObjectFromFile(MainInterface.STATS_PATH);
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
			File path = new File(application.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
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
	 * Creates a new process of Festival that says a word
	 * @param speed
	 * @param words
	 */
	private void sayWord(int[] speed, String... words){
		ProcessBuilder pb = new ProcessBuilder("/bin/bash","-c","festival");
		try {
			if(festivalTask!=null){
				festivalTask.cancel(true);
			}
			Process process = pb.start();
			PrintWriter pw = new PrintWriter(process.getOutputStream());
			for(int i=0;i<words.length;i++){
				if(i<speed.length){
					pw.println("(Parameter.set 'Duration_Stretch "+speed[i]+")");
				}
				pw.println("(SayText \""+words[i]+"\")");
			}
			pw.println("(quit)");
			pw.close();
			festivalTask = new Task<Void>(){
				@Override
				protected Void call() throws Exception {
					process.waitFor();
					return null;
				}
			};
			new Thread(festivalTask).start();
			
		} catch (IOException e) {
			//couldn't find festival
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("You don't have Festival, the Text to Speech synthesiser required for this to work");
			alert.showAndWait();
		}
	}
	/**
	 * Check word against game logic
	 * @param word
	 */
	private void submitWord(String word){
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
				sayWord(new int[]{speed},wordList.get(0));
			}else{
				wordTextArea.setDisable(true);
				confirm.setText("Restart?");
				gameEnded=true;
			}
			//set progressbars for progress through quiz and also denote additional separation for faulted words
			progress.setProgress((wordListSize-wordList.size()+((faulted)?0.5:0))/(double)wordListSize);
		}
	}
	/**
	 * Saves statistics
	 */
	private void saveStats(){
		application.writeObjectToFile(MainInterface.STATS_PATH, stats);
	}
	
	public void setApplication(MainInterface app) {
		application = app;
	}
	/**
	 * Called when Application model notifies controller-view of view change
	 * 
	 */
	public void init(String[] args) {
		//find stored stats
		wordList = new LinkedList<String>();
		if(args!=null && args.length>0 && args[0].equals("failed")){
			startGame(true);
		}else{
			startGame(false);
		}
	}
	/**
	 * Starts the game with option for practice mode (review)
	 * @param practice review -> true
	 */
	private void startGame(boolean practice){
		gameEnded=false;
		stats = getStatsFromFile();
		if(stats==null){
			stats = new StoredStats();
		}
		wordTextArea.setDisable(false);
		confirm.setText("Check");		
		wordTextArea.requestFocus();
		outputLabel.setText("Quiz start!");
		correctWordLabel.setText("Please spell the spoken words");
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
					quitToMainMenu(null);
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
				sayWord(new int[]{1},wordList.get(0));
		}
		//set faulted=false for first word
		progress.setProgress(0);
		wordListSize=(wordList.size()!=0)?wordList.size():1;
		faulted=false;
	}
	
	public void cleanup() {
		//save and quit
		if(wordList.size()!=0){
			String testWord = wordList.get(0);
			stats.addStat(Type.FAILED, testWord, 1);
		}
		saveStats();
	}
	public boolean onExit() {
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
	@Override
	public void onModelChange(Class<? extends Node> updatedPart, String fieldName) {
		// TODO Auto-generated method stub
		
	}
}
