import java.util.*;
import java.io.*;

public class Commit implements Serializable {
	// commit must track its parents files not copy them...
	// if the file is

	HashMap<String, String> fileToLocation;
	boolean isSplitPoint; // true if it is a split point else false
	String ID; // contains the Id of the commit object
	String Message;
	String Time; // string that keeps track of the commit time for this node
	Commit prevCommit;
	public String myBranch;  // keeps track of what branch this commit is on // should be updated during merge

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Commit(String Mess, Commit parentCommit , String branchname) {
		fileToLocation = new HashMap<String, String>();
		Date time = new Date();
		// display time and date using toString()
		Time = time.toString();
		ID = UUID.randomUUID().toString();
		Message = Mess;
		prevCommit = parentCommit;
		myBranch= branchname;
		try {
			fileToLocation = (HashMap) parentCommit.clone();
		} catch (CloneNotSupportedException e) {
			System.out.println("can't clone the hash map at the time;");
			e.printStackTrace();
		}
	}

	/*  
	 * the method will add staged files to commit file and delete the file from the staging area
	 * 
	 * fileToLocation is automatically set same as its parents it is updated if
	 * the stage file updated the file if no file exists by that name in the
	 * parent we add to the kid I don't need
	 * 
	 * No Need to check if the parent has the file or not since put would replace it
	 */
	public void CommitFromStaging(String fileName) {
		IOManagement io = new IOManagement(System.getProperty("user.dir"));

		fileToLocation.put(fileName, fileName + ID);

		// input is weird in order to deal with "/" in-front of filename in Save
		// method in IO

		io.save(io.STAGEDIR.substring(1, io.STAGEDIR.length()) + "/" + fileName,
				io.COMMITDIR);

		// remove the file from the staging area after putting a copy in the
		// commit DIR

		io.Delete(io.STAGEDIR.substring(1, io.STAGEDIR.length()) + "/"
				+ fileName);

	}
	
// un-track all files tracked by the parent and marked for RM
	public void CommitRM(String fileName) {
		if (fileToLocation.containsKey(fileName)){  // if tracked by the parent un-track
			// file is not removed from commit file just from the tracking by the new commit
			fileToLocation.remove(fileName);  
			
		}
       // if not tracked by the parent ignore it...
	}

}
