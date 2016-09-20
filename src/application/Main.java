package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

import controller.LevelController;
import controller.QuizController;
import controller.SceneController;
import controller.StatsController;
import controller.VideoController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
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
 * Main entry class (Application) This class is the entry to the JavaFX
 * application Acts as the application model
 * 
 * @author Mohan Cao
 * @author Ryan Macmillan
 *
 */
public class Main extends Application implements MainInterface {
	private Map<String, Scene> screens; // maps keys to scenes
	private Map<String, FXMLLoader> screenFXMLs; // maps keys to fxmlloaders,
													// needed to get controllers
	private SceneController currentController; // current controller to
												// displayed scene
	private StatisticsModel statsModel;
	private Game game;
	private Queue<Task<Integer>> festivalTasks;
	private FestivalService festivalService;
	Stage _stage;
	{
		screens = new HashMap<String, Scene>();
		screenFXMLs = new HashMap<String, FXMLLoader>();
		statsModel = new StatisticsModel(this);
		festivalService = new FestivalService();
		festivalTasks = new LinkedList<Task<Integer>>();
	}

	@Override
	public void start(Stage primaryStage) {
		this._stage = primaryStage;
		buildMainScenes();
		setupVideoFile();
		try {
			primaryStage.setTitle("VoxSpell v0.9.3-b");
			requestSceneChange("mainMenu");
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		currentController.cleanup();
		statsModel.sessionEnd();
		festivalService.cleanup();
	}

	@Override
	public Object loadObjectFromFile(String path) {
		try {
			File file = new File(path);
			if (!file.exists()) {
				return null;
			}
			FileInputStream fileIn = new FileInputStream(file);
			ObjectInputStream instr = new ObjectInputStream(fileIn);
			Object obj = instr.readObject();
			instr.close();
			return obj;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidClassException ice) {
			writeObjectToFile(path, new StoredStats());
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
	 * Moves video to ~/.user. Uses FFMPEG to speed up video by 4x
	 */
	private void setupVideoFile() {
		/*InputStream video1 = this.getClass().getClassLoader().getResourceAsStream("resources/big_buck_bunny_1_minute.mp4");
		File video = new File(video1);
		File destination = new File(System.getProperty("user.home") + "/.user/BigBuckBunny.mp4");*/
		try {
			exportResource("/resources/big_buck_bunny_1_minute.mp4",System.getProperty("user.home")+"/.user/BigBuckBunny.mp4");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c",
				"ffmpeg -i ~/.user/BigBuckBunny.mp4 -filter_complex \"[0:v]setpts=0.5*PTS[v];[0:a]atempo=2.0[a]\" -map \"[v]\" -map \"[a]\" -strict -2 ~/.user/SpedUpReward.mp4");
		
			//copyFile(video, destination);
			
			Task<Integer> ffmpegTask = new Task<Integer>() {
				@Override
				protected Integer call() throws Exception {
					Process process;
					try {
					process = pb.start(); // probably better to put it
													// in the task, which will
													// be disposed when method
													// ends.
					return process.waitFor();
					} catch (IOException e) {
						// couldn't find BASH
						Alert alert = new Alert(AlertType.ERROR);
						alert.setContentText("This program does not work on non-Linux systems at this time. Sorry about that.");
						alert.showAndWait();
						return 1;
					}
				}

				public void succeeded() {
					super.succeeded();
					try {
						if (get() != 0) {
							Alert alert = new Alert(AlertType.ERROR);
							alert.setContentText("FFMPEG does not work on this system"); // or
																							// the
																							// programmer
																							// did
																							// something
																							// wrong
							alert.showAndWait();
						}
					} catch (InterruptedException | ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			new Thread(ffmpegTask).start();

		
	}

	/**
	 * Copy file from source to destination
	 * 
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException
	 */
	/*private void copyFile(File sourceFile, File destFile) throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}
		FileChannel source = null;
		FileChannel destination = null;
		FileInputStream fis = new FileInputStream(sourceFile);
		FileOutputStream fos = new FileOutputStream(destFile);

		source = fis.getChannel();
		destination = fos.getChannel();
		long count = 0;
		long size = source.size();
		while ((count += destination.transferFrom(source, count, size - count)) < size)
			;

		if (source != null) {
			source.close();
			fis.close();
		}
		if (destination != null) {
			destination.close();
			fos.close();
		}

	}*/
	/**
	 * Exports internal resource to file system
	 * @param resource path of file in jar
	 * @param location location of file to export to
	 * @throws IOException
	 */
	private void exportResource(String resource, String newFilePath) throws IOException {
		InputStream stream = null;
		OutputStream resStreamOut = null;
		try {
			stream = getClass().getResourceAsStream(resource);
			if(stream == null)throw new IOException("Failed to get resource " + resource);
			int readBytes;
			byte[] buffer = new byte[4096];
			resStreamOut = new FileOutputStream(newFilePath);
			while ((readBytes = stream.read(buffer)) > 0) {
				resStreamOut.write(buffer, 0, readBytes);
			}
		} catch (Exception ex) {
			throw ex;
		} finally {
			if(stream!=null)stream.close();
			if(resStreamOut!=null)resStreamOut.close();
		}
	}

	private void buildMainScenes() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(
					new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("config.cfg")));
			String line;
			String[] strSplit;
			while ((line = br.readLine()) != null) {
				strSplit = line.split(",");
				try {
					URL loc;
					FXMLLoader fxml = null;
					Parent menu = null;
					if ((loc = getClass().getClassLoader().getResource(strSplit[1])) != null) {
						fxml = new FXMLLoader(loc);
						menu = (Parent) fxml.load();
						screens.put(strSplit[0], new Scene(menu));
						screenFXMLs.put(strSplit[0], fxml);
					}

				} catch (IOException ioex) {
					System.err.println("Scene loading error");
					ioex.printStackTrace();
				}
			}
		} catch (IOException e) {

			throw new RuntimeException("Config files corrupted");
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e1) {
			}
		}
	}

	public Collection<String> getAvailableSceneKeys() {
		return screens.keySet();
	}

	/**
	 * Request scene change, by default the current stage, with data parameters
	 */
	public boolean requestSceneChange(String key, String... data) {
		boolean success = false;
		if (screens.containsKey(key)) {
			currentController = screenFXMLs.get(key).getController();
			currentController.setApplication(this);
			success = requestSceneChange(key, _stage, data);
			currentController.init(data);
		}
		return success;
	}

	/**
	 * Request scene change in particular stage with data parameters Does not
	 * initialise the controller
	 * 
	 * @param key
	 * @param stage
	 * @param data
	 * @return
	 */
	public boolean requestSceneChange(String key, Stage stage, String... data) {
		if (screens.containsKey(key)) {
			stage.hide();
			stage.setScene(screens.get(key));
			stage.show();
			stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent event) {
					if (game != null && !game.onExit()) {
						event.consume();
					}
				}
			});
			return true;
		}
		return false;
	}

	public void tell(String message, Object... objectParams) {
		// propagate + notify currentController (view-controller) of changes
		currentController.onModelChange(message, objectParams);
	}

	/**
	 * Festival service class.
	 * 
	 * @author mohan0704
	 *
	 */
	class FestivalService extends Service<Integer> {
		private Process _pb;
		private String _voice;
		private String[] _words;
		private int[] _speed;
		{
			try {
				Process p = new ProcessBuilder("/bin/bash", "-c", "type -p festival").start();
				BufferedReader isr = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String output = isr.readLine();
				if (output == null || output.isEmpty()) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setContentText("Could not find Festival text-to-speech\nsynthesiser. Sorry about that.");
					alert.showAndWait();
					Platform.exit();
				}
				_pb = new ProcessBuilder(output).start();
			} catch (IOException e) {
				System.err.println("IOException");
			}
		}

		public final void setWordsToList(int[] speed, String... words) {
			_words = words;
			_speed = speed;
		}

		public void cleanup() {
			_pb.destroy();
		}

		public final void setVoice(String voice) {
			_voice = voice;
		}

		@Override
		protected Task<Integer> createTask() {
			final String voice = _voice;
			final String[] words = _words;
			final int[] speed = _speed;
			return new Task<Integer>() {
				protected Integer call() throws Exception {
					if(_words.length==1&&_words[0].equals("...")){
						//trying to get words to pause
						return 0;
					}
					BufferedWriter bw = new BufferedWriter(new PrintWriter(_pb.getOutputStream()));
					for (int i = 0; i < words.length; i++) {
						if (i < speed.length) {
							bw.write("(Parameter.set 'Duration_Stretch " + speed[i] + ")");
						}
						bw.write("(voice_" + voice + ")");
						bw.write("(SayText \"" + words[i] + "\")");
					}
					bw.flush();
					return 0;
				}

				public void succeeded() {
					if (!festivalTasks.isEmpty()) {
						Task<Integer> task = festivalTasks.poll();
						new Thread(task).start();
					}
				}
			};
		}

	}

	/**
	 * Creates a new process of Festival that says a word
	 * 
	 * @param speed
	 * @param words
	 */
	public void sayWord(final int[] speed, final String voiceType, final String... words) {
		festivalService.setVoice(voiceType);
		festivalService.setWordsToList(speed, words);
		if (!festivalTasks.isEmpty()) {
			festivalTasks.add(festivalService.createTask());
		}
		Task<Integer> festivalTask = festivalService.createTask();
		new Thread(festivalTask).start();
	}

	/**
	 * Called by scene controller to update the main application
	 * 
	 * @param sc
	 */
	public void update(ModelUpdateEvent mue) {
		// Game must be updated
		if (mue.getControllerClass().equals(GameUpdater.class)) {
			game = mue.getUpdatedGame();
		}
		mue.setMain(this);
		mue.setGame(game);
		mue.setStatsModel(statsModel);
		if (mue.getControllerClass().equals(QuizController.class)) {
			mue.updateFromQuizController(screens, screenFXMLs);
		} else if (mue.getControllerClass().equals(StatsController.class)) {
			mue.updateFromStatsController();
		} else if (mue.getControllerClass().equals(LevelController.class)) {
			mue.updateFromLevelController();
		} else if (mue.getControllerClass().equals(VideoController.class)) {
			mue.updateFromVideoController();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
