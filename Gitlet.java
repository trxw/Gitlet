import java.util.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import static java.nio.file.StandardCopyOption.*;

public class Gitlet {
	String BRUKISTHESHIT ="/";
	String OMIDISTHESHIT = "/";
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
		markedForRM = new ArrayList<String>();
		markedForADD = new ArrayList<String>();
		io = new IOManagement(System.getProperty("user.dir"));
		String BRUKISTHESHIT = "/";

	}

	void init() {

		File myfile = new File(io.mainDir + io.GITLETDIR);
		if (!myfile.exists()) {
			if (myfile.mkdir()) {

				//System.out.println("Directory is created!");
				// File myfile1 = new File(io.mainDir + io.GITLETDIR
				// + io.COMMITDIR);
				// myfile1.mkdir();
				File myfile2 = new File(io.mainDir + io.GITLETDIR
						+ io.COMMITDIR);
				myfile2.mkdir();

				File myfile3 = new File(io.mainDir + io.GITLETDIR + io.STAGEDIR);
				myfile3.mkdir();

				File myfile4 = new File(io.mainDir + io.GITLETDIR + io.METADIR);
				myfile4.mkdir(); // put all the the Serialize objects here!

				String Message = "initial commit";
				Commit firstCommit = new Commit(Message, "Master");
				this.Head = firstCommit;

				// the Message is mapped to an ArrayList
				if (MessageToID.containsKey(Message)) {
					ArrayList<String> arr = MessageToID.get(Message);
					arr.add(firstCommit.ID);
					this.MessageToID.put(firstCommit.Message, arr);
				} else {
					ArrayList<String> arr = new ArrayList<String>();
					arr.add(firstCommit.ID);
					this.MessageToID.put(firstCommit.Message, arr);
				}
				this.BranchToCommitObj.put(firstCommit.myBranch, firstCommit);
				this.IdToCommitObj.put(firstCommit.ID, firstCommit);

				// we need to Serialize after all methods is executed
				this.serialize();

			}
		} else {
			System.out
					.println("A gitlet version control system already exists in the current directory.");

		}

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

	@SuppressWarnings("unchecked")
	void Deserialize() {
		MessageToID = (HashMap<String, ArrayList<String>>) this.io
				.deserialize("MessageToID");
		BranchToCommitObj = (HashMap<String, Commit>) this.io
				.deserialize("BranchToCommitObj");
		IdToCommitObj = (HashMap<String, Commit>) this.io
				.deserialize("IdToCommitObj");
		SplitPoints = (ArrayList<Commit>) this.io.deserialize("SplitPoints");
		markedForRM = (ArrayList<String>) this.io.deserialize("markedForRM");
		markedForADD = (ArrayList<String>) this.io.deserialize("markedForADD");
		Head = (Commit) this.io.deserialize("Head");
	}

	void add(String sArr[]) {
		Deserialize();
		/*
		 * If the file had been marked for untracking (more on this in the
		 * description of the rm command), then add just unmarks the file, and
		 * also adds it to the staging area.
		 */

		File myfile = new File(System.getProperty("user.dir") + BRUKISTHESHIT + sArr[0]);
		if (!myfile.exists()) { // if file does not exist
			System.out.println("File does not exist.");
		} else if (markedForRM != null) {
			if (markedForRM.contains(sArr[0])) {
				markedForRM.remove(sArr[0]);
				System.out.println("we have unmarked RM marked file");
				// remove from the staging area... (no )
			} else {
				if (!markedForADD.contains(sArr[0])) {
					markedForADD.add(sArr[0]);
				}
				io.save(sArr[0], io.STAGEDIR, 1);
				this.serialize();
			}

		} else {
			// same files with different paths are considered different...//
			// piazza

			markedForADD.add(sArr[0]);
			io.save(sArr[0], io.STAGEDIR, 1);
			this.serialize();
		}

	}

	void commit(String sArr[]) {
		Deserialize();
		// this.io.save(sArr[], targetDir)
		if (this.markedForADD.isEmpty() && this.markedForRM.isEmpty()) {
			System.out.println("No changes added to the commit.");
		} else {
			// head should be pointing at the current commit at all times...

			Commit newCommit = new Commit(sArr[0], Head, Head.myBranch);
			System.out.println(markedForADD.size());

			if (!this.markedForADD.isEmpty()) { // files to be committed
				for (String filetoadd : markedForADD) {
					newCommit.CommitFromStaging(filetoadd);
				}
				markedForADD.clear(); // remove all add names
										// files are removed from
			}
			if (!this.markedForRM.isEmpty()) {
				for (String filetoRM : markedForRM) {
					newCommit.CommitRM(filetoRM);
				}
				markedForRM.clear(); // remove all marking for next RM
			}
			// the Message is mapped to an ArrayList
			if (MessageToID.containsKey(sArr[0])) {
				ArrayList<String> arr = MessageToID.get(sArr[0]);
				arr.add(newCommit.ID);
				this.MessageToID.put(newCommit.Message, arr);
			} else {
				ArrayList<String> arr = new ArrayList<String>();
				arr.add(newCommit.ID);
				this.MessageToID.put(newCommit.Message, arr);
			}
			this.IdToCommitObj.put(newCommit.ID, newCommit);
			this.BranchToCommitObj.put(Head.myBranch, newCommit); // advance-the-pointer
			Head = newCommit;
			// check if all the staged files are cleared and if RM is emptied
			if (!this.markedForADD.isEmpty() || !this.markedForRM.isEmpty()) {
				System.out
				.println("there is still file name(s) in RM and/or ADD");
			}
		}
		this.serialize();
	}

	void rm(String sArr[]) {
		Deserialize();
		String s = sArr[0];
		if (markedForADD.contains(s) || Head.fileToLocation.containsKey(s)) {
			// the file has been added since last commit...
			System.out.println(markedForADD.toString());
			if (markedForADD.contains(s)) {
				markedForADD.remove(s);
				io.Delete(OMIDISTHESHIT+".gitlet"+OMIDISTHESHIT
						+ io.STAGEDIR + BRUKISTHESHIT
						+ sArr[0]);
			} else {
				if (!markedForRM.contains(s)) {
					markedForRM.add(s);
				}
			}
		} else { // this means the file was not staged nor tracked by the parent
					// node(i.e. Head)
			System.out.println("No reason to remove the file.");
		}
		this.serialize();
	}

	void log() {
		Deserialize();
		Commit C = Head;
		while (C != null) {
			System.out.println("===");
			System.out.println("Commit " + C.ID);
			System.out.println( C.Time);
			System.out.println(C.Message);
			System.out.println();
			C = C.prevCommit;
		}

	}

	void global_log() {
		Deserialize();
		for (Entry<String, Commit> entry : IdToCommitObj.entrySet()) {
			System.out.println("===");
			System.out.println("Commit " + entry.getValue().ID);
			System.out.println(entry.getValue().Time);
			System.out.println(entry.getValue().Message);
			System.out.println();

		}
	}

	void find(String sArr[]) {
		Deserialize();
		// if different commits have the same message our hash system breaks
		// down
		if (!MessageToID.containsKey(sArr[0])) {
			System.out.println("Found no commit with that message.");
		} else {
			for (String S : MessageToID.get(sArr[0])) { // get all ID and Print
														// them out one by
														// one....
				System.out.println(S);
			}
		}
	}

	void status() {
		Deserialize();
		System.out.println("=== Branches ===");
		for (Entry<String, Commit> entry : BranchToCommitObj.entrySet()) {
			System.out.println(entry.getKey());
			System.out.println(Head.myBranch);
			if (entry.getKey().equals(Head.myBranch)) {
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

		Deserialize();
		if (!BranchToCommitObj.containsKey(sArr[0])) {
			BranchToCommitObj.put(sArr[0], Head);
		} else {
			System.out.println("A branch with that name already exists.");
		}
		this.serialize();
	}
	
	void rmbranch(String sArr[]) {
		Deserialize();
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
		Deserialize();
		if (sArr.length == 1) { // it means that sArr[0] is [Branch name] or [file
								// name]
			if (BranchToCommitObj.containsKey(sArr[0])) { // it must be a
															// [Branch]

				if (BranchToCommitObj.get(sArr[0]).equals(Head)){
					System.out.println("No need to checkout the current branch.");
				} else {
					// entry.getValue() is the location of the files (i.e. the
					// branch commit file location)
					for (Entry<String, String> entry : BranchToCommitObj
							.get(sArr[0]).fileToLocation.entrySet()) {
						// loop to place all files in to the current
						// directory...
						io.save(entry.getValue() + BRUKISTHESHIT + sArr[0], "", 1);
					}
					Head = BranchToCommitObj.get(sArr[0]);

				}
			} else { // sArr[0] is [Branch name or [Files name]
				if (!Head.fileToLocation.containsKey(sArr[0])) { // it is
																	// neither
					System.out
							.println("File does not exist in the most recent commit, or no such branch exists.");
				} else {
					// grab the file from the commit Dir. to the current Dir.
					//System.out.println("This is not a problem:"+Head.fileToLocation.get(sArr[0]));
					io.save(Head.fileToLocation.get(sArr[0]) + sArr[0],
							"", 1);
				}
			}

		} else {
			// sArr is [Commit ID] [Files name]
			if (!IdToCommitObj.containsKey(sArr[0])) {
				System.out.println("No commit with that id exists.");
			} else if (!IdToCommitObj.get(sArr[0]).fileToLocation
					.containsKey(sArr[1])) {
				System.out.println("File does not exist in that commit.");
			} else { // file exists in the Commit dir.
				io.save(IdToCommitObj.get(sArr[0]).fileToLocation.get(sArr[1])
						+ "/" + sArr[1], "", 1);
			}

		}

		this.serialize();

	}

	void merge(String sArr[]) {
		Deserialize();

		this.serialize();

	}

	void rebase(String sArr[]) {
		Deserialize();

		this.serialize();
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		// System.out.println(args[0]);
		// System.out.println(args[1]);
		Gitlet G = new Gitlet();

		int length = args.length;

		if (length == 0) {
			System.out.println("Please enter a command.");
		} else if (length == 1) {
			if ((args[0].equals("init"))) {
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
			} else {
				System.out.println("No command with that name exists.");
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
			} else {
				System.out.println("No command with that name exists.");
			}

		}

		else
			System.out.println("No command with that name exists.");
	}

}