import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;


public class Commit implements Serializable {
	// BS and OS are used to to convert from windows and Linux use
	String OS = "/";
	String BS = "/";
	/**
	 * hash map that keeps track of the fileName as key and the location of
	 * where the files is save as the value tracked by this commit
	 */
	HashMap<String, String> fileToLocation;

	// true if it is a split point else false
	boolean isSplitPoint;

	// contains the Id of the commit object
	String ID;
	// contains the the Commit message
	String Message;
	// string that keeps track of the commit time for this commit node
	String Time;
	// a commit reference the the previous commit
	Commit prevCommit;
	// keeps track of what branch this commit is on //
	// should be updated during merge
	public String myBranch;

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
		// Time = time.toString();
		ID = UUID.randomUUID().toString();
		Message = Mess;
		prevCommit = parentCommit;
		myBranch = branchname;
		fileToLocation = (HashMap) parentCommit.fileToLocation.clone();
	}

	/**
	 * the method will add staged files to commit file and delete the file from
	 * the staging area
	 * 
	 * @param fileName
	 *            this is the file name to be added to the commit directory and
	 *            deleted from the staging area
	 */
	public void CommitFromStaging(String fileName) {

		IOManagement io = new IOManagement();
		String targetDir = io.currentDir + io.GITLETDIR + io.COMMITDIR + OS
				+ ID + OS + fileName;
		String grabFromDir = io.currentDir + io.GITLETDIR + io.STAGEDIR + OS
				+ fileName;
		fileToLocation.put(fileName, ID);
		File myCommitDir = new File(io.currentDir + io.GITLETDIR + io.COMMITDIR
				+ OS + ID);
		myCommitDir.mkdir();
		io.save(grabFromDir, targetDir);
		io.Delete(io.currentDir + io.GITLETDIR + io.STAGEDIR, fileName);
	}

	/**
	 * un-track all files tracked by the parent and marked for RM
	 * 
	 * @param fileName
	 *            name of files that this commit stops tracking
	 */
	public void CommitRM(String fileName) {
		if (fileToLocation.containsKey(fileName)) {
			fileToLocation.remove(fileName);
		}
	}
}
