/*****************************************************************************
a ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2013,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
 **  Sahan Abeyasinghe, Jim Keys, Kar-Wai Lim, Zain Mubashir,  Andrew Wang, Peng Zhang
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************/
package ab.demo.other;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import javax.imageio.ImageIO;

import ab.demo.util.StateUtil;
import ab.server.Proxy;
import ab.server.proxy.message.ProxyClickMessage;
import ab.server.proxy.message.ProxyDragMessage;
import ab.server.proxy.message.ProxyMouseWheelMessage;
import ab.server.proxy.message.ProxyScreenshotMessage;
import ab.vision.GameStateExtractor.GameState;

/**
 * Util class for basic functions
 * 
 */
public class ActionRobot {
	public static Proxy proxy;
	public String level_status = "UNKNOWN";
	public int current_score = 0;
	private LoadingLevelSchema lls;
	private RestartLevelSchema rls;


	// A java util class for the standalone version. It provides common
	// functions an agent would use. E.g. get the screenshot
	public ActionRobot() {
		if (proxy == null) {
			try {
				proxy = new Proxy(9000) {
					@Override
					public void onOpen() {
						System.out.println("Client connected");
					}

					@Override
					public void onClose() {
						System.out.println("Client disconnected");
					}
				};
				proxy.start();

				System.out
						.println("Server started on port: " + proxy.getPort());

				System.out.println("Waiting for client to connect");
				proxy.waitForClients(1);

				lls = new LoadingLevelSchema(proxy);
				rls = new RestartLevelSchema(proxy);
				

			} catch (UnknownHostException e) {

				e.printStackTrace();
			}
		}
	}

	public void restartLevel() {
		rls.restartLevel();
	}

	public static void GoFromMainMenuToLevelSelection() {
		// --- go from the main menu to the episode menu
		GameState state = StateUtil.checkCurrentState(proxy);
		while (state == GameState.MAIN_MENU) {

			System.out.println("Go to the Episode Menu");
			proxy.send(new ProxyClickMessage(305, 277));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			state = StateUtil.checkCurrentState(proxy);
		}
		// --- go from the episode menu to the level selection menu
		while (state == GameState.EPISODE_MENU) {
			System.out.println("Select the Poached Eggs Episode");
			proxy.send(new ProxyClickMessage(150, 300));
			state = StateUtil.checkCurrentState(proxy);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			state = StateUtil.checkCurrentState(proxy);
		}

	}

	public GameState shootWithStateInfoReturned(List<Shot> csc) {
		ShootingSchema ss = new ShootingSchema();
		ss.shoot(proxy, csc);
		System.out.println("Shooting Completed");
		GameState state = StateUtil.checkCurrentState(proxy);
		return state;

	}

	public synchronized GameState checkState() {
		GameState state = StateUtil.checkCurrentState(proxy);
		return state;
	}

	public boolean shoot(List<Shot> csc) {
		ShootingSchema ss = new ShootingSchema();
		ss.shoot(proxy, csc);
		System.out.println("Shooting Completed");
		return true;

	}
	public void click()
	{
		proxy.send(new ProxyClickMessage(0, 0));
	}
	public void drag()
	{
		proxy.send(new ProxyDragMessage(0,0,0,0));
	}
	public void loadLevel(int... i) {
		int level = 1;
		if (i.length > 0) {
			level = i[0];
		}
		lls.loadLevel(level);

	}

	public void fullyZoom() {
		for (int k = 0; k < 10; k++) {
			proxy.send(new ProxyMouseWheelMessage(-1));
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static  BufferedImage doScreenShot() {
		byte[] imageBytes = proxy.send(new ProxyScreenshotMessage());
		BufferedImage image = null;
		try {
			image = ImageIO.read(new ByteArrayInputStream(imageBytes));
		} catch (IOException e) {
			// do somethingti
		}

		return image;
	}
	public void testScreenshot()
	{
		 proxy.send(new ProxyScreenshotMessage());
	}

	public static void main(String args[])
	{
		ActionRobot aRobot = new ActionRobot();
	    long time = System.currentTimeMillis();
		ActionRobot.doScreenShot();
		time = System.currentTimeMillis() - time;
		System.out.println(" cost: " + time);
		time = System.currentTimeMillis();
		int count = 0;
		while(count < 40)
		{
			ActionRobot.doScreenShot();
			count++;
		}
		//System.out.println(" Num of screenshots taken within 1000 ms: " + count);
		System.out.println(" time to take 40 screenshots" + (System.currentTimeMillis() - time));
		System.exit(0);
		
	
	}
}
