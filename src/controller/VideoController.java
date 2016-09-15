package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;

import application.MainInterface;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;

public class VideoController extends SceneController {

	@FXML
	private MediaView videoView;

	@FXML
	private Button stopButton;

	@FXML
	private Button menuButton;

	private MediaPlayer mediaPlayer;
	
	@FXML
	private void stopVideo(MouseEvent event) {
		if(mediaPlayer==null)return;
		mediaPlayer.stop();
	}

	@FXML
	private void quitToMainMenu(MouseEvent event) {
		if (mediaPlayer != null){
			killMediaPlayer();
		}
		application.requestSceneChange("mainMenu");
	}
	
	private void killMediaPlayer(){
		if(mediaPlayer==null)return;
		mediaPlayer.stop();
		mediaPlayer = null;
	}

	/**
	 * Optional initialization method (upon controller creation)
	 */
	@FXML
	public void initialize() {
		// as of right now in the initialize() method, there is no access to the application field (it is null)
	}

	/**
	 * Controller is initialised with initialisation arguments
	 * 
	 * @param args
	 */
	public void init(String[] args) {
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

	public void cleanup() {
		if (mediaPlayer != null){ //FIXME: do I need this implementation?
			killMediaPlayer();
		}
	}

	public void onExit() {
		// Once again optional
	}

	@Override
	public void onModelChange(String notificationString, Object... objectsParameters) {
		// Model has changed (video is now ready), so components need to be loaded
		switch(notificationString){
		case "videoReady":
			if(objectsParameters[0]==null){System.err.println("can't find resource");return;}
			Media media = (Media) objectsParameters[0];
			System.out.println(media.getSource());
			try{
				mediaPlayer = new MediaPlayer(media);
			}catch(MediaException me){
				me.printStackTrace();
			}
			
			videoView.setMediaPlayer(mediaPlayer);
			mediaPlayer.play();
			break;
		}
		
	}
}
