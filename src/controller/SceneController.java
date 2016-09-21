package controller;


import application.MainInterface;
import javafx.fxml.FXML;

/**
 * SceneController
 * A view-controller skeleton for the MVC application
 * 
 * Calls updates to the model via the MainInterface and is notified by onModelChange()
 * 
 * @author Mohan Cao
 *
 */
public abstract class SceneController {
	@FXML protected MainInterface application;
	/**
	 * All controllers reference back to application for model/view changes
	 * @param app
	 */
	public void setApplication(MainInterface app){
		application = app;
	}
	/**
	 * Controller is initialised with initialisation arguments
	 * @param args
	 */
	public abstract void init(String[] args);
	/**
	 * Optional method for cleanup on application quit
	 * Model calls this to update view to tell it to clean up garbage
	 */
	public abstract void cleanup();
	/**
	 * Notify view of changes in the model.
	 */
	public abstract void onModelChange(String notificationString, Object... objectsParameters);
}
