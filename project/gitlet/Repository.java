package gitlet;

import java.io.File;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.List;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author jul
 */
public class Repository {

    /** The current working directory. */
    public static final File CWD = Paths.CWD;

    /** The .gitlet directory. */
    public static final File GITLET_DIR = Paths.GITLET_DIR;

    /** Directory inside .gitlet for storing commits. */
    public static final File COMM_DIR = Paths.COMM_DIR;

    /** Directory inside .gitlet for storing blobs. */
    public static final File BLOB_DIR = Paths.BLOB_DIR;

    /** Directory inside .gitlet for storing pointers to branches. */
    public static final File HEAD_DIR = Paths.HEAD_DIR;

    /** Staging directory. Stores added files until commit. */
    public static final File STAGE_DIR = Paths.STAGE_DIR;

    /** File that tracks the current HEAD branch. */
    public static final File HEAD = Paths.HEAD;

    /** File that tracks items added to the staging area. Maps file names to Blob SHA1s. */
    public static final File INDEX = Paths.INDEX;


    /** Creates a new Gitlet version-control system in the current directory. */

    public static void setup() {
        boolean created = GITLET_DIR.mkdir();
        if (!created) {
            Utils.printAndExit("A Gitlet version-control system already exists " +
                    "in the current directory.");

        }
        COMM_DIR.mkdirs();
        BLOB_DIR.mkdirs();
        HEAD_DIR.mkdirs();
        STAGE_DIR.mkdirs();
        // create initial commit
        Commit initial = new Commit();
        initial.saveCommit();
        // create and set head file
        Utils.createFile(HEAD);
        Utils.writeContents(HEAD, "master");
        // create master branch
        addBranch("master", initial);
        // add files to track staged and removed files
        Staging.resetStaging();
    }

    /** Adds a copy of the file as it currently exists to the staging area.
     * If file is already staged, it is overwritten.
     *
     * If the current working version of the file is identical to the version
     * in the current commit, it won't be staged, and will be removed from the
     * staging area if it is already there.
     *
     * The file will no longer be staged for removal, if it was at the time of the command.
     */
    public static void addFile(String fileName) {
        checkInitialized();
        Staging.checkFileExists(fileName);
        Staging.stageFile(fileName);
    }

    /** Saves a snapshot of tracked files in the current commit and staging area,
     * creating a new commit. If no files have been staged, program exits.
     *
     * By default, each commit’s snapshot of files will be exactly the same as its parent’s;
     * a commit will only update the contents of files it is tracking that have been staged
     * for addition at the time of commit.
     *
     * A commit will save and start tracking any files that were staged for addition but
     * were not tracked by its parent.
     *
     * Finally, files tracked in the current commit may be untracked in the new commit as
     * a result being staged for removal by the rm command.
     * */
    public static void commit(String message) {
        checkInitialized();
        if (message.equals("")) {
            Utils.printAndExit("Please enter a commit message.");
        }
        Staging.checkStaged();
        Commit currentCommit = Branch.getHeadCommit();
        TreeMap<String, String> stagedFiles = Staging.getStagedIndex();
        TreeSet<String> removedFiles = Staging.getRemoved();
        Commit newCommit = Commit.addStaged(currentCommit, message, stagedFiles, removedFiles);
        for (String stagedFileSHA: stagedFiles.values()) {
            Blob addedFile = Staging.getStagedFile(stagedFileSHA);
            addedFile.saveBlob();
        }
        newCommit.saveCommit();
        Branch.moveBranchHead(newCommit);
        Staging.resetStaging();
    }

    /** Unstages the file if it is currently staged for addition.
     * If the file is tracked in the current commit, stage it for removal and remove the file
     * from the working directory if the user has not already done so.<br><br>
     *
     * If the file is neither staged nor tracked by the head commit, prints an error message.
     */
    public static void removeFile(String fileName) {
        checkInitialized();
        Staging.removeFile(fileName);
    }

    /** Starting at the current head commit, display information about each commit backwards along
     * the commit tree until the initial commit, following the first parent commit links, ignoring
     * any second parents found in merge commits.
     *
     * For every node in this history, display the commit SHA1, timestamp and message..
     */
    public static void log() {
        checkInitialized();
        Commit head = Branch.getHeadCommit();
        head.printLog();
    }

    /** Like log, except displays information about all commits ever made.
     * Commits are not listed in a particular order.
     */
    public static void logAll() {
        checkInitialized();
        List<String> commits = Utils.plainFilenamesIn(COMM_DIR);
        if (commits != null) {
            for (String sha: commits) {
                System.out.println(Commit.getFromSHA(sha));
            }
        }
    }

    /** Prints out the ids of all commits that have the given commit message, one per line. */
    public static void find(String message) {
        checkInitialized();
        List<String> commits = Utils.plainFilenamesIn(COMM_DIR);
        if (commits != null) {
            for (String sha: commits) {
                Commit comm = (Commit.getFromSHA(sha));
                if (comm.hasMessage(message)) {
                    System.out.println(sha);
                }
            }
        }
    }

    /** Takes the version of the file as it exists in the head commit and puts it in the working
     * directory, overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged.
     */
    public static void checkoutFile(String fileName) {
        checkInitialized();
        String currentSHA = Branch.getHeadCommitSHA();
        checkoutFromCommit(currentSHA, fileName);
    }

    /** Takes the version of the file as it exists in the commit with the given id,
     * and puts it in the working directory, overwriting the version of the file
     * that’s already there if there is one.
     * The new version of the file is not staged.
     */
    public static void checkoutFromCommit(String commitID, String fileName) {
        checkInitialized();
        Commit comm = Commit.getFromSHA(commitID);
        String fileSHA = comm.getFileSHA(fileName);
        if (fileSHA == null) {
            Utils.printAndExit("File does not exist in that commit.");
        }
        Blob fileBlob = Blob.getFromSHA(fileSHA);
        fileBlob.writeContentsToFile(CWD, fileName);
    }

    /** Takes all files in the commit at the head of the given branch, and puts them in the working
     * directory, overwriting the versions of the files that are already there if they exist.
     * At the end of this command, the given branch will be considered the current branch. (HEAD)
     * Any files that are tracked in the current branch but are not present in the checked-out
     * branch are deleted.
     * The staging area is cleared, unless the checked-out branch is the current branch.
     */
    public static void checkoutBranch(String branchName) {

    }

    /** Displays what branches currently exist, and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal.
     * TODO: extra credit not staged and untracked*/
    public static void status() {
        checkInitialized();
        printBranches();
        printStaged();
        printExtraCredit();
    }

    /** Prints what branches currently exist, and marks the current branch with a *. */
    private static void printBranches() {
        String currentBranch = Branch.getCurrentBranchName();
        System.out.println("=== Branches ===");
        List<String> branches = Utils.plainFilenamesIn(HEAD_DIR);
        for (String branch: branches) {
            if (currentBranch.equals(branch)) {
                branch = "*" + branch;
            }
            System.out.println(branch);
        }
        System.out.println();
    }

    /** Prints what files have been staged for addition or removal. */
    private static void printStaged() {
        // Staged for addition
        System.out.println("=== Staged Files ===");
        TreeMap<String, String> added = Staging.getStagedIndex();
        for (String fileName: added.keySet()) {
            System.out.println(fileName);
        }
        System.out.println();

        // Staged for removal
        System.out.println("=== Removed Files ===");
        TreeSet<String> removed = Staging.getRemoved();
        for (String fileName: removed) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    /** Placeholder: modifications not staged and untracked files */
    private static void printExtraCredit() {
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    /** Checks if working directory is an initialized Gitlet directory.
     * Prints error message if it is not.
     */
    private static void checkInitialized() {
        if(!GITLET_DIR.exists()) {
            Utils.printAndExit("Not in an initialized Gitlet directory.");
        }
    }

    public static void addBranch(String name) {
        Branch.addBranch(name);
    }

    private static void addBranch(String name, Commit head) {
        Branch.addBranch(name, head);
    }




    /* TODO: fill in the rest of this class. */
}
