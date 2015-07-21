import java.io.*;
import java.io.File;
import java.nio.file.Files;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.nio.file.StandardCopyOption.*;

public class IOManagement {
	String GITLETDIR = "/.gitlet";
	String STAGEDIR = "/stage";
	String COMMITDIR = "/commit";
	String METADIR  = "/meta";
	
	String mainDir; // Is the main directory which has .gitlet directory
	String currentDir;
	InputStream inStream = null;
	OutputStream outStream = null;
	
	public IOManagement(String mainDirectory){
		mainDir = mainDirectory;
		currentDir = System.getProperty("user.dir");

		File myfile = new File( mainDir + GITLETDIR );
		
		if (!myfile.exists()) {
			if (myfile.mkdir()) {
				System.out.println("Directory is created!");
				File myfile1 = new File(mainDir
						+ COMMITDIR); // should committed files be out side gitlet? NO
				myfile1.mkdir();
				File myfile2 = new File(mainDir+COMMITDIR);
				myfile2.mkdir();

				File myfile3 = new File(mainDir+STAGEDIR);
				myfile3.mkdir();

				File myfile4 = new File(mainDir+METADIR);
				myfile4.mkdir(); // put all the the Serialize objects here!


			} else {
				System.out
						.println("A gitlet version control system already exists in the current directory.");
			}
		}		
	}
	
	
	
	/*
	 * Saves the file given as fileName to the target directory, given as 
	 * COMMITDIR, STAGEDIR, or METADIR.
	 */
	
	public boolean save( String fileName, String targetDir){ 
		
		try{
			//Bruk:
			
			String s = fileName;
			while (s.contains("/")){
				s = s.substring(s.indexOf("/")+1, s.length());
			}
			
			
			File myFile = new File(currentDir+"/"+fileName);
			File myFileCopy = new File(currentDir+ GITLETDIR  + targetDir + "/" + s);



			inStream  = new FileInputStream(myFile);
			outStream = new FileOutputStream(myFileCopy);

			byte[] buffer = new byte[1024];

			int length;
			//copy the file content in bytes 
			while ((length = inStream.read(buffer)) > 0){

				outStream.write(buffer, 0, length);

			}

			inStream.close();
			outStream.close();

			System.out.println("File is copied successfully!");
			return true;
	    
	}catch(IOException e){
		e.printStackTrace();
		return false;
	}
		

	}
	
	/*
	 * 
	 * 
	 */
	public boolean serialize( Object obj, String fileName){
		try( ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(currentDir+METADIR+"/"+fileName)) )
	      {
	         
	         out.writeObject(obj);
	         System.out.printf("Serialized data is saved.");
	         return true;
	      }catch(IOException i)
	      {
	          i.printStackTrace();
	          return false;
	      }
	}
	
	public Object deserialize(String fileName){
		 try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(currentDir+METADIR+"/"+fileName)))
	      {
			
	         Object obj = in.readObject();
	         
	         
	         return obj;
	      }catch(IOException | ClassNotFoundException i)
	      {
	         i.printStackTrace();
			 return null;
	  
	      }
	     
	      
	}
}

