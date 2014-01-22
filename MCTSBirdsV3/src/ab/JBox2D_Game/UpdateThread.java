package ab.JBox2D_Game;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;

import java.util.ArrayList;

/**
 * Class UpdateThread
 * Author: Michael Tannenbaum
 * Date: 15.01.14
 * Time: 21:49
 */
public class UpdateThread extends Thread {

    private Game world;
    private ArrayList<Body> bodiesToDelete;
    private ArrayList<Body> bodiesToExplode;

    public UpdateThread(Game world, ArrayList<Body> bodiesToDelete,ArrayList<Body>bodiesToExplode) {
        this.world = world;
        this.bodiesToDelete = bodiesToDelete;
        this.bodiesToExplode = bodiesToExplode;
        System.out.println(this.bodiesToExplode.hashCode());
    }

    @Override
    public void run() {
//        try {
//            Thread.sleep(3500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        while (true) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < bodiesToExplode.size(); ++i) {
            	world.explode(bodiesToExplode.get(i));
            	System.out.println("War hier!");
            }
            
            for (int i = 0; i < bodiesToDelete.size(); ++i) {
                Body body = bodiesToDelete.get(i);
                System.out.println("Destroy body " + ((Metadata) body.getUserData()).getName());
               
                world.getWorld().destroyBody(body);
            }
            bodiesToExplode.clear();
            bodiesToDelete.clear();
        }
    }
}
