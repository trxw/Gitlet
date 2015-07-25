package Gitlet;

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
	String BS = "\\";
	String OS = "\\";
	String GITLETDIR =  BS +".gitlet";
	String STAGEDIR =  BS +"stage";
	String COMMITDIR =  BS +"commit";
	String METADIR =  BS +"meta";

	String mainDir; // Is the main directory which has .gitlet directory
	String currentDir;
	InputStream inStream = null;
	OutputStream outStream = null;

	public IOManagement() {
		currentDir = System.getProperty("user.dir");
	}

	/*
	 * Saves the file given as fileName to the target directory, given as
	 * COMMITDIR, STAGEDIR, or METADIR.
	 */

	public boolean save(String fromwithfilename, String targetDirwithname) {

		try {
			File myFile = new File(fromwithfilename);
			File myFileCopy;	
			myFileCopy = new File(targetDirwithname );
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

	public boolean Delete(String locationWithName) {
		File myFile = new File(locationWithName);
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
				new FileOutputStream(currentDir +GITLETDIR+ METADIR+OS+fileName+".ser"))) {

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
				new FileInputStream( currentDir +GITLETDIR+ METADIR+OS+fileName+".ser"))) {

			Object obj = in.readObject();

			return obj;
		} catch (IOException | ClassNotFoundException i) {
			i.printStackTrace();
			return null;

		}

	}
}
