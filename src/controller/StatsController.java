package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import application.MainInterface;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import resources.StoredStats;
import resources.StoredStats.Type;

public class StatsController implements SceneController{
	@FXML private MainInterface application;
    @FXML private BarChart<String, Number> barChartView;
    @FXML private Button mainMenuBtn;
    @FXML private Button clearStatsBtn;
    @FXML private TextArea statsTextArea;
	@FXML
	public void initialize(){
	}
	/**
	 * Listener for quit to main menu navigation button
	 * @param me MouseEvent
	 */
	@FXML
	public void quitToMainMenu(MouseEvent me){
		application.requestSceneChange("mainMenu");
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
        	application.writeObjectToFile(MainInterface.STATS_PATH, new StoredStats());
    		barChartView.getData().clear();
    		barChartView.layout();
    		statsTextArea.clear();
    		statsTextArea.layout();
        }
	}
	public void setApplication(MainInterface app) {
		application = app;
	}
	
	private StoredStats getStatsFromFile(){
		Object obj = application.loadObjectFromFile(MainInterface.STATS_PATH);
		StoredStats stats = null;
		if(obj instanceof StoredStats) stats = (StoredStats) obj;
		return stats;
	}
	
	
	public void init(String[] args) {
		barChartView.setAnimated(false);
		barChartView.getData().clear();
		barChartView.setLegendVisible(false);
		statsTextArea.clear();
		statsTextArea.layout();
		statsTextArea.setEditable(false);
		StoredStats stats = getStatsFromFile();
		if(stats!=null){
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
						sb.append("Word: "+key+"\n");
						sb.append("Mastered: "+stats.getStat(Type.MASTERED, key)+"\n");
						sb.append("Failed: "+stats.getStat(Type.FAILED, key)+"\n");
						sb.append("Faulted: "+stats.getStat(Type.FAULTED, key)+"\n\n");
					}
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
		
	}
	public void cleanup() {
	}
	public boolean onExit() {
		// Nothing to confirm
		return true;
	}

}
