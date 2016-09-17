package controller;

import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class LevelController extends SceneController {
	@FXML private Accordion levelAccordion;
	@FXML private boolean review;
	
	class LevelPane extends TitledPane {
		private int _level;
		public LevelPane(int level){
			super();
			_level = level;
		}
		public int getLevel(){
			return _level;
		}
	}
	
	
	@FXML public void initialize(){
	}
	@FXML
	public void quitToMainMenu(MouseEvent me){
		application.requestSceneChange("mainMenu");
	}
	public void init(String[] args) {
		//empty for subclasses to override
		levelAccordion.getPanes().clear();
		application.update(this, "levelViewLoaded");
		if(args!=null && args.length>0 && args[0].equals("failed")){
			review = true;
		}else{
			review = false;
		}
		application.update(this, "requestLevels");
	}
	public void onModelChange(String fieldName, Object...objects) {
		switch(fieldName){
		case "levelsLoaded":
			ArrayList<?> stats = (ArrayList<?>)(objects[0]);
			for(int i=1;i<stats.size();i++){
				LevelPane newPane = new LevelPane(i);
				VBox contentPane = new VBox();
				Button newGameBtn = new Button("Start Game");
				newGameBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
					if(review){
						application.update(this,"startReviewGame");
					}else{
						application.update(this,"startNewGame");
					}
				});
				contentPane.getChildren().add(new Label("Mastery (words mastered/total):"));
				contentPane.getChildren().add(new Label(Math.round((Double)stats.get(i)*100)+"%"));
				contentPane.getChildren().add(newGameBtn);
				newPane.setText("Level " + (i));
				newPane.setContent(contentPane);
				levelAccordion.getPanes().add(newPane);
			}
			break;
		}
	}
	public Integer getLevelSelected(){
		return ((LevelPane)levelAccordion.getExpandedPane()).getLevel();
	}
	
	
	
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

}
