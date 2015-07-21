import java.util.*;
import java.io.*;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import static java.nio.file.StandardCopyOption.*;

public class Gitlet {
	public Commit Head; // keeps track of the most recent checked out
								// branch
	public ArrayList<Commit> SplitPoints; // keeps track of split nodes for reference
	public ArrayList<String> markedForRM; // array of all the files that have been marked
	                                                // for un-tracking since last commit
	public ArrayList<String> markedForADD;  // array of all file names to be added at next commit
	private IOManagement io;
	/*
	 * MessageToID uses the commit Message as KEY and the commit ID as its VALUE
	 * it is used to keep track of the commit messages relative to to the commit
	 * ID it will be used to execute the log method
	 */
	public HashMap<String,String> MessageToID;

	/*
	 * BranchToCommitObj uses the Branch-name (user supplied except for
	 * "Master") as KEY and the commit object as its VALUE it will be used to
	 * keep rack of separate branch heads Nodes...
	 */
	public HashMap<String, Commit> BranchToCommitObj;

	/*
	 * IdToCommitObj uses the commit ID as KEY and the commit object as its
	 * VALUE it will be used to keep track of all the Commits via the IDs'..
	 */
	public HashMap<String, Commit> IdToCommitObj;

	public Gitlet() {
		MessageToID = new HashMap<String, String>();
		BranchToCommitObj = new HashMap<String, Commit>();
		IdToCommitObj = new HashMap<String, Commit>();
		SplitPoints = new ArrayList<Commit>();
		Head = null;
		io = new IOManagement(System.getProperty("user.dir"));
	}

	static void init() {
		
		Gitlet G = new Gitlet();
		Commit firstCommit = new Commit();
		firstCommit.Message = "initial commit.";
		G.Head = firstCommit;
		G.MessageToID.put(firstCommit.Message, firstCommit.ID);
		G.BranchToCommitObj.put("Master", firstCommit);
		G.IdToCommitObj.put(firstCommit.ID, firstCommit);
         
		// we need to Serialize after all methods is executed
	}

	void add(String sArr[]) {
		// deSerialize
		/*
		 * If the file had been marked for untracking (more on this in the
		 * description of the rm command), then add just unmarks the file, and
		 * also adds it to the staging area.
		 */

		
		
		File myfile= new File(System.getProperty("user.dir")+ sArr[0]);
			if (!myfile.exists()){ // if file does not exist
				System.out.println("File does not exist.");
			}else if ( markedForRM.contains(sArr[0]) ){        //HOW TO HANDLE MANY MARKED FILES HERE
				markedForRM.remove(sArr[0]);
				// remove from the staging area... (no )
			}else{
				markedForADD.add(sArr[0]);	
				io.save(sArr[0], io.STAGEDIR);
			}
		
	}

	static void commit(String sArr[]) {

	}

	static void find(String sArr[]) {

	}

	static void rm(String sArr[]) {

	}

	static void log() {

	}

	static void global_log() {

	}

	static void status() {

	}

	static void branch(String sArr[]) {

	}

	static void checkout(String sArr[]) {

	}

	static void merge(String sArr[]) {

	}

	static void rebase(String sArr[]) {

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		
		int length = args.length;

		if (length == 0) {
			System.out.println("Please enter a command.");
		} else if (length == 1) {
			if (args[0].equals("init")) {
				init();
			} else if (args[0].equals("log")) {
				log();
			}

			else if (args[0].equals("global-log")) {
				global_log();
			} else if (args[0].equals("status")) {
				status();
			}

		}

		else if (length > 1) {
			if (args[0].equals("add")) {
				add(Arrays.copyOfRange(args, 1, length));

			}

			else if (args[0].equals("commit")) {
				commit(Arrays.copyOfRange(args, 1, length));
			}

			else if (args[0].equals("rm")) {
				rm(Arrays.copyOfRange(args, 1, length));
			}

			else if (args[0].equals("find")) {
				find(Arrays.copyOfRange(args, 1, length));
			}

			else if (args[0].equals("checkout")) {
				checkout(Arrays.copyOfRange(args, 1, length));
			}

			else if (args[0].equals("branch")) {
				branch(Arrays.copyOfRange(args, 1, length));
			}

			else if (args[0].equals("merge")) {
				merge(Arrays.copyOfRange(args, 1, length));
			}

			else if (args[0].equals("rebase")) {
				rebase(Arrays.copyOfRange(args, 1, length));
			}

		}

		else
			System.out.println("No command with that name exists.");
	}

}