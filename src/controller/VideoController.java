package controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.media.MediaView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class VideoController extends SceneController {

	@FXML
	protected MainInterface application;

	@FXML
	private MediaView videoView;

	@FXML
	private Button stopButton;

	@FXML
	private Button menuButton;

	private MediaPlayer mediaPlayer;
	
	@FXML
	void stopVideo(ActionEvent event) {
		mediaPlayer.stop();
	}

	@FXML
	void quitToMainMenu(ActionEvent event) {
		if (mediaPlayer != null){
			killMediaPlayer();
		}
		application.requestsSceneChange("mainMenu");
	}

	/**
	 * All controllers reference back to application for model/view changes
	 * 
	 * @param app
	 */
	public void setApplication(MainInterface app) {
		application = app;
	}
	
	private void killMediaPlayer(){
		mediaPLayer.stop();
		mediaPlayer = null;
	}

	/**
	 * Optional initialization method (upon controller creation)
	 */
	@FXML
	public void initialize() {
		if (mediaPlayer != null){
			resetMediaPlayer();
		}
		String source = "resources/Gandalf Europop Nod.mp4"; //FIXME
		Media media = new Media(source)
		MediaPlayer mediaPlayer = new MediaPlayer(media);
		mediaPlayer.setAutoPlay(true);
		videoView.setMediaPlayer(mediaPlayer);
		
	}

	/**
	 * Controller is initialised with initialisation arguments
	 * 
	 * @param args
	 */
	public void init(String[] args) {

	}

	public void cleanup() {
		// Optional override
	}

	public boolean onExit() {
		// Once again optional
		return true;
	}

	/**
	 * Notify view of changes in the model.
	 */
	public void onModelChange(Class<? extends Node> updatedPart, String fieldName) {

	}
}
