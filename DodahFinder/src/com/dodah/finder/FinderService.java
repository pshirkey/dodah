package com.dodah.finder;

import java.util.Random;

public class FinderService {

	private Difficulty hardness;
	private double timetofindseconds;
	private int capturedistancepixels;

	public FinderService(Difficulty howhard)
	{
		hardness = howhard;
	}
	
	public void RegisterCallback()
	{
		;
	}
	
	// Later model on gaussian....
	public void TimeToFind()
	{
		Random r = new Random(9278030L);
		double gaussian = r.nextGaussian();
		
		switch(hardness)
		{
		case INSTANT:
			this.timetofindseconds = 0.25;
		case SIMPLE:
			this.timetofindseconds= 5;
		case EASY:
			this.timetofindseconds = 10;
		case MEDIUM:
			this.timetofindseconds = 45;
		case HARD:
			this.timetofindseconds = 120;
		case ARDUOUS:
			this.timetofindseconds = 300;
		case MISSIONIMPOSSIBLE:
			this.timetofindseconds = 3600;
			
		
			
		}
	}
	
	
}
