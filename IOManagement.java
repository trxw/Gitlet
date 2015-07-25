import java.io.*;
import java.nio.file.Files;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import static java.nio.file.StandardCopyOption.*;

public class IOManagement implements Serializable {
	String BRUKISTHESHIT = "/";
	String OMIDISTHESHIT = "/";
	String GITLETDIR =  BRUKISTHESHIT +".gitlet";
	String STAGEDIR =  BRUKISTHESHIT +"stage";
	String COMMITDIR =  BRUKISTHESHIT +"commit";
	String METADIR =  BRUKISTHESHIT +"meta";

	String mainDir; // Is the main directory which has .gitlet directory
	String currentDir;
	InputStream inStream = null;
	OutputStream outStream = null;

	public IOManagement(String mainDirectory) {
		mainDir = mainDirectory;
		currentDir = System.getProperty("user.dir");

	}

	/*
	 * Saves the file given as fileName to the target directory, given as
	 * COMMITDIR, STAGEDIR, or METADIR.
	 */

	public boolean save(String fileName, String targetDir, int t) {

		try {
			 String s = fileName;
			 if (t==2){
			 while (s.contains( BRUKISTHESHIT )){
			s = s.substring(s.indexOf( BRUKISTHESHIT )+1, s.length());
			 }
			 }
			System.out.println("This is "+currentDir+ GITLETDIR +  BRUKISTHESHIT  + fileName);
			System.out.println( currentDir);
			System.out.println("filename"+ fileName);
			System.out.println( GITLETDIR );
			File myFile = new File(currentDir+ GITLETDIR +  BRUKISTHESHIT  + fileName);
			File myFileCopy;
			
			// got rid of GITLETDIR because we can assume the calls are made
			// from with in the gitlet folder
			
			if(targetDir!=""){
				 myFileCopy = new File(currentDir + GITLETDIR + targetDir+  BRUKISTHESHIT + s );}
			else{
				 myFileCopy = new File(currentDir + targetDir+  BRUKISTHESHIT + s );
			}
			myFileCopy.getParentFile().mkdirs();
			
			inStream = new FileInputStream(myFile);
			outStream = new FileOutputStream(myFileCopy);
			

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = inStream.read(buffer)) > 0) {

				outStream.write(buffer, 0, length);
			}
			inStream.close();
			outStream.close();

			//System.out.println("File is copied successfully!");
			return true;

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean Delete(String fileName) {
		File myFile = new File(currentDir + OMIDISTHESHIT + fileName);
		if (!(myFile.isDirectory())) {
			myFile.delete();
			return true;
		} else {
			return false;
		}
	}

	/*
	 * 
	 * 
	 */
	public void serialize(Object obj, String fileName) {
		try (ObjectOutputStream out = new ObjectOutputStream(
				new FileOutputStream(currentDir +GITLETDIR+ METADIR+OMIDISTHESHIT+fileName+".ser"))) {

			out.writeObject(obj);
//			System.out.println("Serialized data is saved.");

		} catch (IOException i) {
			i.printStackTrace();
			System.out.println("Serialization Fail!");
			throw new IllegalStateException();

		}
	}

	public Object deserialize(String fileName) {
		try (ObjectInputStream in = new ObjectInputStream(
				new FileInputStream( currentDir +GITLETDIR+ METADIR+OMIDISTHESHIT+fileName+".ser"))) {

			Object obj = in.readObject();

			return obj;
		} catch (IOException | ClassNotFoundException i) {
			i.printStackTrace();
			return null;

		}

	}
}
