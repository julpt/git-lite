package gitlet;


import java.io.File;


public class Branch {

    public static final File HEAD_DIR = Paths.HEAD_DIR;
    public static final File HEAD = Paths.HEAD;

    /** Adds new branch with given name. Sets current commit as head.
     * Exits if branch with given name already exists. */
    public static void addBranch(String name) {
        Commit head = getHeadCommit();
        addBranch(name, head);
    }

    /** Adds new branch with given name and head. Exits if branch with given name already exists. */
    public static void addBranch(String name, Commit head) {
        File branch = Utils.join(HEAD_DIR, name);
        if (branch.isFile()) {
            Utils.printAndExit("A branch with that name already exists.");
        }
        Utils.createFile(branch);
        Utils.writeContents(branch, head.getSHA1());
    }

    /** Sets given Commit as head of the current branch. */
    public static void moveBranchHead(Commit newHead) {
        String newHeadSHA = newHead.getSHA1();
        String headName = Utils.readContentsAsString(HEAD);
        File headBranch = Utils.join(HEAD_DIR, headName);
        Utils.writeContents(headBranch, newHeadSHA);
    }

    /** Returns the current head commit. */
    public static Commit getHeadCommit() {
        return Commit.getFromSHA(getHeadCommitSHA());
    }

    /** Returns the SHA1 of the current head commit. */
    public static String getHeadCommitSHA() {
        String headName = Utils.readContentsAsString(HEAD);
        File headBranch = Utils.join(HEAD_DIR, headName);
        return Utils.readContentsAsString(headBranch);
    }

    public static String getCurrentBranchName() {
        return Utils.readContentsAsString(HEAD);
    }
}
