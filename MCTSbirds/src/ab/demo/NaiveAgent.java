/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2013, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys, Kar-Wai Lim, Zain Mubashir, Andrew Wang, Peng Zhang
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
import java.util.List;
import java.util.Random;

import ab.demo.other.ActionRobot;
import ab.demo.other.Env;
import ab.demo.other.Shot;
import ab.demo.util.StateUtil;
import ab.planner.TrajectoryPlanner;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

public class NaiveAgent implements Runnable {

	private int focus_x;
	private int focus_y;

	private ActionRobot ar;
	public int currentLevel = 1;
	TrajectoryPlanner tp;

	private boolean firstShot;
	private Point prevTarget;

	// a standalone implementation of the Naive Agent
	public NaiveAgent() {
		ar = new ActionRobot();
		tp = new TrajectoryPlanner();
		prevTarget = null;
		firstShot = true;
		// --- go to the Poached Eggs episode level selection page ---
		ActionRobot.GoFromMainMenuToLevelSelection();

	}

	public int getCurrent_level() {
		return currentLevel;
	}

	public void setCurrent_level(int current_level) {
		this.currentLevel = current_level;
	}

	
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
	static double currentT = 0.35;//0.4210;
	static double angleStep = 0.01;	
	static HashMap<String, Integer> nodeCount = new HashMap<String, Integer>();
	static HashMap<String, Double> scoreCount = new HashMap<String, Double>();	
	static int shotFired = 0;
	
	public void WriteShot(int score){
		  try{
			  // Create file 
			  FileWriter fstream = new FileWriter("/home/s081286/Angry Birds/MCTSbirds/MCTSresults/1-" + Integer.toString(currentLevel) + ".txt",true);
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
		String fileLocation = "/home/s081286/Angry Birds/MCTSbirds/MCTSresults/1-" + Integer.toString(currentLevel) + ".txt"; 
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
	}
	
	
	// run the client
	public void run() {

		
		//Fill up hash map
		for(int i = 0; i<100; i++){
			for(int j=0; j<3000;j++){
				for(int k=0; k<8;k++){	
					String key =  Integer.toString(i) + "-" + Integer.toString(j) + "-"  + Integer.toString(k);
				    nodeCount.put(key, new Integer(0));
				    scoreCount.put(key, new Double(0.0));
				    //System.out.println(key);				
				}
			}			
		}
		
		
		currentLevel = 10;
		ReadShot();
		Storage.clear();//new		
		ar.loadLevel(currentLevel);
		while (true) {
			GameState state = solve();
			if (state == GameState.WON) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int score = -2;
				while (score != StateUtil.checkCurrentScore(ar.proxy)) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					score = StateUtil.checkCurrentScore(ar.proxy);
				}
				System.out.println("###### The game score is " + score
						+ "########");
				if( score > BestScore ){
			      BestScore = score;
				  WriteShot(score);//write score
				}
				currentT += angleStep;
				for(int i=0; i<Storage.size(); i++){ //Update UCT bounds
				   String thisKey = Integer.toString( (int)(Storage.get(i).Theta*100) ) + "-" + Integer.toString( Storage.get(i).tapTime ) + "-"  + Integer.toString(i);
				   int nc = nodeCount.get(thisKey);				    
				   scoreCount.put(thisKey,  (scoreCount.get(thisKey)* nc + score) / (nc+1) );
				}
				Storage.clear();//new
				shotFired=0;
				ar.loadLevel(currentLevel);
				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();

				// first shot on this level, try high shot first
				firstShot = true;
			} else if (state == GameState.LOST) {
				System.out.println("restart");
				//WriteShot(0);//write score
				currentT += angleStep;
				for(int i=0; i<Storage.size(); i++){ //Update UCT bounds
					   String thisKey = Integer.toString( (int)(Storage.get(i).Theta*100) ) + "-" + Integer.toString( Storage.get(i).tapTime ) + "-"  + Integer.toString(i);
					   int nc = nodeCount.get(thisKey);				    
					   scoreCount.put(thisKey,  (scoreCount.get(thisKey)* nc - 1) / (nc+1) );
				}
				Storage.clear();//new
				shotFired=0;
				ar.restartLevel();
			} else if (state == GameState.LEVEL_SELECTION) {
				System.out
						.println("unexpected level selection page, go to the lasts current level : "
								+ currentLevel);
				ar.loadLevel(currentLevel);
			} else if (state == GameState.MAIN_MENU) {
				System.out
						.println("unexpected main menu page, go to the lasts current level : "
								+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				ar.loadLevel(currentLevel);
			} else if (state == GameState.EPISODE_MENU) {
				System.out
						.println("unexpected episode menu page, go to the lasts current level : "
								+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				ar.loadLevel(currentLevel);
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

		Rectangle sling = vision.findSlingshot();

		while (sling == null && ar.checkState() == GameState.PLAYING) {
			System.out
					.println("no slingshot detected. Please remove pop up or zoom out");
			ar.fullyZoom();
			screenshot = ActionRobot.doScreenShot();
			vision = new Vision(screenshot);
			sling = vision.findSlingshot();
		}

		List<Rectangle> red_birds = vision.findRedBirds();
		List<Rectangle> blue_birds = vision.findBlueBirds();
		List<Rectangle> yellow_birds = vision.findYellowBirds();
		List<Rectangle> pigs = vision.findPigs();
		int bird_count = 0;
		bird_count = red_birds.size() + blue_birds.size() + yellow_birds.size();

		System.out.println("...found " + pigs.size() + " pigs and "
				+ bird_count + " birds");
		GameState state = ar.checkState();

		// if there is a sling, then play, otherwise just skip.
		if (sling != null) {
			ar.fullyZoom();
			if (!pigs.isEmpty()) {

				// Initialise a shot list
				ArrayList<Shot> shots = new ArrayList<Shot>();
				Point releasePoint;
				{
					// random pick up a pig
					Random r = new Random();

					int index = r.nextInt(pigs.size());
					Rectangle pig = pigs.get(index);
					Point _tpt = new Point((int) pig.getCenterX(),
							(int) pig.getCenterY());

					System.out.println("the target point is " + _tpt);

					// if the target is very close to before, randomly choose a
					// point near it
					if (prevTarget != null && distance(prevTarget, _tpt) < 10) {
						double _angle = r.nextDouble() * Math.PI * 2;
						_tpt.x = _tpt.x + (int) (Math.cos(_angle) * 10);
						_tpt.y = _tpt.y + (int) (Math.sin(_angle) * 10);
						System.out.println("Randomly changing to " + _tpt);
					}

					prevTarget = new Point(_tpt.x, _tpt.y);

					// estimate the trajectory
					ArrayList<Point> pts = tp.estimateLaunchPoint(sling, _tpt);

					// do a high shot when entering a level to find an accurate
					// velocity
					if (firstShot && pts.size() > 1) {
						releasePoint = pts.get(1);
					} else if (pts.size() == 1)
						releasePoint = pts.get(0);
					else {
						// System.out.println("first shot " + firstShot);
						// randomly choose between the trajectories, with a 1 in
						// 6 chance of choosing the high one
						if (r.nextInt(6) == 0)
							releasePoint = pts.get(1);
						else
							releasePoint = pts.get(0);
					}
					Point refPoint = tp.getReferencePoint(sling);
					/* Get the center of the active bird */
					focus_x = (int) ((Env.getFocuslist()
							.containsKey(currentLevel)) ? Env.getFocuslist()
							.get(currentLevel).getX() : refPoint.x);
					focus_y = (int) ((Env.getFocuslist()
							.containsKey(currentLevel)) ? Env.getFocuslist()
							.get(currentLevel).getY() : refPoint.y);
					
					//score = gameStateExtractor.getScoreInGame(image);
					
					//Get the best shot to take with MCTS-UCT
					int C = 1;
					double bestMCTS = -1.0;
					double bestAngle = 0.0;
					int bestTapTime = 0;
					int np = 1;
					if(  Storage.size() > 1 ){
					   String thisKey = Integer.toString( (int)(Storage.get(Storage.size()-1).Theta*100) ) + "-" + Integer.toString( Storage.get(Storage.size()-1).tapTime ) + "-"  + Integer.toString(Storage.size()-1);
					   np = nodeCount.get(thisKey);
					}
					for(int i=0;i<100; i++){
						for(int j=0; j<3000; j++){
							String findKey =  Integer.toString(i) + "-" + Integer.toString(j) + "-"  + Integer.toString(shotFired);
							double currentScore = scoreCount.get(findKey);
							int ni = nodeCount.get(findKey);
							if(ni == 0){
								ni = 1;
							}
							currentScore += C * Math.sqrt( Math.log(np)  / ni );
							if( currentScore > bestMCTS ){
								bestMCTS = currentScore;
								bestAngle = i / 100.0;
								bestTapTime = j;
							}
						    //nodeCount.put(key, new Integer(0));
						    //scoreCount.put(key, new Double(0.0));
						}
					}
					
					Random rAngle = new Random();
					//double rAd =  rAngle.nextDouble();//3.14159/2.0;//rAngle.nextDouble();
					//double rAd =  currentT;//3.14159/2.0;//rAngle.nextDouble();
					double rAd = bestAngle;
					System.out.println(rAd);
					releasePoint = tp.findReleasePoint(sling, rAd );
					//releasePoint = tp.findReleasePoint(sling, rAd * ( 3.14159/2.0 ) );
					//releasePoint = tp.findReleasePoint(sling, tp.launchToActual(rAd) );
					System.out.println("the release point is: " + releasePoint);
					/*
					 * =========== Get the release point from the trajectory
					 * prediction module====
					 */
					System.out.println("Shoot!!");
					if (releasePoint != null) {
						double releaseAngle = tp.getReleaseAngle(sling,releasePoint);
						System.out.println(" The release angle is : "
								+ Math.toDegrees(releaseAngle));
						int base = 0;
						if (releaseAngle > Math.PI / 4)
							base = 1400;
						else
							base = 550;
						//int tap_time = (int) (base + Math.random() * 1500);
						int tap_time = bestTapTime;
						System.out.println("Tap time is:" + Integer.toString(tap_time));
						
						Storage.add( new ShotData(rAd,Math.toDegrees(releaseAngle),(int)releasePoint.getX(),(int)releasePoint.getY(),tap_time) );
						String thisKey = Integer.toString( (int)(Storage.get(Storage.size()-1).Theta*100) ) + "-" + Integer.toString( Storage.get(Storage.size()-1).tapTime ) + "-"  + Integer.toString(Storage.size()-1);
						nodeCount.put( thisKey, nodeCount.get(thisKey) + 1);						
						
						shotFired++;
						shots.add(new Shot(focus_x, focus_y, (int) releasePoint
								.getX() - focus_x, (int) releasePoint.getY()
								- focus_y, 0, tap_time));
					} else
						System.err.println("Out of Knowledge");
				}

				// check whether the slingshot is changed. the change of the
				// slingshot indicates a change in the scale.
				{
					ar.fullyZoom();
					screenshot = ActionRobot.doScreenShot();
					vision = new Vision(screenshot);
					Rectangle _sling = vision.findSlingshot();
					if (sling.equals(_sling)) {
						state = ar.shootWithStateInfoReturned(shots);
						// update parameters after a shot is made
						if (state == GameState.PLAYING) {
							screenshot = ActionRobot.doScreenShot();
							vision = new Vision(screenshot);
							List<Point> traj = vision.findTrajPoints();
							tp.adjustTrajectory(traj, sling, releasePoint);
							firstShot = false;
							
						}
					} else
						System.out
								.println("scale is changed, can not execute the shot, will re-segement the image");
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
