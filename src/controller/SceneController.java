package controller;

import java.lang.reflect.Method;

import application.MainInterface;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;

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
	 * Optional initialization method (upon controller creation)
	 */
	@FXML public void initialize(){
		//empty for subclasses to override
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
	 * Optional method for confirmation of exiting the program
	 * Model calls this to update view to tell it to finalize any changes before exiting
	 */
	public abstract void onExit();
	/**
	 * Notify view of changes in the model.
	 */
	public abstract void onModelChange(String fieldName);
}
