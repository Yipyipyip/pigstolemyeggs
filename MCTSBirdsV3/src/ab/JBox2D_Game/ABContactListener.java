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
 * Determines that happens, then objects touch each other.
 */
public class ABContactListener implements ContactListener {

    private final long startTime;
    private boolean birdShot = false;
    private ArrayList<Body> bodiesToDelete;
    private ArrayList<Body> bodiesToExplode;
    private ArrayList<Body> bodiesToSpeedUp;
    private ArrayList<Float> speedUp;
    private float discount = 10.f;
    private int points=0;
    private ArrayList<Contact> bodiesInContact = new ArrayList<Contact>();
/**
 * Initializes the contact listener. bodiesToDelete and bodiesToExplodes are pointer,
 * which should be used in the UpdateThread class.
 * @param bodiesToDelete
 * @param bodiesToExplode
 */
    public ABContactListener(ArrayList<Body> bodiesToDelete,ArrayList<Body>bodiesToExplode,ArrayList<Body>bodiesToSpeedUp, ArrayList<Float>speedUp) {
        this.bodiesToDelete = bodiesToDelete;
        this.bodiesToExplode=bodiesToExplode;
        this.bodiesToSpeedUp=bodiesToSpeedUp;
        this.speedUp=speedUp;
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
    
    /**
     * Determines that happens during a contact between two objects. 
     * It calculates the damage for each involved objects.
     * If a black birds get hit, it explodes after a certain time elapse.
     * If a TNT Box or an White Egg breaks, it also explodes.
     * If a certain damage for one of the objects is reached, it gets destroyed.
     * 
     */
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
                	bodiesToExplode.add(b.getBody());
                }
                
                for (float impulse : contactImpulse.normalImpulses) {
                	float impulseA=changeImpulse(impulse, metaA, metaB ,b.getBody());
                	float impulseB=changeImpulse(impulse, metaB, metaA, a.getBody());

                    if (impulseA >= metaA.getDefense() && !metaA.getMaterial().startsWith("BIRD_") && a.getBody().getType() != BodyType.STATIC) {
                       metaA.setStrength(metaA.getStrength() + (metaA.getDefense() - impulseA) / discount);
                        if (metaA.getStrength() <= 0) {
                            
                            points+=metaA.getScore();
                            bodiesToDelete.add(a.getBody());
                            if(metaA.getMaterial().contains("TNT"))
                            {
                            	bodiesToExplode.add(a.getBody());
                            }
                        }
                    }
                    if (impulseB >= metaB.getDefense() && !metaB.getMaterial().startsWith("BIRD_") && b.getBody().getType() != BodyType.STATIC) {
                        metaB.setStrength(metaB.getStrength() + (metaB.getDefense() - impulseB) / discount);
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
    /**
     * Changes the given impulse by the given multipliers in the MetaData class.
     * @param impulse
     * @param metaA
     * @param metaB
     * @return
     */
    private float changeImpulse(float impulse,Metadata metaA,Metadata metaB, Body b)
    {
    	float impulseMultiplier=0.f;
    	float damgeMultiplier=0.f;
    	if(metaA.getMaterial().contains("PIG"))
    	{
    		if(metaA.getMaterial().contains("SMALL"))
    		{
    			impulseMultiplier=metaB.getdvelocityMultiplierPigSmall();
    		}else if(metaA.getMaterial().contains("BIG"))
    		{
    			impulseMultiplier=metaB.getdvelocityMultiplierPigBig();
    		}
    		else if(metaA.getMaterial().contains("MEDIUM"))
    		{
    			impulseMultiplier=metaB.getvelocityMultiplierPigMeduim();
    		}
    		else if(metaA.getMaterial().contains("MUSTACHE"))
    		{
    			impulseMultiplier=metaB.getvelocityMultiplierPigMustache();
    		}
    		else if(metaA.getMaterial().contains("KING"))
    		{
    			impulseMultiplier=metaB.getvelocityMultiplierPigKing();
    		}
    		else if(metaB.getMaterial().contains("HELMET"))
    		{
    			impulseMultiplier=metaB.getvelocityMultiplierPigHelmet();
    		}
    	}else if(metaA.getMaterial().contains("ICE"))
    	{
    		impulseMultiplier=metaB.getvelocityMultiplierIce();
    		damgeMultiplier=metaB.getdamageMultiplierIce();
    	}
    	else if(metaA.getMaterial().contains("STONE"))
    	{
    		impulseMultiplier=metaB.getvelocityMultiplierStone();
    		damgeMultiplier=metaB.getdamageMultiplierStone();
    	}
    	else if(metaA.getMaterial().contains("SNOW_STATIC"))
    	{
    		impulseMultiplier=metaB.getvelocityMultiplierSnowStatic();
    		damgeMultiplier=metaB.getdamageMultiplierSnowStatic();
    	}
    	else if(metaA.getMaterial().contains("WOOD"))
    	{
    		impulseMultiplier=metaB.getvelocityMultiplierWood();
    		damgeMultiplier=metaB.getdamageMultiplierWood();
    	}
    	else if(metaA.getMaterial().contains("SNOW"))
    	{
    		impulseMultiplier=metaB.getvelocityMultiplierSnow();
    		damgeMultiplier=metaB.getdamageMultiplierSnow();
    	}
    	if(impulseMultiplier!=1)
    	{
    		speedUp.add(impulse*impulseMultiplier);
    		bodiesToSpeedUp.add(b);
    	}
    	return impulse*damgeMultiplier;
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
