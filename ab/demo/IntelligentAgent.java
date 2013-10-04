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

public class IntelligentAgent extends Agent {

    private int step = 2;

    private int focus_x;
    private int focus_y;

    private ActionRobot ar;
    public int currentLevel = 1;
    TrajectoryPlanner tp;

    private boolean firstShot;
    private Point prevTarget;

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
                    // find shot which hits the most objects
                    // TODO also aim at pigs?
                    List<Rectangle> ice = vision.getHitableIce();
                    List<Rectangle> wood = vision.getHitableWood();
                    List<Rectangle> stone = vision.getHitableStone();
                    ArrayList<Rectangle> allHittable = new ArrayList<Rectangle>();
                    allHittable.addAll(vision.getHitableIce());
                    allHittable.addAll(vision.getHitableWood());
                    allHittable.addAll(vision.getHitableStone());
                    ArrayList<Rectangle> allObjects = new ArrayList<Rectangle>();
                    allObjects.addAll(vision.findIce());
                    allObjects.addAll(vision.findWood());
                    allObjects.addAll(vision.findStones());
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
                    boolean yellow = false;
                    if (vision.findActiveBird() == "yellow") {
                        // shoot shorter if the yellow bird is active
                        _tpt.x *= 0.95;                 // TODO: 0.95 good? Better: dependent on the release angle
                        yellow = true;
                    }

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

                    releasePoint = launchData.getReleasePoint();
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
                        // calculate tap time for the yellow bird
                        if (yellow) {
                            System.out.println("yellow");
                            for (int x = focus_x; x < _tpt.x; x += xStep) {
                                int y = tp.getYCoordinate(sling, releasePoint, x);
                                System.out.println("y: "+y);
                                // derivation of the trajectory function
                                double gradient = tp.getGradient(sling, releasePoint, x);
                                System.out.println("Gradient: "+gradient);
                                // calculate resulting y is you tap now
                                int dist = _tpt.x - x;
                                int decline = (dist * dist) / 10000;  // TODO 10000 is arbitrary; find better value?
                                double calcY = y + dist * gradient + decline;
                                // set tap time if calculated y is lower than the target's y
                                if (calcY > _tpt.y) {
                                    tap_time = (tap_time + tp.getTapTime(sling, releasePoint, new Point(x, y))) / 2;
                                    break;
                                } else {
                                    tap_time = tp.getTapTime(sling, releasePoint, new Point(x, y));
                                }
                            }
                        } else {
                            int base = 0;
                            if (releaseAngle > Math.PI / 4)
                                base = 1400;
                            else
                                base = 550;
                            tap_time = (int) (base + Math.random() * 1500);
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

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "-ia";
    }

    @Override
    public void setIP(String ip) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setID(int id) {
        // TODO Auto-generated method stub

    }
}
