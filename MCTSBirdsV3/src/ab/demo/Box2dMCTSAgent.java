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

import ab.JBox2D_Game.Game;
import ab.JBox2D_Game.Metadata;
import ab.planner.TrajectoryPlanner;
import ab.vision.GameStateExtractor.GameState;
import org.jbox2d.dynamics.Body;

import java.awt.*;
import java.io.*;
import java.util.*;

public class Box2dMCTSAgent implements Runnable {

    private Random randomGenerator;
    public int currentLevel = 1;
    public static int time_limit = 12;
    private Map<Integer, Integer> scores = new LinkedHashMap<Integer, Integer>();
    private Game game;

    public class LaunchData {
        private int score;
        private Point releasePoint;
        private int angle;

        public LaunchData(int score, Point releasePoint, int theta) {
            this.score = score;
            this.releasePoint = releasePoint;
        }

        public int getAngle() {
            return angle;
        }

        public void setAngle(int angle) {
            this.angle = angle;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public void setReleasePoint(Point releasePoint) {
            this.releasePoint = releasePoint;
        }

        public int getScore() {
            return score;
        }

        public Point getReleasePoint() {
            return releasePoint;
        }
    }

    // a enhanced implementation of the Naive Agent
    public Box2dMCTSAgent() {

        randomGenerator = new Random();
        // --- go to the Poached Eggs episode level selection page ---
        //ActionRobot.GoFromMainMenuToLevelSelection();

    }

    //Begin  extra code for MCTS


    class ShotData {
        double Theta;
        double realAngle;
        int releaseX;
        int releaseY;
        int tapTime;

        public ShotData(double Theta, double realAngle, int releaseX, int releaseY, int tapTime) {
            this.Theta = Theta;
            this.realAngle = realAngle;
            this.releaseX = releaseX;
            this.releaseY = releaseY;
            this.tapTime = tapTime;
        }
    }

    static ArrayList<ShotData> Storage = new ArrayList<ShotData>();
    static int BestScore = 0;
    static HashMap<String, Integer> nodeCount = new HashMap<String, Integer>();
    static HashMap<String, Double> scoreCount = new HashMap<String, Double>();
    static int shotFired = 0;

    //Playing levels 22+ requires you to manually scroll to the corresponding page before running this agent
    static String levelD = "1";//Current page of poached eggs. Has to be set manually.

    public void WriteShot(int score) {
        try {
            // Create file
            FileWriter fstream = new FileWriter("./MCTSresults/" + levelD + "-" + Integer.toString(currentLevel) + ".txt", true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("{\n");
            for (ShotData shot : Storage) {
                out.write("Theta:" + Double.toString(shot.Theta) + "\n");
                out.write("Angle:" + Double.toString(shot.realAngle) + "\n");
                out.write("releaseX:" + Integer.toString(shot.releaseX) + "\n");
                out.write("releaseY:" + Integer.toString(shot.releaseY) + "\n");
                out.write("tapTime:" + Integer.toString(shot.tapTime) + "\n");
            }
            out.write("score:" + Integer.toString(score) + "\n");
            out.write("}\n");
            //Close the output stream
            out.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void ReadShot() {
        //System.out.println(  System.getProperty("user.dir") ); //gets your current working directory which should contain the "MCTSresults" folder
        String fileLocation = "./MCTSresults/" + levelD + "-" + Integer.toString(currentLevel) + ".txt";
        //System.out.println(fileLocation);
        DataInputStream i = null;
        try {
            i = new DataInputStream(new FileInputStream(fileLocation));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        BufferedReader b = new BufferedReader(new InputStreamReader(i));
        String str;
        try {
            while ((str = b.readLine()) != null) {
                if (str.contains("score")) {
                    int getScore = Integer.parseInt(str.substring(str.indexOf(':') + 1));
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
    // [1] : angle: divide by 100 for angle in theta
    // [2] : tap time, divide by 100 to get tap time in ms
    int[][][] BackProp = new int[8][140][32];
    int[][][] Visited = new int[8][140][32];//factor that ensures exploration,
    static ArrayList<Body> Birds;

    static int cAngle; //choice of angle
    static int cTap;  //choice of tap time

    public void Selection(int curBird) {

        Random rAngle = new Random();
        cAngle = rAngle.nextInt(139); //if all values are below threshold, pick a random shot
        cTap = rAngle.nextInt(31); //if all values are below threshold, pick a random shot
        if (Birds.size() > curBird && ((Metadata) Birds.get(curBird).getUserData()).getMaterial().equals("BIRD_RED")) {
            cTap = 0;
        }
        if (Simulations < 0) { //Optional: Do random shots for the first x plays to gather data for the MCTS
            return;
        }


        //System.out.println(BackProp[0][11][0]);
        System.out.println("Current bird:");
        System.out.println(curBird);

        int maxShot = -1;//If all shots seem bad, choose the random shot selected above until it finds a good solution.
        for (int i = 0; i < 140; i++) {
            for (int j = 0; j < 32; j++) {
                if (Birds.size() > curBird && ((Metadata) Birds.get(curBird).getUserData()).getMaterial().equals("BIRD_RED") && j > 0) {
                    continue; //Red birds don't need tap time
                }
                if ((int) ((float) BackProp[curBird][i][j] / Visited[curBird][i][j]) > maxShot) {
                    maxShot = (int) ((float) BackProp[curBird][i][j] / Visited[curBird][i][j]);
                    cAngle = i;
                    cTap = j;
                }
            }
        }

    }

    //End extra code for MCTS


    // run the client
    public void run() {


        Birds = new ArrayList<Body>();
        //Initialize back propagation values
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 140; j++) {
                for (int k = 0; k < 32; k++) {
                    BackProp[i][j][k] = 0;
                    Visited[i][j][k] = 0;
                }
            }
        }


        shotFired = 0;
        currentLevel = 19;//Agent will only play this level
        ReadShot();
        Storage.clear();
        // create a box2d representation of the game
        game = new Game(1, currentLevel);
        // Main loop
        while (true) {
            GameState state = solve();
            // check game state
            if (state == GameState.WON) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int score = game.getEndScore();

                //If the score is better than the best one currently recorded, store the new shot data
                if (score > BestScore) {
                    BestScore = score;
                    WriteShot(score);
                }

                //Back propagation
                int curLoc = 0;
                for (ShotData shot : Storage) {
                    System.out.println("Updating Win Back progagation");
                    System.out.println(shot.Theta);
                    int angle = (int) (shot.Theta * 100);
                    int tap = (shot.tapTime / 100);
                    Visited[curLoc][angle][tap] += 100; //Make sure that it will take a while before this node is revisited
                    System.out.println(angle);
                    for (int a = 0; a < 140; a++) {
                        for (int t = 0; t < 32; t++) {
                            if (angle == a && tap == t) { //prevent division by 0
                                BackProp[curLoc][a][t] += (int) ((score / 5000.0));
                            } else {
                                BackProp[curLoc][a][t] += (int) ((1.0 / (Math.abs(angle - a) + Math.abs(tap - t))) * (score / 5000.0));
                            }
                        }
                    }
                    curLoc++;
                }

                Storage.clear();
                shotFired = 0;
                Simulations++;
                if (!scores.containsKey(currentLevel))
                    scores.put(currentLevel, score);
                else {
                    if (scores.get(currentLevel) < score)
                        scores.put(currentLevel, score);
                }
                int totalScore = 0;
                for (Integer key : scores.keySet()) {

                    totalScore += scores.get(key);
                    System.out.println(" Level " + key
                            + " Score: " + scores.get(key) + " ");
                }
                System.out.println("Total Score: " + totalScore);
                // create a new game
                game = new Game(1, currentLevel);
            } else if (state == GameState.LOST) {
                System.out.println("Restart");
                //Back propagation
                int curLoc = 0;
                for (ShotData shot : Storage) {
                    System.out.println("Updating Back progagation");
                    System.out.println(shot.Theta);
                    int angle = (int) (shot.Theta * 100);
                    int tap = (shot.tapTime / 100);
                    Visited[curLoc][angle][tap]++;
                    System.out.println(angle);
                    //Discourage nearby shots because the last shot here was bad
                    int NegScore = 10;
                    for (int a = -10; a < 11; a++) {
                        for (int t = -5; t < 6; t++) {
                            if (angle + a >= 0 && angle + a < 140 && tap + t >= 0 && tap + t < 32) { //check if out of bounds
                                if (0 == a && 0 == t) { //check if out of bounds
                                    BackProp[curLoc][angle + a][tap + t] -= (int) (NegScore);
                                } else {
                                    BackProp[curLoc][angle + a][tap + t] -= (int) ((1.0 / (Math.abs(a) + Math.abs(t))) * (NegScore));
                                }
                                //BackProp[curLoc][ angle + a ][ tap + t ]--;
                            }
                        }
                    }
                    //BackProp[curLoc][ (int)shot.Theta*100 ][ (int) shot.tapTime/100 ]--;
                    curLoc++;
                }
                Storage.clear();
                shotFired = 0;
                Simulations++;
                // create new game
                game = new Game(1, currentLevel);
            }

        }

    }

    public GameState solve() {

        GameState state = GameState.PLAYING;

        // if there is a sling, then play, otherwise just skip.


        //static int cAngle; //choice of angle
        //static int cTap;  //choice of tap time

        Selection(shotFired);
        double rAd = cAngle / 100.0;
        System.out.println(rAd);

        //int tapTime = tp.getTapTime(sling, releasePoint, _tpt, tapInterval);
        int tapTime = cTap * 100;
        System.out.println(tapTime);
        Storage.add(new ShotData(rAd, Math.toDegrees(rAd), 0, 0, tapTime));
        shotFired++;
        game.shoot(Math.toDegrees(rAd), game.getActiveBird(), tapTime / 1000.f);
        if (game.isWon()) {
            state = GameState.WON;
        } else if (game.isLost()) {
            state = GameState.LOST;
        }
        return state;
    }

    public static void main(String args[]) {

        Box2dMCTSAgent na = new Box2dMCTSAgent();
        if (args.length > 0)
            na.currentLevel = Integer.parseInt(args[0]);
        na.run();

    }
}
