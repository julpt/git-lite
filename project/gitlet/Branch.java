package gitlet;


import java.io.File;


public class Branch {

    public static final File HEAD_DIR = Repository.HEAD_DIR;
    public static final File HEAD = Repository.HEAD;

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

    /** Returns the current head commit. */
    public static Commit getHeadCommit() {
        String headName = Utils.readContentsAsString(HEAD);
        File headBranch = Utils.join(HEAD_DIR, headName);
        String headSHA = Utils.readContentsAsString(headBranch);
        return Commit.getFromSHA(headSHA);
    }
}
