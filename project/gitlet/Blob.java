package gitlet;

import java.io.File;
import java.io.Serializable;

/** Represents a gitlet blob object. Blobs are the saved contents of files.
 * Since Gitlet saves many versions of files, a single file might correspond to
 * multiple blobs: each being tracked in a different commit.
 *
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *  TODO: don't forget the description
 *
 *  @author jul
 */
public class Blob implements Serializable {

    /** Path to directory that stores all Blobs. */
    private static final File BLOB_DIR = Paths.BLOB_DIR;

    /** The SHA1 of this Blob.
     * If two Blobs have the same SHA1, we assume their contents are the same.
     */
    private final String SHA1;

    /** The first two characters in the Blob's SHA1.
     * Used to create a folder inside BLOB_DIR to store this commit.
     */
    private final String prefix;

    /** The name of the file in this Blob. */
    private final String name;

    /** The contents of the Blob. */
    private final byte[] contents;

    /** Returns the SHA1 of this Blob. */

    public String getSHA1() {
        return SHA1;
    }

    /** Returns the prefix of this Blob. */
    public String getPrefix() {
        return prefix;
    }

    /** Creates a Blob of the given file.
     * @param fileName name of the file
     */
    public Blob(String fileName) {
        File target = Utils.join(Paths.CWD, fileName);
        contents = Utils.readContents(target);
        name = fileName;
        SHA1 = Utils.sha1(contents);
        prefix = SHA1.substring(0,2);
    }

    /** Saves this Blob to BLOB_DIR, inside a folder denoted by its prefix.
     * The name of this saved file is the SHA1 of the Blob.
     */
    public void saveBlob() {
        File destination = Utils.join(BLOB_DIR, prefix);
        destination.mkdir();
        File newFile = Utils.join(destination, SHA1);
        Utils.createFile(newFile);
        Utils.writeObject(newFile, this);
    }
}
