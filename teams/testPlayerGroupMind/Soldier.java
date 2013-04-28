package testPlayerGroupMind;

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
	private int numread;
	private boolean endreading;
	MapLocation moveto;
	private int state;//this will take bits 2,3, and 4 of the soldiers channel
	private final static int RUSH=8;
	private final static int MINE=4;
	private final static int ATTACKANDMINE=12;
	//Several unassigned states: all of them are 0(?),4,8,12,16,20,24,28
	private final static int STATEFLAGCHECK=28;
	private final static int SPREADFLAGCHECK=3;
	private int spread; //this will take the first 2 bits (0  and 1) of the soldiers channel
	public Soldier(RobotController myRC)
	{
		super(myRC);
		rallypt=myHQ.add(myHQ.directionTo(enemyHQ),4);
		numread=0;
		endreading=false;
		moveto=rallypt;
		state=MINE;
		spread=1;
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
		if((result&CODE_COMPPATH)>0)
		{
				rc.broadcast(myChannel, 0);
				fullpathingBasetoBase();
				return false;
		}
		state=(result&STATEFLAGCHECK);
		spread=(result&SPREADFLAGCHECK);
		//if(result>STATEFLAGCHECK+SPREADFLAGCHECK+CODE_COMPPATH) //invalid
		//	return true; //COMPROMISED! Probably.
		return true;
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
			Robot enemies[]=rc.senseNearbyGameObjects(Robot.class,5,rc.getTeam().opponent());
			Robot closeallies[]=rc.senseNearbyGameObjects(Robot.class,2,rc.getTeam());
			MapLocation nearestenemy=null;
			MapLocation ally=null;
			//int numclosestenemies=0;	
			if(enemies.length!=0)
				nearestenemy=getNearestEnemy(rc.getLocation(),enemies);
			if(closeallies.length!=0)
			{
				try
				{
					ally=rc.senseLocationOf(closeallies[0]);
				}
				catch (GameActionException e)
				{
					e.printStackTrace();
				}
			}
			if(state==MINE)
			{
				//mineState();
			}
			//System.out.println(numclosestenemies);
			/*if(nearestenemy==null)
			{
				if(Clock.getRoundNum()>300)
				{
					//if(!rc.getLocation().equals(moveto))
					//{
					//	moveordefuse(moveto);
					//	return; //No need to find out where I'm going next. Don't necessarily return, but unless the above is modified, don't try to figure out where to go.
					//}
					boolean findnext=false;
					if(rc.getLocation().distanceSquaredTo(moveto)<4) //pretty close
					{
						//moveordefuse(moveto);
						findnext=true;
						//return; //No need to find out where I'm going next. Don't necessarily return, but unless the above is modified, don't try to figure out where to go.
					}
					
					if(findnext==true)
					{
						MapLocation loc=readNextLoc();
						if(loc!=null && loc.y!=-1) //no longer reading //NOTE: WILL CRASH IF CHANNEL IS INTERFERED WITH (reading will be invalid)
							moveto=loc;
						else
							moveto=enemyHQ;
					}
				}
				moveordefuse(moveto);
			}
			else
				movetoloc(nearestenemy);*/
		}
	}
	
	private void mineState(MapLocation ally)
	{
		int dist=rc.getLocation().distanceSquaredTo(moveto);
		int attract=weighGoal(dist);
		MapLocation goal=rc.getLocation().add(rc.getLocation().directionTo(moveto),attract);
		Direction repulse=computeSpread(ally);
		goal.add(repulse,spread);
		
	}
	
	private int weighGoal(int dist)
	{
		if(dist<5)
			return 1;
		if(dist<12)
			return 3;
		if(dist<25)
			return 5;
		if(dist<100)
			return 10;
		else
			return 40;
	}
	
	private Direction computeSpread(MapLocation ally)
	{
		return rc.getLocation().directionTo(ally).opposite();
	}
	
	public void initRadio() throws GameActionException
	{
		channelBlock=rc.readBroadcast(STARTCHANNEL);
		myChannel=(rc.getRobot().getID()*28+13466+channelBlock)%65535;
		channelDelta=channelBlock%57*3;
	}
	
}