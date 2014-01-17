/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************/
package ab.vision.real.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;

import ab.vision.ABType;
import ab.vision.real.ImageSegmenter;

public class Rect extends Body
{
	private static final long serialVersionUID = 1L;
	// width and height of the rectangle
    public Polygon p;
   protected double preciseWidth = -1, preciseHeight = -1;
    
    public double getPreciseWidth()
    {
	   	 if(preciseWidth != -1)
	   		 return preciseWidth;
	   	 return width;
    }
    
    public double getPreciseHeight()
    {
   	 if(preciseHeight != -1)
   		 return preciseHeight;
   	 return height;
    }
    //Note Rect's width is not always the Rectangle's width 
    public Rect(double xs, double ys,  double w, double h, double theta, ABType type)
    {
        
        
        if (h >= w)
        {
            angle = theta;
            preciseWidth = w;
            preciseHeight = h;
        }
        else
        {
            angle = theta + Math.PI / 2;
            preciseWidth = h;
            preciseHeight = w;
        }
        
        centerY = ys;
        centerX = xs;
        
        
        area = (int) (preciseWidth * preciseHeight);
        this.type  = type;
      
        createPolygon();
        super.setBounds(p.getBounds());

    } 


    private void createPolygon()
    {
    	 
    	 double angle1 = angle;
         double angle2 = perpendicular(angle1);
         
         // starting point for drawing
         double _xs, _ys;
         _ys = centerY + Math.sin(angle) * preciseHeight / 2 + 
              Math.sin(Math.abs(Math.PI/2 - angle)) * preciseWidth / 2;
         if (angle < Math.PI / 2)
             _xs = centerX + Math.cos(angle) * preciseHeight / 2 -
                 Math.sin(angle) * preciseWidth / 2;
         else if (angle > Math.PI / 2)
             _xs = centerX + Math.cos(angle) * preciseHeight / 2 +
                 Math.sin(angle) * preciseWidth / 2;
         else
             _xs = centerX - preciseWidth / 2;
             
         p = new Polygon();
         p.addPoint(round(_xs), round(_ys));
         
        
         
         _xs -= Math.cos(angle1) * preciseHeight;
         _ys -= Math.sin(angle1) * preciseHeight;
         p.addPoint(round(_xs), round(_ys));
         
       
         
         _xs -= Math.cos(angle2) * preciseWidth;
         _ys -= Math.sin(angle2) * preciseWidth;
         p.addPoint(round(_xs), round(_ys));
         
         
         
         _xs += Math.cos(angle1) * preciseHeight;
         _ys += Math.sin(angle1) * preciseHeight;
         p.addPoint(round(_xs), round(_ys));
   
    }
    @Override
    public Rectangle getBounds()
    {
    	return p.getBounds();
    }

    public Rect(int box[], ABType type)
    {
        centerX = (box[0] + box[2]) / 2.0;
        centerY = (box[3] + box[1]) / 2.0;
        preciseWidth = box[2] - box[0];
        preciseHeight = box[3] - box[1];
        angle = Math.PI / 2;
        
        if (preciseHeight < preciseWidth)
        {
            preciseWidth = preciseHeight;
            preciseHeight = box[2] - box[0];
            angle = 0;
        }
       
        
        width = (int)preciseWidth;
        height = (int)preciseHeight;
        
        this.type = type;
        
        area = width * height;
        createPolygon();
      
    }
    public Rect(double centerX, double centerY, double width, double height, double angle, ABType type, int area)
    {
    	  this.centerX = centerX;
    	  this.centerY = centerY;
    	  this.width = (int)width;
    	  this.height = (int)height;
    	  this.type = type;
    	  this.angle = angle;
    	  this.area = area;
    	  createPolygon();
    	  super.setBounds(p.getBounds());
          	
    }

    
    /* draw the rectangle onto canvas */
    public void draw(Graphics2D g, boolean fill, Color boxColor)
    {        
    
        
        if (fill) {
            g.setColor(ImageSegmenter._colors[type.id]);
            g.fillPolygon(p);
        }
        else {
            g.setColor(boxColor);
            g.drawPolygon(p);
        }
    }
    
    public static double perpendicular(double angle)
    {
        return angle > Math.PI / 2 ? angle - Math.PI / 2 : angle + Math.PI / 2;
    }
	
	public String toString()
	{
		return String.format("Rect: id:%d type:%s Area:%d w:%7.3f h:%7.3f a:%3.3f at x:%3.1f y:%3.1f", id, type, area, preciseWidth, preciseHeight, angle, centerX, centerY);
	}
}
