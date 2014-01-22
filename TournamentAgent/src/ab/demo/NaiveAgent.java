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
import ab.vision.ShowSeg;
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

	
	
	//Begin Michael class
	private int step = 2;

    public class LaunchData {
        private int score;
        private Point releasePoint;
        private Rectangle target;

        public LaunchData(int score, Point releasePoint, Rectangle target) {
            this.score = score;
            this.releasePoint = releasePoint;
            this.target = target;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public void setReleasePoint(Point releasePoint) {
            this.releasePoint = releasePoint;
        }

        public void setTarget(Rectangle target) {
            this.target = target;
        }

        public int getScore() {
            return score;
        }

        public Point getReleasePoint() {
            return releasePoint;
        }

        public Rectangle getTarget() {
            return target;
        }
    }
	
	//End Michael class
	
	
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

	class MLModelData{
		int Theta;
		int x;
		int y;	
		int tapTime;
		ArrayList<String> objects;
		public  MLModelData(int x, int y,int Theta,int tapTime, ArrayList<String> obj){
			this.Theta = Theta;
			this.tapTime = tapTime;
			this.x = x;
			this.y = y;
			this.objects = obj;
		}
	}
	
	static 	ArrayList<MLModelData> MachineM = new  ArrayList<MLModelData>();
	
	static 	ArrayList<ShotData> Storage = new  ArrayList<ShotData>();
	static int BestScore = 0;	
	static int shotFired = 0;
	


	ArrayList<Integer> Angles = new ArrayList<Integer>();
	ArrayList<Integer> TapTimes = new ArrayList<Integer>();
	
	
	
	public void ReadModel(){
		//System.out.println(  System.getProperty("user.dir") ); //gets your current working directory which should contain the "MCTSresults" folder
		//String fileLocation = "/home/s081286/Angry Birds/MM.txt"; 
		String fileLocation = "./MM.txt"; 
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
			MLModelData tempM;
			ArrayList<String> objects = new ArrayList<String>();;
			int counter = 0;
			int getAngle = 0;
			int getTaptime = 0;
			int x = 0;
			int y = 0;
			while ( true ) { 
				//System.out.println( str );
				str = b.readLine();
				if(  str == null ){
					break;
				}
				if( str.contains("{") ){
					objects = new ArrayList<String>();
					str = b.readLine();
					x = Integer.parseInt( str );
					str = b.readLine();
					//System.out.println( str);
					y = Integer.parseInt( str );
					str = b.readLine();
					getAngle = Integer.parseInt( str );
					str = b.readLine();
					getTaptime = Integer.parseInt( str );
					while ( true) { 
						str = b.readLine();
						if(  str == null || str.contains("}") ){
							//System.out.println("break");
							break;
						}
						objects.add( str );
						//System.out.println(str);
					}
					tempM = new  MLModelData(x, y,getAngle,getTaptime, objects);
					MachineM.add(tempM);
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

	static int cAngle; //choice of angle
	static int cTap;  //choice of tap time
		
	
	//Determines if the algorithm uses the machine learning for shots, or the trajectory maximization
	boolean MachineModel = true;
	
	//End extra code for MCTS
	
	
	
	boolean[] LevelSucces = new boolean[23];
	int totalLossed = 0;
	
	// run the client
	public void run() {

		//Vision
		//Thread thre = new Thread(new ShowSeg());
		//thre.start();
	
		
		shotFired = 0;
		currentLevel = 1;//Start with level 1
		ReadModel();
		Storage.clear();
		
		for(int i=0; i<22; i++){
			LevelSucces[i] = false;
		}
		
		//Showing machine learning model
		//System.out.println( "Size ML:" );
		//System.out.println( MachineM.size() );
		//for(  MLModelData mOb : MachineM ){
			//System.out.println(mOb.tapTime );
		//}
		
		
		
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
				
				

				Storage.clear();
				shotFired = 0;
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
				if(  MachineModel == false ){
					
					LevelSucces[ currentLevel ] = true;
					totalLossed = 0;
					
					if(  currentLevel != 21 ){
						currentLevel++;
					}
					while( LevelSucces[ currentLevel ] != false  ){
						currentLevel++;
						if( currentLevel == 21 ){
							break;
						}
					}
					aRobot.loadLevel(currentLevel);//go to the next unsolved
					
					
				}
				if(  MachineModel == true ){
					LevelSucces[ currentLevel ] = true;
					currentLevel++;
					if(  currentLevel < 22 ){
					  aRobot.loadLevel(currentLevel);//go to the next level
					} else{
						MachineModel = false;//Been through all levels, now switch to Michaels algorithm
						currentLevel = 1;
						while( LevelSucces[ currentLevel ] != false  ){ //Look for an unsolved level
							currentLevel++;
							if( currentLevel == 21 ){//Cannot go higher than 21
								break;
							}
						}
						aRobot.loadLevel(currentLevel);//go to the next unsolved
					}
				}
				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();

				// first shot on this level, try high shot first
				firstShot = true;
			} else if (state == GameState.LOST) {
				System.out.println("Restart");
				Storage.clear();
				shotFired=0;
				
				if(  MachineModel == false ){
					if(  currentLevel == 21 ){
						aRobot.restartLevel();//Keep on playing level 21 until the time runs out
					} else {
						totalLossed++;
						if( totalLossed < 2 ){
							aRobot.restartLevel();// Retry the level once
						} else {
							totalLossed = 0;
							currentLevel++;

							while( LevelSucces[ currentLevel ] != false  ){
								currentLevel++;
								if( currentLevel == 21 ){
									break;
								}
							}
							
						}
					}
				}
				if(  MachineModel == true ){
					currentLevel++;
					if(  currentLevel < 22 ){
						  aRobot.loadLevel(currentLevel);//go to the next level
						} else{
							MachineModel = false;//Been through all levels, now switch to Michaels algorithm
							currentLevel = 1;
							while( LevelSucces[ currentLevel ] != false  ){  //Look for an unsolved level
								currentLevel++;
								if( currentLevel == 21 ){//Cannot go higher than 21
									break;
								}
							}
							aRobot.loadLevel(currentLevel);//go to the next unsolved
						}
				}
				
				
				
				//aRobot.restartLevel();
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

 		List<ABObject> AllOjbects = pigs;
 		AllOjbects.addAll( vision.findBlocksMBR() );
 		
 		
 		//Compare the vision to the stored models
 		//Only retrieve exact matches
 		ArrayList<Integer> Alltheta = new ArrayList<Integer>();
 		ArrayList<Integer> Uniquetheta = new ArrayList<Integer>();
 		ArrayList<Integer> AlltapTimes = new ArrayList<Integer>();
 		ArrayList<Integer> UniquetapTimes = new ArrayList<Integer>(); 		
 		ActionRobot.fullyZoomOut();
		for(int x=0;x<30;x++){
			for(int y=0; y<23;y++){
				Rectangle newRect = new Rectangle( 20*x,20*y,20,20);
				ArrayList<ABObject> Intersect = new ArrayList<ABObject>();
				ArrayList<String> IntersectS = new ArrayList<String>();
				for(  ABObject ob:  AllOjbects ){
					if(   ob.getBounds().intersects(newRect)  ){
						Intersect.add(ob);
						IntersectS.add(ob.type.toString() );
					}
				}
				if(  !Intersect.isEmpty() ){
					//System.out.println( x );
					for(  MLModelData mOb : MachineM ){
						//System.out.println( mOb.y  );
						if( mOb.x == x && mOb.y == y && mOb.objects.equals(IntersectS)   ){ // && mOb.objects.containsAll( IntersectS )  && IntersectS.containsAll( mOb.objects )
							//System.out.println("Found");
							//System.out.println(mOb.objects);
							//System.out.println( IntersectS );
							//System.out.println( mOb.tapTime );
							Alltheta.add( mOb.Theta );
							AlltapTimes.add( mOb.tapTime );
							if(  !Uniquetheta.contains( mOb.Theta  ) ){
								Uniquetheta.add( mOb.Theta  );
							}
							if(  !UniquetapTimes.contains( mOb.tapTime  ) ){
								UniquetapTimes.add( mOb.tapTime );
							}							
						}
					}
				}
			}
		}	 		

		//Find the angle (theta) that occurs the most given the vision and model
		//That angle has the highest probability of a good shot
		int highest = 0;
		int thetaC = -1;
		for( Integer u:  Uniquetheta  ){
			int count = 0;
			for( Integer a : Alltheta){
				if(  u == a ){
					count++;
				}
			}
			//System.out.println( count );
			if( highest < count ){
				highest = count;
				thetaC = u;
			}
		}

		
		
		
		
		GameState state = aRobot.getState();

		// if there is a sling, then play, otherwise just skip.
		if (sling != null) {
			ActionRobot.fullyZoomOut();
			if (!pigs.isEmpty()) {

				Point releasePoint;
				Shot shot = new Shot();
				int dx,dy;
				{
					//begin Michael trajectory
					
					ABType getBird = null;
					
					if( MachineModel == false ){
					  getBird = aRobot.getBirdTypeOnSling();
					}

					
					List<Rectangle> ice = vision.getHitableIce();
                    List<Rectangle> wood = vision.getHitableWood();
                    List<Rectangle> stone = vision.getHitableStone();
                    ArrayList<Rectangle> allHittable = new ArrayList<Rectangle>();
                    allHittable.addAll(vision.getHitableIce());
                    allHittable.addAll(vision.getHitableWood());
                    allHittable.addAll(vision.getHitableStone());
                    ArrayList<Rectangle> allObjects = new ArrayList<Rectangle>();
                    allObjects.addAll( vision.visionMBR.findIce());
                    allObjects.addAll(vision.visionMBR.findWood());
                    allObjects.addAll(vision.visionMBR.findStones());
                    ArrayList<LaunchData> possibleLaunchPoints = new ArrayList<LaunchData>();
                    // iterate through all hittable objects
                    for (Rectangle rect : allHittable) {
                        System.out.println("Top: " + rect.x + "|" + rect.y);
                        ArrayList<Point> launchpoints = tp.estimateLaunchPoint(sling, new Point((int) rect.getCenterX(), (int) rect.getCenterY()));
                        // iterate through both possible launchpoints to hit that object
                        for (Point launch : launchpoints) {
                            System.out.println("Launch: " + launch);
                            ArrayList<Rectangle> intersects = new ArrayList<Rectangle>();
                            int trajY = tp.getYCoordinate(sling, launch, (int) rect.getCenterX());
                            if(  getBird == ABType.WhiteBird ){
                            	trajY -= 100; //White bird should hit above target to drop egg
                            }
                            int yStart = trajY;
                            int xStart = (int) rect.getCenterX();
                            int score = 0;
                            // iterate through all point behind the hit object
                            for (int trajX = xStart; trajY >= 0 && trajX - xStart < 100 && trajY - yStart < 100; trajX += step) {
                                trajY = tp.getYCoordinate(sling, launch, trajX);
                                for (Rectangle object : allObjects) {
                                    if (object.contains(trajX, trajY) && !intersects.contains(object)) {
                                        intersects.add(object);
                                        System.out.println("intersect " + object);
                                        score += 1 - Math.max(trajX - xStart, trajY - yStart) / 100;
                                    }
                                }
                            }
                            // add launchpoint to possibilities
                            if (possibleLaunchPoints.size() < 3) {
                                possibleLaunchPoints.add(new LaunchData(intersects.size(), launch, rect));
                            } else {
                                // find worst possiblity
                                LaunchData min = null;
                                for (LaunchData ld : possibleLaunchPoints) {
                                    if (min == null || ld.getScore() < min.getScore()) {
                                        min = ld;
                                    }
                                }
                                // replace worst possibility
                                if (intersects.size() > min.getScore()) {
                                    possibleLaunchPoints.remove(min);
                                    possibleLaunchPoints.add(new LaunchData(intersects.size(), launch, rect));
                                }
                            }
                        }
                    }
                    Random r = new Random();
                    int index = r.nextInt(possibleLaunchPoints.size());
                    LaunchData launchData = possibleLaunchPoints.get(index);
                    Rectangle target = launchData.getTarget();
                    Point _tpt = new Point((int) target.getCenterX(),
                            (int) target.getCenterY());					
					
					
					//end Michael trajectory
					
					// random pick up a pig
					//ABObject pig = pigs.get(randomGenerator.nextInt(pigs.size()));
					
					//Point _tpt = pig.getCenter();// if the target is very close to before, randomly choose a
					// point near it
					prevTarget = new Point(_tpt.x, _tpt.y);

					if (prevTarget != null && distance(prevTarget, _tpt) < 10) {
						double _angle = randomGenerator.nextDouble() * Math.PI * 2;
						_tpt.x = _tpt.x + (int) (Math.cos(_angle) * 10);
						_tpt.y = _tpt.y + (int) (Math.sin(_angle) * 10);
						System.out.println("Randomly changing to " + _tpt);
					}	
					
					/*
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
					*/
					
					// Get the reference point
					Point refPoint = tp.getReferencePoint(sling);
					releasePoint = launchData.getReleasePoint();
					
			

					//System.out.println(rAd);
					if( MachineModel == true ){
					  double rAd = thetaC / 100.0;
					  releasePoint = tp.findReleasePoint(sling, rAd );	
					}
					System.out.println("Release Point: " + releasePoint);

					//Calculate the tapping time according the bird type 
					if (releasePoint != null) {
						double releaseAngle = tp.getReleaseAngle(sling,
								releasePoint);

						System.out.println("Release Angle: "
								+ Math.toDegrees(releaseAngle));
						int tapInterval = 0;
						//ABType getBird = aRobot.getBirdTypeOnSling();

						
						//if( MachineModel == true ){
							//  getBird = aRobot.getBirdTypeOnSling();
						//}
						getBird = aRobot.getBirdTypeOnSling();
						
						switch (getBird) 
						{

						case RedBird:
							tapInterval = 0; break;               // start of trajectory
						case YellowBird:
							//tapInterval = 65 + randomGenerator.nextInt(25);break; // 65-90% of the way
							tapInterval = 90;break; // 90% of the way (strategy)
						case WhiteBird:
							tapInterval =  70 + randomGenerator.nextInt(30);break; // 70-100% of the way
							
						case BlackBird:
							//tapInterval =  70 + randomGenerator.nextInt(20);break; // 70-90% of the way
							tapInterval =  0;break;//Blacks birds work best when they don't get tapped
						case BlueBird:
							tapInterval =  65 + randomGenerator.nextInt(20);break; // 65-85% of the way
						default:
							tapInterval =  0;
						}

						System.out.println( getBird );
						
						
						int highestT = 0;
						int taptimeC = -1;
						//Find the tap time that occurs the most given the vision and model
						//That tapTime has the highest probability of a good shot
						System.out.println(UniquetapTimes);
						System.out.println(AlltapTimes);
						
						for( int i=0; i<UniquetapTimes.size(); i++  ){	
							int u = UniquetapTimes.get(i);
							int count = 0;
							if( u == 0 && !getBird.equals(ABType.BlackBird ) &&  !getBird.equals(ABType.RedBird )   ){//Skip 0 tap time for birds that use it								
								continue;
							}	
							for(  int j=0; j<AlltapTimes.size(); j++){
								int a = AlltapTimes.get(j);
								if(  u == a ){
									count += 1;
								}
							}
							System.out.println( count );
							if( highestT < count ){
								highestT = count;
								taptimeC = u;
							}
						}
						System.out.println(taptimeC);
						
						int tapTime;
						if( MachineModel == true ){
							tapTime = taptimeC;
						} else {
							tapTime = tp.getTapTime(sling, releasePoint, _tpt, tapInterval);
						}
						
						//int tapTime = tp.getTapTime(sling, releasePoint, _tpt, tapInterval);
						//int tapTime =  TapTimes.get(shotFired);
						//int tapTime =  cTap*100;
						//int tapTime = taptimeC;
						if( getBird.equals(ABType.RedBird) || getBird.equals(ABType.BlackBird) ){//Red and black bird do not need tap time (strategy)
							tapTime = 0;
						}
						System.out.println( tapTime );
						//Storage.add( new ShotData(rAd,Math.toDegrees(releaseAngle),(int)releasePoint.getX(),(int)releasePoint.getY(),tapTime) );

						/* MCTS not used in the final agent. But could possible be combined
						 * 
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
							if( MinTap != 0 && MaxTap != 0 ){ //Red bird does not use tap time
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
							*/					
										
						
						
						dx = (int)releasePoint.getX() - refPoint.x;
						dy = (int)releasePoint.getY() - refPoint.y;
						shotFired++;
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
