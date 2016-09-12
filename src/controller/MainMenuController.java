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
	
	@FXML public void newQuiz(MouseEvent e){
		application.requestSceneChange("quizMenu");
	}
	@FXML public void viewStats(MouseEvent e){
		application.requestSceneChange("statsMenu");
	}
	@FXML public void reviewMistakes(MouseEvent e){
		application.requestSceneChange("quizMenu","failed");
	}
	@FXML
	public void initialize() {
	}
	public void setApplication(MainInterface app) {
		application = app;
	}
	@Override
	public void init(String[] args) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}
}
