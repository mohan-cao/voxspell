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
import java.nio.channels.FileChannel;
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
		setupVideoFile();
		try {
			primaryStage.setTitle("VoxSpell v0.9.2-b");
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
	
	/**
	 * Moves video to ~/user. Uses FFMPEG to speed up video by 4x
	 */
	private void setupVideoFile() {
		File video = new File("src/resources/big_buck_bunny_1_minute.mp4");
		File destination = new File(System.getProperty("user.home") + "/.user/BigBuckBunny.mp4");
		ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c",
				"ffmpeg -i ~/.user/BigBuckBunny.mp4 -filter_complex \"[0:v]setpts=0.5*PTS[v];[0:a]atempo=2.0[a]\" -map \"[v]\" -map \"[a]\" -strict -2 ~/.user/SpedUpReward.mp4");
		try {
			copyFile(video,destination);
			Process process = pb.start();
			
			Task<Integer> ffmpegTask = new Task<Integer>() {
				@Override
				protected Integer call() throws Exception {
					return process.waitFor();
				}

				public void succeeded() {
					super.succeeded();
					try {
						if (get() != 0) {
							// couldn't find festival
							Alert alert = new Alert(AlertType.ERROR);
							alert.setContentText(
									"FFMPEG does not work on this system"); //or the programmer did something wrong
							alert.showAndWait();
						}
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				}
			};
			new Thread(ffmpegTask).start();

		} catch (IOException e) {
			// couldn't find BASH
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("This program does not work on non-Linux systems at this time. Sorry about that.");
			alert.showAndWait();
		}
	}

	/**
	 * Source:
	 * http://stackoverflow.com/questions/300559/move-copy-file-operations-in-java
	 * 
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException
	 */
	private void copyFile(File sourceFile, File destFile) throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}
		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			long count = 0;
			long size = source.size();
			while ((count += destination.transferFrom(source, count, size - count)) < size)
				;
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
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
		boolean success = false;
		if(screens.containsKey(key)){
			currentController = screenFXMLs.get(key).getController();
			currentController.setApplication(this);
			success = requestSceneChange(key,_stage,data);
			currentController.init(data);
		}
		return success;
	}
	/**
	 * Request scene change in particular stage with data parameters
	 * Does not initialise the controller
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
				ProcessBuilder pb = new ProcessBuilder("/bin/bash","-c", "festival --pipe");
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
						System.out.println(get());
						Alert alert = new Alert(AlertType.ERROR);
						alert.setContentText("Could not find Festival text-to-speech\nsynthesiser. Sorry about that.");
						alert.showAndWait();
						Platform.exit();
					}
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					process.destroy();
				}
			}
			public void failed(){
				process.destroy();
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
