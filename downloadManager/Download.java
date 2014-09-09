package downloadManager;

import java.net.*;
import java.util.*;
import java.io.*;

public class Download implements Runnable{

public enum Status{ NOT_STARTED,DOWNLOADING,PAUSED,ABORTED,FINISHED,ERROR,CONNECTING }

private URL downloadURL;
private Date startDate;
private Date endDate;
private boolean pauseFlag;
//private Object syncObject;
private Status status;
private long downloaded;	//this is file size downloaded after resumption and is updated in thread
private long sizeStarted;	//this is starting size of the file into which the data is downloaded
private long sizeRemaining;  //this is remaining file size to be downloaded and is not updated in thread
private File file;

private long estimatedEndTime;
private boolean resumeAvailable;
private long pauseTime;
private int speed;
private int downloadNumber;
private FileParser fileParser;

public static int MAX_BUFFER_SIZE=10000;
public static int fileCount;
private final String directory="/home/chaitanya/Desktop";

public static final int NLINE=1;
public static final int TILDE=0;

public final int READ_TIMEOUT=100000;
public final int CONNECTION_TIMEOUT=100000;

private InputStream inputStream;
private RandomAccessFile raf;

public static int TEST_COUNT=0;

public Download(URL url, File filep,int downloadNumber,FileParser fileParser)
{
	//file=new File("/home/chaitanya/Desktop/mydownloadtest.flv");	//////////// to be deleted for testing purposes
	initializeDownload(url,filep,downloadNumber,fileParser);
}

private void initializeDownload(URL url,File filep,int downloadNumber,FileParser fileParser)
{
	this.downloadURL=url;
	System.out.println("file name length "+filep.getName().length());
	
	if(filep.getName().length()>30||filep.getName().length()<1)
		this.file=new File(directory+"/download"+(fileCount++));
	else
		this.file=new File(directory,filep.getName());
	
	startDate=new Date();
	pauseFlag=false;
	//syncObject=new Object();
	status=Status.NOT_STARTED;
	sizeStarted=this.file.length();
	System.out.println("file starting size "+sizeStarted+" file name "+this.file.getName());
	System.out.println("start size "+sizeStarted);
	sizeRemaining=-1;
	pauseTime=0;
	downloaded=0;
	
	this.downloadNumber=downloadNumber;
	this.fileParser=fileParser;
}

public Download(URL url, File file,int downloadNumber)
{
	this(url,new File(url.getFile()),downloadNumber,null);
}

public Download(URL url,int downloadNumber,FileParser fileParser)
{
	this(url,new File(url.getFile()),downloadNumber,fileParser);
}

public Download(URL url,int downloadNumber)
{
	this(url,new File(url.getFile()),downloadNumber,null);
}

public Download(URL url)
{
	this(url,new File(url.getFile()),-1);
}

public void setEstimatedEndTime(long time)
{
	estimatedEndTime=time;
}

public Status getStatus()
{
	return status;
}

//speed in bytes per milli second which amounts nearly to kilobytes per second
public void setSpeed(int speed)
{
	this.speed=speed;
}
//speed in kbps
public int getSpeed()
{
	return speed;
}

//estimated end time in milli-seconds
public long getEstimatedEndTime()
{
	return estimatedEndTime;
}

public Date getStartDate()
{
	return startDate;
}

public Date getEndDate()
{
	return endDate;
}

public long getDownloaded()
{
	return downloaded;
}

public long getPauseTime()
{
	return pauseTime;
}

public void run()
{
	System.out.println("test count "+(Download.TEST_COUNT++));
	try{
		HttpURLConnection connection =(HttpURLConnection)downloadURL.openConnection();
		connection.setReadTimeout(READ_TIMEOUT);
		connection.setConnectTimeout(CONNECTION_TIMEOUT);
		
		//System.out.println("connection time out "+connection.getConnectTimeout());/////////////////////////
		connection.setRequestProperty("Range","bytes="+sizeStarted+"-");
		status=Status.CONNECTING;
		
		connection.connect();
		
		int responseCode=connection.getResponseCode();
		
		if(responseCode / 100 !=2)
		{
			System.out.println("bad response code ("+responseCode+")");
		}		
		
		if(responseCode!=206)
		{
			resumeAvailable=false;
			if(sizeStarted>0)
			{
				System.out.println("cannot resume download, response code "+responseCode);
				abort();
				return;
			}
		}
		else
		{
			resumeAvailable=true;
		}
		
		sizeRemaining=connection.getContentLength();
		
		if(sizeRemaining < 1)
		{
			System.out.println("bad total size");
			abort();
			return;
		}
		System.out.println("download size remaining  "+sizeRemaining);
		
		raf=new RandomAccessFile(file,"rw");
		raf.seek(sizeStarted);
		inputStream=connection.getInputStream();
		
		status=Status.DOWNLOADING;

		byte buffer[];
		
		while(status!=Status.ABORTED)
		{
			//choose buffer size according to remaining download size of file
			
			if(sizeRemaining-downloaded>=Download.MAX_BUFFER_SIZE)
				buffer=new byte[Download.MAX_BUFFER_SIZE];
			else
				buffer=new byte[(int)(sizeRemaining-downloaded)];
						
			 int read = inputStream.read(buffer);
             if (read == -1)
                 break;
             // Write buffer to file.
             raf.write(buffer, 0, read);
             
             downloaded += read;
             
            while(pauseFlag)
 			{
 				status=Status.PAUSED;
 				//System.out.println("paused ");
 				//synchronized(syncObject)
 				//{
 				//syncObject.wait();
 				//}
 				Thread.sleep(1000);	//
 				pauseTime+=1000;	//
 			//	System.out.println("resumed ");
 			//	if(status!=Status.ABORTED)
 			//		status=Status.DOWNLOADING;
 			}
		}
		if(status==Status.ABORTED)
			System.out.println("aborted");
	}catch(Exception e){
		System.out.println("some error "+downloadNumber);
		status=Status.ERROR;
		}
	
	finally
	{
		try{
			if(raf!=null)
			raf.close();
			
			if(inputStream!=null)				
			inputStream.close();
			
			}catch(Exception e){e.printStackTrace();}
		
			
			if(status==Status.DOWNLOADING)
			{
				status=Status.FINISHED;
				afterDownload();
			}
			
			if(status==Status.ERROR)
			{
				System.out.println("download "+downloadNumber+" waiting for internet "+Download.TEST_COUNT);
				
				waitForInet();
				System.out.println("waited download "+downloadNumber+" count "+Download.TEST_COUNT);
				
				restartDownload();
				System.out.println("Restarted download "+downloadNumber+" because of an error, count "+Download.TEST_COUNT);
			}
	}
}

private void restartDownload()
{
	initializeDownload(downloadURL,file,downloadNumber,this.fileParser);
	try{
		Thread th=new Thread(this);
		th.start();
	}catch(Exception e){e.printStackTrace();}
}

private void afterDownload()
{
	if(fileParser!=null)
		fileParser.deleteLine(downloadNumber);
	
	endDate=new Date();
	
	System.out.println(endDate+" Completed:::: "+this.toString(NLINE));
}
//sets abort flag so that the download doesn't continue
public void abort()
{
	if(status==Download.Status.PAUSED)
		this.resume();
	
	status=Status.ABORTED;
	System.out.println("aborting");
}

//remaining download size in bytes
public long getRemainingDownloadSize()
{
	return sizeRemaining-downloaded;
}

public void pause()
{
	pauseFlag=true;
	System.out.println("pausing");
}

public void resume()
{
	pauseFlag=false;
	status=Status.DOWNLOADING;
	System.out.println("resuming");
	//synchronized(syncObject)
	//{
	//syncObject.notify();
	//}
}

public int getDownloadNumber()
{
	return downloadNumber;
}

private String sizeInString(long size)
{
	String temp;
	
	if(size/1000>1000*1000)
		temp=size/(1000*1000*1000)+" Gb";
	else if (size/1000>1000)
		temp=size/(1000*1000)+" Mb";
	else 
		temp=size/1000+" Kb";
	
	return temp;
}

public String toString(int par)
{
	String lineEnd="";
	
	if(par==Download.NLINE)
		lineEnd="\n";
	else if(par==Download.TILDE)
		lineEnd="~";
	
String result="";
	
	result+=lineEnd+"Download no:"+downloadNumber+lineEnd;
	result+="\tName: "+file+lineEnd;
	result+="\tStarted At: "+startDate.toString()+lineEnd;	
	result+="\tStart Size: "+sizeInString(sizeStarted)+lineEnd;
	result+="\tDownload Amt: "+sizeInString(sizeStarted+downloaded)+lineEnd;
	result+="\tRem: "+getRemainingDownloadSize()+lineEnd;
	result+="\tStatus:  "+status+lineEnd;
	result+="\tSpd: "+getSpeed()+lineEnd;
	
	long hours,minutes,seconds;
	
	long tempTime=estimatedEndTime/1000; //time in seconds
	hours=tempTime/(60*60);
	minutes=(tempTime-hours*60*60)/60;
	seconds=tempTime%60;
	
	result+="\tEstimated time: "+hours+":"+minutes+":"+seconds+""+lineEnd;
	result+="\tResume availability: "+resumeAvailable+lineEnd;
		
	return result;
}
public String toString()
{
	return this.toString(Download.TILDE);
}

public void waitForInet()
{
	
	while(!CheckInetConnection.testInternetConnection())
	{
		try{
			Thread.sleep(10000);
		}catch(Exception e){e.printStackTrace();}
	}
}

/*public void restartDownload()
{
	try {
		inputStream.close();
		raf.close();
		
		Thread th=new Thread(this);
		th.start();
	} catch (Exception e) {
		e.printStackTrace();
	}
}*/

/*public static void main(String args[]) throws Exception
{
	Download dwn=new Download(new URL("http://get2pc.com/download.php?data=aHR0cDovLzIwOC4xMTcuMjUwLjE3L3ZpZGVvcGxheWJhY2s/aXA9MC4wLjAuMCZzcGFyYW1zPWlkJTJDZXhwaXJlJTJDaXAlMkNpcGJpdHMlMkNpdGFnJTJDYnVyc3QlMkNmYWN0b3ImaXRhZz01JmlwYml0cz0wJnNpZ25hdHVyZT0xNzhEQzBCMUZEMTZEMzQ4MDY4RTkxMDhDQzJDNDZBNDVGMzBFQkMyLkM0M0M2NDI3MkZFNjE0QjZDNkEzQjI3RDM4MDQ0ODRCMjI0QzEzMEImc3Zlcj0zJmV4cGlyZT0xMjQ2MTQ3MjAwJmtleT15dDEmZmFjdG9yPTEuMjUmYnVyc3Q9NDAmaWQ9NTE0ZmM4YjE0YzAxYTA4OA=="),new File("/home/chaitanya/Desktop/testing"),0);
	System.out.println("downloaded size is  "+dwn.downloaded);
	Thread th=new Thread(dwn);
	th.start();
	BufferedReader read=new BufferedReader(new InputStreamReader(System.in));
	String temp=null;
	while(!(temp=read.readLine()).equals("quit"))
	{
		if(temp.equals("pause"))
			dwn.pause();
		if(temp.equals("resume"))
			dwn.resume();
		if(temp.equals("abort"))
			dwn.abort();
	}
}*/
}
