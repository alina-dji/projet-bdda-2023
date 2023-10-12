package CODE.MAIN;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.io.*;

public class DiskManager {
    private static DiskManager instance;
    private final String fileName;
    private final int pageSize;
    private int pageCount;

    public DiskManager(String fileName, int pageSize) {
        this.fileName = fileName;
        this.pageSize = DBParams.SGBDPageSize;
        this.pageCount = 0;
    }

    public PageId AllocPage() {
        PageId DPageId;
        String newFileName = "F" + pageCount;
        File newFile = new File(newFileName);

        // Créez un nouveau fichier et augmentez la taille du fichier pour ajouter une page
        try {
            RandomAccessFile file = new RandomAccessFile(newFile, "rw");
            file.setLength(pageSize);
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Créez un PageId pour la nouvelle page
        PageId newPageId = new PageId(newFileName, pageCount);

        // Mettez à jour le compteur de pages allouées
        pageCount++;

        return newPageId;
    }

    public void ReadPage(PageId pageId, byte[] buff) {
        long offset = (long) pageId.getPageNumber() * pageSize;
        try (RandomAccessFile file = new RandomAccessFile(fileName, "r")) {
            file.seek(offset);
            int bytesRead = file.read(buff);
            if (bytesRead < pageSize) {
                // Gérez la lecture incomplète si nécessaire
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void WritePage(PageId pageId, byte[] buff) {
        long offset = (long) pageId.getPageNumber() * pageSize;
        try (RandomAccessFile file = new RandomAccessFile(fileName, "rw")) {
            file.seek(offset);
            file.write(buff);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void DeallocPage(PageId pageId) {
        // Implémentez la logique de désallocation de page ici
    }

    public int GetCurrentCountAllocPages() {
        // Implémentez la logique pour compter les pages allouées ici
        return 0;
    }
}
