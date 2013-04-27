package testPlayerRadio;

import java.util.ArrayList;

public class SearchFringe
{
	int weightToDate;
	ArrayList<point> path;
	int mine;
	public SearchFringe(int weightToDate, ArrayList<point> path)
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
