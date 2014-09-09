package downloadManager;

import java.util.*;

public class SpeedCalc implements Runnable{

private Vector<Download> downloadVector;
public int REFRESH_TIME=1000;
public int totalSpeed;

public SpeedCalc(Vector<Download> downloadVector)
{
	this.downloadVector=downloadVector;
}

//updates speed and estimated time to complete
public void run()
{
	while(true)
	{
		
	Date nowDate=new Date();
	totalSpeed=0;
	
	if(downloadVector.size()!=0)
	{
	for(Download i:downloadVector)
	{
		if(i.getStatus()!=Download.Status.DOWNLOADING)
			continue;
		
		    long totalTime=nowDate.getTime()-i.getStartDate().getTime();
			long runTime=totalTime-i.getPauseTime();
			int speed=(int)(i.getDownloaded()/runTime);
		
			i.setSpeed(speed);
		
			if(speed<1)
				i.setEstimatedEndTime(100000000);
			else
				i.setEstimatedEndTime(i.getRemainingDownloadSize()/speed);
		
			totalSpeed+=speed;
	}
	}
	
	try{
	Thread.sleep(REFRESH_TIME);
	}catch(Exception e){e.printStackTrace();}
	
	}
}
}
