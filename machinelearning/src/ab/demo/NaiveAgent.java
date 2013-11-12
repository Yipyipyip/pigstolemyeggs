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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ab.demo.other.ActionRobot;
import ab.demo.other.Env;
import ab.demo.other.Shot;
import ab.demo.util.StateUtil;
import ab.planner.TrajectoryPlanner;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

import java.io.*;


class AngryRect{
	int ID;
	Rectangle rect;
	String Type;
	public AngryRect(int ID,Rectangle rect,String Type){
		this.ID = ID;
		this.rect = rect;
		this.Type = Type;
	}
}

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

	// run the client
	public void run() {

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
				for(MLojbects s : Storage){
					WinStorage.add(s);	//add all current shots as winning shots
				}
				Storage.clear();
				ar.loadLevel(++currentLevel);
				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();

				FindTerrainSquares();//use the json file to find the terrain squares in the level
				
				// first shot on this level, try high shot first
				firstShot = true;
			} else if (state == GameState.LOST) {
				System.out.println("restart");
				for(MLojbects s : Storage){
					LostStorage.add(s);	
				}
				Storage.clear();
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

	
	public void ObjectRelations(){
		// capture Image
		BufferedImage screenshot = ActionRobot.doScreenShot();
		// process image
		Vision vision = new Vision(screenshot);			
		List<Rectangle> pigs = vision.findPigs();
		List<Rectangle> Obstacles_wood = vision.findWood();
		List<Rectangle> Obstacles_stone = vision.findStones();
        List<Rectangle> Obstacles_ice   = vision.findIce();		
		// AngryRect
		ArrayList<AngryRect> Structure = new ArrayList<AngryRect>();
		int RectCount = 0;
		for( Rectangle rec : pigs ){
			AngryRect newRect = new AngryRect(RectCount,rec,"Pig");
			Structure.add(newRect);
			RectCount++;
		}
		for( Rectangle stone : Obstacles_stone ){
			AngryRect newRect = new AngryRect(RectCount,stone,"Stone");
			Structure.add(newRect);
			RectCount++;			
		}
		for( Rectangle ice : Obstacles_ice ){
			AngryRect newRect = new AngryRect(RectCount,ice,"Ice");
			Structure.add(newRect);
			RectCount++;			
		}
		for( Rectangle wood : Obstacles_wood ){
			AngryRect newRect = new AngryRect(RectCount,wood,"Wood");
			Structure.add(newRect);
			RectCount++;				
		}
		for( AngryRect rectStruct : Structure ){
			System.out.println( rectStruct.ID );
			System.out.println( rectStruct.rect );
			System.out.println( rectStruct.Type );
		}
		for( AngryRect rectStruct : Structure ){
			if( rectStruct.Type != "Pig"){
				continue;
			}
			Rectangle rec = rectStruct.rect;
			int Spacing = 5;
			Rectangle BelowCheck =  new Rectangle(  (int) rec.getX(), (int)  (rec.getY() + rec.getHeight()), (int)rec.getWidth(), Spacing);
			Rectangle AboveCheck =  new Rectangle(  (int) rec.getX(), (int)  (rec.getY() - Spacing), (int)rec.getWidth(), Spacing);
			Rectangle ToLeftCheck =  new Rectangle(  (int) rec.getX() - Spacing, (int)  rec.getY(), Spacing, (int) rec.getHeight());
			Rectangle ToRightCheck =  new Rectangle(  (int) (rec.getX() + rec.getWidth() ) , (int)  rec.getY(), Spacing, (int) rec.getHeight());
			for( AngryRect rectStruct2 : Structure ){
				if( rectStruct.ID == rectStruct2.ID ){//Don't compare to yourself
					continue;
				}
			  	if( rectStruct2.rect.intersects( BelowCheck ) ){
			  		System.out.println("Intersection below:");
			  		System.out.println( rectStruct.ID );
			  		System.out.println( rectStruct.Type );
			  		System.out.println( rectStruct2.ID );
			  		System.out.println( rectStruct2.Type );
			  	}
			  	if( rectStruct2.rect.intersects( AboveCheck ) ){
			  		System.out.println("Intersection above:");
			  		System.out.println( rectStruct.ID );
			  		System.out.println( rectStruct.Type );
			  		System.out.println( rectStruct2.ID );
			  		System.out.println( rectStruct2.Type );
			  	}
			  	if( rectStruct2.rect.intersects( ToLeftCheck ) ){
			  		System.out.println("Intersection to the left:");
			  		System.out.println( rectStruct.ID );
			  		System.out.println( rectStruct.Type );
			  		System.out.println( rectStruct2.ID );
			  		System.out.println( rectStruct2.Type );
			  	}
			  	if( rectStruct2.rect.intersects( ToRightCheck ) ){
			  		System.out.println("Intersection to the right:");
			  		System.out.println( rectStruct.ID );
			  		System.out.println( rectStruct.Type );
			  		System.out.println( rectStruct2.ID );
			  		System.out.println( rectStruct2.Type );
			  	}
			}
		}		
		
	}
	
	
	class MLojbects {
		ArrayList<String> objects;
		int x;
		int y;
		public  MLojbects(int x, int y){
			this.x = x;
			this.y = y;
			this.objects = new ArrayList<String>();
		}
	}
	
	static 	ArrayList<MLojbects> Storage = new  ArrayList<MLojbects>();
	static 	ArrayList<MLojbects> WinStorage = new  ArrayList<MLojbects>();
	static 	ArrayList<MLojbects> LostStorage = new  ArrayList<MLojbects>();
	
	static 	ArrayList<Rectangle> TerrainSquares = new  ArrayList<Rectangle>();
	
	public void FindTerrainSquares(){
		TerrainSquares.clear();
		String fileLocation = "JsonFiles/Level1-" + Integer.toString( currentLevel ) + ".json"; 
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
				if( str.contains("TERRAIN_TEXTURED") ){
					String sizeString = str.substring(36);
					System.out.println (sizeString); 
					int ObjectX = Integer.parseInt( sizeString.substring( 0 ,  sizeString.indexOf('X') ) );
					int ObjectY = Integer.parseInt( sizeString.substring(  sizeString.indexOf('X') + 1,  sizeString.indexOf('\"') ) );					
					//str.substring(30);
					//30
					System.out.println (ObjectX); 
					System.out.println (ObjectY); 
					str = b.readLine();
					double LocationDx = Double.parseDouble( str.substring(11, str.indexOf(',') ) );
					//Double.parseDouble(
					//11 
					System.out.println (str); 
					System.out.println (LocationDx); 
					str = b.readLine();	
					double LocationDy = Double.parseDouble( str.substring(11) );
					System.out.println (str); 
					System.out.println (LocationDy); 
					double widthR = 0.0;
					double heigthR = 0.0;
					if( ObjectX == 1 && ObjectY == 1 ){
						widthR = 18.4;
						heigthR = 18.4;
					} else if( ObjectX == 5 && ObjectY == 2){
						widthR = 97.0;
						heigthR = 32.0;						
					} else if( ObjectX == 5 && ObjectY == 5){
						widthR = 97.0;
						heigthR = 97.0;						
					} else if( ObjectX == 10 && ObjectY == 2){
						widthR = 194.0;
						heigthR = 33.0;						
					} else if( ObjectX == 10 && ObjectY == 10){
						widthR = 194.0;
						heigthR = 97.0;						
					} else if( ObjectX == 32 && ObjectY == 2){
						widthR = 642.0;
						heigthR = 33.0;						
					}
					Rectangle terrainTest = new Rectangle( (int) (LocationDx*40), (int) (100-LocationDy*40), (int)widthR,(int)heigthR);
					TerrainSquares.add(terrainTest);
				} 
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Find all objects (rectangles) in the 7x7 grid around the target
	public void RectInGrid(Point target){

		// capture Image
		BufferedImage screenshot = ActionRobot.doScreenShot();
		// process image
		Vision vision = new Vision(screenshot);			
		List<Rectangle> pigs = vision.findPigs();
		List<Rectangle> Obstacles_wood = vision.findWood();
		List<Rectangle> Obstacles_stone = vision.findStones();
        List<Rectangle> Obstacles_ice   = vision.findIce();			
		
	  int GridSize = 20; //Grid cell size for the 7x7 grid
	  double StartX = target.getX() - 1*GridSize;
	  double StartY = target.getY() - 3*GridSize;

	  //ArrayList<MLojbects> Storage = new  ArrayList<MLojbects>();
      for(int y=0; y<7; y++){
    	  for(int x=0; x<7; x++){  
    		  MLojbects curObject = new MLojbects(x,y);
    		  Rectangle gridTest = new Rectangle((int)(StartX + x*GridSize),(int)(StartY + y*GridSize),GridSize,GridSize);
    			for( Rectangle rec : pigs ){
    				if( rec.intersects(gridTest) ){
    					curObject.objects.add("Pig");    					
    				}
    			}
    			for( Rectangle stone : Obstacles_stone ){
    				if( stone.intersects(gridTest) ){
    					curObject.objects.add("Stone");    					
    				}		
    			}
    			for( Rectangle ice : Obstacles_ice ){
    				if( ice.intersects(gridTest) ){
    					curObject.objects.add("Ice");    					
    				}	
    			}
    			for( Rectangle wood : Obstacles_wood ){
    				if( wood.intersects(gridTest) ){
    					curObject.objects.add("Wood");    					
    				}	
    			}  
    			for( Rectangle terrain : TerrainSquares ){
    				if( terrain.intersects(gridTest) ){
    					curObject.objects.add("Terrain");    					
    				}	
    			}   			
    		System.out.println(x);
    		System.out.println(y);
    		System.out.println( curObject.objects );
    		Storage.add(curObject);
    	  }
      }
	  
	  
	}
	
	
	//Chance of winning by shooting here
	public double ProbGrid(Point target){

		// capture Image
		BufferedImage screenshot = ActionRobot.doScreenShot();
		// process image
		Vision vision = new Vision(screenshot);			
		List<Rectangle> pigs = vision.findPigs();
		List<Rectangle> Obstacles_wood = vision.findWood();
		List<Rectangle> Obstacles_stone = vision.findStones();
        List<Rectangle> Obstacles_ice   = vision.findIce();			
		
	  int GridSize = 20; //Grid size for the 7x7 grid
	  double StartX = target.getX() - 1*GridSize;
	  double StartY = target.getY() - 3*GridSize;

	  double Prob = 0.0;
	  //ArrayList<MLojbects> Storage = new  ArrayList<MLojbects>();
      for(int y=0; y<7; y++){
    	  for(int x=0; x<7; x++){  
    		  MLojbects curObject = new MLojbects(x,y);
    		  Rectangle gridTest = new Rectangle((int)(StartX + x*GridSize),(int)(StartY + y*GridSize),GridSize,GridSize);
    			for( Rectangle rec : pigs ){
    				if( rec.intersects(gridTest) ){
    					curObject.objects.add("Pig");    					
    				}
    			}
    			for( Rectangle stone : Obstacles_stone ){
    				if( stone.intersects(gridTest) ){
    					curObject.objects.add("Stone");    					
    				}		
    			}
    			for( Rectangle ice : Obstacles_ice ){
    				if( ice.intersects(gridTest) ){
    					curObject.objects.add("Ice");    					
    				}	
    			}
    			for( Rectangle wood : Obstacles_wood ){
    				if( wood.intersects(gridTest) ){
    					curObject.objects.add("Wood");    					
    				}	
    			}  
    			for( Rectangle terrain : TerrainSquares ){
    				if( terrain.intersects(gridTest) ){
    					curObject.objects.add("Terrain");    					
    				}	
    			}   
    		//System.out.println(x);
    		//System.out.println(y);
    		//System.out.println( curObject.objects );
    		//Storage.add(curObject);
    		int TotalWin = 0; //Count amount of times in history where grid shot win
    		int TotalLost = 0; //Count amount of times in history where grid shot lost    		
    		for( MLojbects won : WinStorage){
    			if( won.x != x || won.y != y ){
    				continue;
    			}
    			if( won.objects.containsAll(curObject.objects) ){
    				TotalWin++;
    				//System.out.println("WinGrid");
    			}
    		}
    		for( MLojbects lost : LostStorage){
    			if( lost.x != x || lost.y != y ){
    				continue;
    			}
    			if( lost.objects.containsAll(curObject.objects) ){
    				TotalLost++;
    				//System.out.println("LostGrid");
    			}
    		}
    		if(  TotalWin + TotalLost != 0){
    			Prob += ( (double)TotalWin/(double)(TotalWin + TotalLost) )/(7.0*7.0);//Add chance of winning
    		}
    	  }
      }
	  
	  return Prob;
	}	
	
	//Chance of winning by shooting here
	public Point FindMaxChance(){	
		Point Result = null;
		
		// capture Image
		BufferedImage screenshot = ActionRobot.doScreenShot();
		// process image
		Vision vision = new Vision(screenshot);		
		
		List<Rectangle> pigs = vision.findPigs();
		List<Rectangle> Obstacles_wood = vision.findWood();
		List<Rectangle> Obstacles_stone = vision.findStones();
        List<Rectangle> Obstacles_ice   = vision.findIce();
        
        double MaxChance = 0.0;
		for( Rectangle rec : pigs ){
			Point _tpt = new Point((int) rec.getCenterX(),
					(int) rec.getCenterY());
			if(  ProbGrid(_tpt) > MaxChance ){
				Result  =  _tpt;
				MaxChance = ProbGrid(_tpt);
			}
		}
		for( Rectangle rec : Obstacles_stone ){
			Point _tpt = new Point((int) rec.getCenterX(),
					(int) rec.getCenterY());
			if(  ProbGrid(_tpt) > MaxChance ){
				Result  =  _tpt;
				MaxChance = ProbGrid(_tpt);
			}	
		}
		for( Rectangle rec : Obstacles_ice ){
			Point _tpt = new Point((int) rec.getCenterX(),
					(int) rec.getCenterY());
			if(  ProbGrid(_tpt) > MaxChance ){
				Result  =  _tpt;
				MaxChance = ProbGrid(_tpt);
			}
		}
		for( Rectangle rec : Obstacles_wood ){
			Point _tpt = new Point((int) rec.getCenterX(),
					(int) rec.getCenterY());
			if(  ProbGrid(_tpt) > MaxChance ){
				Result  =  _tpt;
				MaxChance = ProbGrid(_tpt);
			}
		}  
		for( Rectangle rec : TerrainSquares ){
			Point _tpt = new Point((int) rec.getCenterX(),
					(int) rec.getCenterY());
			if(  ProbGrid(_tpt) > MaxChance ){
				Result  =  _tpt;
				MaxChance = ProbGrid(_tpt);
			}
		}   
        if( Result == null ){
			// random pick up a pig
			Random r = new Random();

			//int index = r.nextInt(Obstacles_wood.size());
			//Rectangle pig = Obstacles_wood.get(index);
			int index = r.nextInt(pigs.size());
			Rectangle pig = pigs.get(index);
			Result = new Point((int) pig.getCenterX(),
					(int) pig.getCenterY());          	
        }
		return Result;
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
		List<Rectangle> Obstacles_wood = vision.findWood();
		List<Rectangle> Obstacles_stone = vision.findStones();
        List<Rectangle> Obstacles_ice   = vision.findIce();					
		int bird_count = 0;
		bird_count = red_birds.size() + blue_birds.size() + yellow_birds.size();

		System.out.println("...found " + pigs.size() + " pigs and "
				+ bird_count + " birds");
		GameState state = ar.checkState();
         /*
    		try
     		 {
                   FileWriter fw = new FileWriter("LevelStructure.txt");
                     //System.out.println(aString);
                     //fw.write(aString);                  
                   fw.close(); 
   		   }
    		catch (IOException ioe)
    		  {
    		    System.out.println(ioe);        
    		  }
    		*/
		// if there is a sling, then play, otherwise just skip.
		if (sling != null) {
			ar.fullyZoom();
			ObjectRelations();
			//MaxChanceShot = 
			if (!pigs.isEmpty()) {

				// Initialise a shot list
				ArrayList<Shot> shots = new ArrayList<Shot>();
				Point releasePoint;
				{
					// random pick up a pig
					Random r = new Random();

					//int index = r.nextInt(Obstacles_wood.size());
					//Rectangle pig = Obstacles_wood.get(index);
					int index = r.nextInt(pigs.size());
					Rectangle pig = pigs.get(index);
					Point _tpt = new Point((int) pig.getCenterX(),
							(int) pig.getCenterY());

					//Extra: use the point with maximum probability instead
					_tpt = FindMaxChance();
					
					//Store probability calculations
					RectInGrid(_tpt);
					System.out.println( ProbGrid(_tpt) );//probability of winning when shooting here
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
					System.out.println("the release point is: " + releasePoint);
					/*
					 * =========== Get the release point from the trajectory
					 * prediction module====
					 */
					System.out.println("Shoot!!");
					if (releasePoint != null) {
						double releaseAngle = tp.getReleaseAngle(sling,
								releasePoint);
						System.out.println(" The release angle is : "
								+ Math.toDegrees(releaseAngle));
						int base = 0;
						if (releaseAngle > Math.PI / 4)
							base = 1400;
						else
							base = 550;
						int tap_time = (int) (base + Math.random() * 1500);
						
						
						
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
