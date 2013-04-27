package testPlayerPathing;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.Robot;
import battlecode.common.RobotController;

public class HQ extends BaseRobot
{
	public HQ(RobotController myRC)
	{
		super(myRC);
		Direction dir=rc.getLocation().directionTo(enemyHQ).opposite(); 
		if(rc.canMove(dir))
		{
			try
			{
				rc.spawn(dir);
			}catch(GameActionException E)
			{
				System.out.println("Caught exception at spawn moment initial");
			}
		}
		//Note: active in constructor. But not anymore - spawned.
	}

	public boolean run()
	{
		Robot allies[]=rc.senseNearbyGameObjects(Robot.class, 10000, rc.getTeam());
		if(allies==null || allies.length==0)
			return false;
		else if(allies.length==1) //found the first spawned robot!
		{
			try
			{
				rc.broadcast((allies[0].getID()*28+13466+channelBlock)%65535,CODE_COMPPATH);
			}
			catch (GameActionException e)
			{
				System.out.println("Error broadcasting to first unit");
				e.printStackTrace();
			}
			return true;
		}
		else
		{
			System.out.println("MAJOR ERROR IN HQ first run!!!");
			return true;
		}
		
	}
	public void run2()
	{
		if(rc.isActive())
		{
			makeunit();
		}
	}

	public void initRadio() throws GameActionException
	{
		channelBlock=(int)((Math.random()*65535)+493671)%65535;
		if(channelBlock==STARTCHANNEL)
			channelBlock+=437;
		myChannel=(rc.getRobot().getID()*28+13466+channelBlock)%65535;
		channelDelta=channelBlock%57*3;
		rc.broadcast(STARTCHANNEL, channelBlock);
	}
	
	private void makeunit()
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
}
