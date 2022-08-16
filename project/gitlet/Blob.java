package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {

    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Blob class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** Path to directory that stores all Blobs. */
    private static final File BLOB_DIR = Paths.BLOB_DIR;

    /** The SHA1 of this Blob.
     * If two Blobs have the same SHA1, we assume their contents are the same.
     */
    private final String SHA1;

    /** The first two characters in the Blob's SHA1.
     * Used to create a folder inside .gitlet/blobs to store this commit.
     */
    private final String prefix;

    /** The name of the file in this Blob. */
    private final String name;

    /** The contents of the Blob. */
    private final byte[] contents;

    public String getSHA1() {
        return SHA1;
    }

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

    public void saveBlob() {
        File destination = Utils.join(BLOB_DIR, prefix);
        destination.mkdir();
        File newFile = Utils.join(destination, SHA1);
        Utils.createFile(newFile);
        Utils.writeObject(newFile, this);
    }
}
