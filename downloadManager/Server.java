package downloadManager;

import java.net.*;
import java.io.*;
import java.util.*;

public class Server implements Runnable{

private Socket socket;
private BufferedReader reader;
private BufferedWriter writer;
private Vector<Download> downloadVector;

public Server(Socket socket,Vector<Download>downloadVector)
{
	this.socket=socket;
	this.downloadVector=downloadVector;
}

public void run()
{

	String temp=null;
	String commands[];
		
	try{
		reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));		
		writer=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		temp=reader.readLine();
	}catch(Exception e){e.printStackTrace();}
		
	while(temp!=null)
	{
		try{
			
		temp=temp.trim();
		if(temp.equals("q"))
			break;
		//System.out.println("server: read the line"+temp);
		commands=temp.split(" ");
		
		if(commands[0].equals("sa"))
		{
			writer.write(getStatusOfAll()+"\n");
			writer.flush();
		}
		
		else if (commands[0].equals("s"))
		{
			writer.write(getStatus(Integer.parseInt(commands[1]))+"\n");
			writer.flush();
		}
		
		else if(commands[0].equals("p"))
		{
			writer.write(pause(Integer.parseInt(commands[1]))+"\n");
			writer.flush();
		}
		
		else if (commands[0].equals("r"))
		{
			writer.write(resume(Integer.parseInt(commands[1]))+"\n");
			writer.flush();
		}
		
		else if (commands[0].equals("a"))
		{
			writer.write(abort(Integer.parseInt(commands[1]))+"\n");
			writer.flush();
		}
		
		else if(commands[0].equals("h"))
		{
			writer.write(help()+"\n");
			writer.flush();
		}
		
		else
		{
			writer.write("No such command ~"+help()+"\n");
			writer.flush();
		}

		}catch(Exception e){
			try{ writer.write("Improper input\n");writer.flush();}catch(IOException ex){ex.printStackTrace();}
		}
		
		try {
			temp=reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	try{
	writer.write("closing client's connection~");
	
	if(temp!=null)
	{
		for(Download i:downloadVector)
			i.abort();
		writer.write("quitting download manager\n");
		writer.flush();
		socket.close();
		System.exit(0);
	}
	
	else
	{
		writer.write("\n");
		writer.flush();
		socket.close();
	}
	
	}catch(Exception e){e.printStackTrace();}	
}

private String getStatusOfAll()
{
	String result="";
	
	if(downloadVector.size()==0)
		return "no downloads";
	
	for(Download i:downloadVector)
		result+=i.toString(Download.TILDE)+"~";
	
	return result;
}

private String getStatus(int no)
{
	Download download;
	
	try{
		download=downloadVector.get(no);
	}
		catch(ArrayIndexOutOfBoundsException e){return "no such download";}
	
	return download.toString(Download.TILDE);
}
private String pause(int no)
{
	Download download;
	
	try{
		download=downloadVector.get(no);
	}
		catch(ArrayIndexOutOfBoundsException e){return "no such download";}
		
	if(download.getStatus()!=Download.Status.DOWNLOADING)
		return "the download is presently not downloading";
	
	download.pause();
	return "paused download";
}

private String resume(int no)
{
	Download download;
	
	try{
		download=downloadVector.get(no);
	}
		catch(ArrayIndexOutOfBoundsException e){return "no such download";}
	
	if(download.getStatus()==Download.Status.PAUSED)
	{
		download.resume();
		return "resumed download";
	}
	
	return "the download is not paused";
}

private String abort(int no)
{
	Download download;
	
	try{
		download=downloadVector.get(no);
	}
		catch(ArrayIndexOutOfBoundsException e){return "no such download";}
	
	if(download.getStatus()==Download.Status.DOWNLOADING||download.getStatus()==Download.Status.PAUSED)
	{
		download.abort();
		return "aborted the download";
	}
	
	return "download cannot be aborted";
}

private String help()
{
	String temp="";
	
	temp+="Help Menu: All Commands:::~";
	temp+="sa: status of all downlaods~";
	temp+="p<download number>: pause the selected download~";
	temp+="s<download number>: status of selected download~";
	temp+="r<download number>: resume the selected download~";
	temp+="a<download number>: abort the selected download~";
	temp+="h: display this help~";
	temp+="q: quit the downloader~~";
	
	return temp;
}
}
