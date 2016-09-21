package controller;

import application.ModelUpdateEvent;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
/**
 * A view-controller that is bound to the video_layout fxml
 * @author Mohan Cao
 * @author Ryan MacMillan
 */
public class VideoController extends SceneController {

	@FXML
	private MediaView videoView;

	@FXML
	private Button stopButton;

	private MediaPlayer mediaPlayer;
	
	@FXML
	private void stopVideo(MouseEvent event) {
		if(mediaPlayer==null)return;
		mediaPlayer.stop();
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
		// here's some hints as to how to approach the MVC pattern:
		// - controller updates model on changes that occurred (namely, in this case, an initialization occurred)
		// - model updates view with new data, namely the Media source which is loaded.
		
		// The video controller acts as both a controller and a view as it has both "update" components
		// and "listener" components
		application.update(new ModelUpdateEvent(this, "requestVideo"));
	}

	public void cleanup() {
		killMediaPlayer();
	}
	public void onModelChange(String notificationString, Object... objectsParameters) {
		// Model has changed (video is now ready), so components need to be loaded
		switch(notificationString){
		case "videoReady":
			if(objectsParameters[0]==null){System.err.println("can't find resource");return;}
			Media media = (Media) objectsParameters[0];
			try{
				mediaPlayer = new MediaPlayer(media);
			}catch(MediaException me){
				me.printStackTrace();
			}
			
			videoView.setMediaPlayer(mediaPlayer);
			
			DoubleProperty widthProperty = videoView.fitWidthProperty();
			widthProperty.bind(Bindings.selectDouble(videoView.parentProperty(), "width"));
			videoView.setPreserveRatio(true);
			mediaPlayer.play();
			break;
		}
		
	}
}
