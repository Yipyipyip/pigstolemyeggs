package ab.JBox2D_Game;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.contacts.Contact;

import java.util.ArrayList;

/**
 * Class ABContactListener
 * Author: Michael Tannenbaum
 * Date: 13.01.14
 * Time: 16:31
 */
public class ABContactListener implements ContactListener {

    private final long startTime;
    private boolean birdShot = false;
    private ArrayList<Body> bodiesToDelete;
    private ArrayList<Body> bodiesToExplode;
    private float discount = 10.f;
    private int points=0;
    private ArrayList<Contact> bodiesInContact = new ArrayList<Contact>();

    public ABContactListener(ArrayList<Body> bodiesToDelete,ArrayList<Body>bodiesToExplode) {
        this.bodiesToDelete = bodiesToDelete;
        this.bodiesToExplode=bodiesToExplode;
        startTime = System.currentTimeMillis();
        System.out.println();
    }

    @Override
    public void beginContact(Contact contact) {
    }

    @Override
    public void endContact(Contact contact) {
        for (int i = 0; i < bodiesInContact.size(); ++i) {
            Contact bodies = bodiesInContact.get(i);
            if ((contact.getFixtureA() == bodies.getFixtureA() && contact.getFixtureB() == bodies.getFixtureB()) || (contact.getFixtureB() == bodies.getFixtureA() && contact.getFixtureA() == bodies.getFixtureB())) {
                bodiesInContact.remove(bodies);
            }
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {
        for (int i = 0; i < bodiesInContact.size(); ++i) {
            Contact bodies = bodiesInContact.get(i);
            if ((contact.getFixtureA() == bodies.getFixtureA() && contact.getFixtureB() == bodies.getFixtureB()) || (contact.getFixtureB() == bodies.getFixtureA() && contact.getFixtureA() == bodies.getFixtureB())) {
                return;
            }
        }
        if (birdShot && System.currentTimeMillis() - startTime > 3000) {
            Fixture a = contact.getFixtureA();
            Fixture b = contact.getFixtureB();
            bodiesInContact.add(contact);
            if (a != null && a.getBody() != null && a.getBody().getUserData() != null) {
                Metadata metaA = (Metadata) a.getBody().getUserData();
                Metadata metaB = (Metadata) b.getBody().getUserData();
                if(metaA.getMaterial().contains("BLACK"))
                {
                	try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                	bodiesToExplode.add(a.getBody());
                }else if(metaA.getName().contains("EGG"))
                {
                	bodiesToExplode.add(a.getBody());
                	
                }
                if(metaB.getMaterial().contains("BLACK"))
                {
                	try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                	bodiesToExplode.add(b.getBody());
                }else if(metaB.getName().contains("EGG"))
                {
                  	System.out.println("War hier!");
                	bodiesToExplode.add(b.getBody());
                }
                
                for (float impulse : contactImpulse.normalImpulses) {
                    if (impulse >= metaA.getDefense() && !metaA.getMaterial().startsWith("BIRD_") && a.getBody().getType() != BodyType.STATIC) {
                       metaA.setStrength(metaA.getStrength() + (metaA.getDefense() - impulse) / discount);
                        if (metaA.getStrength() <= 0) {
                            
                            points+=metaA.getScore();
                            bodiesToDelete.add(a.getBody());
                            if(metaA.getMaterial().contains("TNT"))
                            {
                            	bodiesToExplode.add(a.getBody());
                            }
                        }
                    }
                    if (impulse >= metaB.getDefense() && !metaB.getMaterial().startsWith("BIRD_") && b.getBody().getType() != BodyType.STATIC) {
                        metaB.setStrength(metaB.getStrength() + (metaB.getDefense() - impulse) / discount);
                        if (metaB.getStrength() <= 0) {
                           
                            points+=metaB.getScore();
                            bodiesToDelete.add(b.getBody());
                            if(metaB.getMaterial().contains("TNT"))
                            {
                            	bodiesToExplode.add(b.getBody());
                            }
                        }
                    }
                }
//                for (float impulse : contactImpulse.tangentImpulses) {
//                    if (impulse > metaA.getDefense() && metaA.getMaterial().equals("PIG_BASIC_SMALL"))
//                        System.out.println(metaA.getName() + " (A/Tangent): " + impulse);
//                    if (impulse > metaB.getDefense() && metaB.getMaterial().equals("PIG_BASIC_SMALL"))
//                        System.out.println(metaB.getName() + " (B/Tangent): " + impulse);
//                }
            }
        }
    }

    public boolean isBirdShot() {
        return birdShot;
    }

    public void setBirdShot(boolean birdShot) {
        this.birdShot = birdShot;
    }
    public int getPoints()
    {
    	return points;
    }
}
