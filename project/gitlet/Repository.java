package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

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
     * creating a new commit. If no files have been staged, program exits.<br>
     *
     * By default, each commit’s snapshot of files will be exactly the same as its parent’s;
     * a commit will only update the contents of files it is tracking that have been staged
     * for addition at the time of commit.<br>
     *
     * A commit will save and start tracking any files that were staged for addition but
     * weren’t tracked by its parent.<br>
     *
     * Finally, files tracked in the current commit may be untracked in the new commit as
     * a result being staged for removal by the rm command.
     * */
    public static void commit(String message) {
        checkInitialized();
        Staging.checkStaged();
        Commit currentCommit = Branch.getHeadCommit();
        TreeMap<String, String> stagedFiles = Staging.getStagedIndex();
        ArrayList<String> removedFiles = Staging.getRemoved();
        Commit newCommit = Commit.addStaged(currentCommit, message, stagedFiles, removedFiles);
        for (String fileName: stagedFiles.keySet()) {
            Blob addedFile = new Blob(fileName);
            addedFile.saveBlob();
        }
        newCommit.saveCommit();
        Branch.moveBranchHead(newCommit);
        Staging.resetStaging();
    }

    /** Checks if working directory is an initialized Gitlet directory.
     * Exits if it is not.
     */
    public static void checkInitialized() {
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
