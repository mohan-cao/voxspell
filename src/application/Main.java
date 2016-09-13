package application;
	
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import controller.SceneController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import resources.StoredStats;

public class Main extends Application implements MainInterface {
	private Map<String,Scene> screens; //maps keys to scenes
	private Map<String,FXMLLoader> screenFXMLs; //maps keys to fxmlloaders, needed to get controllers
	private SceneController currentController; //current controller to displayed scene
	private StatisticsModel statsModel;
	Stage stage;
	
	{
		screens = new HashMap<String, Scene>();
		screenFXMLs = new HashMap<String, FXMLLoader>();
		statsModel = new StatisticsModel();
	}
	
	public void start(Stage primaryStage) {
		this.stage = primaryStage;
		buildMainScenes();
		statsModel.setMain(this);
		try {
			primaryStage.setTitle("VoxSpell v0.0.2-b");
			requestSceneChange("mainMenu");
			primaryStage.show();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void stop(){
        currentController.cleanup();
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
					if(!currentController.onExit()){
						event.consume();
					}
				}
			});
			return true;
		}
		return false;
	}
	public void tell() {
		//notify currentController of changes
	}
	

	public static void main(String[] args) {
		launch(args);
	}
}
