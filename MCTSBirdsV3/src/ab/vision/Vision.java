/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014,XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe, Jim Keys,   Andrew Wang, Peng Zhang
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************/
package ab.vision;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Vision {
	private BufferedImage image;
	private VisionMBR visionMBR = null;
	private VisionRealShape visionRealShape = null;
	
	public Vision(BufferedImage image)
	{
		this.image = image;
	}
	
	public List<ABObject> findBirdsMBR()
	{
		if (visionMBR == null)
		{
			visionMBR = new VisionMBR(image);
		} 
		return visionMBR.findBirds();
			
	}
    public List<ABObject> getAllObjects() {
        List <ABObject> complete = new ArrayList<ABObject>();
        complete.addAll(findBlocksMBR());
        complete.addAll(findBlocksRealShape());
        return complete;
    }

	/**
	 * @return a list of MBRs of the blocks in the screenshot. Blocks: Stone, Wood, Ice, and TNT 
	 * */
	public List<ABObject> findBlocksMBR()
	{
		if (visionMBR == null)
		{
			visionMBR = new VisionMBR(image);
		}
		return visionMBR.findBlocks();
	}
	public List<ABObject> findPigsMBR()
	{
		if (visionMBR == null)
		{
			visionMBR = new VisionMBR(image);
		}
		return visionMBR.findPigs();
	}
	public List<ABObject> findPigsRealshape()
	{
		if(visionRealShape == null)
		{
			visionRealShape = new VisionRealShape(image);
		}
		
		return visionRealShape.findPigs();
	} 
	public List<ABObject> findBirdsRealshape()
	{
		if(visionRealShape == null)
		{
			visionRealShape = new VisionRealShape(image);
		}
		
		return visionRealShape.findBirds();
	} 
	
	public Rectangle findSlingshotMBR()
	{
		if (visionMBR == null)
		{
			visionMBR = new VisionMBR(image);
		}
		return visionMBR.findSlingshotMBR();
	}
	public List<Point> findTrajPoints()
	{
		if (visionMBR == null)
		{
			visionMBR = new VisionMBR(image);
		}
		return visionMBR.findTrajPoints();
	}
	/**
	 * @return a list of real shapes (represented by Body.java) of the blocks in the screenshot. Blocks: Stone, Wood, Ice, and TNT 
	 * */
	public List<ABObject> findBlocksRealShape()
	{
		if(visionRealShape == null)
		{
			visionRealShape = new VisionRealShape(image);
		}
		List<ABObject> allBlocks = visionRealShape.findObjects();
		allBlocks.removeAll(findPigsMBR());
		return allBlocks;
	}
	public VisionMBR getMBRVision()
	{
		if(visionMBR == null)
			visionMBR = new VisionMBR(image);
		return visionMBR;
	}


    public List<ABObject> findBlueBirds() {
        List<ABObject> blueBirds = new ArrayList<ABObject>();
        for (ABObject bird : findBirdsMBR()) {
            if (bird.getType() == ABType.BlueBird) {
                blueBirds.add(bird);
            }
        }
        return blueBirds;
    }

    public List<ABObject> findYellowBirds() {
        List<ABObject> yellowBirds = new ArrayList<ABObject>();
        for (ABObject bird : findBirdsMBR()) {
            if (bird.getType() == ABType.YellowBird) {
                yellowBirds.add(bird);
            }
        }
        return yellowBirds;
    }

    public List<ABObject> findWhiteBirds() {
        List<ABObject> whiteBirds = new ArrayList<ABObject>();
        for (ABObject bird : findBirdsMBR()) {
            if (bird.getType() == ABType.WhiteBird) {
                whiteBirds.add(bird);
            }
        }
        return whiteBirds;
    }

    public List<ABObject> findBlackBirds() {
        List<ABObject> blackBirds = new ArrayList<ABObject>();
        for (ABObject bird : findBirdsMBR()) {
            if (bird.getType() == ABType.BlackBird) {
                blackBirds.add(bird);
            }
        }
        return blackBirds;
    }
    
    public List<ABObject> findRedBirds() {
        List<ABObject> redBirds = new ArrayList<ABObject>();
        for (ABObject bird : findBirdsMBR()) {
            if (bird.getType() == ABType.RedBird) {
                redBirds.add(bird);
            }
        }
        return redBirds;
    }

    public List<ABObject> findWood() {
        List<ABObject> wood = new ArrayList<ABObject>();
        for (ABObject block : findBlocksMBR()) {
            if (block.getType() == ABType.Wood) {
                wood.add(block);
            }
        }
        return wood;
    }

    public List<ABObject> findIce() {
        List<ABObject> ice = new ArrayList<ABObject>();
        for (ABObject block : findBlocksMBR()) {
            if (block.getType() == ABType.Ice) {
                ice.add(block);
            }
        }
        return ice;
    }

    public List<ABObject> findStone() {
        List<ABObject> stone = new ArrayList<ABObject>();
        for (ABObject block : findBlocksMBR()) {
            if (block.getType() == ABType.Stone) {
                stone.add(block);
            }
        }
        return stone;
    }
}
