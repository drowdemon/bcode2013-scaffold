package testPlayerBucketPathing;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.Team;

public class Soldier extends BaseRobot
{
	private int numread;
	private boolean endreading;
	MapLocation moveto;
	private int state;//this will take bits 2,3, and 4 of the soldiers channel
	private int miningRad;
//	private final static int RUSH=8;
//	private final static int MINE=4;
//	private final static int ATTACKANDMINE=12;
//	private final static int DEFEND=16;
//	//Several unassigned states: all of them are 0(?),4,8,12,16,20,24,28
//	private final static int STATEFLAGCHECK=28;
//	private final static int SPREADFLAGCHECK=3;
	private int spread; //this will take the first 2 bits (0  and 1) of the soldiers channel
	public Soldier(RobotController myRC)
	{
		super(myRC);
		numread=0;
		endreading=false;
		moveto=rallypt;
		state=MINE;
		spread=0;
		miningRad=16;
		MapLocation poss=readNextLoc(-1);
		if(poss!=null && poss.y!=-1)
			moveto=readNextLoc(-1); //TODO make this fix better and less improvised
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
					endreading=true; //remove null terminator
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
	private MapLocation readNextLoc(int where)
	{
		int possCommand=0;
		try
		{
			possCommand=rc.readBroadcast((channelBlock+where*channelDelta)%65535);
		}
		catch (GameActionException e)
		{
			System.out.println("Error reading channel");
			e.printStackTrace();
		}
		if(possCommand!=0) //If it does: COMPROMISED or no directions posted yet 
		{
			return RobotTools.inttoloc(possCommand);
		}
		else
			return new MapLocation(-1,-1); //COMPROMISED, or no directions currently
	}
	private void movetoloc(MapLocation loc)
	{
		if(rc.isActive())
		{
			int trydirs[]={0,1,-1,2,-2}; //trying all directions in these 180 degrees
			MapLocation myLoc=rc.getLocation();
			for(int d : trydirs)
			{
				Direction dir=myLoc.directionTo(loc);
				if(myLoc.equals(loc))
					return;
				Direction newd=Direction.values()[(dir.ordinal()+d+8)%8];
				Team t=rc.senseMine(myLoc.add(newd));
				if(rc.canMove(newd) && (t==null || t==rc.getTeam())) //if I can move and theres no mine, do it
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
						if(t!=null && t!=rc.getTeam()) //if theres a mine, get rid of it, else move
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
		rc.setIndicatorString(0, "Result: " + result);
		if((result&CODE_COMPPATH)>0) //I get to path
		{
			//rc.broadcast(myChannel, 0);
			rc.broadcast((rc.senseObjectAtLocation(myHQ).getID()*28+13466+channelBlock)%65535, PATHING); //respond to base that I'm pathing
			fullpathingBasetoBase(); //do it
			System.out.println("BucketSearch Round:" + Clock.getRoundNum());
			return false;
		}
		state=(result&STATEFLAGCHECK); //getting info. 
		spread=(result&SPREADFLAGCHECK);
		miningRad=(result&MININGFLAGCHECK);
		miningRad=(miningRad/64+4)*(miningRad/64+4);
		if((result&CHECK_NEWLOC)>0) //new location to go to
		{
			MapLocation test=readNextLoc(-1);
			if(test!=null && test.y!=-1)
				moveto=test;
		}
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
				if(rc.getTeamPower()>=GameConstants.BROADCAST_READ_COST)
					keepgoing=readMyData(); //read my channel and check for important stuff
				else
					System.out.println("Not enough energy to communicate!!!");
			}
			catch (GameActionException e)
			{
				System.out.println("Exception in readMyData");
				e.printStackTrace();
			}
			if(keepgoing==false)
				return;
			Robot enemies[]=rc.senseNearbyGameObjects(Robot.class,5,rc.getTeam().opponent()); //get data on nearby stuff
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
			//rc.setIndicatorString(1, moveto.toString());
			if(state==MINE) //state machine
			{
				mineState(ally,nearestenemy,closeallies.length,enemies.length);
			}
			if(state==ATTACKANDMINE)
			{
				mineState(ally,nearestenemy,closeallies.length,enemies.length);
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
	
	private void mineState(MapLocation ally, MapLocation enemy, int numallies, int numenemies) //If I'm mining places
	{
		MapLocation myloc=rc.getLocation();
		int dist=myloc.distanceSquaredTo(moveto);
		/*
		int attract=weighGoal(dist);
		MapLocation goal=myloc.add(myloc.directionTo(moveto),attract);
		Direction repulse=(ally!=null) ? computeSpread(ally) : Direction.NONE;
		goal=goal.add(repulse,spread*3);*/
		MapLocation goal=groupGo(myloc, moveto, ally, enemy, numenemies, numallies, true, 3); //TODO CHANGE SO THAT IT DOESN'T IGNORE ENEMIES
		//rc.setIndicatorString(0, "Repulse: " + repulse + ", Attract: " + attract);
		//rc.setIndicatorString(1, "Spread: " + spread*3);
		rc.setIndicatorString(1, "Moveto: " + moveto + "Goal: " + goal);
		//rc.setIndicatorString(0, "Rad: " + miningRad);
		if(dist<=miningRad && rc.senseMine(myloc)==null) //put mines in this radius
		{	
			if(((myloc.x+myloc.y)%2==0))
			{
				try
				{
					rc.layMine();
				}
				catch (GameActionException e)
				{
					System.out.println("Error laying mine");
					e.printStackTrace();
				}
			}
		}
		moveordefuse(goal);
		//movetoloc(goal);
	}
	
	private MapLocation groupGo(MapLocation myLoc, MapLocation generalGoal, MapLocation ally, MapLocation enemy, int numenemies, int numallies, boolean ignoreEnemies, int spreadMult)
	{
		//TODO finish cases where ignoreEnemy!=false
		int dist=myLoc.distanceSquaredTo(generalGoal);
		int attract=weighGoal(dist); //how desperate am I to get to where I wanna go
		MapLocation goal=myLoc.add(myLoc.directionTo(generalGoal),attract);
		if(ignoreEnemies) //If I don't care about enemies
		{
			Direction repulse=(ally!=null) ? (rc.getLocation().directionTo(ally).opposite()) : Direction.NONE; //repulsion from allies, if they exist. This is the direction, the next line is using it //TODO: MERGE THIS LINE AND THE NEXT ONE
			goal=goal.add(repulse,spread*spreadMult);
		}
		else //TODO: THIS IS CRAP
		{
			Direction repulse=(ally!=null) ? (rc.getLocation().directionTo(ally).opposite()) : Direction.NONE;
			goal=goal.add(repulse,spread*spreadMult);
			
		}
		return goal;
	}
	
	private int weighGoal(int dist) //how much I care about my goals
	{
		if(dist<5)
			return 1;
		if(dist<12)
			return 2;
		if(dist<25)
			return 4;
		if(dist<100)
			return 7;
		else
			return 12;
	}
	
	/*private Direction computeSpread(MapLocation ally)
	{
		return rc.getLocation().directionTo(ally).opposite();
	}*/
	
	public void initRadio() throws GameActionException //Initializes my radio channels
	{
		channelBlock=rc.readBroadcast(STARTCHANNEL);
		myChannel=(rc.getRobot().getID()*28+13466+channelBlock)%65535;
		channelDelta=channelBlock%57*3;
	}
	
}