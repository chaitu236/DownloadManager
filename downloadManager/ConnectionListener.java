package downloadManager;

import java.net.*;
import java.io.*;
import java.util.*;

public class ConnectionListener {

private ServerSocket serverSocket;
private Vector<Download> downloadVector;
private final String defaultFile="/home/chaitanya/Desktop/myUrls";
private boolean anotherInstance;
private String fileName;

public static void main(String[] args)
{
	ConnectionListener conListener=new ConnectionListener();
	
	if(!conListener.anotherInstance)
	{
		Thread checkInetConnectionThread=new Thread(new CheckInetConnection());
		checkInetConnectionThread.start();
		conListener.waitForInet();
	}
	
	if(args.length!=0)
		conListener.fileName=args[0];
	else
		conListener.fileName=conListener.defaultFile;	
	
	conListener.run();
}

public void waitForInet()
{
	System.out.println("waiting for internet");
	
	while(!CheckInetConnection.testInternetConnection())
	{
		try{
			Thread.sleep(10000);
		}catch(Exception e){e.printStackTrace();}
	}
}

public ConnectionListener()
{
	try{
		serverSocket=new ServerSocket(23600);
		}
	catch(Exception e){
			anotherInstance=true;
			System.out.println("DMan already running");
			return;
		}
	System.out.println("no other DMan instance");
}

public void run()
{	
		Client client=new Client();
		Thread thread=new Thread(client);
		thread.setDaemon(false);
		System.out.println("starting client");
		thread.start();
		
	if(!anotherInstance)
	{
		
		DownloadSpanner downloadSpanner=new DownloadSpanner(new File(fileName));
		this.downloadVector=downloadSpanner.getDownloadVector();
		
		Thread th=new Thread(downloadSpanner);
		th.setDaemon(true);
		th.start();
		System.out.println("Download spanner started");
		
	while(true)
	{
		Socket socket=null;
		
		try{
		System.out.println("conlistener: waiting for a client to connect");
		socket=serverSocket.accept();
		System.out.println("conlistener: found a client");
		}catch(Exception ex){ex.printStackTrace();}
		
		Thread thr=new Thread(new Server(socket,downloadVector));
		thr.start();
		System.out.println("started server");
	}
	}
	
}
}
