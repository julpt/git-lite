package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;


/** Represents a gitlet commit object.
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
    /** The current working directory */
    private static final File CWD = Paths.CWD;

    /** Path to directory that stores all Commits. */
    private static final File COMM_DIR = Paths.COMM_DIR;

    /** The SHA1 of this Commit. */
    private final String sha1;

    /** The message of this Commit. */
    private final String message;

    /** The timestamp of this Commit. */
    private final Date timestamp;

    /** The snapshot of this Commit - mapping file names to blob references. */
    private TreeMap<String, String> snapshot;

    /** The SHA1 of the main parent of this Commit. */
    private String mainParent;

    /** The SHA1 of the second parent of this Commit, if it exists. Null otherwise */
    private String secondParent;

    /** True if this Commit is the result of a merge, false otherwise. */
    private final boolean isMerged;

    public String getSHA1() {
        return sha1;
    }


    /** Constructor for initial Commit.
     *
     * The initial commit in all repositories created by Gitlet will
     * have exactly the same content, so all repositories will share this commit
     * and all commits in all repositories will trace back to it.
     * */
    public Commit() {
        isMerged = false;
        message = "initial commit";
        mainParent = null;
        snapshot = new TreeMap<String, String>();
        timestamp = new Date(0);
        sha1 = Utils.sha1(timestamp.toString(), message);
    }

    /** Constructor for non-merged, not initial Commits.
     * @param message commit message
     * @param parent main parent's SHA1
     * @param snapshot mapping of file names to blob references.
     */
    public Commit(String message, String parent, TreeMap<String, String> snapshot) {
        isMerged = false;
        this.message = message;
        this.mainParent = parent;
        this.snapshot = snapshot;
        this.timestamp = new Date();
        sha1 = Utils.sha1(timestamp.toString(), message, snapshot.toString(), parent);
    }

    /** Constructor for merged Commits.
     * @param mergedBranch name of the branch being merged
     * @param mainParent main parent's SHA1
     * @param secondParent second parent's SHA1
     * @param snapshot mapping of file names to blob references.
     */
    public Commit(String mergedBranch, String mainParent, String secondParent,
                  TreeMap<String, String> snapshot) {
        isMerged = true;
        this.timestamp = new Date();
        this.message = String.format("Merged %s into %s.",
                mergedBranch, Branch.getCurrentBranchName());
        this.mainParent = mainParent;
        this.secondParent = secondParent;
        this.snapshot = snapshot;
        sha1 = Utils.sha1(timestamp.toString(), message,
                snapshot.toString(), mainParent, secondParent);
    }

    /** Saves this Commit to COMM_DIR.
     * The name of this saved file is the SHA1 of the Commit.
     */
    public void saveCommit() {
        File commFile = Utils.join(COMM_DIR, sha1);
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
        return new Commit(message, currentCommit.sha1, newFiles);
    }

    /** Returns the Commit with the given SHA1. If no commit is found, prints an error message. */
    public static Commit getFromSHA(String sha) {
        // For abbreviated Commits
        if (sha.length() < 40) {
            List<String> commits = Utils.plainFilenamesIn(COMM_DIR);
            for (String shaFromList: commits) {
                if (shaFromList.startsWith(sha)) {
                    sha = shaFromList;
                    break;
                }
            }
        }
        try {
            return Utils.readObject(Utils.join(COMM_DIR, sha), Commit.class);
        } catch (IllegalArgumentException e) {
            Utils.printAndExit("No commit with that id exists.");
            return null;
        }
    }

    /** Copies every file tracked by this Commit to the working directory,
     * overwriting the versions of the files that are already there if they exist.
     */
    public void copyToCWD() {
        for (Map.Entry<String, String> entry: snapshot.entrySet()) {
            String name = entry.getKey();
            String sha = entry.getValue();
            Blob.getFromSHA(sha).writeContentsToFile(CWD, name);
        }
    }

    /**  */
    public Set<String> getContents() {
        return snapshot.keySet();
    }

    /** Returns the SHA1 of a given file in this Commit. Null if file not in Commit. */
    public String getFileSHA(String fileName) {
        return snapshot.get(fileName);
    }


    /** Returns true if this commit's message is the same as the given string, false otherwise. */
    public boolean hasMessage(String givenMessage) {
        return message.equals(givenMessage);
    }

    /** Starting at the given commit, prints details of each commit backwards along the commit
     * tree until the initial commit, following the first parent commit links, ignoring any
     * second parents found in merge commits. */
    public void printLog() {
        Commit current = this;
        while (current.mainParent != null) {
            System.out.println(current);
            current = Commit.getFromSHA(current.mainParent);
        }
        System.out.print(current);
    }

    /** Returns true if the [current] commit is the ancestor of the [other] commit. */
    public static boolean isAncestor(Commit current, Commit other) {
        if (current.mainParent == null || current.equals(other)) {
            // The initial commit is an ancestor of all others;
            // The [other] commit is either the original commit passed to this method or one of
            // its ancestors. If it is equal to the [current] commit, then the [current] commit
            // is indeed an ancestor of the [other] originally passed to this method.
            return true;
        } else if (other.mainParent == null || current.timestamp.after(other.timestamp)) {
            // The initial commit has no ancestor;
            // If a commit's timestamp is more recent than another's, it cannot be that commit's
            // ancestor.
            return false;
        }
        if (other.isMerged) {
            return isAncestor(current, getFromSHA(other.mainParent))
                    || isAncestor(current, getFromSHA(other.secondParent));
        }
        return isAncestor(current, getFromSHA(other.mainParent));
    }

    /** Returns the split point, which is the latest common ancestor of the two given commits. */
    public static Commit getSplitPoint(Commit current, Commit merged) {
        Commit older;
        Commit newer;
        if (current.timestamp.before(merged.timestamp)) {
            older = current;
            newer = merged;
        } else {
            older = merged;
            newer = current;
        }
        if (isAncestor(older, newer)) {
            return older;
        }
        // Move up the line of [older]'s main parents until we find an ancestor of [newer].
        while (older.mainParent != null) {
            if (isAncestor(older, newer)) {
                return older;
            }
            older = getFromSHA(older.mainParent);
        }
        // By the end, older points to the initial commit;
        // If no other ancestors are found, then this has to be the only common ancestor.
        return older;
    }

    public TreeMap<String, String> getSnapshot() {
        return snapshot;
    }

    @Override
    public String toString() {
        String commit;
        if (!isMerged) {
            commit = String.format("===%n"
                    + "commit %1$s%n"
                    + "Date: %2$ta %2$tb %2$te %2$tH:%2$tM:%2$tS %2$tY %2$tz%n"
                    + "%3$s%n",
                    sha1, timestamp, message);
        } else {
            commit = String.format("===%n"
                    + "commit %1$s%n"
                    + "Merge: %4$s %5$s%n"
                    + "Date: %2$ta %2$tb %2$te %2$tH:%2$tM:%2$tS %2$tY %2$tz%n"
                    + "%3$s%n",
                    sha1, timestamp, message, mainParent.substring(0, 7),
                    secondParent.substring(0, 7));
        }
        return commit;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        Commit other = (Commit) obj;
        return this.sha1.equals(other.sha1);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sha1);
    }
}
