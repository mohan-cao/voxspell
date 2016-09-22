package controller;

import application.ModelUpdateEvent;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Paint;

/**
 * A view-controller that is bound to the quiz_layout fxml
 * @author Mohan Cao
 *
 */
public class QuizController extends SceneController{
	@FXML private Label outputLabel;
	@FXML private Label correctWordLabel;
	@FXML private TextArea wordTextArea;
	@FXML private Button confirm;
	@FXML private Button voiceBtn;
	@FXML private Button repeatBtn;
	@FXML private ProgressBar progress;
	@FXML private FlowPane buttonPanel;
	
	@FXML
	public void initialize(){
		Tooltip tts = new Tooltip("Change TTS voice");
		Tooltip repeat = new Tooltip("Say the word again");
		Tooltip.install(voiceBtn,tts);
		Tooltip.install(repeatBtn, repeat);
	}
	
	/**
	 * Listener for change voice button
	 * @param me MouseEvent: mouse clicked button
	 * @author Ryan Macmillan
	 */
	@FXML
	public void changeVoice(MouseEvent me){
		application.update(new ModelUpdateEvent(this, "changeVoice_onClick"));
	}
	
	/**
	 * Listener for repeat word button
	 * @param me MouseEvent: mouse clicked button
	 * @author Ryan MacMillan
	 */
	@FXML
	public void repeatWord(MouseEvent me){
		application.update(new ModelUpdateEvent(this, "repeatWord_onClick"));
	}
	
	/**
	 * Listener for quit to main menu navigation button
	 * @param me MouseEvent
	 * @author Mohan Cao
	 */
	@FXML
	public void quitToMainMenu(MouseEvent me){
		application.update(new ModelUpdateEvent(this, "quitToMainMenu_onClick"));
	}
	/**
	 * Listener for text area key entered
	 * Prevents enter from entering a newline character
	 * @param ke KeyEvent from textArea
	 * @author Mohan Cao
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
	 * @author Mohan Cao
	 */
	@FXML
	public void textAreaType(KeyEvent ke){
		if(ke.getCharacter().matches("[^A-Za-z'\\s]")){
			ke.consume();
		}
	}
	/**
	 * Listener for confirmation button (for marking of the word)
	 * @param me MouseEvent
	 * @author Mohan Cao
	 */
	@FXML
	public void btnConfirm(MouseEvent me){
		application.update(new ModelUpdateEvent(this, "btnConfirm_onClick"));
	}
	@FXML
	public void btnNextLevel(MouseEvent me){
		application.update(new ModelUpdateEvent(this, "nextLevel"));
	}
	@FXML
	public void btnVideoReward(MouseEvent me){
		application.update(new ModelUpdateEvent(this, "videoReward"));
	}
	@FXML
	public void btnSpeedyReward(MouseEvent me){
		application.update(new ModelUpdateEvent(this,"speedyReward"));
	}
	/**
	 * Validates input before sending it to the marking algorithm
	 * @author Mohan Cao
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
		application.update(new ModelUpdateEvent(this, "submitWord"));
		wordTextArea.setText("");
		wordTextArea.requestFocus();
	}
	/**
	 * Called when Application model notifies controller-view of view change
	 * 
	 */
	public void init(String[] args) {
		if(args!=null && args.length>0 && args[0].equals("failed")){
			application.update(new ModelUpdateEvent(this, "reviewGame"));
		}else{
			application.update(new ModelUpdateEvent(this, "newGame"));
		}
		buttonPanel.setVisible(false);
		
	}
	/**
b	 * Gets text area input
	 * @return textarea text
	 * @author Mohan Cao
	 */
	public String getTextAreaInput(){
		return wordTextArea.getText();
	}
	
	public void cleanup() {
		application.update(new ModelUpdateEvent(this, "cleanup"));
	}
	public void onModelChange(String signal, Object... objectParameters) {
		switch(signal){
		case "gameStartConfigure":
			buttonPanel.setVisible(false);
			wordTextArea.setDisable(false);
			confirm.setText("Check");		
			wordTextArea.requestFocus();
			outputLabel.setText("Level "+(int)objectParameters[0]);
			outputLabel.setTextFill(Paint.valueOf("black"));
			correctWordLabel.setText("Please spell the spoken words.\n"
					+ "Feel free to replay the word anytime with the right side buttons.\n"
					+ "You may also change the voice if you find it necessary.");
			break;
		case "resetGame":
			outputLabel.setText("Well done!");
			outputLabel.setTextFill(Paint.valueOf("black"));
			if(objectParameters.length==2){
				correctWordLabel.setText("You got "+objectParameters[0]+" out of "+objectParameters[1]+" words correct.");
			}else if(objectParameters.length==3){
				correctWordLabel.setText("You got "+objectParameters[0]+" out of "+objectParameters[1]+" words correct."
					+ "\nThe last word was \""+objectParameters[2]+"\"");
			}
			wordTextArea.setDisable(true);
			confirm.setText("Restart?");
			break;
		case "masteredWord":
			outputLabel.setText("Well done");
			outputLabel.setTextFill(Paint.valueOf("#44a044"));
			correctWordLabel.setText("Correct, the word is \""+objectParameters[0]+"\"");
			progress.setStyle("-fx-accent: lightgreen;");
			break;
		case "faultedWord":
			outputLabel.setText("Try again!");
			outputLabel.setTextFill(Paint.valueOf("#cf8f14"));
			correctWordLabel.setText("Sorry, that wasn't quite right");
			progress.setStyle("-fx-accent: #ffbf44;");
			break;
		case "lastChanceWord":
			outputLabel.setText("Last try!");
			outputLabel.setTextFill(Paint.valueOf("#cf8f14"));
			correctWordLabel.setText("Let's slow it down...");
			progress.setStyle("-fx-accent: #ffbf44;");
			break;
		case "failedWord":
			outputLabel.setText("Incorrect");
			outputLabel.setTextFill(Paint.valueOf("orangered"));
			correctWordLabel.setText("The word was \""+objectParameters[0]+"\"");
			progress.setStyle("-fx-accent: orangered;");
			break;
		case "setProgress":
			progress.setProgress(Double.class.cast(objectParameters[0]));
			break;
		case "showRewards":
			buttonPanel.setVisible(true);
			break;
		}
	}
}
