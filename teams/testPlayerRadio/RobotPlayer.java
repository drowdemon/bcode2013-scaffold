package testPlayerRadio;

import battlecode.common.*;

public class RobotPlayer
{
	public static BaseRobot br;
	public static void run(RobotController myRC)
	{
		if(myRC.getType()==RobotType.HQ)
		{
			br=new HQ(myRC);
		}
		else if(myRC.getType()==RobotType.SOLDIER)
		{
			br=new Soldier(myRC);
		}
		while(true)
		{
			br.run();
			br.rc.yield();
		}
	}
}