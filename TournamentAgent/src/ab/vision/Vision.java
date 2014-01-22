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
	public VisionMBR visionMBR = null;
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
	
	

    public List<Rectangle> getHitableIce()
    {
            return getHitableObjects(visionMBR.findIce());
    }
    public List<Rectangle> getHitableWood()
    {
            return getHitableObjects(visionMBR.findWood());
    }
    public List<Rectangle> getHitableStone()
    {
            return getHitableObjects(visionMBR.findStones());
    }
    
    private List<Rectangle> getHitable(List<Rectangle>objects1,List<Rectangle> objects2)
    {
            List<Rectangle> hitable=new ArrayList<Rectangle>();
            for(Rectangle o1:objects1)
            {
                    boolean addLeft=true;
                    boolean addHigh=true;
                    for(Rectangle o2:objects2)
                    {
                            if(o2!=o1)
                            {
                            if(!isObjectMoreLeft(o1, o2))
                            {
                                    addLeft=false;
                            }
                            if(!isObjectHigher(o1, o2))
                            {
                                    addHigh=false;
                            }
                            }
                    }
                    if(addLeft==true||addHigh==true)
                    {
                    hitable.add(o1);
                    }
                    }
            return hitable;
    }
    
    
    private List<Rectangle> getHitableObjects(List<Rectangle> objects)
    {
            List<Rectangle> wood=  visionMBR.findWood();
            List<Rectangle> ice=visionMBR.findIce();
            List<Rectangle> stone=visionMBR.findStones();
            List<Rectangle> surfaceObjects1=getHitable(objects, wood);
            List<Rectangle> surfaceObjects2=getHitable(surfaceObjects1, stone);
            surfaceObjects1=new ArrayList<Rectangle>();
            surfaceObjects1=getHitable(surfaceObjects2,ice);
            return surfaceObjects1;
    }

    public boolean isObjectMoreLeft(Rectangle o1, Rectangle o2)
    {
            if(o1.getX()>o2.getX())
            {
                    if(o2.getY()<o1.getY())
                    {
                            if(o2.getY()+o2.getHeight()<(o1.getY()+o1.getHeight()))
                            {
                                    return false;
                            }
                    }
                    else if(o2.getY()<(o1.getY()+o1.getHeight()))
                    {
                            if ((o2.getY()+o2.getHeight())>(o1.getY()+o1.getHeight()))
                            {
                                    return false;
                            }
                    }
                    else if(o2.getY()==o1.getY())
                    {
                    return false;
                    }
            }
            return true;
    }
    public boolean isObjectHigher(Rectangle o1, Rectangle o2)
    {
            if(o1.getY()>o2.getY())
            {
                    if(o2.getX()<o1.getX())
                    {
                            if(o2.getX()+o2.getWidth()<(o1.getX()+o1.getWidth()))
                            {
                                    return false;
                            }
                    }
                    else if(o2.getX()<(o1.getX()+o1.getWidth()))
                    {
                            if ((o2.getX()+o2.getWidth())>(o1.getX()+o1.getWidth()))
                            {
                                    return false;
                            }
                    }
                    else if(o2.getX()==o1.getX())
                    {
                    return false;
                    }
            }
            return true;
    }	
	
	
	
	
	
	


}
