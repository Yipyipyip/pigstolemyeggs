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

import ab.demo.other.ActionRobot;
import ab.demo.other.Env;
import ab.demo.other.Shot;
import ab.demo.util.StateUtil;
import ab.planner.TrajectoryPlanner;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IntelligentAgent implements Runnable {

    private int focus_x;
    private int focus_y;

    private ActionRobot ar;
    public int currentLevel = 1;
    TrajectoryPlanner tp;

    private boolean firstShot;
    private Point prevTarget;

    // Michael's Agent
    public IntelligentAgent() {
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
                        e.printStackTrace();
                    }
                    score = StateUtil.checkCurrentScore(ar.proxy);
                }
                System.out.println("###### The game score is " + score
                        + "########");
                ar.loadLevel(++currentLevel);
                // make a new trajectory planner whenever a new level is entered
                tp = new TrajectoryPlanner();

                // first shot on this level, try high shot first
                firstShot = true;
            } else if (state == GameState.LOST) {
                System.out.println("restart");
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
                    _tpt.x *= 0.95;                 // TODO: only for yellow birds!! 0.9 good?

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

                        int tap_time = 0;
                        int xStep = 1;
                        for (int x = focus_x; x < _tpt.x; x += xStep) {
                            int y = tp.getYCoordinate(sling, releasePoint, x);
                            double v = tp.getVelocity(releaseAngle);
                            // derivation of the trajectory function
                            int scale = sling.height + sling.width;
                            double gradient = 2*x/(scale*v*v)-1;//1 - (2 * x) / (v * v);
                            // calculate resulting y is you tap now
                            int dist = _tpt.x-x;
                            int decline = (dist*dist)/1000;
                            double calcY = y + dist * gradient + decline;
                            // set tap time if calculated y is lower than the target's y
                            if (calcY > _tpt.y) {
                                tap_time = (tap_time + tp.getTapTime(sling, releasePoint, new Point(x, y))) / 2;
                                break;
                            } else {
                                tap_time = tp.getTapTime(sling, releasePoint, new Point(x, y));
                            }
                        }
                        shots.add(new Shot(focus_x, focus_y, (int) releasePoint
                                .getX() - focus_x, (int) releasePoint.getY()
                                - focus_y, 0, tap_time));
                    } else {
                        System.err.println("Out of Knowledge");
                    }
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

        IntelligentAgent ma = new IntelligentAgent();
        if (args.length > 0)
            ma.currentLevel = Integer.parseInt(args[0]);
        ma.run();

    }
}
