import java.util.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import static java.nio.file.StandardCopyOption.*;

public class Gitlet {

	public Commit Head; // keeps track of the most recent checked out
						// branch
	public ArrayList<Commit> SplitPoints; // keeps track of split nodes for
											// reference
	public ArrayList<String> markedForRM; // array of all the files that have
											// been marked
											// for un-tracking since last commit
	public ArrayList<String> markedForADD; // array of all file names to be
											// added at next commit
	private IOManagement io;
	/*
	 * MessageToID uses the commit Message as KEY and the commit ID as its VALUE
	 * it is used to keep track of the commit messages relative to to the commit
	 * ID it will be used to execute the log method
	 */
	public HashMap<String, ArrayList<String>> MessageToID;

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

		MessageToID = new HashMap<String, ArrayList<String>>();
		BranchToCommitObj = new HashMap<String, Commit>();
		IdToCommitObj = new HashMap<String, Commit>();
		SplitPoints = new ArrayList<Commit>();
		Head = null;
		io = new IOManagement(System.getProperty("user.dir"));

	}

	void init() {
		String Message = "initial commit.";
		Commit firstCommit = new Commit(Message, null, "Master");
		this.Head = firstCommit;
		
		// the Message is mapped to an ArrayList
		if(MessageToID.containsKey(Message)){
			ArrayList<String> arr=MessageToID.get(Message);
			arr.add(firstCommit.ID);
			this.MessageToID.put(firstCommit.Message, arr);
		}else{
			ArrayList<String> arr=new ArrayList<String>();
			arr.add(firstCommit.ID);
			this.MessageToID.put(firstCommit.Message, arr);
		}
		this.BranchToCommitObj.put(firstCommit.myBranch, firstCommit);
		this.IdToCommitObj.put(firstCommit.ID, firstCommit);

		// we need to Serialize after all methods is executed
		this.serialize();

	}

	boolean serialize() {
		try {
			this.io.serialize(this.MessageToID, "MessageToID");
			this.io.serialize(this.BranchToCommitObj, "BranchToCommitObj");
			this.io.serialize(this.IdToCommitObj, "IdToCommitObj");
			this.io.serialize(this.SplitPoints, "SplitPoints");
			this.io.serialize(this.markedForRM, "markedForRM");
			this.io.serialize(this.markedForADD, "markedForADD");
			this.io.serialize(this.Head, "Head");
			return true;
		} catch (IllegalStateException e) {
			return false;
		}

	}

	void add(String sArr[]) {

		/*
		 * If the file had been marked for untracking (more on this in the
		 * description of the rm command), then add just unmarks the file, and
		 * also adds it to the staging area.
		 */

		File myfile = new File(System.getProperty("user.dir") + "/" + sArr[0]);
		if (!myfile.exists()) { // if file does not exist
			System.out.println("File does not exist.");
		} else if (markedForRM.contains(sArr[0])) { // HOW TO HANDLE MANY MARKED
													// FILES HERE
			markedForRM.remove(sArr[0]);
			// remove from the staging area... (no )
		} else {
			// this is done so we can only save the file name only (i.e. not
			// including the folder)
			// it also consistent with the way we save the files in the staging
			// area...

			// same files with different paths are considered different...
			// piazza
			markedForADD.add(sArr[0]);
			io.save(sArr[0], io.STAGEDIR);
			this.serialize();
		}

	}

	void commit(String sArr[]) {
		// this.io.save(sArr[], targetDir)
		if (this.markedForADD.isEmpty() && this.markedForRM.isEmpty()) {
			System.out.println("No changes added to the commit.");
		} else {
			// head should be pointing at the current commit at all times...

			Commit newCommit = new Commit(sArr[0], Head, Head.myBranch);
			if (!this.markedForADD.isEmpty()) { // files to be committed
				for (String filetoadd : markedForADD) {
					newCommit.CommitFromStaging(filetoadd);
				}
			}
			if (!this.markedForRM.isEmpty()) {
				for (String filetoRM : markedForADD) {
					newCommit.CommitRM(filetoRM);
				}
			}
			// the Message is mapped to an ArrayList
			if(MessageToID.containsKey(sArr[0])){
				ArrayList<String> arr=MessageToID.get(sArr[0]);
				arr.add(newCommit.ID);
				this.MessageToID.put(newCommit.Message, arr);
			}else{
				ArrayList<String> arr=new ArrayList<String>();
				arr.add(newCommit.ID);
				this.MessageToID.put(newCommit.Message, arr);
			}
			this.IdToCommitObj.put(newCommit.ID, newCommit);
			this.BranchToCommitObj.put(Head.myBranch, newCommit); // advance the
																	// pointer
																	// // what
																	// ever it
																	// is!

			Head = newCommit;
			// check if all the staged files are cleared and if RM is emptied
			if (!this.markedForADD.isEmpty() && !this.markedForRM.isEmpty()) {
				System.out
						.println("there is still file name(s) in RM and/or ADD");
			}
		}
		this.serialize();
	}

	void rm(String sArr[]) {
		String s = sArr[0];
		if (markedForADD.contains(s) || Head.fileToLocation.containsKey(s)) {
			// the file has been added since last commit...

			if (markedForADD.contains(s)) {
				markedForADD.remove(s);
				io.Delete(io.STAGEDIR.substring(1, io.STAGEDIR.length()) + "/"
						+ sArr);
			} else {
				markedForRM.add(s);

			}
		} else { // this means the file was not staged nor tracked by the parent
					// node(i.e. Head)
			System.out.println("No reason to remove the file.");
		}
		this.serialize();
	}

	void log() {
		Commit C = Head;
		while (C != null) {
			System.out.println("===");
			System.out.println("Commit ID:-" + C.ID);
			System.out.println("Time:-" + C.Time);
			System.out.println("Commit Message" + C.Message);
			System.out.println();
			C = C.prevCommit;
		}

	}

	void global_log() {

		for (Entry<String, Commit> entry : IdToCommitObj.entrySet()) {
			System.out.println("===");
			System.out.println("Commit ID:-" + entry.getValue().ID);
			System.out.println("Time:-" + entry.getValue().Time);
			System.out.println("Commit Message" + entry.getValue().Message);
			System.out.println();

		}
	}

	void find(String sArr[]) {
		// if different commits have the same message our hash system breaks
		// down
		if (!MessageToID.containsKey(sArr[0])) {
			System.out.println("Found no commit with that message.");
		}else{
			for(String S: MessageToID.get(sArr[0])){ // get all ID and Print them out one by one....
				System.out.println(S);	
			}
		}
	}

	void status() {
		System.out.println("=== Branches ===");
		for (Entry<String, Commit> entry : BranchToCommitObj.entrySet()) {
			if (entry.getKey() == Head.myBranch) {
				System.out.println("*" + entry.getKey()); // * on current
															// branch!
			} else {
				System.out.println(entry.getKey());
			}
		}
		System.out.println();
		System.out.println("=== Staged Files ===");
		for (String S : markedForADD) {
			System.out.println(S);
		}
		System.out.println();
		System.out.println("=== Files Marked for Untracking ===");
		for (String S : markedForRM) {
			System.out.println(S);
		}
	}

	void branch(String sArr[]) {
		if (!BranchToCommitObj.containsKey(sArr[0])) {
			BranchToCommitObj.put(sArr[0], Head);
		} else {
			System.out.println("A branch with that name already exists.");
		}
		this.serialize();
	}

	void rmbranch(String sArr[]) {
		if (!BranchToCommitObj.containsKey(sArr[0])) {
			System.out.println("A branch with that name does not exist.");
		} else if (BranchToCommitObj.get(sArr[0]).equals(Head)) {
			System.out.println("Cannot remove the current branch.");
		} else {
			BranchToCommitObj.remove(sArr[0]);
		}
		this.serialize();
	}

	void checkout(String sArr[]) {
		if (sArr.length == 1) { // it means that sArr is [Branch name] or [file
								// name]
			if (BranchToCommitObj.containsKey(sArr[0])) { // it must be a [Branch]

				if (BranchToCommitObj.get(sArr).equals(Head)) {
					System.out
							.println("No need to checkout the current branch.");
				} else {
					// entry.getValue() is the location of the files (i.e. the
					// branch commit file location)
					for (Entry<String, String> entry : BranchToCommitObj
							.get(sArr).fileToLocation.entrySet()) {
						// loop to place all files in to the current
						// directory...
						io.save(entry.getValue() + "/" + sArr[0], "");
					}
					Head= BranchToCommitObj.get(sArr);
					
				}
			} else { // sArr is [Branch name] or [Files name]
				if (!Head.fileToLocation.containsKey(sArr[0])) {  // it is neither
					System.out
							.println("File does not exist in the most recent commit, or no such branch exists.");
				} else {
					// grab the file from the commit Dir. to the current Dir.
					io.save(Head.fileToLocation.get(sArr[0]) + "/" + sArr[0],
							"");
				}
			}

		} else {		
			// sArr is [Commit ID] [Files name]
			if (!IdToCommitObj.containsKey(sArr[0])){
				System.out.println("No commit with that id exists.");
			}else if(!IdToCommitObj.get(sArr[0]).fileToLocation.containsKey(sArr[1])){
				System.out.println("File does not exist in that commit.");
			}else{ // file exists in the Commit dir. 
				io.save(IdToCommitObj.get(sArr[0]).fileToLocation.get(sArr[1])+ "/" + sArr[1], "");
			}
			
		}

		this.serialize();

	}

	void merge(String sArr[]) {

		this.serialize();

	}

	void rebase(String sArr[]) {

		this.serialize();
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		Gitlet G = new Gitlet();
		// Deserialize
		G.MessageToID = (HashMap<String, ArrayList<String>>) G.io
				.deserialize("MessageToID");
		G.BranchToCommitObj = (HashMap<String, Commit>) G.io
				.deserialize("BranchToCommitObj");
		G.IdToCommitObj = (HashMap<String, Commit>) G.io
				.deserialize("IdToCommitObj");
		G.SplitPoints = (ArrayList<Commit>) G.io.deserialize("SplitPoints");
		G.markedForRM = (ArrayList<String>) G.io.deserialize("markedForRM");
		G.markedForADD = (ArrayList<String>) G.io.deserialize("markedForADD");
		G.Head = (Commit) G.io.deserialize("Head");

		int length = args.length;

		if (length == 0) {
			System.out.println("Please enter a command.");
		} else if (length == 1) {
			if (args[0].equals("init")) {
				G.init();
			} else if (args[0].equals("log")) {
				G.log();
			}

			else if (args[0].equals("global-log")) {
				G.global_log();
			} else if (args[0].equals("status")) {
				G.status();
			} else if (args[0].equals("commit")) {
				System.out.println("Please enter a commit message.");
			}

		}

		else if (length > 1) {
			if (args[0].equals("add")) {
				G.add(Arrays.copyOfRange(args, 1, length));

			}

			else if (args[0].equals("commit")) {
				G.commit(Arrays.copyOfRange(args, 1, length));
			}

			else if (args[0].equals("rm")) {
				G.rm(Arrays.copyOfRange(args, 1, length));
			}

			else if (args[0].equals("find")) {
				G.find(Arrays.copyOfRange(args, 1, length));
			}

			else if (args[0].equals("checkout")) {
				G.checkout(Arrays.copyOfRange(args, 1, length));
			}

			else if (args[0].equals("branch")) {
				G.branch(Arrays.copyOfRange(args, 1, length));
			} else if (args[0].equals("rm-branch")) {
				G.rmbranch(Arrays.copyOfRange(args, 1, length));
			}

			else if (args[0].equals("merge")) {
				G.merge(Arrays.copyOfRange(args, 1, length));
			}

			else if (args[0].equals("rebase")) {
				G.rebase(Arrays.copyOfRange(args, 1, length));
			}

		}

		else
			System.out.println("No command with that name exists.");
	}

}