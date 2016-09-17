package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import application.MainInterface;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import resources.StoredStats;
import resources.StoredStats.Type;

public class StatsController extends SceneController{
    @FXML private BarChart<String, Number> barChartView;
    @FXML private Button mainMenuBtn;
    @FXML private Button clearStatsBtn;
    @FXML private TextArea statsTextArea;
    @FXML private ComboBox<String> statsSelection;
	@FXML
	public void initialize(){
		statsSelection.getItems().addAll("Global statistics", "Session statistics");
		statsSelection.getSelectionModel().select(1);
		statsSelection.setEditable(false);
		StatsController thisController = this;
		statsSelection.valueProperty().addListener(new ChangeListener<String>(){
			SceneController sc = thisController;
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if(newValue.equals("Global statistics")){
					application.update(sc, "requestGlobalStats");
				}else if(newValue.equals("Session statistics")){
					application.update(sc, "requestSessionStats");
				}
			}
		});
	}
	/**
	 * Listener for quit to main menu navigation button
	 * @param me MouseEvent
	 */
	@FXML
	public void quitToMainMenu(MouseEvent me){
		application.requestSceneChange("mainMenu");
	}
	@FXML
	public void changeStatsView(ActionEvent ae){
		
	}
	/**
	 * Listener for clear statistics button
	 * @param me
	 */
	@FXML
	public void clearStats(MouseEvent me){
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Are you sure?");
        alert.setContentText("Your stats will be cleared! You can't undo this change.");
        Optional<ButtonType> response = alert.showAndWait();
        if(response.get()==ButtonType.OK){
        	application.update(this, "clearStats");
        	barChartView.getData().clear();
    		barChartView.layout();
    		statsTextArea.clear();
    		statsTextArea.layout();
        }
	}
	public void setApplication(MainInterface app) {
		application = app;
	}
	
	public void init(String[] args) {
		barChartView.setAnimated(false);
		barChartView.setLegendVisible(false);
		statsTextArea.setEditable(false);
		if(statsSelection.getSelectionModel().getSelectedItem().equals("Global statistics")){
		application.update(this, "requestGlobalStats");
		}else if(statsSelection.getSelectionModel().getSelectedItem().equals("Session statistics")){
		application.update(this, "requestSessionStats");
		}
	}
	
	private void statsChange(StoredStats stats){
		barChartView.getData().clear();
		statsTextArea.clear();
		statsTextArea.layout();
		XYChart.Series<String, Number> series1 = new XYChart.Series<>();
		XYChart.Data<String,Number> masteredData = new XYChart.Data<String,Number>("Total Mastered", stats.getTotalStatsOfType(Type.MASTERED));
		XYChart.Data<String,Number> faultedData = new XYChart.Data<String,Number>("Total Faulted", stats.getTotalStatsOfType(Type.FAULTED));
		XYChart.Data<String,Number> failedData = new XYChart.Data<String,Number>("Total Failed", stats.getTotalStatsOfType(Type.FAILED));
		series1.getData().add(masteredData);
		series1.getData().add(faultedData);
		series1.getData().add(failedData);
		barChartView.getData().add(series1);
		masteredData.getNode().setStyle("-fx-bar-fill: #33cc66;");
		faultedData.getNode().setStyle("-fx-bar-fill: #ffcc66;");
		failedData.getNode().setStyle("-fx-bar-fill: #cc6666;");
		Task<String> loader = new Task<String>(){
			protected String call() throws Exception {
				StringBuffer sb = new StringBuffer();
				ArrayList<String> keys = new ArrayList<String>(stats.getKeys());
				Collections.sort(keys);
				for(String key : keys){
					int mastered = stats.getStat(Type.MASTERED, key);
					int failed = stats.getStat(Type.FAILED, key);
					int faulted = stats.getStat(Type.FAULTED, key);
					if(mastered+failed+faulted==0){continue;}
					sb.append("Word: "+key+"\n");
					sb.append("Mastered: "+mastered+"\n");
					sb.append("Failed: "+failed+"\n");
					sb.append("Faulted: "+faulted+"\n");
					sb.append("Mastery: "+Math.round(mastered/(double)(mastered+failed+faulted)*100)+"%\n\n");
				}
				if(sb.length()==0){return "No stats to display :(\nGo and do some quizzes";}
				return sb.toString();
			}
			public void succeeded(){
				try {
					statsTextArea.appendText(this.get());
					statsTextArea.positionCaret(0);
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(loader).run();
	}
	
	@Override
	public void onModelChange(String notificationString, Object... objectParameters) {
		// TODO Auto-generated method stub
		switch(notificationString){
		case "globalStatsLoaded":
			statsChange((StoredStats)objectParameters[0]);
			break;
		case "sessionStatsLoaded":
			statsChange((StoredStats)objectParameters[0]);
			break;
		}
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
