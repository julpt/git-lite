package gitlet;

import java.io.File;
import java.util.Objects;
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
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. */
    public static final File GITLET_DIR = Utils.join(CWD, ".gitlet");

    /** Directory inside .gitlet for storing commits. */
    public static final File COMM_DIR = Utils.join(GITLET_DIR, "commits");

    /** Directory inside .gitlet for storing blobs. */
    public static final File BLOB_DIR = Utils.join(GITLET_DIR, "blobs");

    /** Directory inside .gitlet for storing pointers to branches. */
    public static final File HEAD_DIR = Utils.join(GITLET_DIR, "heads");

    /** Staging directory. */
    public static final File STAGE_DIR = Utils.join(GITLET_DIR, "staging");

    /** File that tracks the current HEAD branch. */
    public static final File HEAD = Utils.join(GITLET_DIR, "head");

    /** File that tracks items added to the staging area. Maps file names to Blob SHA1s. */
    public static final File INDEX = Utils.join(GITLET_DIR, "index");


    /** Creates a new Gitlet version-control system in the current directory. */

    public static void Initialize() {
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
        // add staging index
        makeStagingIndex();
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
        // check if file to be added exists
        File source = Utils.join(CWD, fileName);
        if (!source.isFile()) {
            Utils.printAndExit("File does not exist.");
        }
        TreeMap<String, String> stagedBlobs = Utils.readObject(INDEX, TreeMap.class);
        Blob addedFile = new Blob(fileName);
        File destination = Utils.join(STAGE_DIR, addedFile.getSHA1());
        // check if file is already staged
        String stagedSHA = stagedBlobs.get(fileName);
        if (stagedSHA != null) {
            // delete old staged version
            Utils.join(STAGE_DIR, stagedSHA).delete();
        }
        /* TODO: UNSTAGE FOR REMOVAL when you get to rm */
        // check if new version is the same as the one in the most recent commit
        String currentSHA = Branch.getHeadCommit().getFileSHA(fileName);
        if (Objects.equals(currentSHA, addedFile.getSHA1())) {
            stagedBlobs.remove(fileName);
            Utils.writeObject(INDEX, stagedBlobs);
        } else {
            // stage file
            Utils.createFile(destination);
            Utils.writeObject(destination, addedFile);
            // add to index
            stagedBlobs.put(fileName, addedFile.getSHA1());
            Utils.writeObject(INDEX, stagedBlobs);
        }
    }

    /** Checks if working directory is an initialized Gitlet directory.
     * Exits if it is not. */
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

    private static void makeStagingIndex() {
        if (!INDEX.isFile()) {
            Utils.createFile(INDEX);
        }
        Utils.writeObject(INDEX, new TreeMap<String, String>());
    }


    /* TODO: fill in the rest of this class. */
}
