package ab.demo;

import ab.utils.GameImageRecorder;
import ab.vision.TestVision;

/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK Copyright (c) 2013, XiaoYu (Gary) Ge, Jochen
 * Renz,Stephen Gould, Sahan Abeyasinghe,Jim Keys, Kar-Wai Lim, Zain Mubashir,
 * Andrew Wang, Peng Zhang All rights reserved. This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-nc-sa/3.0/ or send a letter to
 * Creative Commons, 444 Castro Street, Suite 900, Mountain View, California,
 * 94041, USA.
 *****************************************************************************/

public class MainEntry {
	// the entry of the software.
	public static void main(String args[]) {
		Agent a = null;
		String command = "";
		if (args.length > 0) {
			command = args[0];
				if (command.equalsIgnoreCase("-na")) {
					a = new NaiveAgent();
				}else if(command.equalsIgnoreCase("-nae"))
				{
					a=new NaiveAgentEnhanced();
				}else if(command.equalsIgnoreCase("-naet"))
				{
					a=new NaiveAgentTargetingStructure();
				}else if((command.equalsIgnoreCase("-nasc")))
				{
					a=new ClientNaiveAgent();
				}
				else if((command.equalsIgnoreCase("-ia")))
				{
					a=new IntelligentAgent();
				}
				else if (command.equalsIgnoreCase("-showTraj")) {
					 String[] param = {};
					 // TestTrajectory.main(param);
					 abTrajectory.main(param);
					 } else if (command.equalsIgnoreCase("-recordImg")) {
					
					 if (args.length < 2)
					 System.out.println("please specify the directory");
					 else {
					 String[] param = { args[1] };
					
					 GameImageRecorder.main(param);
					 }
					} else if (command.equals("-showSeg")) {
						Thread thre = new Thread(new TestVision());
						thre.start();
					}
			}
			for (int i = 1; i < args.length; i++) {
				command = args[1];
				if (args[1].contains("-l")) {
					command = command.replace("-l", "");
					try {
						int level = Integer.parseInt(command);
						a.setCurrent_level(level);
					} catch (NumberFormatException e) {

					}
				} else if (command.contains("-id")) {
					command = command.replace("-id", "");
					a.setID(Integer.parseInt(command));
				} else if (command.contains("-ip")) {
					command = command.replace("-ip", "");
					a.setIP(command);
				} else if (command.equals("-showSeg")) {
					Thread thre = new Thread(new TestVision());
					thre.start();
				}
			}if(a!=null)
			{
			a.run();
			}
		}
		
}
