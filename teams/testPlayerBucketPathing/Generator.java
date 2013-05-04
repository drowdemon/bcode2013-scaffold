package testPlayerBucketPathing;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;

public class Generator extends BaseRobot
{

	public Generator(RobotController myRC)
	{
		super(myRC);
	}
	public boolean run()
	{
		int result=0;
		try
		{
			result=rc.readBroadcast(myChannel);
		}
		catch (GameActionException e)
		{
			e.printStackTrace();
		}
		if(result==ACKNOLEDGED || result==CODE_COMPPATH)
		{
			//System.out.println("Over and Out ");
			return true;
		}
		else
		{
			if(rc.getTeamPower()>GameConstants.BROADCAST_SEND_COST);
			{
				try
				{
					rc.broadcast((rc.senseObjectAtLocation(myHQ).getID()*28+13466+channelBlock)%65535, ENCEXISTS|RobotTools.loctoint(rc.getLocation())|(rc.getRobot().getID()<<18));
					//System.out.println("I HAVE ARRIVED! YOU ARE SAVED! DO YOU READ?" + result + " I am: " + rc.getRobot().getID());
				}
				catch (GameActionException e)
				{
					e.printStackTrace();
				}
			}
			return false;
		}
	}
	public void run2()
	{
		int result=0;
		try
		{
			result=rc.readBroadcast(myChannel);
//			System.out.println("Result: " + result);
		}
		catch (GameActionException e)
		{
			e.printStackTrace();
		}
		if(result==CODE_COMPPATH)
		{
			try
			{
				System.out.println("Searching");
				rc.broadcast((rc.senseObjectAtLocation(myHQ).getID()*28+13466+channelBlock)%65535, PATHING); //respond to base that I'm pathing
				fullpathingBasetoBase();
				rc.broadcast((rc.getRobot().getID()*28+13466+channelBlock)%65535, 0);
			}
			catch (GameActionException e)
			{
				e.printStackTrace();
			}
		}
	}
}
