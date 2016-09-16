package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Accordion;

public class LevelController extends SceneController {
	@FXML private Accordion levelAccordion;
	@FXML private boolean review;
	@FXML public void initialize(){
		//empty for subclasses to override
		application.update(this, "levelViewLoaded");
		levelAccordion.getPanes().clear();
	}
	@Override
	public void init(String[] args) {
		// TODO Auto-generated method stub
		if(args!=null && args.length>0 && args[0].equals("failed")){
			review = true;
			application.update(this, "requestReviewGameLevels");
		}else{
			review = false;
			application.update(this, "requestNewGameLevels");
		}
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
