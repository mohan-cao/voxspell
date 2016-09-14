package controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Accordion;

package controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.media.MediaView;

public class VideoController extends SceneController {

	@FXML
	protected MainInterface application;

	@FXML
	private MediaView videoView;

	@FXML
	private Button stopButton;

	@FXML
	private Button menuButton;

	/**
	 * All controllers reference back to application for model/view changes
	 * @param app
	 */
	public void setApplication(MainInterface app) {
		application = app;
	}

	/**
	 * Optional initialization method (upon controller creation)
	 */
	@FXML
	public void initialize() {
		// empty for subclasses to override
	}

	/**
	 * Controller is initialised with initialisation arguments
	 * 
	 * @param args
	 */
	public void init(String[] args){
		
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
	public void onModelChange(Class<? extends Node> updatedPart, String fieldName){
		
		
	}
}
