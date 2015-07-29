import java.io.*;
import java.nio.file.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import static java.nio.file.StandardCopyOption.*;

public class IOManagement implements Serializable {
	// BS and OS are used to to convert from windows and Linux use
	String BS = "/";
	String OS = "/";
	// this is used the refer to .gitlet directory from the working
	// directory
	String GITLETDIR = BS + ".gitlet";
	// used the refer to stage directory from the .gitlet directory
	String STAGEDIR = BS + "stage";
	// used the refer to commit directory from the .gitlet directory
	String COMMITDIR = BS + "commit";
	// used the refer to meta directory from the .gitlet directory
	String METADIR = BS + "meta";
	// this is reference to the working directory
	String currentDir;
	// Initiations of inStream and outStream use in the methods below;
	InputStream inStream = null;
	OutputStream outStream = null;

	public IOManagement() {
		currentDir = System.getProperty("user.dir");
	}

	/**
	 * Saves the file given as fileName to the target directory, given as
	 * COMMITDIR, STAGEDIR, or METADIR from the given current location
	 * 
	 * @param fromwithfilename
	 *            location of the file to be copied
	 * @param targetDirwithname
	 *            Location of the files to be copied
	 * @return Returns true if copying was else returns false
	 */
	public boolean save(String fromwithfilename, String targetDirwithname) {

		try {
			File myFile = new File(fromwithfilename);
			File myFileCopy;
			myFileCopy = new File(targetDirwithname);
			myFileCopy.getParentFile().mkdirs();
			inStream = new FileInputStream(myFile);
			outStream = new FileOutputStream(myFileCopy);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inStream.read(buffer)) > 0) {
				outStream.write(buffer, 0, length);
			}
			inStream.close();
			outStream.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Deletes the file @ the given location and the given name by calling
	 * recursiveDelete
	 * 
	 * @param locationWithName
	 *            Location of the file to be deleted
	 * @param S
	 *            Name of file to be deleted
	 */

	public void Delete(String locationWithName, String S) {

		if (S.contains(BS)) {
			S = S.substring(0, S.indexOf(BS));
		}
		File myFile = new File(locationWithName + BS + S);
		try {
			recursiveDelete(myFile);
		} catch (IOException e) {

		}
		myFile.delete();
	}

	/**
	 * given the handle to a file, it deletes the file... -fails if the method
	 * throws an IOExceoption
	 * 
	 * @param d
	 *            the handle too the file to be deleted
	 * @throws IOException
	 */
	void recursiveDelete(File d) throws IOException {
		if (d.isDirectory()) {
			for (File f : d.listFiles()) {
				recursiveDelete(f);
			}
		}
		if (!d.delete()) {
			throw new IOException("Failed to delete file " + d.getPath());
		}
	}

	/**
	 * this method given an object to serialize an the name of the object it
	 * serializes and save the the file in the meta directory - fails if the
	 * method throws an IOExceoption
	 *
	 * @param obj
	 *            is the object to be serialized
	 * @param fileName
	 *            the name of the object to be serialized
	 */
	public void serialize(Object obj, String fileName) {
		try (ObjectOutputStream out = new ObjectOutputStream(
				new FileOutputStream(currentDir + GITLETDIR + METADIR + OS
						+ fileName + ".ser"))) {
			out.writeObject(obj);
		} catch (IOException i) {
			i.printStackTrace();
			System.out.println("Serialization Fail!");
			throw new IllegalStateException();

		}
	}

	/**
	 * this method deserialized the file with given file name from the metta
	 * directory - fails if the method throws an IOExceoption
	 * 
	 * @param fileName
	 *            name of the file to be deserialized
	 * @return returns the object that was deserialized
	 */

	public Object deserialize(String fileName) {
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				currentDir + GITLETDIR + METADIR + OS + fileName + ".ser"))) {

			Object obj = in.readObject();

			return obj;
		} catch (IOException | ClassNotFoundException i) {
			i.printStackTrace();
			return null;

		}

	}
}
