package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;


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
    private static final File COMM_DIR = Repository.COMM_DIR;

    /** The SHA1 of this Commit. */
    private final String SHA1;

    /** The first two characters in the Commit's SHA1.
     * Used to create a folder inside .gitlet/commits to store this commit.
     */
    private final String prefix;

    /** The message of this Commit. */
    private final String message;

    /** The timestamp of this Commit. */
    private final Date timestamp;

    /** The mapping of file names to blob references. */
    private Map<String, String> fileMap;

    /** The SHA1s of the parents of this Commit. */
    private final String[] parents = new String[2];

    /** True for the initial Commit, false for any other Commit. */
    private final boolean isInitial;

    /* TODO: fill in the rest of this class. */

    public String getSHA1() {
        return SHA1;
    }

    public String getPrefix() {
        return prefix;
    }

    /** Constructor for initial Commit.
     *
     * The initial commit in all repositories created by Gitlet will
     * have exactly the same content, so all repositories will share this commit
     * and all commits in all repositories will trace back to it.
     * */
    public Commit() {
        isInitial = true;
        message = "initial commit";
        timestamp = new Date(0);
        SHA1 = Utils.sha1(timestamp.toString(), message);
        prefix = SHA1.substring(0,2);
    }

    /** Constructor for non-merged, not initial Commits.
     * @param message commit message
     * @param parent main parent's SHA1
     * @param fileMap hash map of file names to blob references.
     * */
    public Commit(String message, String parent, Map<String, String> fileMap) {
        isInitial = false;
        this.message = message;
        this.parents[0] = parent;
        this.fileMap = fileMap;
        this.timestamp = new Date();
        SHA1 = Utils.sha1(timestamp.toString(), message, fileMap.toString(), parent);
        prefix = SHA1.substring(0,2);
    }

    public void saveCommit() {
        File destination = Utils.join(COMM_DIR, prefix);
        destination.mkdir();
        File commFile = Utils.join(destination, SHA1);
        Utils.createFile(commFile);
        Utils.writeObject(commFile, this);
    }

    /** Returns the Commit which has the given SHA1. */
    public static Commit getFromSHA(String SHA) {
        return Utils.readObject(Utils.join(COMM_DIR, SHA.substring(0,2), SHA), Commit.class);
    }

    public boolean isInitial() {
        return isInitial;
    }

}
