package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import static gitlet.Utils.join;

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
    private File COMM_DIR = join(new File(System.getProperty("user.dir")), ".gitlet", "commits");

    /** The SHA1 of this Commit. */
    private String SHA1;
    /** The first two characters in the Commit's SHA1.
     * Used to create a folder inside .gitlet/commits to store this commit.
     */
    private String prefix;

    /** The message of this Commit. */
    private String message;

    /** The timestamp of this Commit. */
    private Date timestamp;

    /** The mapping of file names to blob references of this Commit. */
    private Map<String, String> fileMap;

    /** The parents of this Commit. */
    private String[] parents = new String[2];

    /* TODO: fill in the rest of this class. */

    public String getSHA1() {
        return SHA1;
    }

    public String getPrefix() {
        return prefix;
    }

    public Commit(String message, String parent, Map<String, String> fileMap, boolean isInitial) {
        this.message = message;
        this.parents[0] = parent;
        this.fileMap = fileMap;
        if (isInitial) {
            this.timestamp = new Date(0);
            SHA1 = Utils.sha1(timestamp.toString(), message);
        } else {
            this.timestamp = new Date();
            SHA1 = Utils.sha1(timestamp.toString(), message, fileMap.toString(), parent);
        }
        prefix = SHA1.substring(0,2);
        this.addCommit();
    }

    private void addCommit() {
        File destination = Utils.join(COMM_DIR, prefix);
        destination.mkdir();
        File commFile = Utils.join(destination, SHA1);
        try {
            commFile.createNewFile();
        } catch (Exception e) {
            System.out.printf("Caught exception %s when creating file.%n", e.toString());
            System.exit(0);
        }
        Utils.writeObject(commFile, this);
    }

}
