package gitlet;


import java.io.File;


public class Branch {

    public static final File BRANCH_DIR = Paths.BRANCH_DIR;
    public static final File HEAD = Paths.HEAD;


    /** Adds new branch with given name and head.
     * Prints error message if branch with given name already exists.
     */
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

    /** Makes given branch the current (HEAD) branch. */
    public static void changeHead(String name) {
        Utils.writeContents(HEAD, name);
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

    /** Returns the head commit of the given branch.
     * Prints error message if branch doesn't exist.
     */
    public static Commit getCommitFromBranch(String branchName) {
        if (branchName.equals(getCurrentBranchName())) {
            Utils.printAndExit("No need to checkout the current branch.");
        }
        File branch = Utils.join(BRANCH_DIR, branchName);
        if (!branch.isFile()) {
            Utils.printAndExit("No such branch exists.");
        }
        return Commit.getFromSHA(Utils.readContentsAsString(branch));
    }

    /** Returns the name of the current branch. */
    public static String getCurrentBranchName() {
        return Utils.readContentsAsString(HEAD);
    }
}
