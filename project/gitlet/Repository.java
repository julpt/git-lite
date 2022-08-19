package gitlet;

import java.io.File;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.List;


/** Represents a gitlet repository. */
public class Repository {

    /** The current working directory. */
    public static final File CWD = Paths.CWD;

    /** The .gitlet directory. */
    public static final File GITLET_DIR = Paths.GITLET_DIR;

    /** Directory inside .gitlet for storing commits. */
    public static final File COMM_DIR = Paths.COMM_DIR;

    /** Directory inside .gitlet for storing blobs. */
    public static final File BLOB_DIR = Paths.BLOB_DIR;

    /** Directory inside .gitlet for storing pointers to branches. */
    public static final File BRANCH_DIR = Paths.BRANCH_DIR;

    /** Staging directory. Stores added files until commit. */
    public static final File STAGE_DIR = Paths.STAGE_DIR;

    /** File that tracks the current HEAD branch. */
    public static final File HEAD = Paths.HEAD;


    /** Creates a new Gitlet version-control system in the current working directory. */

    public static void setup() {
        boolean created = GITLET_DIR.mkdir();
        if (!created) {
            Utils.printAndExit("A Gitlet version-control system already exists "
                    + "in the current directory.");

        }
        COMM_DIR.mkdirs();
        BLOB_DIR.mkdirs();
        BRANCH_DIR.mkdirs();
        STAGE_DIR.mkdirs();
        // create initial commit
        Commit initial = new Commit();
        initial.saveCommit();
        // create and set head file
        Utils.writeContents(HEAD, "master");
        // create master branch
        Branch.addBranch("master", initial);
        // add files to track staged and removed files
        Staging.resetStaging();
    }

    /** Adds a copy of the file as it currently exists to the staging area.
     * If file is already staged, it is overwritten.
     *
     * If the current working version of the file is identical to the version
     * in the current commit, it won't be staged, and will be removed from the
     * staging area if it is already there.
     *
     * The file will no longer be staged for removal, if it was at the time of the command. */
    public static void addFile(String fileName) {
        checkInitialized();
        TreeSet<String> removed = Staging.getRemoved();
        if (removed.contains(fileName)) {
            Commit head = Branch.getHeadCommit();
            String sha = head.getFileSHA(fileName);
            Blob.getFromSHA(sha).writeContentsToFile(CWD, fileName);
        }
        if (!Utils.join(CWD, fileName).isFile()) {
            Utils.printAndExit("File does not exist.");
        }
        Staging.stageFile(fileName);
    }

    /** Saves a snapshot of tracked files in the current commit and staging area,
     * creating a new commit. If no files have been staged, program exits.
     *
     * By default, each commit’s snapshot of files will be exactly the same as its parent’s;
     * a commit will only update the contents of files it is tracking that have been staged
     * for addition at the time of commit.
     *
     * A commit will save and start tracking any files that were staged for addition but
     * were not tracked by its parent.
     *
     * Finally, files tracked in the current commit may be untracked in the new commit as
     * a result being staged for removal by the rm command. */
    public static void commit(String message) {
        checkInitialized();
        if (message.equals("")) {
            Utils.printAndExit("Please enter a commit message.");
        }
        if (!Staging.checkStaged()) {
            Utils.printAndExit("No changes added to the commit.");
        }
        Commit currentCommit = Branch.getHeadCommit();
        TreeMap<String, String> stagedFiles = Staging.getStagedIndex();
        TreeSet<String> removedFiles = Staging.getRemoved();
        Commit newCommit = Commit.addStaged(currentCommit, message, stagedFiles, removedFiles);
        for (String stagedFileSHA: stagedFiles.values()) {
            Blob addedFile = Utils.readObject(Utils.join(STAGE_DIR, stagedFileSHA), Blob.class);
            addedFile.saveBlob();
        }
        newCommit.saveCommit();
        Branch.moveBranchHead(newCommit);
        Staging.resetStaging();
    }

    /** Unstages the file if it is currently staged for addition.
     * If the file is tracked in the current commit, stage it for removal and remove the file
     * from the working directory if the user has not already done so.
     * If the file is neither staged nor tracked by the head commit, prints an error message. */
    public static void removeFile(String fileName) {
        checkInitialized();
        Staging.removeFile(fileName);
    }

    /** Starting at the current head commit, display information about each commit backwards along
     * the commit tree until the initial commit, following the first parent commit links, ignoring
     * any second parents found in merge commits. */
    public static void log() {
        checkInitialized();
        Commit head = Branch.getHeadCommit();
        head.printLog();
    }

    /** Like log, except displays information about all commits ever made.
     * Commits are not listed in a particular order. */
    public static void logAll() {
        checkInitialized();
        List<String> commits = Utils.plainFilenamesIn(COMM_DIR);
        if (commits != null) {
            for (String sha: commits) {
                System.out.println(Commit.getFromSHA(sha));
            }
        }
    }

    /** Prints out the ids of all commits that have the given commit message, one per line. */
    public static void find(String message) {
        checkInitialized();
        List<String> commits = Utils.plainFilenamesIn(COMM_DIR);
        boolean found = false;
        if (commits != null) {
            for (String sha: commits) {
                Commit comm = (Commit.getFromSHA(sha));
                if (comm.hasMessage(message)) {
                    System.out.println(sha);
                    found = true;
                }
            }
        }
        if (!found) {
            Utils.printAndExit("Found no commit with that message.");
        }
    }

    /** Takes the version of the file as it exists in the head commit and puts it in the working
     * directory, overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged. */
    public static void checkoutFile(String fileName) {
        checkInitialized();
        String currentSHA = Branch.getHeadCommitSHA();
        checkoutFromCommit(currentSHA, fileName);
    }

    /** Takes the version of the file as it exists in the commit with the given id,
     * and puts it in the working directory, overwriting the version of the file
     * that’s already there if there is one.
     * The new version of the file is not staged. */
    public static void checkoutFromCommit(String commitID, String fileName) {
        checkInitialized();
        Commit comm = Commit.getFromSHA(commitID);
        String fileSHA = comm.getFileSHA(fileName);
        if (fileSHA == null) {
            Utils.printAndExit("File does not exist in that commit.");
        }
        Blob fileBlob = Blob.getFromSHA(fileSHA);
        fileBlob.writeContentsToFile(CWD, fileName);
    }

    /** Checks out all files tracked by the head commit of the given branch.
     * Removes tracked files that are not present in that commit.
     * At the end of this command, the given branch will be considered the current branch. (HEAD)
     * If a working file is untracked in the current branch and would be overwritten by the
     * checkout, prints an error message.
     * The staging area is cleared, unless the checked-out branch is the current branch. */
    public static void checkoutBranch(String branchName) {
        checkInitialized();
        Commit currentHead = Branch.getHeadCommit();
        if (branchName.equals(Branch.getCurrentBranchName())) {
            Utils.printAndExit("No need to checkout the current branch.");
        }
        File branch = Utils.join(BRANCH_DIR, branchName);
        if (!branch.isFile()) {
            Utils.printAndExit("No such branch exists.");
        }
        Commit branchHead = Commit.getFromSHA(Utils.readContentsAsString(branch));
        checkoutCopyFiles(branchHead, currentHead);
        Utils.writeContents(HEAD, branchName);
    }


    /** Displays what branches currently exist, and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal, as well as
     * modified and untracked files. */
    public static void status() {
        checkInitialized();
        printBranches();
        printStaged();
        printModifiedAndUntracked();
    }

    /** Prints what branches currently exist, and marks the current branch with a "*". */
    private static void printBranches() {
        String currentBranch = Branch.getCurrentBranchName();
        System.out.println("=== Branches ===");
        List<String> branches = Utils.plainFilenamesIn(BRANCH_DIR);
        for (String branch: branches) {
            if (currentBranch.equals(branch)) {
                branch = "*" + branch;
            }
            System.out.println(branch);
        }
        System.out.println();
    }

    /** Prints what files have been staged for addition or removal. */
    private static void printStaged() {
        // Staged for addition
        System.out.println("=== Staged Files ===");
        TreeMap<String, String> added = Staging.getStagedIndex();
        for (String fileName: added.keySet()) {
            System.out.println(fileName);
        }
        System.out.println();

        // Staged for removal
        System.out.println("=== Removed Files ===");
        TreeSet<String> removed = Staging.getRemoved();
        for (String fileName: removed) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    /** Prints what files in the current directory differ from the current commit or are
     * not tracked by it. */
    private static void printModifiedAndUntracked() {
        TreeMap<String, String> tracked = Branch.getHeadCommit().getSnapshot();
        TreeSet<String> modified = new TreeSet<>();
        TreeMap<String, String> added = Staging.getStagedIndex();
        TreeSet<String> removed = Staging.getRemoved();
        List<String> files = Utils.plainFilenamesIn(CWD);

        System.out.println("=== Modifications Not Staged For Commit ===");
        // A file in the working directory is modified but not staged if it is:
        //      (1) Tracked in the current commit, changed in the working directory but not staged;
        //      (2) Staged for addition, but with different contents than in the working directory;
        //      (3) Staged for addition, but deleted in the working directory; or
        //      (4) Not staged for removal, but tracked in the current commit and deleted from the
        //      working directory.
        for (String fileName: files) {
            // compare file contents
            Blob thisFile = new Blob(fileName);
            if (tracked.containsKey(fileName)
                    && !thisFile.getSHA1().equals(tracked.get(fileName))
                    && !added.containsKey(fileName)) {
                // case 1
                modified.add(fileName + " (modified)");
            } else if (added.containsKey(fileName)
                    && !thisFile.getSHA1().equals(added.get(fileName))) {
                // case 2
                modified.add(fileName + " (modified)");
            }
        }
        // case 3
        for (String fileName: added.keySet()) {
            if (!files.contains(fileName)) {
                modified.add(fileName + " (deleted)");
            }
        }
        // case 4
        for (String fileName: tracked.keySet()) {
            if (!files.contains(fileName)
                    && !removed.contains(fileName)) {
                modified.add(fileName + " (deleted)");
            }
        }
        for (String m: modified) {
            System.out.println(m);
        }
        System.out.println();

        System.out.println("=== Untracked Files ===");
        // Untracked files are files present in the working directory but neither staged for
        // addition nor tracked. This includes files that have been staged for removal, but
        // then re-created without Gitlet’s knowledge.
        for (String fileName: files) {
            if ((!added.containsKey(fileName) && !tracked.containsKey(fileName))
                    || removed.contains(fileName)) {
                System.out.println(fileName);
            }
        }
        System.out.println();
    }

    /** Checks if working directory is an initialized Gitlet directory.
     * Prints error message if it is not. */
    private static void checkInitialized() {
        if (!GITLET_DIR.exists()) {
            Utils.printAndExit("Not in an initialized Gitlet directory.");
        }
    }

    public static void addBranch(String name) {
        checkInitialized();
        Commit head = Branch.getHeadCommit();
        Branch.addBranch(name, head);
    }

    /** Removes the given branch. Prints error messages if trying to remove current branch
     * or if given branch doesn't exist. */
    public static void removeBranch(String name) {
        checkInitialized();
        if (name.equals(Branch.getCurrentBranchName())) {
            Utils.printAndExit("Cannot remove the current branch.");
        }
        for (String branch: Utils.plainFilenamesIn(BRANCH_DIR)) {
            if (name.equals(branch)) {
                Utils.join(BRANCH_DIR, name).delete();
                return;
            }
        }
        Utils.printAndExit("A branch with that name does not exist.");
    }

    /** Checks out all the files tracked by the given commit. Removes tracked files that are
     * not present in that commit. Also moves the current branch’s head to that commit node. */
    public static void reset(String commitID) {
        checkInitialized();
        Commit target = Commit.getFromSHA(commitID);
        Commit head = Branch.getHeadCommit();
        checkoutCopyFiles(target, head);
        Utils.writeContents(Utils.join(BRANCH_DIR, Branch.getCurrentBranchName()), commitID);
    }

    /** Method that copies and deletes files for checkoutBranch and reset, and clears staging.
     * Does not change the current branch or branch heads. */
    private static void checkoutCopyFiles(Commit targetCommit, Commit currentCommit) {
        Set<String> trackedFiles = currentCommit.getContents();
        checkUntrackedConflicts(targetCommit, currentCommit);
        for (String name: trackedFiles) {
            File file = Utils.join(CWD, name);
            Utils.restrictedDelete(file);
        }
        targetCommit.copyToCWD();
        Staging.resetStaging();
    }

    /** Checks if a working file is untracked in the current branch and would be overwritten
     * by a checkout. If so, prints an error message. */
    private static void checkUntrackedConflicts(Commit targetCommit, Commit currentCommit) {
        Set<String> trackedFiles = currentCommit.getContents();
        for (String fileName: targetCommit.getContents()) {
            if (Utils.join(CWD, fileName).isFile() && !trackedFiles.contains(fileName)) {
                Utils.printAndExit("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
            }
        }
    }

    /**  Merges files from the given branch into the current branch. */
    public static void merge(String branchName) {
        checkInitialized();
        if (Staging.checkStaged()) {
            Utils.printAndExit("You have uncommitted changes.");
        }
        Commit current = Branch.getHeadCommit();
        if (branchName.equals(Branch.getCurrentBranchName())) {
            Utils.printAndExit("Cannot merge a branch with itself.");
        }
        File branch = Utils.join(BRANCH_DIR, branchName);
        if (!branch.isFile()) {
            Utils.printAndExit("A branch with that name does not exist.");
        }
        Commit given = Commit.getFromSHA(Utils.readContentsAsString(branch));
        Commit split = Commit.getSplitPoint(current, given);
        if (split.equals(given)) {
            // If the split point is the given branch, do nothing; the merge is complete.
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        } else if (split.equals(current)) {
            // If the split point is the current branch, check out the given branch.
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        checkUntrackedConflicts(given, current);

        // If there are no uncommitted changes, begin merging.
        // Use sets to keep track of files in the three referenced commits.
        // Use a TreeMap to build the snapshot of the merged commit, starting from the snapshot of
        // the split point.
        TreeSet<String> givenFiles = new TreeSet<>(given.getContents());
        TreeSet<String> currentFiles = new TreeSet<>(current.getContents());
        TreeSet<String> splitFiles = new TreeSet<>(split.getContents());
        TreeMap<String, String> mergeMap = new TreeMap<>(split.getSnapshot());

        mergeFromCurrent(currentFiles, givenFiles, splitFiles, current, mergeMap);
        mergeSameInBoth(currentFiles, givenFiles, splitFiles, current, given, split, mergeMap);
        mergeFromGiven(currentFiles, givenFiles, splitFiles, current, given, split, mergeMap);
        mergeOnlyPresentInOne(currentFiles, givenFiles, splitFiles,
                current, given, split, mergeMap);

        boolean hasConflicts = mergeConflicts(currentFiles, givenFiles, splitFiles,
                current, given, split, mergeMap);

        Commit mergedCommit = new Commit(branchName, current.getSHA1(), given.getSHA1(), mergeMap);
        mergedCommit.saveCommit();
        Branch.moveBranchHead(mergedCommit);
        Staging.resetStaging();
        if (hasConflicts) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** Any files that were not present at the split point and are present only in the current
     * branch should remain as they are. Adds them to the merged commit's snapshot.*/
    private static void mergeFromCurrent(TreeSet<String> currentFiles,
                                         TreeSet<String> givenFiles,
                                         TreeSet<String> splitFiles,
                                         Commit current,
                                         TreeMap<String, String> mergeMap) {

        Set<String> filesInCurrent = new TreeSet<>(currentFiles);
        filesInCurrent.removeAll(splitFiles);
        filesInCurrent.removeAll(givenFiles);
        for (String fileName : filesInCurrent) {
            mergeMap.put(fileName, current.getFileSHA(fileName));
        }
    }

    /** Any files that have been modified in both the current and given branch in the same way
     * (i.e., both files now have the same content or were both removed) are left unchanged
     * by the merge. */
    private static void mergeSameInBoth(TreeSet<String> currentFiles,
                                        TreeSet<String> givenFiles,
                                        TreeSet<String> splitFiles,
                                        Commit current, Commit given, Commit split,
                                        TreeMap<String, String> mergeMap) {

        Set<String> filesInCurrAndGiven = new TreeSet<>(currentFiles);
        filesInCurrAndGiven.retainAll(givenFiles);
        // Same file in current and given, different or absent at split point
        for (String fileName: filesInCurrAndGiven) {
            if (current.getFileSHA(fileName).equals(given.getFileSHA(fileName))) {
                if (!current.getFileSHA(fileName).equals(split.getFileSHA(fileName))) {
                    mergeMap.put(fileName, current.getFileSHA(fileName));
                }
            }
        }
        // File absent in current and given commits, but present at split point
        for (String fileName: splitFiles) {
            if (!currentFiles.contains(fileName) && !givenFiles.contains(fileName)) {
                mergeMap.remove(fileName);
            }
        }
    }

    /** Stages non-conflicting files from the given branch. */
    private static void mergeFromGiven(TreeSet<String> currentFiles,
                                       TreeSet<String> givenFiles,
                                       TreeSet<String> splitFiles,
                                       Commit current, Commit given, Commit split,
                                       TreeMap<String, String> mergeMap) {

        for (String fileName: givenFiles) {
            String sha = given.getFileSHA(fileName);
            // Files present in current commit and at split point:
            if (currentFiles.contains(fileName) && splitFiles.contains(fileName)) {
                String shaAtCurrent = current.getFileSHA(fileName);
                String shaAtSplit = split.getFileSHA(fileName);
                // Files that were modified in the given branch since the split point but are the
                // same in the current branch as at the split are checked out from the given commit
                // and staged.
                if (!sha.equals(shaAtSplit) && shaAtCurrent.equals(shaAtSplit)) {
                    mergeMap.put(fileName, sha);
                    checkoutFromCommit(given.getSHA1(), fileName);
                    addFile(fileName);
                }
            }
            // Files that were not present at the split point and are present only in the given
            // branch are checked out from the given commit and staged.
            if (!splitFiles.contains(fileName) && !currentFiles.contains(fileName)) {
                mergeMap.put(fileName, sha);
                checkoutFromCommit(given.getSHA1(), fileName);
                addFile(fileName);
            }
        }
    }

    /** Handles files present at split point, unchanged in one branch and removed in the other. */
    private static void mergeOnlyPresentInOne(TreeSet<String> currentFiles,
                                              TreeSet<String> givenFiles,
                                              TreeSet<String> splitFiles,
                                              Commit current, Commit given, Commit split,
                                              TreeMap<String, String> mergeMap) {
        for (String fileName: splitFiles) {
            // Any files present at the split point, unmodified in the current branch,
            // and absent in the given branch are removed (and untracked).
            if (!givenFiles.contains(fileName)
                    && split.getFileSHA(fileName).equals(current.getFileSHA(fileName))) {
                mergeMap.remove(fileName);
                removeFile(fileName);
            }
            // Any files present at the split point, unmodified in the given branch,
            // and absent in the current branch should remain absent.
            if (!currentFiles.contains(fileName)
                    && split.getFileSHA(fileName).equals(given.getFileSHA(fileName))) {
                mergeMap.remove(fileName);
            }
        }
    }

    /** Handles conflicts. Returns true if any conflicts were found, false otherwise.
     * Any files modified in different ways in the current and given branches are in conflict.
     * "Modified in different ways" can mean:
     *      - (1) the contents of both are changed and different from other,
     *      - (2) the contents of one are changed and the other file is deleted,
     *      - (3) the file was absent at the split point and has different contents
     *      in the given and current branches. */
    private static boolean mergeConflicts(TreeSet<String> currentFiles,
                                          TreeSet<String>  givenFiles,
                                          TreeSet<String> splitFiles,
                                          Commit current, Commit given, Commit split,
                                          TreeMap<String, String> mergeMap) {

        boolean foundConflict = false;
        // For files present at the split point:
        for (String fileName: splitFiles) {
            String curr = current.getFileSHA(fileName);
            String givn = given.getFileSHA(fileName);
            String splt = split.getFileSHA(fileName);
            if (currentFiles.contains(fileName) && givenFiles.contains(fileName)
                    && (!curr.equals(givn) && !curr.equals(splt) && !givn.equals(splt))) {
                // case (1)
                Blob.writeConflict(CWD, fileName, curr, givn);
            } else if ((curr == null && givn != null && !givn.equals(splt))
                    || (givn == null && curr != null && !curr.equals(splt))) {
                // case (2)
                Blob.writeConflict(CWD, fileName, curr, givn);
            } else {
                continue;
            }
            foundConflict = true;
            Blob conflicted = new Blob(fileName);
            conflicted.saveBlob();
            mergeMap.put(fileName, conflicted.getSHA1());
        }
        // case (3)
        // Files not present at the split point:
        // Make a set of files not present in the split point, but present in both the current
        // and the given commit
        TreeSet<String> notSplit = new TreeSet<>(currentFiles);
        notSplit.removeAll(splitFiles);
        notSplit.retainAll(givenFiles);
        for (String fileName: notSplit) {
            String curr = current.getFileSHA(fileName);
            String givn = given.getFileSHA(fileName);
            if (!curr.equals(givn)) {
                foundConflict = true;
                Blob.writeConflict(CWD, fileName, curr, givn);
                Blob conflicted = new Blob(fileName);
                conflicted.saveBlob();
                mergeMap.put(fileName, conflicted.getSHA1());
            }
        }
        return foundConflict;
    }
}
