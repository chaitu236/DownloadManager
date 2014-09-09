//this class spans fileParser,speedCalc threads.
//this class has downloadVector as its field which is supplied to other threads

package downloadManager;

import java.util.*;
import java.io.*;
import java.net.*;

public class DownloadSpanner implements Runnable{

private int MAX_DOWNLOADS=4;
//private int MIN_SPEED=150;

private Vector<Download> downloadVector;
private ThreadGroup threadGroup;
@SuppressWarnings("unused")
private File urlFile;
private FileParser fileParser;
private SpeedCalc speedCalc;
private int downloadCount;

public DownloadSpanner(File urlFile)
{
	this.urlFile=urlFile;
	threadGroup=new ThreadGroup("downloads");
	downloadVector=new Vector<Download>();
	fileParser=new FileParser(urlFile);
	speedCalc=new SpeedCalc(downloadVector);
}

public Vector<Download> getDownloadVector()
{
	return downloadVector;
}

public void run()
{
	Thread speedCalcThread=new Thread(speedCalc);
	speedCalcThread.setDaemon(true);
	speedCalcThread.start();
	
		this.spanDownloads();
	
	System.out.println("\nno more downloads, exiting the downloader");
	afterAllDownloads();
}

private void spanDownloads()
{
	URL url;
	boolean sleepState=false;
	
	while(true)
	{
	
	while((url=fileParser.getNextURL())!=null)
	{
		if(sleepState)
		{
			System.out.println(getLine()+"\ndwnspanner:  Found new url, waking up\n"+getLine());
			sleepState=false;
		}
		
		Download dwn=new Download(url,downloadCount++,fileParser);
		downloadVector.add(dwn);
		
		Thread th=new Thread(threadGroup,dwn);
		th.setDaemon(false);
		th.start();
		
		while(threadGroup.activeCount()>=MAX_DOWNLOADS)
		{
			try{
				Thread.sleep(10000);
			}catch(Exception e){e.printStackTrace();}			
		}
	}
	if(!sleepState)
	{
		System.out.println(getLine()+"\ndwnspanner: EOF, going to sleep state\n"+getLine());
		sleepState=true;
	}
	
	if(threadGroup.activeCount()==0)
		break;
	
	try{
		Thread.sleep(10000);}catch(Exception e){e.printStackTrace();}
	}
}

public String getLine()
{
	String ans="";
		for(int i=0;i<30;i++)
			ans+="-";
	return ans;
}

public void afterAllDownloads()
{
	try {
		Process s=Runtime.getRuntime().exec("/usr/bin/sudo /usr/sbin/pppoe-stop");
		s.waitFor();
		
		Process p=Runtime.getRuntime().exec("/usr/bin/sudo /usr/sbin/rtcwake -m mem -s 100000");
		p.waitFor();
	} catch (Exception e) {
		e.printStackTrace();
	}
}
}
