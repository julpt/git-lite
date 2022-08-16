package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;


// TODO: any imports you need here

// TODO: You'll likely use this in this class

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *  Each commit object keeps track of:
 *      a log message,
 *      a timestamp,
 *      a mapping of file names to blob references,
 *      a parent reference,
 *      (for merges) a second parent reference.
 *
 *  @author jul
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** Path to directory that stores all Commits. */
    private static final File COMM_DIR = Paths.COMM_DIR;

    /** The SHA1 of this Commit. */
    private final String SHA1;

    /** The message of this Commit. */
    private final String message;

    /** The timestamp of this Commit. */
    private final Date timestamp;

    /** The snapshot of this Commit - mapping file names to blob references. */
    private Map<String, String> snapshot;

    /** The SHA1 of the main parent of this Commit. */
    private String mainParent;

    /** The SHA1 of the second parent of this Commit, if it exists. Null otherwise */
    private String mergedParent;

    /** True for the initial Commit, false for any other Commit. */
    private final boolean isInitial;

    /** True if this Commit is the result of a merge, false otherwise. */
    private final boolean isMerged;

    /* TODO: fill in the rest of this class. */

    public String getSHA1() {
        return SHA1;
    }


    /** Constructor for initial Commit.
     *
     * The initial commit in all repositories created by Gitlet will
     * have exactly the same content, so all repositories will share this commit
     * and all commits in all repositories will trace back to it.
     * */
    public Commit() {
        isInitial = true;
        isMerged = false;
        message = "initial commit";
        timestamp = new Date(0);
        SHA1 = Utils.sha1(timestamp.toString(), message);
        snapshot = new TreeMap<String, String>();
    }

    /** Constructor for non-merged, not initial Commits.
     * @param message commit message
     * @param parent main parent's SHA1
     * @param snapshot hash map of file names to blob references.
     * */
    public Commit(String message, String parent, TreeMap<String, String> snapshot) {
        isInitial = false;
        isMerged = false;
        this.message = message;
        this.mainParent = parent;
        this.snapshot = snapshot;
        this.timestamp = new Date();
        SHA1 = Utils.sha1(timestamp.toString(), message, snapshot.toString(), parent);
    }

    /** Saves this Commit to COMM_DIR.
     * The name of this saved file is the SHA1 of the Commit.
     */
    public void saveCommit() {
        File commFile = Utils.join(COMM_DIR, SHA1);
        Utils.createFile(commFile);
        Utils.writeObject(commFile, this);
    }

    /** Creates new Commit. By default, its snapshot of files is the same as its parent's.
     * Files staged for addition and removal are the updates to the commit.
     *
     * @param currentCommit current commit
     * @param message Commit message
     * @param added names and SHA1s of files staged for addition
     * @param removed names of files staged for removal
     */
    public static Commit addStaged(Commit currentCommit, String message,
                                   TreeMap<String, String> added, TreeSet<String> removed) {
        TreeMap<String, String> newFiles = new TreeMap<>(currentCommit.snapshot);
        for (String fileName: removed) {
            newFiles.remove(fileName);
        }
        newFiles.putAll(added);
        return new Commit(message, currentCommit.SHA1, newFiles);
    }

    /** Returns the Commit with the given SHA1. */
    public static Commit getFromSHA(String SHA) {
        return Utils.readObject(Utils.join(COMM_DIR, SHA), Commit.class);
    }

    /** Returns the SHA1 of a given file in this Commit. Null if file not in Commit. */
    public String getFileSHA (String fileName) {
        return snapshot.get(fileName);
    }

    /** Returns true if this is the initial Commit, false otherwise. */
    public boolean isInitial() {
        return isInitial;
    }

    /** Returns true if this commit's message is the same as the given string, false otherwise. */
    public boolean hasMessage(String message) {
        return message.equals(this.message);
    }

    /** Starting at the given commit, prints details of each commit backwards along the commit
     * tree until the initial commit, following the first parent commit links, ignoring any
     * second parents found in merge commits. */
    public void printLog() {
        System.out.println(this);
        if (isInitial) {
            return;
        }
        getFromSHA(mainParent).printLog();
    }

    @Override
    public String toString() {
        String commit;
        if (!isMerged) {
            commit = String.format("===%n" +
                    "commit %1$s%n" +
                    "Date: %2$ta %2$tb %2$te %2$tk:%2$tM:%2$tS %2$tY %2$tz%n" +
                    "%3$s%n", SHA1, timestamp, message);
        } else {
            commit = String.format("===%n" +
                    "commit %1$s%n" +
                    "Merge: %4$s %5$s" +
                    "Date: %2$ta %2$tb %2$te %2$tk:%2$tM:%2$tS %2$tY %2$tz%n" +
                    "%3$s%n", SHA1, timestamp, message, mainParent.substring(0, 7),
                    mergedParent.substring(0,7));
        }
        return commit;
    }

}
