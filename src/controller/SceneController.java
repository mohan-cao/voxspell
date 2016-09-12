package controller;

import application.MainInterface;

public interface SceneController {
	/**
	 * Must be implemented by all controllers to reference back to application for model/view changes
	 * @param app
	 */
	public void setApplication(MainInterface app);
	/**
	 * Controller is initialised with initialisation arguments
	 * @param args
	 */
	public void init(String[] args);
	/**
	 * Optional method for cleanup on application quit
	 */
	public void cleanup();
}
