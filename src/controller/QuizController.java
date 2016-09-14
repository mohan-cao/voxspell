package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
		/*if(wordList.size()!=0){
			if(!onExit()){return;}
			String testWord = wordList.get(0);
			stats.addStat(Type.FAILED, testWord, 1);
		}
		saveStats();*/
		application.update(this, "quitToMainMenu_onClick");
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
		//send control signal to game to submit input,
		/*if(!gameEnded){
			validateAndSubmitInput();
		}else{
			saveStats();
			startGame(review);
		}*/
		application.update(this, "btnConfirm_onClick");
	}
	/**
	 * Validates input before sending it to the marking algorithm
	 */
	public void validateAndSubmitInput(){
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
		application.update(this, "submitWord");
		wordTextArea.setText("");
		wordTextArea.requestFocus();
	}
	public void setApplication(MainInterface app) {
		application = app;
	}
	/**
	 * Called when Application model notifies controller-view of view change
	 * 
	 */
	public void init(String[] args) {
		if(args!=null && args.length>0 && args[0].equals("failed")){
			application.update(this, "reviewGame");
		}else{
			application.update(this, "newGame");
		}
		
	}
	
	public String getTextAreaInput(){
		return wordTextArea.getText();
	}
	
	public void cleanup() {
		application.update(this, "cleanup");
	}
	@Override
	public void onModelChange(String signal) {
		switch(signal){
		case "gameStartConfigure":
			wordTextArea.setDisable(false);
			confirm.setText("Check");		
			wordTextArea.requestFocus();
			outputLabel.setText("Quiz start!");
			correctWordLabel.setText("Please spell the spoken words");
			break;
		case "resetGame":
			wordTextArea.setDisable(true);
			confirm.setText("Restart?");
			break;
		}
		if(signal.contains("masteredWord=")){
			outputLabel.setText("Well done");
			correctWordLabel.setText("Correct, the word is "+signal.split("masteredWord=")[1]);
			progress.setStyle("-fx-accent: lightgreen;");
		}else if(signal.contains("faultedWord=")){
			outputLabel.setText("Try again!");
			correctWordLabel.setText("Sorry, that wasn't quite right");
			progress.setStyle("-fx-accent: #ffbf44;");
		}else if(signal.contains("failedWord=")){
			outputLabel.setText("Incorrect");
			correctWordLabel.setText("The word was "+signal.split("failedWord=")[1]);
			progress.setStyle("-fx-accent: orangered;");
		}else if(signal.contains("lastChanceWord=")){
			outputLabel.setText("Last try!");
			correctWordLabel.setText("Let's slow it down...");
			progress.setStyle("-fx-accent: #ffbf44;");
		}else if(signal.contains("setProgress=")){
			progress.setProgress(Double.parseDouble(signal.split("setProgress=")[1]));
		}
	}
}
