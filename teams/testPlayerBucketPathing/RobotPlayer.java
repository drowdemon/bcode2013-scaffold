package testPlayerBucketPathing;

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
		while(!br.run())
		{
			br.rc.yield();
		}
		while(true)
		{
			br.run2();
			br.rc.yield();
		}
	}
}