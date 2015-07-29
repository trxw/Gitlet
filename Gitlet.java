import java.io.File;
import java.util.*;
import java.util.Map.Entry;

public class Gitlet {
	// Veritable used to distinguish if we are in a conflicted state or not
	static boolean conflicted;

	// Veritable used to find out if we have deserialized or not
	static boolean deserialized;

	// keeps track of the name of the current Branch
	String CurrentBranch;

	// BS and OS are used to to convert from windows and Linux use
	String BS = "/";
	String OS = "/";

	// keeps track of the current commit object
	public Commit Head;

	// keeps track of split nodes for reference
	public ArrayList<Commit> SplitPoints;

	// array of all the files that have been marked for un-tracking since last
	// commit
	public ArrayList<String> markedForRM;

	// array of all file names to be added at next commit
	public ArrayList<String> markedForADD;

	// pointer for the class IOManagement that is used to save,copy and delete
	// files...
	private IOManagement io;
	/*
	 * MessageToID uses the commit Message as KEY and the commit ID as its VALUE
	 * it is used to keep track of the commit messages relative to to the commit
	 * ID it will be used to execute the log method
	 */
	public HashMap<String, ArrayList<String>> MessageToID;

	/*
	 * BranchToCommitObj uses the Branch-name (user supplied except for
	 * "master") as KEY and the commit object as its VALUE it will be used to
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
		io = new IOManagement();
		conflicted = false;
		deserialized = false;
	}

	/**
	 * inti is a method that creates the .gilet directory and initiates three
	 * others with in .gitlet called... (Stage,commit,Metta)
	 * 
	 * it also creates the first commit object. - if the .gitlet directory
	 * already exists init will not run!
	 */

	void init() {

		File myfile = new File(io.currentDir + io.GITLETDIR);
		if (!myfile.exists()) {
			if (myfile.mkdir()) {
				File myfile2 = new File(io.currentDir + io.GITLETDIR
						+ io.COMMITDIR);
				myfile2.mkdir();

				File myfile3 = new File(io.currentDir + io.GITLETDIR
						+ io.STAGEDIR);
				myfile3.mkdir();

				File myfile4 = new File(io.currentDir + io.GITLETDIR
						+ io.METADIR);
				myfile4.mkdir(); // put all the the Serialize objects here!

				String Message = "initial commit";
				CurrentBranch = "master";
				Commit firstCommit = new Commit(Message, "master");
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

			} else {
				System.out.println("failed to make DIR");
			}

		} else {

			System.out
					.println("A gitlet version control system already exists in the current directory.");

		}

	}

	/**
	 * this methods is used to serialize every object used to operate the Gitlet
	 * return true if the function was successful else returns false.
	 */
	boolean serialize() {
		try {

			this.io.serialize(this.MessageToID, "MessageToID");
			this.io.serialize(this.BranchToCommitObj, "BranchToCommitObj");
			this.io.serialize(this.IdToCommitObj, "IdToCommitObj");
			this.io.serialize(this.SplitPoints, "SplitPoints");
			this.io.serialize(this.CurrentBranch, "CurrentBranch");
			this.io.serialize(this.markedForRM, "markedForRM"+CurrentBranch);
			this.io.serialize(this.markedForADD, "markedForADD"+CurrentBranch);
			this.io.serialize(this.Head, "Head");
			this.io.serialize(conflicted, "conflicted");
			return true;
		} catch (IllegalStateException e) {
			return false;
		}

	}

	/**
	 * this method is used used to deserialized every object used to operate the
	 * functionality of Gitlet
	 * 
	 */
	@SuppressWarnings("unchecked")
	void Deserialize() {
		if (!deserialized) {
			MessageToID = (HashMap<String, ArrayList<String>>) this.io
					.deserialize("MessageToID");
			CurrentBranch = (String) this.io.deserialize("CurrentBranch");
			BranchToCommitObj = (HashMap<String, Commit>) this.io
					.deserialize("BranchToCommitObj");
			IdToCommitObj = (HashMap<String, Commit>) this.io
					.deserialize("IdToCommitObj");
			SplitPoints = (ArrayList<Commit>) this.io
					.deserialize("SplitPoints");
			markedForRM = (ArrayList<String>) this.io
					.deserialize("markedForRM"+CurrentBranch);
			markedForADD = (ArrayList<String>) this.io
					.deserialize("markedForADD"+CurrentBranch);
			Head = (Commit) this.io.deserialize("Head");
			
			conflicted = (boolean) io.deserialize("conflicted");
			deserialized = true;
		}
	}

	/**
	 * add is used to add files to the staging area from the working directory
	 * or if the file has been marked for un-tracking add just un-marks it.
	 * 
	 * @param sArr
	 *            is the name of the file to be added
	 */
	void add(String sArr[]) {
		Deserialize();
		String filename = sArr[0];
		String getToGitlet = io.currentDir;

		File myfile = new File(getToGitlet + BS + filename);

		if (!myfile.exists()) {
			System.out.println("File does not exist.");
		} else if (markedForRM != null) {
			if (markedForRM.contains(filename)) {
				markedForRM.remove(filename);
				System.out.println("we have unmarked RM marked file");
			} else {
				if (!markedForADD.contains(filename)) {
					markedForADD.add(filename);
				}
				io.save(getToGitlet + BS + filename, io.currentDir
						+ io.GITLETDIR + io.STAGEDIR + BS + filename);
				this.serialize();
			}
		} else {
			if (!markedForADD.contains(filename)) {
				markedForADD.add(filename);
			}
			io.save(getToGitlet + BS + filename, getToGitlet + io.STAGEDIR + BS
					+ filename);
			this.serialize();
		}

	}

	/**
	 * the commit method is used to commit files from the staging area to the
	 * commit directory and/or to remove files from tracing
	 * 
	 * - fails if there are no files to stage or un-track
	 * 
	 * @param sArr
	 *            is the message associated with the specific commit
	 */
	void commit(String sArr[]) {
		Deserialize();

		if (conflicted) {
			conflicted = false;
		}

		if (this.markedForADD.isEmpty() && this.markedForRM.isEmpty()) {
			System.out.println("No changes added to the commit.");
		} else {
			// head should be pointing at the current commit at all times...

			Commit newCommit = new Commit(sArr[0], Head, CurrentBranch);
			if (!this.markedForADD.isEmpty()) { // files to be committed
				for (String filetoadd : markedForADD) {
					newCommit.CommitFromStaging(filetoadd);
				}
				markedForADD.clear(); // remove all add names
										// files are removed in commit
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
			this.BranchToCommitObj.put(CurrentBranch, newCommit); // advance-the-pointer

			Head = newCommit;
			// check if all the staged files are cleared and if RM is emptied
			if (!this.markedForADD.isEmpty() || !this.markedForRM.isEmpty()) {
				System.out
						.println("there is still file name(s) in RM and/or ADD");
			}
		}
		this.serialize();
	}

	/**
	 * reset is used reset the working directory and the head to commit that has
	 * been made previously
	 * 
	 * - fails if the given branch name does not exist
	 * 
	 * @param sArr
	 *            it is the name of the branch that we want to reset to
	 */
	void reset(String[] sArr) {
		Deserialize();

		if (IdToCommitObj.containsKey(sArr[0])) {
			for (Entry<String, String> entry : IdToCommitObj.get(sArr[0]).fileToLocation
					.entrySet()) {
				String S = io.currentDir + io.GITLETDIR + io.COMMITDIR + BS
						+ entry.getValue() + BS + entry.getKey();
				io.save(S,
						io.currentDir + BS + entry.getValue() + BS
								+ entry.getKey());

			}
			Head = IdToCommitObj.get(sArr[0]);
			CurrentBranch = Head.myBranch;

		} else {
			System.out.println("No commit with that id exists.");
		}

		serialize();
	}

	/**
	 * rm is used to mark files for un-tracking of removing the from the staging
	 * area if called on a file after it has been added
	 * 
	 * - fails if the given files is not tracked by the previous commit && if
	 * the file was not added since the last commit
	 * 
	 * @param sArr
	 *            it is the name of the file to be un-tracked or un-added
	 */

	void rm(String sArr[]) {
		Deserialize();
		String fileName = sArr[0];
		if (markedForADD.contains(fileName)
				|| Head.fileToLocation.containsKey(fileName)) {
			
			if (markedForADD.contains(fileName)) {
				markedForADD.remove(fileName);
				String grabFromDir = io.currentDir + io.GITLETDIR + io.STAGEDIR;
				io.Delete(grabFromDir, fileName);
			} else {
				if (!markedForRM.contains(fileName)) {
					markedForRM.add(fileName);
				}
			}
		} else {
			System.out.println("No reason to remove the file.");
		}
		this.serialize();
	}

	/**
	 * log methods is used to display the sequence of commits from the current
	 * branch in oredr starting from the HEAD
	 * 
	 */
	void log() {
		Deserialize();
		Commit C = Head;
		while (C != null) {
			System.out.println("===");
			System.out.println("Commit " + C.ID);
			System.out.println(C.Time);
			System.out.println(C.Message);
			System.out.println();
			C = C.prevCommit;
		}

	}

	/*
	 * this method is used to dislpay information on all the commit that have
	 * ever been made since init has been called.
	 */

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

	/**
	 * this method is used to find all the commit-IDs with the given commit
	 * message
	 * 
	 * - fails if the given commit message was never associated with and commits
	 * ID
	 * 
	 * @param sArr
	 *            is the commit message we are trying to find an ID for
	 */

	void find(String sArr[]) {
		Deserialize();
		if (!MessageToID.containsKey(sArr[0])) {
			System.out.println("Found no commit with that message.");
		} else {
			for (String S : MessageToID.get(sArr[0])) {
				System.out.println(S);
			}
		}
	}

	/**
	 * this method displays a list branch name, staged files and untracked file
	 * names...
	 */
	void status() {
		Deserialize();
		System.out.println("=== Branches ===");
		for (Entry<String, Commit> entry : BranchToCommitObj.entrySet()) {
			if (entry.getKey().equals(CurrentBranch)) {
				System.out.println("*" + entry.getKey());
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
		System.out.println();
	}

	/**
	 * this method creates a branch this the given name and points it at the
	 * current commit
	 * 
	 * - fails if a branch with the same name already exists...
	 * 
	 * @param sArr
	 *            it is the name of the branch to b created
	 */
	void branch(String sArr[]) {

		Deserialize();

		if (!BranchToCommitObj.containsKey(sArr[0])) {
			BranchToCommitObj.put(sArr[0], Head);

			this.SplitPoints.add(Head);
			Head.isSplitPoint= true;

		} else {
			System.out.println("A branch with that name already exists.");
		}
		this.serialize();
	}

	/**
	 * this method is used to delete a given branch (i.e. remove from hash map)
	 * 
	 * - fails a branch with the given name does not exist or if the name is the
	 * the same as the current branch
	 * 
	 * @param sArr
	 *            is the name of the branch to be removed
	 */
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

	/**
	 * this method is to do one of these functions depending on the argument
	 * sArr... - to check out a branch by putting all its files in the current
	 * directory and making it the current commit - to check out the file with
	 * the given name form the current commit - to check out the file with the
	 * given name form the given commit (via. commit ID)
	 * 
	 * - fails if the given files name or branch name don't or commit object
	 * does not exits
	 * 
	 * @param sArr
	 *            the input can be three different things depending - the name
	 *            of a branch - the name of a file in the current branch - the
	 *            ID o a commit and a file name in that commit
	 */
	void checkout(String sArr[]) {
		Deserialize();
		String grabFrom = io.currentDir + io.GITLETDIR + io.COMMITDIR;
		String putIn = io.currentDir;

		if (sArr.length == 1) {
			if (BranchToCommitObj.containsKey(sArr[0])) {
				if (sArr[0].equals(CurrentBranch)) {
					System.out
							.println("No need to checkout the current branch.");
				} else {
					for (Entry<String, String> entry : BranchToCommitObj
							.get(sArr[0]).fileToLocation.entrySet()) {
						io.save(grabFrom + BS + entry.getValue() + BS
								+ entry.getKey(), putIn + BS + entry.getKey());
					}
					Head = BranchToCommitObj.get(sArr[0]);
					CurrentBranch = sArr[0];
				}
			} else {
				if (!Head.fileToLocation.containsKey(sArr[0])) { // it-is-neither
					System.out
							.println("File does not exist in the most recent commit, or no such branch exists.");
				} else {
					io.save(grabFrom + BS + Head.fileToLocation.get(sArr[0])
							+ BS + sArr[0], putIn + BS + sArr[0]);
				}
			}

		} else if (sArr.length == 2) {

			if (!IdToCommitObj.containsKey(sArr[0])) {
				System.out.println("No commit with that id exists.");
			} else if (!IdToCommitObj.get(sArr[0]).fileToLocation
					.containsKey(sArr[1])) {
				System.out.println("File does not exist in that commit.");
			} else {
				io.save(grabFrom
						+ BS
						+ IdToCommitObj.get(sArr[0]).fileToLocation
								.get(sArr[1]) + BS + sArr[1], putIn + BS
						+ sArr[1]);
			}
		}

		this.serialize();

	}

	/**
	 * this method merges the given merge with the current branch
	 * 
	 * - fails if the given branch is does not exist or if the given branch is
	 * the same as the current commit
	 * 
	 * @param sArr
	 *            the name of the branch to be merged with the current branch
	 */
	void merge(String sArr[]) {
		Deserialize();

		if (!BranchToCommitObj.containsKey(sArr[0])) {
			System.out.println("A branch with that name does not exist.");
		} else if (BranchToCommitObj.get(sArr[0]).ID.equals(Head.ID)) {
			System.out.println("Cannot merge a branch with itself.");
		} else {
			// Obtain the union of set of files associated with the Head and
			// last commit of given branch
			Set<String> allFiles = new HashSet<String>();
			// String[] checkoutSArr;
			allFiles.addAll(BranchToCommitObj.get(sArr[0]).fileToLocation
					.keySet());
			allFiles.addAll(Head.fileToLocation.keySet());
			Commit splitPoint = getSplitPoint(BranchToCommitObj.get(sArr[0]),
					Head);
			System.out.println("Splitpoint"+splitPoint.ID);
			// Loop through all the files:
			for (String fileName : allFiles) {
				System.out.println(fileName);
				if (!compare(fileName, BranchToCommitObj.get(sArr[0]),
						splitPoint) && compare(fileName, Head, splitPoint)) {
					if (!markedForRM.contains(fileName)) {
						io.save(io.currentDir + io.GITLETDIR + io.COMMITDIR
								+ BS+ BranchToCommitObj.get(sArr[0]).fileToLocation.get(fileName)
								+ BS+ fileName, io.currentDir + fileName
								+ ".conflicted");
						// Add the file to staging directory
						String[] fileNameArr = { fileName };
						add(fileNameArr);
					} else if (markedForADD.contains(fileName)) {
						markedForADD.remove(fileName);
					}

				} else if (!compare(fileName, BranchToCommitObj.get(sArr[0]),
						splitPoint) && !compare(fileName, Head, splitPoint)) {
					conflicted = true;
					// copy the file from given branch after adding ".confliced"
					// to file name.
					io.save(io.currentDir + io.GITLETDIR + io.COMMITDIR
							+ BS+ BranchToCommitObj.get(sArr[0]).fileToLocation.get(fileName)
							+ BS+ fileName, io.currentDir + fileName
							+ ".conflicted");
					String[] fileNameArr = { fileName + ".conflicted" };
					add(fileNameArr);
				}
			}

			if (!conflicted) {
				String[] mergeMessage = { "Merged " + CurrentBranch + " with "
						+ sArr[0] };
				// Make a new commit
				commit(mergeMessage);
			} else {
				System.out.println("Encountered a merge conflict.");
			}

		}

		this.serialize();

	}

	/**
	 * takes in two commit objects and find the split point of the two branches
	 * - Assumes that there is a split point between the two branches...
	 * 
	 * @param currentCommit
	 *            Current branch in the merge/rebase Method
	 * @param givenCommit
	 *            Current branch in the merge/rebase Method
	 * @return thE Commit object @ the split-point of the two given branches OR
	 *         Null if there is no split-point...
	 */
	public Commit getSplitPoint(Commit currentCommit, Commit givenCommit) {
		Commit C = currentCommit;
		Commit G = givenCommit;
		ArrayList<Commit> Arr = new ArrayList<Commit>();
		int Count = 0;
		boolean V = false;
		while (C != null && Count < SplitPoints.size()) {
			for(Commit C1: SplitPoints){
				if (C1.ID.equals(C.ID))
					Arr.add(C1);
				System.out.println(Arr.add(C1));
				Count++;
			}
			C = C.prevCommit;
		}
		Count = 0;
		while (G != null) {
			for (Commit A : Arr) {
				if (A.ID.equals(G.ID)) {
					return G;
				}
			}
			G = G.prevCommit;
		}
		return null;
	}

	/**
	 * Compare fileName location in commitObj with splitCommitObj,
	 * 
	 * @param fileName
	 *            name of the file to be compared.
	 * @param commitObj
	 *            commit object at the lead of a branch
	 * @param splitCommitObj
	 *            the split-point Commit object to the given branch and another
	 *            branch...
	 * @return return true if they are the same, false if they are different.
	 *         Return false if the fileName does not exist in either commitObj
	 *         or in splitCommitObj.
	 */
	public boolean compare(String fileName, Commit commitObj,
			Commit splitCommitObj) {

		if (commitObj.fileToLocation.containsKey(fileName)
				&& splitCommitObj.fileToLocation.containsKey(fileName)) {
			return commitObj.fileToLocation.get(fileName).equals(
					splitCommitObj.fileToLocation.get(fileName));
		} else {
			return false;
		}
	}

	/**
	 * this method copies the current branch from the split-point (from the
	 * given branch) to the head and places in-front of the given branch.
	 * 
	 * - fails if the given branch does not exist or the given branch is the
	 * same as the current branch or if the given branch is in the history of
	 * the current branch
	 * 
	 * @param sArr
	 *            is the name of the branch we want to attach the current branch
	 *            to...
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void rebase(String sArr[]) {
		Deserialize();
		Commit givenCommit = BranchToCommitObj.get(sArr[0]);
		Commit currentCommit = Head;
		if (!BranchToCommitObj.containsKey(sArr[0])) {
			System.out.println("A branch with that name does not exist.");

		} else if (givenCommit.ID.equals(currentCommit.ID)) {
			System.out.println("Cannot rebase a branch onto itself.");

		} else if (isInHistory(givenCommit, currentCommit)) {
			System.out.println("Already up-to-date.");

		} else if (isInHistory(currentCommit, givenCommit)) { // Special case
			BranchToCommitObj.put(currentCommit.myBranch, givenCommit);
			Head = givenCommit;
		} else {
			
			Commit Splitpoint = getSplitPoint(currentCommit, givenCommit);
			
			int countTillSplit = countTillSplit(Splitpoint, currentCommit);
			int n = countTillSplit;
			Commit oldCommit = givenCommit;
			Commit similarCommit = currentCommit;
			for (int i = 0; i < countTillSplit; i++) {
				for (int j = 0; j < n-1; j++) {
					// gives us the the current brunch to be copied!
					similarCommit = similarCommit.prevCommit;
				}

				Commit newCommit = new Commit(similarCommit.Message, oldCommit,
						CurrentBranch);
				newCommit.fileToLocation = (HashMap) similarCommit.fileToLocation
						.clone();

				ArrayList<String> allfileNames = new ArrayList<String>();
				for (Entry<String, String> entry : newCommit.fileToLocation
						.entrySet()) {
					String fileName = entry.getKey();
					allfileNames.add(fileName);

					if (Splitpoint.fileToLocation.containsKey(fileName)) {
						if (compare(fileName, newCommit, Splitpoint)) {
							if (oldCommit.fileToLocation.containsKey(fileName)) {
								newCommit.fileToLocation.put(fileName,
										oldCommit.fileToLocation.get(fileName));
							}
						}
					}
				}

				for (Entry<String, String> entry : oldCommit.fileToLocation
						.entrySet()) {
					if (!allfileNames.contains(entry.getKey())) {
						newCommit.fileToLocation.put(entry.getKey(),
								entry.getValue());
					}
				}
				oldCommit = newCommit; // update for the next loop
				n--;
				BranchToCommitObj.put(currentCommit.myBranch, newCommit);
				Head = newCommit;
			}
		}
		Commit C = Head;
		String S = Head.myBranch;
		while (C != null) {
			C.myBranch = S;
			C = C.prevCommit;
		}
		serialize();
	}

	/**
	 * finds out if the given commit object is in the branch of the current
	 * Head.
	 * 
	 * @param givenCommit
	 *            the commit object we want to check if it is in the Branch lead
	 *            by the given branchLead
	 * @param branchLead
	 *            the commit object that is a lead to a branch
	 * @return return true if given commit object is in the branch of the
	 *         current Head else returns false
	 */
	boolean isInHistory(Commit givenCommit, Commit branchLead) {
		Commit C = branchLead;
		while (C != null) {
			if (C.ID.equals(givenCommit.ID)) {
				return true;	
			}
			C=C.prevCommit;
		}
		return false;
	}

	/**
	 * given the split-point commit an a branchLead commit it returns the number
	 * of commit nodes before we reach the lead
	 * 
	 * @param Splitpoint
	 *            split-point to the given branchLead branch and another branch
	 * @param branchLead
	 *            a lead to a branch with a split
	 * @return returns the number of commit from the branchLead to the
	 *         split-point commit
	 */

	int countTillSplit(Commit Splitpoint, Commit branchLead) {
		int count = 0;
		while (!branchLead.ID.equals(Splitpoint.ID)){
			count++;
			branchLead = branchLead.prevCommit;
		}

		return count;
	}

	/**
	 * this method acts like the brain of the package and calls methods as
	 * appropriate - fails if the methods does not exist or the method is
	 * restricted since the program is a conflicted state...
	 * 
	 * @param args
	 *            the name of the operations supported by this package and the
	 *            appropriate arguments it takes as an ARRay of Strings
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		Gitlet G = new Gitlet();
		String[] arr = { "init", "add", "commit", "rm", "log", "global-log",
				"find", "status", "checkout", "branch", "rm-branch", "reset",
				"merge", "rebase" };
		ArrayList<String> commandList = new ArrayList<String>();
		for (String e : arr) {
			commandList.add(e);
		}

		if (!conflicted) {
			int length = args.length;
			// System.out.println(args[1].toString());
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
				} else if (args[0].equals("reset")) {
					G.reset(Arrays.copyOfRange(args, 1, length));
				} else if (args[0].equals("rm")) {
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

		} else {
			int length = args.length;
			// System.out.println(args[1].toString());
			if (length == 0) {
				System.out.println("Please enter a command.");
			} else if (length == 1) {
				if (args[0].equals("log")) {
					G.log();
				} else if (args[0].equals("global-log")) {
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

				else if (commandList.contains(args[0])) {
					System.out
							.println("Cannot do this command until the merge conflict has been resolved.");
				} else {
					System.out.println("No command with that name exists.");
				}

			}

		}
	}
}