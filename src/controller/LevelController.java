package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Accordion;

public class LevelController extends SceneController {
	@FXML private Accordion levelAccordion;
	@FXML public void initialize(){
		//empty for subclasses to override
		application.update(this, "levelViewLoaded");
	}
	@Override
	public void init(String[] args) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onModelChange(String fieldName, Object...objects) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onExit() {
		// TODO Auto-generated method stub
		
	}

}
