package testPlayerPathing;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.Team;

public class Soldier extends BaseRobot
{

	MapLocation rallypt;
	int numread;
	boolean endreading;
	MapLocation moveto;
	public Soldier(RobotController myRC)
	{
		super(myRC);
		rallypt=new MapLocation((myHQ.x*3+enemyHQ.x)/4,(myHQ.y*3+enemyHQ.y)/4);
		numread=0;
		endreading=false;
		moveto=rallypt;
	}
	private MapLocation readNextLoc()
	{
		int possCommand=0;
		if(endreading==false)
		{
			try
			{
				possCommand=rc.readBroadcast((channelBlock+numread*channelDelta)%65535);
			}
			catch (GameActionException e)
			{
				System.out.println("Error reading channel");
				e.printStackTrace();
			}
			if(possCommand!=0) //If it does: COMPROMISED or no directions posted yet 
			{
				if(possCommand+2000000000<0 && possCommand/500000000<0) //Both tests just in case, not really required to do both. Means null terminator
				{
					endreading=true;
					possCommand+=2000000000;
				}
				else
					numread++;
				return RobotTools.inttoloc(possCommand);
			}
			else
				return new MapLocation(-1,-1); //COMPROMISED, or no directions currently
		}
		else
			return null;
	}
	@SuppressWarnings("unused")
	private void movetoloc(MapLocation loc)
	{
		if(rc.isActive())
		{
			int trydirs[]={0,1,-1,2,-2};
			for(int d : trydirs)
			{
				Direction dir=rc.getLocation().directionTo(loc);
				if(rc.getLocation().equals(loc))
					return;
				Direction newd=Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(newd))
				{
					try
					{
						rc.move(newd);
					}
					catch (GameActionException e)
					{
						System.out.println("Thrown in soldier movement");
						e.printStackTrace();
					}
					break;
				}
			}
		}	
	}
	private void moveordefuse(MapLocation loc)
	{
		if(rc.isActive())
		{
			int trydirs[]={0,1,-1,2,-2};
			for(int d : trydirs)
			{
				MapLocation myLoc=rc.getLocation();
				Direction dir=myLoc.directionTo(loc);
				if(myLoc.equals(loc))
					return;
				Direction newd=Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(newd))
				{
					try
					{
						MapLocation newLoc=myLoc.add(newd);
						Team t=rc.senseMine(newLoc);
						if(t!=null && t!=rc.getTeam())
							rc.defuseMine(newLoc);
						else
							rc.move(newd);
					}
					catch (GameActionException e)
					{
						System.out.println("Thrown in soldier movement");
						e.printStackTrace();
					}
					break;
				}
			}
		}
	}
	private boolean readMyData() throws GameActionException
	{
		int result=0;
		result=rc.readBroadcast(myChannel);
		switch(result)
		{
			case CODE_COMPPATH:
			{
				rc.broadcast(myChannel, 0);
				fullpathingBasetoBase();
				return false;
			}
			default:
				return true; //COMPROMISED! Probably.
		}
	}
	public boolean run()
	{
		return true;
	}
	public void run2()
	{
		if(rc.isActive())
		{
			boolean keepgoing=false;
			try
			{
				keepgoing=readMyData();
			}
			catch (GameActionException e)
			{
				System.out.println("Exception in readMyData");
				e.printStackTrace();
			}
			if(keepgoing==false)
				return;
			Robot enemies[]=rc.senseNearbyGameObjects(Robot.class,10000000,rc.getTeam().opponent());
			MapLocation nearestenemy=null;
			if(enemies.length!=0)
				nearestenemy=getNearestEnemy(rc.getLocation(),enemies);
			if(nearestenemy==null)
			{
				if(Clock.getRoundNum()>300)
				{
					if(!rc.getLocation().equals(moveto))
					{
						moveordefuse(moveto);
						return; //No need to find out where I'm going next. Don't necessarily return, but unless the above is modified, don't try to figure out where to go.
					}
					
					MapLocation loc=readNextLoc();
					if(loc!=null && loc.y!=-1) //no longer reading //NOTE: WILL CRASH IF CHANNEL IS INTERFERED WITH (reading will be invalid)
						moveto=loc;
					else
						moveto=enemyHQ;
				}
				moveordefuse(moveto);
			}
			else
				moveordefuse(nearestenemy);
		}
	}

	public void initRadio() throws GameActionException
	{
		channelBlock=rc.readBroadcast(STARTCHANNEL);
		myChannel=(rc.getRobot().getID()*28+13466+channelBlock)%65535;
		channelDelta=channelBlock%57*3;
	}
	
}