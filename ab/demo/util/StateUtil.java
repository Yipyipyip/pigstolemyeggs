/*****************************************************************************
** ANGRYBIRDS AI AGENT FRAMEWORK
** Copyright (c) 2013,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
**  Sahan Abeyasinghe, Jim Keys, Kar-Wai Lim, Zain Mubashir,  Andrew Wang, Peng Zhang
** All rights reserved.
**This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
**To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
*or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
*****************************************************************************/

package ab.demo.util;

import ab.server.Proxy;
import ab.server.proxy.message.ProxyScreenshotMessage;
import ab.vision.GameStateExtractor;
import ab.vision.GameStateExtractor.GameState;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;


public class StateUtil {
    /**
     * Check the current state
     * @return GameState: check the current state 
     * */
	public static  GameState  checkCurrentState(Proxy proxy)
	{
		 byte[] imageBytes = proxy.send(new ProxyScreenshotMessage());
		
	        BufferedImage image = null;
	        try {
	            image = ImageIO.read(new ByteArrayInputStream(imageBytes));
	        } catch (IOException e) {
	          
	        }
	        GameStateExtractor gameStateExtractor = new GameStateExtractor();
	        GameStateExtractor.GameState state = gameStateExtractor.getGameState(image);
	        System.out.println("the game state is :     " + state);
		  return state;
	}
	public static  GameState  checkCurrentState(BufferedImage image)
	{
		 
	        GameStateExtractor gameStateExtractor = new GameStateExtractor();
	        GameStateExtractor.GameState state = gameStateExtractor.getGameState(image);
	        System.out.println("the game state is :     " + state);
		  return state;
	}
	public static int checkCurrentScore(Proxy proxy)
	{
		byte[] imageBytes = proxy.send(new ProxyScreenshotMessage());
        int score = -1;

		BufferedImage image = null;
	    try {
	           image = ImageIO.read(new ByteArrayInputStream(imageBytes));
	        } 
	    catch (IOException e) {
	            // do something
	        }
	    
        GameStateExtractor gameStateExtractor = new GameStateExtractor();
        GameState state = gameStateExtractor.getGameState(image);
        if (state == GameState.PLAYING)
        	score = gameStateExtractor.getScoreInGame(image);
        else
        	if(state == GameState.WON)
        		score = gameStateExtractor.getScoreEndGame(image);
       if(score == -1)
    	   System.out.println(" the game score is unavailable "); 
    
    	   
		return score;
	}
	

}
