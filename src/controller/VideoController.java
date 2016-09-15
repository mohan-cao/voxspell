package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;

import application.MainInterface;
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
		application.requestSceneChange("mainMenu");
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
		mediaPlayer.stop();
		mediaPlayer = null;
	}

	/**
	 * Optional initialization method (upon controller creation)
	 */
	@FXML
	public void initialize() {
		if (mediaPlayer != null){
			killMediaPlayer();
		}
		String source = "resources/Gandalf Europop Nod.mp4"; //FIXME
		Media media = new Media(source);
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
		if (mediaPlayer != null){ //FIXME: do I need this implementation?
			killMediaPlayer();
		}
	}

	public void onExit() {
		// Once again optional
	}

	@Override
	public void onModelChange(String fieldName) {
		// TODO Auto-generated method stub
		
	}
}
