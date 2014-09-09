package downloadManager;

import java.net.Socket;


public class CheckInetConnection implements Runnable{
	
public static final int NO_OF_TRIES=4;
public static final int SLEEP_TIME=10000;
public static final String SCRIPTS_DIRECTORY="/home/chaitanya/";
public static boolean INTERNET_ON;

public void run()
{
	while(true)
	{
		if(!CheckInetConnection.testInternetConnection())
		{
			System.out.println("...No Internet!!");
			CheckInetConnection.ifNoInternet();
		}
		
		try{
			Thread.sleep(CheckInetConnection.SLEEP_TIME);
		}catch(InterruptedException e){e.printStackTrace();}
	}
}

public static void ifNoInternet()
{
	int i;
	
	if(!modemOn())
	{
		noInternetAlarm();
		return;
	}
	
	System.out.println("...Thank God, modem is on");
	
	for(i=0;i<CheckInetConnection.NO_OF_TRIES;i++)
	{
		try {
			Process p=Runtime.getRuntime().exec(CheckInetConnection.SCRIPTS_DIRECTORY+"noinet");
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(testInternetConnection())
		{
			System.out.println("...And we're back with Internet!");
			break;
		}
	}
	//if(i==CheckInetConnection.NO_OF_TRIES)
	//	noInternetAlarm();	
}

public static boolean modemOn()
{
	Process p=null;
	try{
		p=Runtime.getRuntime().exec(CheckInetConnection.SCRIPTS_DIRECTORY+"contest");
		p.waitFor();
	}catch(Exception e){e.printStackTrace();}
	
		if(p==null||p.exitValue()>=10)
			return false;
		else
			return true;
}

public static void noInternetAlarm()
{
	if(isAlarmOn())
		return;
	try {
		Runtime.getRuntime().exec(CheckInetConnection.SCRIPTS_DIRECTORY+"alarm");
	} catch (Exception e) {
		e.printStackTrace();
	}
}

public static boolean isAlarmOn()
{
	Process p=null;
	
	try {
		p = Runtime.getRuntime().exec("pgrep mpg321");
		p.waitFor();
	} catch (Exception e) {
		e.printStackTrace();
	}
	
	if(p.exitValue()==0)
		return true;
	return false;
}

public static void killAlarmIfNeeded()
{
	
}
/**
 * This function checks connection to two popular websites, and if both are down,
 * returns a false, else true.
 */
public static boolean testInternetConnection()
{
	System.out.println("test internet conn");
	try{
		new Socket("google.com",80);
	}
	catch(Exception e)
	{
		try{
			new Socket("yahoo.com",80);
		}catch(Exception ex){
			System.out.println("out test internet conn, no inet");
			CheckInetConnection.INTERNET_ON=false;
		return false;}
	}
	System.out.println("out test internet conn, yes inet");
	CheckInetConnection.INTERNET_ON=true;
	return true;
}
}
