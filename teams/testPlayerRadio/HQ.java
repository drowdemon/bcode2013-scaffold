package testPlayerRadio;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.Robot;
import battlecode.common.RobotController;

public class HQ extends BaseRobot
{
	public HQ(RobotController myRC)
	{
		super(myRC);
	}

	public void run()
	{
		if(rc.isActive())
		{
			Direction dir=rc.getLocation().directionTo(enemyHQ); 
			if(rc.canMove(dir))
			{
				try
				{
					rc.spawn(dir);
				}catch(GameActionException E)
				{
					System.out.println("Caught exception at spawn moment");
				}
			}
		}
		/*Robot enemies[]=rc.senseNearbyGameObjects(Robot.class,10000000,rc.getTeam().opponent());
		if(enemies.length>0)
		{
			try
			{
				rc.broadcast(channelBlock, RobotTools.loctoint(getNearestEnemies(rc.getLocation(),enemies)));
			}
			catch (GameActionException e)
			{
				System.out.println("Error Broadcasting");
				e.printStackTrace();
			}
		}
		else
		{
			try
			{
				rc.broadcast(channelBlock, 0);
			}
			catch (GameActionException e)
			{
				System.out.println("Error Broadcasting");
				e.printStackTrace();
			}
		}*/
	}

	public void initRadio() throws GameActionException
	{
		channelBlock=(int)((Math.random()*65535)+2919834)%65535;
		if(channelBlock==STARTCHANNEL)
			channelBlock+=918;
		myChannel=(rc.getRobot().getID()*42+19857+channelBlock)%65535;
		rc.broadcast(STARTCHANNEL, channelBlock);
	}
}
