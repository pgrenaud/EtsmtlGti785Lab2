package ca.etsmtl.gti785.lib.repository;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import ca.etsmtl.gti785.lib.entity.FileEntity;

public class FileRepository {

    private final Map<UUID, FileEntity> files;

    public FileRepository() {
        files = new ConcurrentHashMap<>();
    }

    public FileEntity get(UUID uuid) {
        return files.get(uuid);
    }

    public Collection<FileEntity> getAll() {
        return files.values();
    }

    public void add(FileEntity file) {
        files.put(file.getUuid(), file);
    }

    public void addAll(String path) {
        File f = new File(path);
        File[] files = f.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                add(new FileEntity(file));
            }
        }
    }

    public void remove(FileEntity file) {
        files.remove(file.getUuid());
    }

    public void removeAll() {
        files.clear();
    }
}
