package gitlet;


import java.io.File;


public class Branch {

    /** Directory inside .gitlet for storing pointers to branches. */
    public static final File HEAD_DIR = Repository.HEAD_DIR;

    /** File that tracks the current HEAD branch */
    public static final File HEAD = Repository.HEAD;

    /** Adds a new branch, if there isn't one with the given name. */
    public static void addBranch(String name) {
        Commit head = getHeadCommit();
        addBranch(name, head);
    }

    /** Adds new branch with given name and head. */
    public static void addBranch(String name, Commit head) {
        File branch = Utils.join(HEAD_DIR, name);
        if (branch.isFile()) {
            Utils.printAndExit("A branch with that name already exists.");
        }
        Utils.createFile(branch);
        Utils.writeContents(branch, head.getSHA1());
    }

    /** Returns the current head commit. */
    private static Commit getHeadCommit() {
        String headName = Utils.readContentsAsString(HEAD);
        System.out.println(headName);
        File headBranch = Utils.join(HEAD_DIR, headName);
        String headSHA = Utils.readContentsAsString(headBranch);
        return Commit.getFromSHA(headSHA);
    }
}
