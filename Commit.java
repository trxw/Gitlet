import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

public class Commit implements Serializable {
	// commit must track its parents files not copy them...
	// if the file is
	String OMIDISTHESHIT = "/";
	String BRUKISTHESHIT = "/";

	HashMap<String, String> fileToLocation;
	boolean isSplitPoint; // true if it is a split point else false
	String ID; // contains the Id of the commit object
	String Message;
	String Time; // string that keeps track of the commit time for this node
	Commit prevCommit;
	public String myBranch; // keeps track of what branch this commit is on //
							// should be updated during merge

	public Commit(String Mess, String branchname) {
		fileToLocation = new HashMap<String, String>();
		Date time = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Time = sdf.format(time);

		ID = UUID.randomUUID().toString();
		Message = Mess;
		prevCommit = null;
		myBranch = branchname;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Commit(String Mess, Commit parentCommit, String branchname) {
		fileToLocation = new HashMap<String, String>();
		Date time = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Time = sdf.format(time);

		// display time and date using toString()
		//Time = time.toString();
		ID = UUID.randomUUID().toString();
		Message = Mess;
		prevCommit = parentCommit;
		myBranch = branchname;
		fileToLocation = (HashMap) parentCommit.fileToLocation.clone();
	}

	/*
	 * the method will add staged files to commit file and delete the file from
	 * the staging area
	 * 
	 * fileToLocation is automatically set same as its parents it is updated if
	 * the stage file updated the file if no file exists by that name in the
	 * parent we add to the kid I don't need
	 * 
	 * No Need to check if the parent has the file or not since put would
	 * replace it
	 */
	public void CommitFromStaging(String fileName) {
		IOManagement io = new IOManagement(System.getProperty("user.dir"));
		// location relative to the current Directory
		String targetDir = io.COMMITDIR + OMIDISTHESHIT + ID;

		fileToLocation.put(fileName, targetDir.substring(1, targetDir.length()));

		// input is weird in order to deal with "/" in-front of filename in Save
		// method in IO

		// got rid of GITLETDIR because we can assume the calls are made from
		// with in the gitlet folder
		File myCommitDir = new File(io.mainDir +io.GITLETDIR + targetDir);
		myCommitDir.mkdir();

		io.save("stage" + OMIDISTHESHIT + fileName,
				targetDir,2);

		// remove the file from the staging area after putting a copy in the
		// commit DIR

		io.Delete(OMIDISTHESHIT+".gitlet"+io.STAGEDIR + BRUKISTHESHIT
				+ fileName);
	}

	// un-track all files tracked by the parent and marked for RM
	public void CommitRM(String fileName) {
		if (fileToLocation.containsKey(fileName)) { // if tracked by the parent
													// un-track
			// file is not removed from commit file just from the tracking by
			// the new commit
			fileToLocation.remove(fileName);

		}
		// if not tracked by the parent ignore it...
	}

}
