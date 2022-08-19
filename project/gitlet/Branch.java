package gitlet;


import java.io.File;

/** Some methods to create and interact with gitlet branches.
 * A branch is a file located inside BRANCH_DIR that keeps track of a commit - its head.
 * At any time, there exists a HEAD branch, designated by the HEAD file. The head of
 * this branch is the current commit, and this branch is also called the current branch.
 */
public class Branch {

    public static final File BRANCH_DIR = Paths.BRANCH_DIR;
    public static final File HEAD = Paths.HEAD;


    /** Adds new branch with given name and head.
     * Prints error message if branch with given name already exists.  */
    public static void addBranch(String name, Commit head) {
        File branch = Utils.join(BRANCH_DIR, name);
        if (branch.isFile()) {
            Utils.printAndExit("A branch with that name already exists.");
        }
        Utils.writeContents(branch, head.getSHA1());
    }

    /** Sets given Commit as head of the current branch. */
    public static void moveBranchHead(Commit newHead) {
        String newHeadSHA = newHead.getSHA1();
        String headName = Utils.readContentsAsString(HEAD);
        File headBranch = Utils.join(BRANCH_DIR, headName);
        Utils.writeContents(headBranch, newHeadSHA);
    }

    /** Returns the current head commit. */
    public static Commit getHeadCommit() {
        return Commit.getFromSHA(getHeadCommitSHA());
    }

    /** Returns the SHA1 of the current head commit. */
    public static String getHeadCommitSHA() {
        String headName = Utils.readContentsAsString(HEAD);
        File headBranch = Utils.join(BRANCH_DIR, headName);
        return Utils.readContentsAsString(headBranch);
    }

    /** Returns the name of the current branch. */
    public static String getCurrentBranchName() {
        return Utils.readContentsAsString(HEAD);
    }
}
