package application;

import java.util.List;

import resources.StoredStats;
import resources.StoredStats.Type;

public class Game {
	private List<String> wordList;
	private boolean faulted;
	private boolean prevFaulted;
	private int wordListSize;
	private StoredStats stats;
	private boolean review;
	private boolean gameEnded;
	
	public boolean isGameEnded(){
		return gameEnded;
	}
	/**
	 * Check word against game logic
	 * @param word
	 */
	public void submitWord(String word){
		if(!wordList.isEmpty()){
			int speed = 1;
			boolean prev2Faulted = prevFaulted;
			prevFaulted = faulted;
			String testWord = wordList.get(0);
			faulted=!word.toLowerCase().equals(testWord.toLowerCase());
			if(!faulted&&!prevFaulted){
				//mastered
				outputLabel.setText("Well done");
				correctWordLabel.setText("Correct, the word is "+testWord);
				progress.setStyle("-fx-accent: lightgreen;");
				faulted=false;
				stats.addStat(Type.MASTERED,testWord, 1);
				// if review, remove from failedlist
				stats.setStats(Type.FAILED, testWord, 0);
				wordList.remove(0);
			}else if(faulted&&!prevFaulted){
				//faulted once => set faulted
				outputLabel.setText("Try again!");
				correctWordLabel.setText("Sorry, that wasn't quite right");
				progress.setStyle("-fx-accent: #ffbf44;");
				speed = 2;
			}else if(!faulted&&prevFaulted){
				//correct after faulted => store faulted
				outputLabel.setText("Well done");
				correctWordLabel.setText("Correct, the word is "+testWord);
				progress.setStyle("-fx-accent: lightgreen;");
				stats.addStat(Type.FAULTED,testWord, 1);
				wordList.remove(0);
			}else if(review&&!prev2Faulted){
				//give one more chance in review, set speed to very slow
				outputLabel.setText("Last try!");
				correctWordLabel.setText("Let's slow it down...");
				progress.setStyle("-fx-accent: #ffbf44;");
				speed = 3;
			}else{
				//faulted twice => failed
				outputLabel.setText("Incorrect");
				correctWordLabel.setText("The word was "+testWord);
				progress.setStyle("-fx-accent: orangered;");
				faulted=false;
				stats.addStat(Type.FAILED, testWord, 1);
				wordList.remove(0);
			}
			if(wordList.size()!=0){
				application.sayWord(new int[]{speed},wordList.get(0));
			}else{
				wordTextArea.setDisable(true);
				confirm.setText("Restart?");
				gameEnded=true;
			}
			//set progressbars for progress through quiz and also denote additional separation for faulted words
			progress.setProgress((wordListSize-wordList.size()+((faulted)?0.5:0))/(double)wordListSize);
		}
	}
}
