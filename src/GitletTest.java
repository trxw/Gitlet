
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

/**
 * Class that provides JUnit tests for Gitlet, as well as a couple of utility
 * methods.
 * 
 * @author Joseph Moghadam
 * 
 *         Some code adapted from StackOverflow:
 * 
 *         http://stackoverflow.com/questions
 *         /779519/delete-files-recursively-in-java
 * 
 *         http://stackoverflow.com/questions/326390/how-to-create-a-java-string
 *         -from-the-contents-of-a-file
 * 
 *         http://stackoverflow.com/questions/1119385/junit-test-for-system-out-
 *         println
 * 
 */
public class GitletTest {
	static String BS="\\"; 
	private static final String GITLET_DIR = ".gitlet/";
	private static final String TESTING_DIR = "test_files/";

	/* matches either unix/mac or windows line separators */
	private static final String LINE_SEPARATOR = "\r\n|[\r\n]";

	/**
	 * Deletes existing gitlet system, resets the folder that stores files used
	 * in testing.
	 * 
	 * This method runs before every @Test method. This is important to enforce
	 * that all tests are independent and do not interact with one another.
	 */
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
	 * Tests that init creates a .gitlet directory. Does NOT test that init
	 * creates an initial commit, which is the other functionality of init.
	 */
	@Test
	public void testBasicInitialize() {
		gitlet("init");
		File f = new File(GITLET_DIR);
		assertTrue(f.exists());
	}

	/**
	 * Tests that checking out a file name will restore the version of the file
	 * from the previous commit. Involves init, add, commit, and checkout.
	 */
	@Test
	public void testBasicCheckout() {
		String wugFileName = TESTING_DIR + "wug.txt";
		String wugText = "This is a wug.";
		createFile(wugFileName, wugText);
		gitlet("init");
		gitlet("add", wugFileName);
		gitlet("commit", "added wug");
		writeFile(wugFileName, "This is not a wug.");
		gitlet("checkout", wugFileName);
		assertEquals(wugText, getText(wugFileName));
	}

	/**
	 * Tests that log after one commit conforms to the format in the spec.
	 * Involves init, add, commit, and log.
	 */
	@Test
	public void testBasicLog() {
		gitlet("init");
		String commitMessage1 = "initial commit";

		String wugFileName = TESTING_DIR + "wug.txt";
		String wugText = "This is a wug.";
		createFile(wugFileName, wugText);
		gitlet("add", wugFileName);
		String commitMessage2 = "added wug";
		gitlet("commit", commitMessage2);

		String logContent = gitlet("log");
		assertArrayEquals(new String[] { commitMessage2, commitMessage1 },
				extractCommitMessages(logContent));
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

@Test
	public void testOldCheckoutOldCommit() {

		String wugFileName = TESTING_DIR + "wug.txt";
		String wugText = "This is a wug.";
		createFile(wugFileName, wugText);

		String wugsFileName = TESTING_DIR + "wugs.txt";
		createFile(wugsFileName, "There are two wugs.");

		gitlet("init");
		gitlet("add", wugFileName);
		gitlet("commit", "added wug");

		writeFile(wugFileName, "This is not a wug.");
		gitlet("add", wugsFileName);
		String wugsMessage = "added wugs";
		gitlet("commit", wugsMessage);

		writeFile(wugFileName, "This is definitely not a wug.");
		String commitId = getFirstId(wugsMessage);
		gitlet("checkout", commitId, wugFileName);
		assertEquals(wugText, getText(wugFileName));
	}
	@Test
	public void testResetHistory() {
		String wugFileName = TESTING_DIR + "wug.txt";
		String commitMessage0 = "initial commit";
		String commitMessage1 = "added wug";
		String commitMessage2 = "made wug awesome";
		String commitMessage3 = "what... what happened?!";
		createFile(wugFileName, "This is a wug.");

		gitlet("init");
		gitlet("add", wugFileName);
		gitlet("commit", commitMessage1);

		writeFile(wugFileName, "This is an awesome wug.");
		gitlet("add", wugFileName);
		gitlet("commit", commitMessage2);

		writeFile(wugFileName, "It's... it's a...?!");
		gitlet("add", wugFileName);
		gitlet("commit", commitMessage3);

		System.out.println(getFirstId(commitMessage2));
		gitlet("reset", getFirstId(commitMessage2));
		assertArrayEquals(new String[] { commitMessage2, commitMessage1,
		commitMessage0 }, extractCommitMessages(gitlet("log")));
	}

	@Test
	public void testCheckoutFromAnotherBranch() {
		String wugFileName = TESTING_DIR + "wug.txt";
		createFile(wugFileName, "This is a wug.");

		gitlet("init");
		gitlet("add", wugFileName);
		gitlet("commit", "added wug");

		gitlet("branch", "cool-beans");
		gitlet("checkout", "cool-beans");
		String coolMessage = "cool branch!";
		writeFile(wugFileName, "This is a cool wug.");
		gitlet("add", wugFileName);
		gitlet("commit", coolMessage);

		gitlet("checkout", "master");
		assertEquals("This is a wug.", getText(wugFileName));

		writeFile(wugFileName, "This is the master of all wugs.");
		gitlet("add", wugFileName);
		gitlet("commit", "mastered wugs");

		writeFile(wugFileName, "Wug is mastered.");
		String commitId = getFirstId(coolMessage);
		gitlet("checkout", commitId, wugFileName);
		assertEquals("This is a cool wug.", getText(wugFileName));
	}
	@Test
	public void testUnstage() {
		String wugFileName = TESTING_DIR + "wug.txt";
		String wugText = "This is a wug.";
		createFile(wugFileName, wugText);

		String wugsFileName = TESTING_DIR + "wugs.txt";
		createFile(wugsFileName, "There are two wugs.");

		gitlet("init");

		gitlet("add", wugFileName);
		gitlet("commit", "added wug");

		writeFile(wugFileName, "This is not a wug.");
		gitlet("add", wugFileName);
		gitlet("add", wugsFileName);
		gitlet("rm", wugFileName);
		gitlet("commit", "added wugs but not wug");

		gitlet("checkout", wugFileName);
		assertEquals(wugText, getText(wugFileName));
	}

	@Test
	public void testRemove() {
		String wugFileName = TESTING_DIR + "wug.txt";
		createFile(wugFileName, "This is a wug.");

		String wugsFileName = TESTING_DIR + "wugs.txt";
		createFile(wugsFileName, "There are two wugs.");

		gitlet("init");

		gitlet("add", wugFileName);
		gitlet("commit", "added wug");

		String changedText = "This is definitely a wug.";
		writeFile(wugFileName, changedText);
		gitlet("rm", wugFileName);
		gitlet("add", wugsFileName);
		gitlet("commit", "added wugs and removed wug");

		String errorMessage = gitlet("checkout", wugFileName);
		assertEquals(
				"File does not exist in the most recent commit, or no such branch exists.",
				errorMessage.trim());
		assertEquals(changedText, getText(wugFileName));
	}
	@Test
	public void testBasicStatus() {
		String wugFileName = TESTING_DIR + "wug.txt";
		String wugText = "This is a wug.";
		createFile(wugFileName, wugText);

		String wugsFileName = TESTING_DIR + "wugs.txt";
		String wugsText = "There are two wugs.";
		createFile(wugsFileName, wugsText);

		gitlet("init");

		gitlet("add", wugFileName);
		gitlet("commit", "added wug");

		gitlet("add", wugsFileName);
		gitlet("rm", wugFileName);

		String statusText = "";
		PrintStream originalOut = System.out;
		try {
			/*
			 * I'm constructing statusText this way so I don't have to deal with
			 * operating system-dependent new lines... thanks Windows!
			 */
			ByteArrayOutputStream printingResults = new ByteArrayOutputStream();
			System.setOut(new PrintStream(printingResults));
			System.out.println("=== Branches ===");
			System.out.println("*master");
			System.out.println();
			System.out.println("=== Staged Files ===");
			System.out.println(wugsFileName);
			System.out.println();
			System.out.println("=== Files Marked for Untracking ===");
			System.out.println(wugFileName);
			System.out.println();
			statusText = printingResults.toString();
		} finally {
			System.setOut(originalOut);
		}
		assertEquals(statusText.trim(), gitlet("status").trim());
	}






















}