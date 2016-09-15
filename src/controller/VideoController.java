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
		// TODO
		// here's some hints as to how to approach the MVC pattern:
		// - controller updates model on changes that occurred (namely, in this case, an initialization occurred)
		// - model updates view with new data, namely the Media source which is loaded.
		
		// The video controller acts as both a controller and a view as it has both "update" components
		// and "listener" components
		
		/*if (mediaPlayer != null){
			killMediaPlayer();
		}
		String source = "../resources/Gandalf%20Europop%20Nod.mp4"; //FIXME
		Media media = new Media(source);
		MediaPlayer mediaPlayer = new MediaPlayer(media);
		mediaPlayer.setAutoPlay(true);
		videoView.setMediaPlayer(mediaPlayer);*/
		application.update(this, "requestVideo");
		
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

	public void onExit() {
		// Once again optional
	}

	@Override
	public void onModelChange(String notificationString, Object... objectsParameters) {
		// Model has changed (video is now ready), so components need to be loaded
		switch(notificationString){
		case "videoReady":
			Media media = (Media) objectsParameters[0];
			MediaPlayer mediaPlayer = new MediaPlayer(media);
			mediaPlayer.setAutoPlay(true);
			videoView.setMediaPlayer(mediaPlayer);
			break;
		}
		
	}
}
