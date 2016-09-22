package controller;

import application.ModelUpdateEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
/**
 *
 * A view-controller that is bound to the one_time_layout fxml
 * 
 * @author Mohan Cao
 *
 */
public class IntroController extends SceneController {
	private class Level {
		private int _level;
		private String _message;
		public Level(String message, int lvl){
			_message = message;
			_level = lvl;
		}
		public int getLevel(){
			return _level;
		}
		public String toString(){
			return _message;
		}
	}
	@FXML private ComboBox<Level> levels;
	@FXML private int unlockUpTo;
	@FXML
	public void initialize(){}
	/**
	 * Submits levels
	 * @param me
	 */
	@FXML
	public void submit(MouseEvent me){
		unlockUpTo = levels.getSelectionModel().getSelectedItem().getLevel();
		application.update(new ModelUpdateEvent(this,"unlockLevels"));
	}
	public int getLevelsToUnlock(){
		return unlockUpTo;
	}
	@Override
	public void init(String[] args) {
		application.update(new ModelUpdateEvent(this,"requestLevels"));
	}

	@Override
	public void cleanup() {}

	@Override
	public void onModelChange(String notificationString, Object... objectsParameters) {
		switch (notificationString) {
		case "levelsLoaded":
			for(Object obj : objectsParameters){
				levels.getItems().add(new Level("Level "+obj,(int)obj));
			}
			break;
		}
	}

}
