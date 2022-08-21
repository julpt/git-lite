package gitlet;

import java.io.File;
import java.util.TreeSet;
import java.util.TreeMap;

/** Handles most interactions with staged files or the Staging Area. */
public class Staging {

    /** Current working directory. */
    public static final File CWD = Paths.CWD;

    /** Directory of files staged for addition. */
    public static final File STAGE_DIR = Paths.STAGE_DIR;

    /** File that tracks items added to the staging area. Maps file names to Blob SHA1s. */
    public static final File INDEX = Paths.INDEX;

    /** File that tracks items staged for removal. */
    public static final File REMOVED = Paths.REMOVED;


    /** Adds a copy of the file as it currently exists to the staging area.
     * If the file is already staged, it is overwritten.
     *
     * If the current working version of the file is identical to the version
     * in the current commit, it won't be staged, and will be removed from the
     * staging area if it is already there.
     *
     * The file will no longer be staged for removal, if it was at the time of the command. */
    public static void stageFile(String fileName) {
        Blob addedFile = new Blob(fileName);
        TreeMap<String, String> stagedFiles = getStagedIndex();
        File destination = Utils.join(STAGE_DIR, addedFile.getSHA1());

        /* If file was staged for removal, it will be unstaged. */
        TreeSet<String> removedFiles = getRemoved();
        if (removedFiles.remove(fileName)) {
            Utils.writeObject(REMOVED, removedFiles);
        }

        /* Check if a version of the file is already staged. If so, it will get deleted.
        The new version will be staged, unless it is reverting to the current commit. */
        String stagedSHA = stagedFiles.get(fileName);
        if (stagedSHA != null) {
            Utils.join(STAGE_DIR, stagedSHA).delete();
        }

        /* Check if the new version is the same as the one in the current commit.
        In that case, this version of the file isn't saved and its reference in
        the index is removed.
        Otherwise, the file is added to the staging area and to the index. */
        String currentSHA = Branch.getHeadCommit().getFileSHA(fileName);
        if (addedFile.getSHA1().equals(currentSHA)) {
            stagedFiles.remove(fileName);
            Utils.writeObject(INDEX, stagedFiles);
        } else {
            // Add file to Staging area.
            Utils.writeObject(destination, addedFile);

            // Add file name and SHA1 to index.
            stagedFiles.put(fileName, addedFile.getSHA1());
            Utils.writeObject(INDEX, stagedFiles);
        }
    }

    /** Unstages the file if it is currently staged for addition.
     * Returns true if the file was unstaged, false otherwise. */
    private static boolean unstage(String fileName) {
        TreeMap<String, String> stagedFiles = getStagedIndex();
        if (stagedFiles.containsKey(fileName)) {
            stagedFiles.remove(fileName);
            Utils.writeObject(INDEX, stagedFiles);
            return true;
        }
        return false;
    }

    /** If the file is tracked in the current commit, stages it for removal and
     * removes it from the working directory.
     * Returns true if the file was staged for removal, false otherwise. */
    private static boolean stageForRemoval(String fileName) {
        String fileInCurrentCommit = Branch.getHeadCommit().getFileSHA(fileName);
        if (fileInCurrentCommit != null) {
            TreeSet<String> removed = getRemoved();
            if (removed.add(fileName)) {
                Utils.writeObject(REMOVED, removed);
                File fileToDelete = Utils.join(CWD, fileName);
                Utils.restrictedDelete(fileToDelete);
                return true;
            }
        }
        return false;
    }

    /** Unstages the file if it is currently staged for addition.
     * If the file is tracked in the current commit, stages it for removal and removes the file
     * from the working directory if the user has not already done so.
     *
     * If the file is neither staged nor tracked by the head commit, prints an error message. */
    public static void removeFile(String fileName) {
        if (!unstage(fileName) && !stageForRemoval(fileName)) {
            Utils.printAndExit("No reason to remove the file.");
        }
    }

    /** Clears the Staging directory.
     * Creates an empty [index] file to track files staged for addition.
     * Also creates an empty [removed] file to track files staged for removal.
     * If these files exist, they are reset. */
    public static void resetStaging() {
        clearStaging();
        Utils.writeObject(INDEX, new TreeMap<String, String>());
        Utils.writeObject(REMOVED, new TreeSet<String>());
    }


    /** Deletes all files in the Staging directory. Used after a commit. */
    private static void clearStaging() {
        File[] stagedContents = STAGE_DIR.listFiles();
        if (stagedContents != null) {
            for (File stagedFile: stagedContents) {
                stagedFile.delete();
            }
        }
    }

    /** Returns true if any files have been staged for addition or removal. False otherwise. */
    public static boolean checkStaged() {
        return !(getStagedIndex().size() == 0 && getRemoved().size() == 0);
    }

    /** Returns a map of the staged file names and their respective SHA1s. */
    @SuppressWarnings("unchecked")
    public static TreeMap<String, String> getStagedIndex() {
        return Utils.readObject(INDEX, TreeMap.class);
    }

    /** Returns a set of files staged to be removed. */
    @SuppressWarnings("unchecked")
    public static TreeSet<String> getRemoved() {
        return Utils.readObject(REMOVED, TreeSet.class);
    }

}
