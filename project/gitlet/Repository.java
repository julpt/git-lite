package gitlet;

import java.io.File;

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



    public static void makeRepo() {
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
    }

    public static boolean isInitialized() {
        return GITLET_DIR.exists();
    }

    public static void addBranch(String name) {
        Branch.addBranch(name);
    }

    public static void addBranch(String name, Commit head) {
        Branch.addBranch(name, head);
    }




    /* TODO: fill in the rest of this class. */
}
