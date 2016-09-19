package application;
	
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import controller.LevelController;
import controller.QuizController;
import controller.SceneController;
import controller.StatsController;
import controller.VideoController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import resources.StoredStats;
/**
 * Main entry class (Application)
 * This class is the entry to the JavaFX application
 * Acts as the application model
 * 
 * @author Mohan Cao
 * @author Ryan Macmillan
 *
 */
public class Main extends Application implements MainInterface {
	private Map<String,Scene> screens; //maps keys to scenes
	private Map<String,FXMLLoader> screenFXMLs; //maps keys to fxmlloaders, needed to get controllers
	private SceneController currentController; //current controller to displayed scene
	private StatisticsModel statsModel;
	private Game game;
	private Task<Integer> festivalTask;
	Stage _stage;
	{
		screens = new HashMap<String, Scene>();
		screenFXMLs = new HashMap<String, FXMLLoader>();
		statsModel = new StatisticsModel(this);
	}
	@Override
	public void start(Stage primaryStage) {
		this._stage = primaryStage;
		buildMainScenes();
		try {
			primaryStage.setTitle("VoxSpell v0.9.1-b");
			requestSceneChange("mainMenu");
			primaryStage.show();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void stop(){
        currentController.cleanup();
        statsModel.sessionEnd();
	}
	@Override
	public Object loadObjectFromFile(String path) {
		try {
			File file = new File(path);
			if(!file.exists()){return null;}
			FileInputStream fileIn = new FileInputStream(file);
			ObjectInputStream instr = new ObjectInputStream(fileIn);
			Object obj = instr.readObject();
			instr.close();
			return obj;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidClassException ice){
			writeObjectToFile(path,new StoredStats());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public boolean writeObjectToFile(String path, Object obj) {
		try {
			File file = new File(path);
			FileOutputStream fileout = new FileOutputStream(file);
			ObjectOutputStream outstr = new ObjectOutputStream(fileout);
			outstr.writeObject(obj);
			outstr.close();
			fileout.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void buildMainScenes(){
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("config.cfg")));
			String line;
			String[] strSplit;
			while((line = br.readLine())!=null){
				strSplit = line.split(",");
				try{
					URL loc;
					FXMLLoader fxml = null;
					Parent menu = null;
					if((loc=getClass().getClassLoader().getResource(strSplit[1]))!=null){
						fxml = new FXMLLoader(loc);
						menu = (Parent)fxml.load();
						screens.put(strSplit[0], new Scene(menu));
						screenFXMLs.put(strSplit[0], fxml);
					}
					
				}catch(IOException ioex){
					System.err.println("Scene loading error");
					ioex.printStackTrace();
				}
			}
		} catch (IOException e) {
			
			throw new RuntimeException("Config files corrupted");
		} finally {
			try {
				if(br!=null){
				br.close();
				}
			} catch (IOException e1) {}
		}
	}
	
	public Collection<String> getAvailableSceneKeys(){
		return screens.keySet();
	}
	/**
	 * Request scene change, by default the current stage, with data parameters
	 */
	public boolean requestSceneChange(String key, String... data) {
		if(screens.containsKey(key)){
			currentController = screenFXMLs.get(key).getController();
			currentController.setApplication(this);
			currentController.init(data);
		}
		return requestSceneChange(key,_stage,data);
	}
	/**
	 * Request scene change in particular stage with data parameters
	 * @param key
	 * @param stage
	 * @param data
	 * @return
	 */
	public boolean requestSceneChange(String key, Stage stage, String... data) {
		if(screens.containsKey(key)){
			stage.hide();
			stage.setScene(screens.get(key));
			stage.show();
			stage.setOnCloseRequest(new EventHandler<WindowEvent>(){
				public void handle(WindowEvent event) {
					if(game!=null&&!game.onExit()){
						event.consume();
					}
				}
			});
			return true;
		}
		return false;
	}
	public void tell(String message, Object... objectParams) {
		//propagate + notify currentController (view-controller) of changes
		currentController.onModelChange(message, objectParams);
	}
	/**
	 * Creates a new process of Festival that says a word
	 * @param speed
	 * @param words
	 */
	public void sayWord(final int[] speed, final String voiceType, final String... words){
		if(festivalTask!=null){
			festivalTask.cancel(true);
		}
		festivalTask = new Task<Integer>(){
			private Process process;
			@Override
			protected Integer call() throws Exception {
				ProcessBuilder pb = new ProcessBuilder("/bin/bash","-c", "\"festival --tts\"");
				process = pb.start();
				PrintWriter pw = new PrintWriter(process.getOutputStream());
				for(int i=0;i<words.length;i++){
					if(i<speed.length){
						pw.println("(Parameter.set 'Duration_Stretch "+speed[i]+")");
					}
					pw.println("(voice_" + voiceType +")");
					pw.println("(SayText \""+words[i]+"\")");
				}
				pw.println("(quit)");
				pw.close();
				return process.waitFor();
			}
			public void succeeded(){
				super.succeeded();
				try {
					if(get()!=0){
						//couldn't find festival
						Alert alert = new Alert(AlertType.ERROR);
						alert.setContentText("Could not find Festival text-to-speech\nsynthesiser. Sorry about that.");
						alert.showAndWait();
						Platform.exit();
					}
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					process.destroy();
					e.printStackTrace();
				}
			}
		};
		new Thread(festivalTask).start();
	}
	/**
	 * Called by scene controller to update the main application
	 * @param sc
	 */
	public void update(ModelUpdateEvent mue){
		//Game must be updated
		if(mue.getControllerClass().equals(GameUpdater.class)){
			game = mue.getUpdatedGame();
		}
		mue.setMain(this);
		mue.setGame(game);
		mue.setStatsModel(statsModel);
		if(mue.getControllerClass().equals(QuizController.class)){
			mue.updateFromQuizController(screens, screenFXMLs);
		}else if(mue.getControllerClass().equals(StatsController.class)){
			mue.updateFromStatsController();
		}else if(mue.getControllerClass().equals(LevelController.class)){
			mue.updateFromLevelController();
		}else if(mue.getControllerClass().equals(VideoController.class)){
			mue.updateFromVideoController();
		}
	}
	
	
	public static void main(String[] args) {
		launch(args);
	}
}
