package ca.etsmtl.gti785.lib.entity;

import java.io.File;
import java.util.UUID;

public class FileEntity {

    // Do not serialize file
    private transient File file;

    private UUID uuid;
    private String name;
    private Long size;

    public FileEntity(String path) {
        this(new File(path));
    }

    public FileEntity(String path, String name) {
        this(new File(path, name));
    }

    public FileEntity(File file) {
        this.file = file;

        name = file.getName();
        size = file.length();

        uuid = UUID.nameUUIDFromBytes(generateNameForUUID());
    }

    private byte[] generateNameForUUID() {
        return file.getAbsolutePath().getBytes();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    /**
     * Use {@link android.text.format.Formatter#formatFileSize} to render the human readable
     *
     * @see <a href="http://stackoverflow.com/a/26502430">http://stackoverflow.com/a/26502430</a>
     */
    public Long getSize() {
        return size;
    }
}
