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
	
	
	
	@FXML
	public void initialize(){
	}
	/**
	 * Listener for quit to main menu navigation button
	 * @param me MouseEvent
	 */
	@FXML
	public void quitToMainMenu(MouseEvent me){
		//save and quit to main menu
		if(application.){
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
				application.sayWord(new int[]{1},wordList.get(0));
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
