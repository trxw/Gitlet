import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

public class GitletTest1 {

	/**
	 * Class that provides JUnit tests for Gitlet, as well as a couple of
	 * utility methods. Some code adapted from Joseph Moghadam:
	 */
	static String BS = "/";
	private static final String GITLET_DIR = ".gitlet" + BS;
	private static final String TESTING_DIR = "test_files" + BS;

	/* matches either unix/mac or windows line separators */
	private static final String LINE_SEPARATOR = "\r\n|[\r\n]";

	@Before
	public void setUp() {
		File f = new File(GITLET_DIR);
		if (f.exists()) {
			try {
				recursiveDelete(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		f = new File(TESTING_DIR);
		if (f.exists()) {
			try {
				recursiveDelete(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		f.mkdirs();
	}

	/**
	 * Calls a gitlet command using the terminal.
	 * 
	 * Warning: Gitlet will not print out anything _while_ it runs through this
	 * command, though it will print out things at the end of this command. It
	 * will also return this as a string.
	 * 
	 * The '...' syntax allows you to pass in an arbitrary number of String
	 * arguments, which are packaged into a String[].
	 */
	private static String gitlet(String... args) {

		String[] commandLineArgs = new String[args.length + 2];
		commandLineArgs[0] = "java";
		commandLineArgs[1] = "Gitlet";
		for (int i = 0; i < args.length; i++) {
			commandLineArgs[i + 2] = args[i];
		}
		String results = command(commandLineArgs);
		System.out.println(results);
		return results.trim();
	}

	/**
	 * Another convenience method for calling Gitlet's main. This calls Gitlet's
	 * main directly, rather than through the terminal. This is slightly
	 * cheating the concept of end-to-end tests. But, this allows you to
	 * actually use the debugger during the tests, which you might find helpful.
	 * It's also a lot faster.
	 * 
	 * Warning: Like the other version of this method, Gitlet will not print out
	 * anything _while_ it runs through this command, though it will print out
	 * things at the end of this command. It will also return what it prints as
	 * a string.
	 */
	private static String gitletFast(String... args) {
		PrintStream originalOut = System.out;
		ByteArrayOutputStream printingResults = new ByteArrayOutputStream();
		try {
			/*
			 * Below we change System.out, so that when you call
			 * System.out.println(), it won't print to the screen, but will
			 * instead be added to the printingResults object.
			 */
			System.setOut(new PrintStream(printingResults));
			Gitlet.main(args);
		} finally {
			/*
			 * Restores System.out (So you can print normally).
			 */
			System.setOut(originalOut);
		}
		System.out.println(printingResults.toString());
		return printingResults.toString().trim();
	}

	/**
	 * Returns the text from a standard text file.
	 */
	private static String getText(String fileName) {
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(fileName));
			return new String(encoded, StandardCharsets.UTF_8);
		} catch (IOException e) {
			return "";
		}
	}

	/**
	 * Creates a new file with the given fileName and gives it the text
	 * fileText.
	 */
	private static void createFile(String fileName, String fileText) {
		File f = new File(fileName);
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		writeFile(fileName, fileText);
	}

	/**
	 * Replaces all text in the existing file with the given text.
	 */
	private static void writeFile(String fileName, String fileText) {
		FileWriter fw = null;
		try {
			File f = new File(fileName);
			fw = new FileWriter(f, false);
			fw.write(fileText);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Deletes the file and all files inside it, if it is a directory.
	 * 
	 * @throws IOException
	 */
	private static void recursiveDelete(File d) throws IOException {
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
	 * Returns an array of commit messages associated with what log has printed
	 * out.
	 */
	private static String[] extractCommitMessages(String logOutput) {
		String[] logChunks = logOutput.split("===");
		int numMessages = logChunks.length - 1;
		String[] messages = new String[numMessages];
		for (int i = 0; i < numMessages; i++) {
			String[] logLines = logChunks[i + 1].split(LINE_SEPARATOR);
			messages[i] = logLines[3];
		}
		return messages;
	}

	/**
	 * Executes the given command on the terminal, and return what it prints out
	 * as a string.
	 * 
	 * Example: If you want to call the terminal command `java Gitlet add
	 * wug.txt` you would call the method like so: `command("java", "Gitlet",
	 * "add", "wug.txt");` The `...` syntax allows you to pass in however many
	 * strings you want.
	 */
	private static String command(String... args) {
		try {
			StringBuilder results = new StringBuilder();
			Process p = Runtime.getRuntime().exec(args);
			p.waitFor();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					p.getInputStream()));) {
				String line = null;
				while ((line = br.readLine()) != null) {
					results.append(line).append(System.lineSeparator());
				}
				return results.toString();
			}
		} catch (IOException e) {
			return e.getMessage();
		} catch (InterruptedException e) {
			return e.getMessage();
		}
	}

	private static String getFirstId(String message) {
		try {
			String content = gitlet("find", message);
			return content.split(LINE_SEPARATOR)[0];
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * this will test the condition of just moving the Current branch pointer to
	 * the final branch
	 */
	@Test
	public void testrebase1() {
		String wugFileName = TESTING_DIR + "wug.txt";
		String wugText = "This is a wug.";
		createFile(wugFileName, wugText);
		gitlet("init");
		gitlet("add", wugFileName);
		gitlet("commit", "added two wags"); // commit to the master branch

		gitlet("branch", "bra");
		gitlet("checkout", "bra");
		String wugsFileName = TESTING_DIR + "wugs.txt";
		createFile(wugsFileName, "There are two wugs.");
		gitlet("add", wugsFileName);
		gitlet("commit", "added two wags"); // commit to the bra file
		gitlet("rebase", "master");
		String IDForBra = getFirstId("added two wags");
		gitlet("checkout", "master"); // master is expected to point at the same
										// commit
		System.out.println("Commit ID for the Bra:-" + IDForBra);
		System.out.println("Commit ID for the :-"
				+ getFirstId("added two wags"));
		assertEquals(IDForBra, getFirstId("added two wags"));
	}

	/*
	 * tests no propagation && proper rebase of commit object
	 */
	@Test
	public void testrebase2() {
		String wugsFileName = TESTING_DIR + "wug.txt";
		String wugText = "This is a wug.";
		createFile(wugsFileName, wugText);
		gitlet("init");
		gitlet("add", wugsFileName);
		gitlet("commit", "added two wags"); // commit to the master branch

		gitlet("branch", "bra");
		gitlet("checkout", "bra");
		createFile(wugsFileName, "There are two wugs.");
		gitlet("add", wugsFileName);
		gitlet("commit", "two wags to bra"); // commit to the bra file

		gitlet("checkout", "master");
		createFile(wugsFileName, "There are two wugs.");
		gitlet("add", wugsFileName);
		gitlet("commit", "added two wags to master"); // commit to the master
														// branch

		gitlet("rebase", "bra");

		gitlet("checkout", wugsFileName);
		assertEquals("There are two wugs.", getText(wugsFileName));
		String logContent = gitlet("log");
		assertArrayEquals(
				new String[] { "two wags to bra", "added two wags to master",
						"added two wags", "initial commit" },
				extractCommitMessages(logContent));
	}

	/*
	 * this test for the functionality of propagation in rebase...
	 */

	@Test
	public void testrebase3() {
		String wugsFileName = TESTING_DIR + "wug.txt";
		String wugText = "This is a wug.";
		createFile(wugsFileName, wugText);
		gitlet("init");
		gitlet("add", wugsFileName);
		String wugFileName = TESTING_DIR + "wug.txt";
		createFile(wugFileName, "I Hate Wag");
		gitlet("add", wugFileName);
		gitlet("commit", "added two wags");

		gitlet("branch", "bra");
		gitlet("checkout", "bra");
		gitlet("rm", wugFileName);
		gitlet("commit", "keep spitpoint wugsFileName."); // commit to the bra
															// file

		gitlet("checkout", "master");
		createFile(wugsFileName, "There are two wugs.");
		gitlet("add", wugsFileName);
		gitlet("commit", "added two wags to master"); // commit to the master
														// branch

		gitlet("rebase", "bra");
		gitlet("checkout", wugsFileName);
		assertEquals("added two wags to master.", getText(wugsFileName)); // expecting
																			// propagation
		String logContent = gitlet("log");
		assertArrayEquals(
				new String[] { "keep spitpoint wugsFileName.",
						"added two wags to master", "added two wags",
						"initial commit" }, extractCommitMessages(logContent));
	}

	/**
	 * this test merge with no conflict
	 */

	@Test
	public void testreMerge1() {
		String wugsFileName = TESTING_DIR + "wug.txt";
		String wugText = "This is a wug.";
		createFile(wugsFileName, wugText);
		gitlet("init");
		gitlet("add", wugsFileName);
		gitlet("commit", "added two wags");

		gitlet("branch", "bra");
		gitlet("checkout", "bra");
		createFile(wugsFileName, "There are two wugs.");
		gitlet("add", wugsFileName);
		gitlet("commit", "two wags to bra");

		gitlet("checkout", "master");
		String wugFileName = TESTING_DIR + "wug.txt";
		createFile(wugFileName, " wugs.");
		gitlet("add", wugFileName);
		gitlet("commit", "added wags");

		gitlet("merge", "bra");
		String logContent = gitlet("log");
		assertArrayEquals(new String[] { "Merged master with bra.",
				"added wags", "added two wags", "initial commit" },
				extractCommitMessages(logContent));

		gitlet("checkout", wugsFileName);
		 // this should come from bra node not master...
		assertEquals("There are two wugs.", getText(wugsFileName));
		gitlet("checkout", wugFileName);
		assertEquals("wugs.", getText(wugFileName));
	}
	/**
	 * this test that merge changes the current 
	 * nodes files with the updated version
	 * from the given node & not keep the inherited 
	 * file from the parent...
	 */
	@Test
	public void testreMerge2() {
		String wugsFileName = TESTING_DIR + "wug.txt";
		String wugText = "This is a wug.";
		createFile(wugsFileName, wugText);
		gitlet("init");
		gitlet("add", wugsFileName);
		
		String wugFileName = TESTING_DIR + "wug.txt";
		createFile(wugFileName, "hi");
		gitlet("add", wugFileName);
		gitlet("commit", "added two wag files");
		
		gitlet("branch", "bra");
		gitlet("checkout", "bra");
		createFile(wugsFileName, "There are two wugs.");
		gitlet("add", wugsFileName);
		gitlet("commit", "two wags to bra");

		gitlet("checkout", "master");
		gitlet("rm", wugFileName);
		gitlet("commit", "added wags");

		gitlet("merge", "bra");
		
		gitlet("checkout", wugsFileName);
		assertEquals("There are two wugs.", getText(wugsFileName));
	}	
}





















