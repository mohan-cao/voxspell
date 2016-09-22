package application;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import controller.LevelController;
import controller.SceneController;
import controller.VideoController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.stage.Stage;
import resources.StoredStats;
import resources.StoredStats.Type;
/**
 * Model update event class
 * Created by controllers to update the model.
 * @author Mohan Cao
 *
 */
public class ModelUpdateEvent {
	private final String _message;
	private final SceneController _sc;
	private final Class<? extends SceneController> _class;
	private StatisticsModel _statsModel;
	private MainInterface _main;
	private Game _game;
	public ModelUpdateEvent(SceneController sc, String message){
		_message = message;
		_sc = sc;
		_class = sc.getClass();
	}
	public Class<? extends SceneController> getControllerClass(){
		return _class;
	}
	/**
	 * Sets the main interface in which this object will interact with
	 * @param main
	 */
	public void setMain(MainInterface main){
		_main = main;
	}
	/**
	 * Sets stats model in which this object will interact with
	 * @param stats
	 */
	public void setStatsModel(StatisticsModel stats){
		_statsModel = stats;
	}
	/**
	 * Should be called if class is an update from a quiz controller
	 * This must be called by the main application, and will not be called automatically
	 * @param screens
	 * @param screenFXMLs
	 */
	public void updateFromQuizController(Map<String,Scene> screens,Map<String,FXMLLoader> screenFXMLs){
		switch(_message){
		case "quitToMainMenu_onClick":
			if(_game!=null&&!_game.isGameEnded()){
				if(!_game.onExit()){return;}
				String testWord = _game.wordList().get(0);
				_statsModel.getSessionStats().addStat(Type.FAILED, testWord, 1, _game.level());
			}
			_main.requestSceneChange("mainMenu");
			_game = null;
			sendGameUpdateRequest();
			break;
		case "btnConfirm_onClick":
			if(_game!=null&&!_game.isGameEnded()){
				try {
					Method method = _class.getMethod("validateAndSubmitInput");
					method.invoke(_sc);
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}else{
				_game.startGame();
			}
			break;
		case "nextLevel":
			boolean review = _game.isReview();
			_game = new Game(_main, _statsModel, _game.level()+1);
			sendGameUpdateRequest();
			_game.startGame(review);
			break;
		case "videoReward":
			Stage newWindow = new Stage();
			if(screens.containsKey("videoReward")){
				VideoController controller = screenFXMLs.get("videoReward").getController();
				_main.requestSceneChange("videoReward",newWindow);
				controller.setApplication(_main);
				String[] init = new String[1];
				init[0] = "videoReward";
				controller.init(init);
				newWindow.setOnCloseRequest(event->{
						controller.cleanup();
				});
			}
			newWindow.setResizable(false);
			newWindow.show();
			break;
		case "speedyReward":
			newWindow = new Stage();
			if(screens.containsKey("videoReward")){
				VideoController controller = screenFXMLs.get("videoReward").getController();
				_main.requestSceneChange("videoReward",newWindow);
				controller.setApplication(_main);
				String[] init = new String[1];
				init[0] = "speedyReward";
				controller.init(init);
				newWindow.setOnCloseRequest(event->{
						controller.cleanup();
				});
			}
			newWindow.setResizable(false);
			newWindow.show();
			break;
			
		case "changeVoice_onClick":
			_game.changeVoice();
			break;
		case "repeatWord_onClick":
			_game.repeatWord();
			break;
		case "newGame":
			_game.startGame(false);
			break;
		case "reviewGame":
			_game.startGame(true);
			break;
		case "submitWord":
			try {
				Method method = _class.getMethod("getTextAreaInput");
				String word = (String) method.invoke(_sc);
				_game.submitWord(word);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
			
			break;
		case "cleanup":
			//save and quit
			if(_game!=null&&!_game.isGameEnded()){
				String testWord = _game.wordList().get(0);
				_statsModel.getSessionStats().addStat(Type.FAILED, testWord, 1, _game.level());
			}
			_game = null;
			sendGameUpdateRequest();
			break;
		}
	}
	/**
	 * Should be called if class is an update from a stats controller
	 * This must be called by the main application, and will not be called automatically
	 */
	public void updateFromStatsController(){
		switch(_message){
		case "clearStats":
			_statsModel.getGlobalStats().clearStats();
			_statsModel.getSessionStats().clearStats();
			_statsModel.sessionEnd();
    		break;
		case "requestGlobalStats":
			_sc.onModelChange("globalStatsLoaded", _statsModel.getGlobalStats());
			break;
		case "requestSessionStats":
			_sc.onModelChange("sessionStatsLoaded", _statsModel.getSessionStats());
			break;
		}
	}
	/**
	 * Should be called if class is an update from a level controller
	 * This must be called by the main application, and will not be called automatically
	 */
	public void updateFromLevelController(){
		LevelController lc = (LevelController) _sc;
		switch(_message){
		case "requestLevels":
			ArrayList<Double> levelStats = new ArrayList<Double>();
			Set<Integer> unlockedLevelSet = new LinkedHashSet<Integer>();
			unlockedLevelSet.addAll(_statsModel.getGlobalStats().getUnlockedLevelSet());
			unlockedLevelSet.addAll(_statsModel.getSessionStats().getUnlockedLevelSet());
			ArrayList<Integer> unlockedLevels = new ArrayList<Integer>(unlockedLevelSet);
			Collections.sort(unlockedLevels);
			StoredStats sStats = _statsModel.getGlobalStats();
			StoredStats gStats = _statsModel.getSessionStats();
			int mastered = 0;
			int failed = 0;
			for(Integer i : unlockedLevels){
				mastered = gStats.getTotalStatsOfLevel(i, StoredStats.Type.MASTERED)+sStats.getTotalStatsOfLevel(i, StoredStats.Type.MASTERED);
				failed = gStats.getTotalStatsOfLevel(i, StoredStats.Type.FAILED)+gStats.getTotalStatsOfLevel(i, StoredStats.Type.FAULTED)+sStats.getTotalStatsOfLevel(i, StoredStats.Type.FAILED)+sStats.getTotalStatsOfLevel(i, StoredStats.Type.FAULTED);
				if((mastered+failed)!=0){
					levelStats.add(i,(mastered)/(double)(failed+mastered));
				}else{
					levelStats.add(i, 0d);
				}
			}
			_sc.onModelChange("levelsLoaded", levelStats);
			break;
		case "startNewGame":
			_game = new Game(_main, _statsModel, lc.getLevelSelected());
			sendGameUpdateRequest();
			_main.requestSceneChange("quizMenu");
			break;
		case "startReviewGame":
			_game = new Game(_main, _statsModel, lc.getLevelSelected());
			sendGameUpdateRequest();
			_main.requestSceneChange("quizMenu","failed");
			break;
		}
	}
	/**
	 * Should be called if class is an update from a video controller
	 * This must be called by the main application, and will not be called automatically
	 */
	public void updateFromVideoController(){
		switch(_message){
		case "speedyReward":
			try {
				String home = System.getProperty("user.home");
				File videoFile = new File(home + "/.user/SpedUpReward.mp4");
				URL url = videoFile.toURI().toURL();
				Media media = new Media(url.toString());
				_sc.onModelChange("speedyReward", media);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			break;
		case "requestVideo":
			URL url = getClass().getClassLoader().getResource("resources/big_buck_bunny_1_minute.mp4");
			Media media = new Media(url.toString());
			_sc.onModelChange("videoReady", media);
			break;
		}
	}
	/**
	 * Sets the game to the MainController's game
	 * @param game Game
	 */
	public void setGame(Game game){
		_game = game;
	}
	/**
	 * Gets the updated game.
	 * @return Game
	 */
	public Game getUpdatedGame(){
		return _game;
	}
	/**
	 * Sends game updated request back to main interface
	 */
	public void sendGameUpdateRequest(){
		ModelUpdateEvent mue = new ModelUpdateEvent(new GameUpdater(), "updateGame");
		mue.setGame(_game);
		_main.update(mue);
	}
}
/**
 * Empty class representing a game update event.
 * @author Mohan Cao
 *
 */
class GameUpdater extends SceneController {
	@Override
	public void init(String[] args) {}
	@Override
	public void cleanup() {}
	@Override
	public void onModelChange(String notificationString, Object... objectsParameters) {}
}