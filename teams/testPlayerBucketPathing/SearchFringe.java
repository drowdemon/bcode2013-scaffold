package testPlayerBucketPathing;

import java.util.ArrayList;

import battlecode.common.Direction;

public class SearchFringe
{
	int weightToDate;
	ArrayList<Direction> path;
	int mine;
	public SearchFringe(int weightToDate, ArrayList<Direction> path)
	{
		this.weightToDate = weightToDate;
		this.path = path;
		mine=0;
	}
	public SearchFringe(int weightToDate)
	{
		this.weightToDate = weightToDate;
	}
	public SearchFringe()
	{
		weightToDate=999999999;
		mine=0;
	}
}
