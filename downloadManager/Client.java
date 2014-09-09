//client side creator of socket and only forwards the commands from terminal to the program
package downloadManager;

import java.net.*;
import java.io.*;

public class Client implements Runnable{

private Socket socket;
private BufferedReader cliReader,streamReader;
private BufferedWriter streamWriter;

public Client()
{
	try {
		Thread.sleep(1000);
		socket=new Socket("127.0.0.1",23600);
	
		streamReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
		streamWriter=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		
	} catch (Exception e) {
		e.printStackTrace();
	}
	cliReader=new BufferedReader(new InputStreamReader(System.in));
}
public void run()
{
	String temp;
	String[] lines;
	
	System.out.print("\n>");
	
	try {
		while(!(temp=cliReader.readLine().trim()).equals("exit"))
		{
			//System.out.println("client: read the line  "+temp+"adfa");
			streamWriter.write(temp+"\n");
			streamWriter.flush();
			lines=streamReader.readLine().split("~");
			
			for(String i:lines)
				System.out.println(i);

			if(temp.equals("q"))
				break;
			
			System.out.print("\n>");
		}
	} catch (NullPointerException e) {
		System.out.println("looks like you're using cron ( no System.in to read from )");
	}
	catch (IOException e){
		e.printStackTrace();
	}
	finally{
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
}
