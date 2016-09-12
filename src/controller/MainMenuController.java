package controller;

import application.MainInterface;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

public class MainMenuController implements SceneController{
	@FXML private MainInterface application;
	@FXML private Button nQuizBtn;
	@FXML private Button vStatsBtn;
	@FXML private Button cStatsBtn;
	@FXML private Button rMistakesBtn;
	/**
	 * Listener for new quiz navigation button
	 * @param e MouseEvent
	 */
	@FXML public void newQuiz(MouseEvent e){
		application.requestSceneChange("quizMenu");
	}
	/**
	 * Listener for Stats view navigation button
	 * @param e MouseEvent
	 */
	@FXML public void viewStats(MouseEvent e){
		application.requestSceneChange("statsMenu");
	}
	/**
	 * Listener for review mistakes view navigation button
	 * @param e MouseEvent
	 */
	@FXML public void reviewMistakes(MouseEvent e){
		application.requestSceneChange("quizMenu","failed");
	}
	@FXML
	public void initialize() {
	}
	public void setApplication(MainInterface app) {
		application = app;
	}
	public void init(String[] args) {
		// Nothing to initialise
	}
	public void cleanup() {
		// Nothing to cleanup
	}
	public boolean onExit() {
		// Nothing to confirm
		return true;
	}
}
