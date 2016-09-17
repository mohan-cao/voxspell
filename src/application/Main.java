package application;
	
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.media.Media;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import resources.StoredStats;
import resources.StoredStats.Type;

public class Main extends Application implements MainInterface {
	private Map<String,Scene> screens; //maps keys to scenes
	private Map<String,FXMLLoader> screenFXMLs; //maps keys to fxmlloaders, needed to get controllers
	private SceneController currentController; //current controller to displayed scene
	private StatisticsModel statsModel;
	private Game game;
	private Task<Integer> festivalTask;
	Stage stage;
	
	{
		screens = new HashMap<String, Scene>();
		screenFXMLs = new HashMap<String, FXMLLoader>();
		statsModel = new StatisticsModel(this);
	}
	
	public void start(Stage primaryStage) {
		this.stage = primaryStage;
		buildMainScenes();
		try {
			primaryStage.setTitle("VoxSpell v0.1.3-b");
			requestSceneChange("mainMenu");
			primaryStage.show();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void stop(){
        currentController.cleanup();
        statsModel.sessionEnd();
	}

	public Object loadObjectFromFile(String path) {
		try {
			File file = new File(path);
			if(!file.exists()){return null;}
			FileInputStream fileIn = new FileInputStream(file);
			ObjectInputStream instr = new ObjectInputStream(fileIn);
			Object obj = instr.readObject();
			instr.close();
			return obj;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
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
	
	public boolean requestSceneChange(String key, String... data) {
		if(screens.containsKey(key)){
			stage.hide();
			stage.setScene(screens.get(key));
			currentController = screenFXMLs.get(key).getController();
			currentController.setApplication(this);
			currentController.init(data);
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
	public void sayWord(int[] speed, String... words){
		ProcessBuilder pb = new ProcessBuilder("/bin/bash","-c", "festival");
		try {
			if(festivalTask!=null){
				festivalTask.cancel(true);
			}
			Process process = pb.start();
			PrintWriter pw = new PrintWriter(process.getOutputStream());
			for(int i=0;i<words.length;i++){
				if(i<speed.length){
					pw.println("(Parameter.set 'Duration_Stretch "+speed[i]+")");
				}
				pw.println("(SayText \""+words[i]+"\")");
			}
			pw.println("(quit)");
			pw.close();
			festivalTask = new Task<Integer>(){
				@Override
				protected Integer call() throws Exception {
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
						}
					} catch (InterruptedException | ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			new Thread(festivalTask).start();
			
		} catch (IOException e) {
			//couldn't find BASH
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("This program does not work on non-Linux systems at this time. Sorry about that.");
			alert.showAndWait();
		}
	}
	/**
	 * Called by scene controller to update the main application
	 * @param sc
	 */
	public void update(SceneController sc, String message){
		//TODO
		if(sc instanceof QuizController){
			QuizController qc = (QuizController)sc;
			switch(message){
			case "quitToMainMenu_onClick":
				if(game!=null&&!game.isGameEnded()){
					if(!game.onExit()){return;}
					String testWord = game.wordList().get(0);
					statsModel.getSessionStats().addStat(Type.FAILED, testWord, 1, game.level());
				}
				this.requestSceneChange("mainMenu");
				game = null;
				break;
			case "btnConfirm_onClick":
				if(game!=null&&!game.isGameEnded()){
					qc.validateAndSubmitInput();
				}else{
					game.startGame();
				}
				break;
			case "newGame":
				//make new game, start it
				game = new Game(this, statsModel);
				game.startGame(false);
				break;
			case "reviewGame":
				game = new Game(this, statsModel);
				game.startGame(true);
				break;
			case "submitWord":
				game.submitWord(qc.getTextAreaInput());
				break;
			case "cleanup":
				//save and quit
				if(game!=null&&!game.isGameEnded()){
					String testWord = game.wordList().get(0);
					statsModel.getSessionStats().addStat(Type.FAILED, testWord, 1, game.level());
				}
				game = null;
				break;
			}
		}else if(sc instanceof StatsController){
			StatsController stc = (StatsController)sc;
			switch(message){
			case "clearStats":
				statsModel.getGlobalStats().clearStats();
				statsModel.getSessionStats().clearStats();
				statsModel.sessionEnd();
	    		break;
			case "requestGlobalStats":
				sc.onModelChange("globalStatsLoaded", statsModel.getGlobalStats());
				break;
			case "requestSessionStats":
				sc.onModelChange("sessionStatsLoaded", statsModel.getSessionStats());
				break;
			}
		}else if(sc instanceof LevelController){
			LevelController lc = (LevelController) sc;
			int mastered = 0;
			int failed = 0;
			int[] levelStats = new int[10];
			switch(message){
			case "requestNewGameLevels":
				//faulted words do not count towards calculation of accuracy (DESIGN DECISION)
				for(int i=0;i<10;i++){
				mastered = statsModel.getSessionStats().getTotalStatsOfLevel(i+1, StoredStats.Type.MASTERED); //just get stats of level 1 now
				failed = statsModel.getSessionStats().getTotalStatsOfLevel(i+1, StoredStats.Type.FAILED);
				levelStats[i] = ((failed+mastered)!=0)?mastered/(failed+mastered):0;
				}
				sc.onModelChange("levelsLoaded", levelStats);
				
				break;
			case "requestReviewGameLevels":
				//faulted words do not count towards calculation of accuracy (DESIGN DECISION)
				for(int i=0;i<10;i++){
				mastered = statsModel.getSessionStats().getTotalStatsOfLevel(i+1, StoredStats.Type.MASTERED); //just get stats of level 1 now
				failed = statsModel.getSessionStats().getTotalStatsOfLevel(i+1, StoredStats.Type.FAILED);
				levelStats[i] = ((failed+mastered)!=0)?mastered/(failed+mastered):0;
				}
				sc.onModelChange("levelsLoaded", levelStats);
				
				break;
			}
		}else if(sc instanceof VideoController){
			switch(message){
			case "requestVideo":
				Media media = new Media("http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4");
				sc.onModelChange("videoReady", media);
				break;
			}
		}
		//else if (sc instanceof VideoController){
		//messages -> pause, quit to main menu
	//}
	}
	

	public static void main(String[] args) {
		launch(args);
	}
}
