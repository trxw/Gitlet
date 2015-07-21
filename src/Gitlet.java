import java.util.*;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

public class Gitlet {
	public static Commit Head; // keeps track of the most recent checked out
								// branch
	public static ArrayList<Commit> SplitPoints; // keeps track of split nodes for reference
	public static ArrayList<String> markedForRM; // array of all the files that have been marked
	                                                // for un-tracking since last commit
	public static ArrayList<String> markedForADD;  // array of all file names to be added at next commit
	
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
	}

	static void init() {
		File myfile = new File(System.getProperty("user.dir") + "/.gitlet");
		if (!myfile.exists()) {
			if (myfile.mkdir()) {
				System.out.println("Directory is created!");
				File myfile1 = new File(System.getProperty("user.dir")
						+ "/.gitlet/CommitFiles"); // should committed files be out side gitlet?
				myfile1.mkdir();
				File myfile2 = new File(System.getProperty("user.dir")
						+ "/.gitlet/Staging");
				myfile2.mkdir();

				File myfile3 = new File(System.getProperty("user.dir")
						+ "/.gitlet/Staging");
				myfile3.mkdir();

				File myfile4 = new File(System.getProperty("user.dir")
						+ "/.gitlet/Meta");
				myfile4.mkdir(); // put all the the Serialize objects here!

				Gitlet G = new Gitlet();
				Commit firstCommit = new Commit();
				firstCommit.Message = "initial commit.";
				Head = firstCommit;
				G.MessageToID.put(firstCommit.Message, firstCommit.ID);
				G.BranchToCommitObj.put("Master", firstCommit);
				G.IdToCommitObj.put(firstCommit.ID, firstCommit);

				// we need to Serialize after all methods is executed

			} else {
				System.out
						.println("A gitlet version control system already exists in the current directory.");
			}
		}
	}

	static void add(String sArr[]) {
		// deSerialize
		/*
		 * If the file had been marked for untracking (more on this in the
		 * description of the rm command), then add just unmarks the file, and
		 * does not also add it to the staging area.
		 */
		File myfile= new File(System.getProperty("user.dir")+ sArr[0]);
		if (!myfile.exists()){ // if file does not exist
			System.out.println("File does not exist.");
		}else if ( markedForRM.contains(sArr[0])){        
			markedForADD.remove(sArr[0]);
			// remove from the staging area... (no )
			
			
		}else{
			markedForADD.add(sArr[0]);
			// add to staging area... (not sure! )
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