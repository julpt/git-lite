package gitlet;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/** Represents a gitlet blob object. Blobs are the saved contents of files.
 * Since Gitlet saves many versions of files, a single file might correspond to
 * multiple blobs: each being tracked in a different commit.
 *
 *
 *  @author jul
 */
public class Blob implements Serializable {

    /** The current working directory. */
    public static final File CWD = Paths.CWD;

    /** Path to directory that stores all Blobs. */
    private static final File BLOB_DIR = Paths.BLOB_DIR;

    /** The SHA1 of this Blob.
     * If two Blobs have the same SHA1, we assume their contents are the same.
     */
    private final String sha1;

    /** The contents of the Blob. */
    private final byte[] contents;

    /** Returns the SHA1 of this Blob. */
    public String getSHA1() {
        return sha1;
    }

    /** Creates a Blob of the given file.
     * @param fileName name of the file, which must be in the current working directory.
     */
    public Blob(String fileName) {
        File target = Utils.join(CWD, fileName);
        contents = Utils.readContents(target);
        sha1 = Utils.sha1(contents);
    }

    /** Saves this Blob to BLOB_DIR.
     * The name of this saved file is the SHA1 of the Blob.
     */
    public void saveBlob() {
        File newFile = Utils.join(BLOB_DIR, sha1);
        Utils.writeObject(newFile, this);
    }

    /** Returns the Blob with the given SHA1. */
    public static Blob getFromSHA(String sha) {
        return Utils.readObject(Utils.join(BLOB_DIR, sha), Blob.class);
    }

    /** Saves the contents of this Blob to a file with the given name in the given directory. */
    public void writeContentsToFile(File directory, String fileName) {
        File file = Utils.join(directory, fileName);
        Utils.writeContents(file, contents);
    }

    /** Saves a "conflict" file to the given directory. Conflict files have a particular layout
     * and have the contents of both the current and the given versions of the named file.
     */
    public static void writeConflict(File directory, String fileName,
                                     String currentSha, String givenSha) {
        File file = Utils.join(directory, fileName);
        String currentContents = "";
        String givenContents = "";
        if (currentSha != null) {
            currentContents = new String(getFromSHA(currentSha).contents, StandardCharsets.UTF_8);
        }
        if (givenSha != null) {
            givenContents = new String(getFromSHA(givenSha).contents, StandardCharsets.UTF_8);
        }

        String merged = "<<<<<<< HEAD" + System.lineSeparator()
                + currentContents
                + "=======" + System.lineSeparator()
                + givenContents
                + ">>>>>>>" + System.lineSeparator();
        Utils.writeContents(file, merged);
    }
}
