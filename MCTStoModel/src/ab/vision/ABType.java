/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************/
package ab.vision;

public enum ABType {
	
	Hill(1),
	Sling(2),
	RedBird(3), 
	YellowBird(4), 
	BlueBird(5), 
	BlackBird(6), 
	WhiteBird(7), 
	Pig(8),
	Ice(9), 
	Wood(10), 
	Stone(11), 
	TNT(12),
	Unknown(0);
	public int id;
	private ABType(int id)
	{
		this.id = id;
	}
}
