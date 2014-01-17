/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************/
package ab.demo;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.utils.StateUtil;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

public class NaiveAgent implements Runnable {

	private ActionRobot aRobot;
	private Random randomGenerator;
	public int currentLevel = 1;
	public static int time_limit = 12;
	private Map<Integer,Integer> scores = new LinkedHashMap<Integer,Integer>();
	TrajectoryPlanner tp;
	private boolean firstShot;
	private Point prevTarget;
	// a standalone implementation of the Naive Agent
	public NaiveAgent() {
		
		aRobot = new ActionRobot();
		tp = new TrajectoryPlanner();
		prevTarget = null;
		firstShot = true;
		randomGenerator = new Random();
		// --- go to the Poached Eggs episode level selection page ---
		ActionRobot.GoFromMainMenuToLevelSelection();

	}

	//Begin  extra code for MCTS
	
	

	
	class ShotData{
		double Theta;
		double realAngle;
		int releaseX;
		int releaseY;	
		int tapTime;
		public  ShotData(double Theta,double realAngle,int releaseX, int releaseY,int tapTime){
			this.Theta = Theta;
			this.realAngle = realAngle;
			this.releaseX = releaseX;
			this.releaseY = releaseY;
			this.tapTime = tapTime;
		}
	}

	static 	ArrayList<ShotData> Storage = new  ArrayList<ShotData>();
	static int BestScore = 0;
	static HashMap<String, Integer> nodeCount = new HashMap<String, Integer>();
	static HashMap<String, Double> scoreCount = new HashMap<String, Double>();	
	static int shotFired = 0;
	
	//Playing levels 22+ requires you to manually scroll to the corresponding page before running this agent
	static String levelD = "1";//Current page of poached eggs. Has to be set manually.
	
	public void WriteShot(int score){
		  try{
			  // Create file 
			  FileWriter fstream = new FileWriter("./MCTSresults/" + levelD + "-" + Integer.toString(currentLevel) + ".txt",true);
			  BufferedWriter out = new BufferedWriter(fstream);
			  out.write("{\n");
			  for( ShotData  shot: Storage ){
				  out.write("Theta:" + Double.toString(shot.Theta) + "\n");
				  out.write("Angle:" + Double.toString(shot.realAngle) + "\n");
				  out.write("releaseX:" + Integer.toString(shot.releaseX) + "\n");
				  out.write("releaseY:" + Integer.toString(shot.releaseY) + "\n");
				  out.write("tapTime:" + Integer.toString(shot.tapTime) + "\n");
			  }
			  out.write("score:" + Integer.toString(score) + "\n");
			  out.write("}\n");
			  //out.write("Hello Java");
			  //Close the output stream
			  out.close();
			  }catch (Exception e){//Catch exception if any
			    System.err.println("Error: " + e.getMessage());
			  }	
	}
	
	public void ReadShot(){
		//System.out.println(  System.getProperty("user.dir") ); //gets your current working directory which should contain the "MCTSresults" folder
		String fileLocation = "./MCTSresults/"   + levelD + "-"  + Integer.toString(currentLevel) + ".txt"; 
		//System.out.println(fileLocation);
		DataInputStream i = null;
		try {
			i = new DataInputStream(new FileInputStream( fileLocation ));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader b = new BufferedReader(new InputStreamReader(i));
		String str;
		try {
			while ((str = b.readLine()) != null) { 
				if( str.contains("score") ){
					int getScore = Integer.parseInt( str.substring( str.indexOf(':')+1 ) );
					System.out.println(getScore);
					BestScore = Math.max(BestScore, getScore);
				} 
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("bestScore");
		//System.out.println(BestScore);
	}
	
	
	
	//
	static int Simulations = 0; //total simulations already played
	
	//Back propagation variables. Used in the selection of MCTS
	// [0]: depth of the game tree (how many birds shot before this bird) 
	// [1] : angle: divide by 100 for angle in theta   [2] : tap time, divide by 100 to get tap time in ms
	int[][][] BackProp = new int[8][140][32];
	int[][][] Visited = new int[8][140][32];//factor that ensures exploration,
	static ArrayList<ABType> Birds;
	
	static int cAngle; //choice of angle
	static int cTap;  //choice of tap time
	
	public void Selection(int curBird){
		
		Random rAngle = new Random();
		cAngle = rAngle.nextInt(139); //if all values are below threshold, pick a random shot
		cTap = rAngle.nextInt(31); //if all values are below threshold, pick a random shot		
		if(  Birds.size() > curBird &&  Birds.get(curBird) == ABType.RedBird ){
			cTap = 0;
		}
		if( Simulations  < 0 ){ //Optional: Do random shots for the first x plays to gather data for the MCTS
			return;
		}
		
		
		//System.out.println(BackProp[0][11][0]);
		System.out.println("Current bird:");
		System.out.println(curBird);
		
		int maxShot = -1;//If all shots seem bad, choose the random shot selected above untill it finds a good solution.
		for(int i=0; i<140; i++){
			for(int j=0; j<32; j++){
				if( Birds.size() > curBird &&  Birds.get(curBird) == ABType.RedBird  && j > 0 ){
					continue; //Red birds don't need tap time
				}
				if(  (int)((float)BackProp[curBird][i][j]/Visited[curBird][i][j]) > maxShot){
					maxShot =  (int)((float)BackProp[curBird][i][j]/Visited[curBird][i][j]);
					cAngle = i;
					cTap = j;
				}
			}
		}
		
	}
	
	//End extra code for MCTS
	
	
	
	
	
	
	// run the client
	public void run() {

		
		Birds = new ArrayList<ABType>();
		//Initialize back propagation values
		for(int i=0; i<8; i++){
			for(int j=0; j<140; j++){
				for(int k=0; k<32; k++){
					BackProp[i][j][k] = 0;
					Visited[i][j][k] = 0;
				}
			}
			
		}
		
		
		
		shotFired = 0;
		currentLevel = 19;//Agent will only play this level
		ReadShot();		
		Storage.clear();
		
		aRobot.loadLevel(currentLevel);
		while (true) {
			GameState state = solve();
			if (state == GameState.WON) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int score = StateUtil.getScore(ActionRobot.proxy);
				
				//If the score is better than the best one currently recorded, store the new shot data
				if( score > BestScore ){
				      BestScore = score;
					  WriteShot(score);
					}
				
				//Back propagation
				int curLoc = 0;
				for( ShotData shot : Storage ){
					System.out.println("Updating Win Back progagation");
					System.out.println(shot.Theta);
					int angle = (int)(shot.Theta*100);
					int tap = (int) (shot.tapTime/100);
					Visited[curLoc][angle][tap]+=100;//Make sure that it will take a while before this node is revisited
					System.out.println(angle);
					for(int a=0; a<140; a++){
						for(int t=0; t<32; t++){
							if( angle == a &&  tap == t ){ //prevent division by 0
								BackProp[curLoc][ a ][ t ] += (int)(  ( score/5000.0 )  );
							} else {
								BackProp[curLoc][ a ][ t ] += (int)(   ( 1.0 /  (  Math.abs(angle - a)  + Math.abs(tap - t)) )*( score/5000.0 )  );								
							}
						}
					}
					//BackProp[curLoc][ (int)shot.Theta*100 ][ (int) shot.tapTime/100 ]--;
					curLoc++;
				}			
				
				Storage.clear();
				shotFired = 0;
				Simulations++;
				if(!scores.containsKey(currentLevel))
					scores.put(currentLevel, score);
				else
				{
					if(scores.get(currentLevel) < score)
						scores.put(currentLevel, score);
				}
				int totalScore = 0;
				for(Integer key: scores.keySet()){

					totalScore += scores.get(key);
					System.out.println(" Level " + key
							+ " Score: " + scores.get(key) + " ");
				}
				System.out.println("Total Score: " + totalScore);
				//aRobot.loadLevel(++currentLevel);
				aRobot.loadLevel(currentLevel);//Replay current level
				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();

				// first shot on this level, try high shot first
				firstShot = true;
			} else if (state == GameState.LOST) {
				System.out.println("Restart");
				//Back propagation
				int curLoc = 0;
				for( ShotData shot : Storage ){
					System.out.println("Updating Back progagation");
					System.out.println(shot.Theta);
					int angle = (int)(shot.Theta*100);
					int tap = (int) (shot.tapTime/100);
					Visited[curLoc][angle][tap]++;
					System.out.println(angle);
					//Discourage nearby shots because the last shot here was bad
					int NegScore = 10;
					for(int a=-10; a<11; a++){
						for(int t=-5; t<6; t++){
							if( angle + a >= 0 && angle + a < 140 &&  tap + t >= 0 && tap + t < 32 ){ //check if out of bounds
								if( 0 == a &&  0 == t ){ //check if out of bounds
									BackProp[curLoc][ angle + a ][ tap + t ] -= (int)(  NegScore  );
								} else {
									BackProp[curLoc][ angle + a ][ tap + t ] -= (int)(   ( 1.0 /  (  Math.abs( a )  + Math.abs( t )) )*( NegScore  ));								
								}
								//BackProp[curLoc][ angle + a ][ tap + t ]--;
							}
						}
					}
					//BackProp[curLoc][ (int)shot.Theta*100 ][ (int) shot.tapTime/100 ]--;
					curLoc++;
				}
				Storage.clear();
				shotFired=0;
				Simulations++;
				aRobot.restartLevel();
			} else if (state == GameState.LEVEL_SELECTION) {
				System.out
				.println("Unexpected level selection page, go to the last current level : "
						+ currentLevel);
				aRobot.loadLevel(currentLevel);
			} else if (state == GameState.MAIN_MENU) {
				System.out
				.println("Unexpected main menu page, go to the last current level : "
						+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
			} else if (state == GameState.EPISODE_MENU) {
				System.out
				.println("Unexpected episode menu page, go to the last current level : "
						+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
			}

		}

	}

	private double distance(Point p1, Point p2) {
		return Math
				.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
						* (p1.y - p2.y)));
	}

	public GameState solve()
	{

		// capture Image
		BufferedImage screenshot = ActionRobot.doScreenShot();

		// process image
		Vision vision = new Vision(screenshot);

		// find the slingshot
		Rectangle sling = vision.findSlingshotMBR();

		// confirm the slingshot
		while (sling == null && aRobot.getState() == GameState.PLAYING) {
			System.out
			.println("No slingshot detected. Please remove pop up or zoom out");
			ActionRobot.fullyZoomOut();
			screenshot = ActionRobot.doScreenShot();
			vision = new Vision(screenshot);
			sling = vision.findSlingshotMBR();
		}
        // get all the pigs
 		List<ABObject> pigs = vision.findPigsMBR();

		GameState state = aRobot.getState();

		// if there is a sling, then play, otherwise just skip.
		if (sling != null) {

			if (!pigs.isEmpty()) {

				Point releasePoint;
				Shot shot = new Shot();
				int dx,dy;
				{
					// random pick up a pig
					ABObject pig = pigs.get(randomGenerator.nextInt(pigs.size()));
					
					Point _tpt = pig.getCenter();// if the target is very close to before, randomly choose a
					// point near it
					prevTarget = new Point(_tpt.x, _tpt.y);

					// estimate the trajectory
					ArrayList<Point> pts = tp.estimateLaunchPoint(sling, _tpt);
					
					// do a high shot when entering a level to find an accurate velocity
					if (firstShot && pts.size() > 1) {
						releasePoint = pts.get(1);
					} else if (pts.size() == 1)
						releasePoint = pts.get(0);
					else {
						// randomly choose between the trajectories, with a 1 in
						// 6 chance of choosing the high one
						if (randomGenerator.nextInt(6) == 0)
							releasePoint = pts.get(1);
						else
							releasePoint = pts.get(0);
					}
					
					// Get the reference point
					Point refPoint = tp.getReferencePoint(sling);


					//static int cAngle; //choice of angle
					//static int cTap;  //choice of tap time
					
					Selection(shotFired);					
					double rAd = cAngle / 100.0;
					System.out.println(rAd);
					releasePoint = tp.findReleasePoint(sling, rAd );										
					System.out.println("Release Point: " + releasePoint);

					//Calculate the tapping time according the bird type 
					if (releasePoint != null) {
						double releaseAngle = tp.getReleaseAngle(sling,
								releasePoint);

						System.out.println("Release Angle: "
								+ Math.toDegrees(releaseAngle));
						int tapInterval = 0;
						ABType getBird = aRobot.getBirdTypeOnSling();
						switch (getBird) 
						{

						case RedBird:
							tapInterval = 0; break;               // start of trajectory
						case YellowBird:
							tapInterval = 65 + randomGenerator.nextInt(25);break; // 65-90% of the way
						case WhiteBird:
							tapInterval =  70 + randomGenerator.nextInt(20);break; // 70-90% of the way
						case BlackBird:
							tapInterval =  70 + randomGenerator.nextInt(20);break; // 70-90% of the way
						case BlueBird:
							tapInterval =  65 + randomGenerator.nextInt(20);break; // 65-85% of the way
						default:
							tapInterval =  60;
						}

						//int tapTime = tp.getTapTime(sling, releasePoint, _tpt, tapInterval);
						int tapTime =  cTap*100;
						if( getBird  == ABType.RedBird){//Red bird does not need tap time
							tapTime = 0;
						}
						System.out.println( tapTime );
						Storage.add( new ShotData(rAd,Math.toDegrees(releaseAngle),(int)releasePoint.getX(),(int)releasePoint.getY(),tapTime) );

						
						shotFired++;
						if(  Birds.size() < shotFired ){
							Birds.add( getBird );
							//Use tap intervals from naive agent as heuristic
							int MinTap = 0;
							int MaxTap = 0;
							switch (getBird) 
							{
							case RedBird:
								MinTap = 0;
								MaxTap = 0;
								break;
							case YellowBird:
								MinTap = 65;
								MaxTap = 90;
								break;
							case WhiteBird:
								MinTap = 70;
								MaxTap = 90;
								break;
							case BlackBird:
								MinTap = 70;
								MaxTap = 90;
								break;
							case BlueBird:
								MinTap = 65;
								MaxTap = 85;
								break;
							default:
								MinTap = 0;
								MaxTap = 0;
								break;
							}
							System.out.println( "new bird added");
							System.out.println( getBird);
							System.out.println( MinTap);
							System.out.println( MaxTap);
							//Add heuristic from naive agent to MCTS
							if( MinTap != 0 && MaxTap != 0 ){ //Red bird does ot use tap time
							 for(int a=0; a<140; a++){
								int lowerB = tp.getTapTime(sling, tp.findReleasePoint(sling, a/100.0 ) , _tpt, MinTap);
								int upperB = tp.getTapTime(sling, tp.findReleasePoint(sling, a/100.0 ) , _tpt, MaxTap);
								lowerB = Math.min(lowerB, 3000);
								upperB = Math.min(lowerB, 3000);
								//System.out.println(  lowerB );
								//System.out.println(  upperB  );
								for(int t=0; t<((int)(lowerB/100.0)); t++){
                                      BackProp[ Birds.size()-1 ][ a ][ t ] -= 10;
							  }
								for(int t=((int)(upperB/100.0))+1; t<32; t++){
                                    BackProp[ Birds.size()-1 ][ a ][ t ] -= 10;
							  }								
							}
						  }
						}
						
						dx = (int)releasePoint.getX() - refPoint.x;
						dy = (int)releasePoint.getY() - refPoint.y;
						shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime);
					}
					else
						{
							System.err.println("No Release Point Found");
							return state;
						}
				}

				// check whether the slingshot is changed. the change of the slingshot indicates a change in the scale.
				{
					ActionRobot.fullyZoomOut();
					screenshot = ActionRobot.doScreenShot();
					vision = new Vision(screenshot);
					Rectangle _sling = vision.findSlingshotMBR();
					if(_sling != null)
					{
						double scale_diff = Math.pow((sling.width - _sling.width),2) +  Math.pow((sling.height - _sling.height),2);
						if(scale_diff < 25)
						{
							if(dx < 0)
							{
								aRobot.cshoot(shot);
								state = aRobot.getState();
								if ( state == GameState.PLAYING )
								{
									screenshot = ActionRobot.doScreenShot();
									vision = new Vision(screenshot);
									List<Point> traj = vision.findTrajPoints();
									tp.adjustTrajectory(traj, sling, releasePoint);
									firstShot = false;
								}
							}
						}
						else
							System.out.println("Scale is changed, can not execute the shot, will re-segement the image");
					}
					else
						System.out.println("no sling detected, can not execute the shot, will re-segement the image");
				}

			}

		}
		return state;
	}

	public static void main(String args[]) {

		NaiveAgent na = new NaiveAgent();
		if (args.length > 0)
			na.currentLevel = Integer.parseInt(args[0]);
		na.run();

	}
}
