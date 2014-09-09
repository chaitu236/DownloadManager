package downloadManager;

import java.io.*;
import java.util.*;
import java.net.*;

public class FileParser {
	
private File fileName;
private RandomAccessFile in;
private Vector<String> lineEndPointers;

public FileParser(File fileName)
{
	
	this.fileName=fileName;
	lineEndPointers=new Vector<String>();
	
	try{
	in=new RandomAccessFile(this.fileName,"rw");
	}
	catch(Exception e){e.printStackTrace();}
	
}

private String getNextLine()
{		
	String temp=null;
	boolean lineFlag=true;
	
	try{		
		
		if(in.getFilePointer()==in.length())
		{
			//System.out.println("\tfp:gnl: EOF reached");
			return null;
		}
		
	while(lineFlag)
	{
		lineEndPointers.add(Long.toString(in.getFilePointer()));
		temp=in.readLine();
		lineFlag=false;
		
		if(temp==null)
		{
			lineEndPointers.removeElementAt(lineEndPointers.size()-1);
			return null;
		}
		
		if(temp.startsWith("#"))
		{
			lineFlag=true;
			lineEndPointers.removeElementAt(lineEndPointers.size()-1);
		}
	}	
		
	}catch(Exception e){e.printStackTrace();}
	
	return temp;
}

public URL getNextURL()
{
	String tempString=getNextLine();
	
	if(tempString==null)
		return null;
	else
		tempString=tempString.trim();
	
	System.out.println("here  "+tempString);
	
	if(tempString.startsWith("y "))
	{
		tempString=tempString.substring(2);
		return getYoutubeURL(tempString);
	}
	
	if(tempString.startsWith("n "))
	{
		tempString=tempString.substring(2);
		return getUrlFromString(tempString);
	}
	// The following options are the default ones, and are not recommended
	if(tempString.contains("www.youtube."))
	{
		System.out.println(tempString+"  yes, its a youtube url");
		return getYoutubeURL(tempString);
	}
	
	return getUrlFromString(tempString);
}

private URL getUrlFromString(String name)
{
	URL url=null;
	try {
		url=new URL(name);
	} catch (MalformedURLException e) {
		e.printStackTrace();
		return null;
	}
	return url;
}

//line numbers start from 0. i.e., 0,1,2,3.......
public void deleteLine(int lineNo)
{
	try{
		
	long pos=in.getFilePointer();
	in.seek(Long.parseLong(lineEndPointers.get(lineNo)));	
	in.writeBytes("#D");
	
	//lineEndPointers.remove(lineNo);
	in.seek(pos);
	
	}catch(Exception e){e.printStackTrace();}
}

private URL getYoutubeURL(String line)
{
	URL url=null;
	String data="";
	
	try{ 
		url=new URL(line.replaceFirst("www.youtube", "get2pc")); 
	}catch(MalformedURLException e){
		e.printStackTrace();
		System.out.println("main:findurl: malformed url 2");}
	
	System.out.println("url is  "+url);
	try{
		BufferedReader input=new BufferedReader(new InputStreamReader(url.openStream()));
	
	String temp="";
	while((temp=input.readLine())!=null)
		{
			data+=temp;
		}
	data=data.split("The download link is located below")[1];
	data=data.split("<a href=\"")[1];
	data=data.split("\">")[0];
	}catch(IOException e){System.out.println("main:findUrl: io exception");}
	
	try{ url=new URL(data); }catch(MalformedURLException e){
		e.printStackTrace();
		System.out.println("main:findURL: malformed url");}
	
	return url;
}

public void closeFile()
{
	try {
		in.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
}

/*public static void main(String args[])
{
	FileParser fp=new FileParser(new File("/home/chaitanya/Desktop/temp"));
	String temp;
	
	while((temp=fp.getNextLine())!=null)
		System.out.println("line    "+temp);
	
	for(int i=0;i<fp.lineEndPointers.size();i++)
		System.out.println("line number "+fp.lineEndPointers.get(i));
	
	for(int i=0;i<fp.lineEndPointers.size();i++)
	{
		try{fp.in.seek(Long.parseLong(fp.lineEndPointers.get(i)));
		fp.in.writeBytes("aaaa");
		System.out.println("filepos  "+fp.in.getFilePointer());
		System.out.println(fp.in.readLine());}catch(Exception e){e.printStackTrace();}
	}
	//System.out.println(fp.getYoutubeURL("http://www.youtube.com/watch?v=bciLUE_S-JE&feature=fvhl"));
	fp.deleteLine(3);
}*/

}


