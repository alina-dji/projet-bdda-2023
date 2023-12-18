package Main;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;
import java.util.Stack;

public class DiskManager {
    private static DiskManager instance;
    private Stack<PageId> deallocatedPages;
    private int allocatedPagesCount = 0;
    private String fileExtension = ".data";

    // Constructeur privé pour le singleton
    private DiskManager() {
        this.deallocatedPages = new Stack<>();
    }

    // Méthode pour obtenir l'instance unique du DiskManager (Singleton)
    public static DiskManager getInstance() {
        if (instance == null) {
            instance = new DiskManager();
        }
        return instance;
    }

    // Méthode pour allouer une nouvelle page
    public PageId allocPage() throws IOException {
        allocatedPagesCount++;
        PageId newPageId;
        File file = new File(DBParams.DBPath);
        String[] fileNames = file.list();
        long[] fileSizes = new long[DBParams.DMFileCount];

        // Obtention des tailles des fichiers existants dans le répertoire DB
        for (int i = 0; i < fileNames.length; i++) {
            int j = Integer.parseInt(fileNames[i].replaceAll("[^0-9]", ""));
            file = new File(DBParams.DBPath + fileNames[i]);
            fileSizes[j] = file.length();
        }

        // Vérification de la disponibilité des pages désallouées
        if (!deallocatedPages.isEmpty()) {
            newPageId = deallocatedPages.pop();
            return newPageId;
        } else {
            // Recherche de l'indice du fichier le plus léger
            int lightestFileIndex = 0;
            for (int i = 0; i < fileSizes.length; i++) {
                if (fileSizes[i] < fileSizes[lightestFileIndex]) {
                    lightestFileIndex = i;
                }
            }

            String path = DBParams.DBPath + "F" + lightestFileIndex + fileExtension;

            if (fileSizes[lightestFileIndex] == 0L) {
                try (RandomAccessFile raf = new RandomAccessFile(path, "rw")) {
                    // Création d'un nouveau fichier et allocation de la page
                    File newFile = new File(path);
                    if (newFile.createNewFile()) {
                        System.out.println("Fichier créé : " + newFile.getName());
                    } else {
                        System.out.println("Fichier existe déjà : " + newFile.getName());
                    }

                    newPageId = new PageId(lightestFileIndex, 0);
                    ByteBuffer randomBuffer = generateRandomByteBuffer(DBParams.SGBDPageSize);
                    raf.getChannel().write(randomBuffer);
                } catch (IOException e) {
                    System.out.println("Erreur lors de la création du fichier");
                    e.printStackTrace();
                    newPageId = new PageId(-1, -1);
                }
            } else {
                // Modification d'un fichier existant pour allouer une nouvelle page
                int pageIndex = (int) fileSizes[lightestFileIndex] / DBParams.SGBDPageSize;
                newPageId = new PageId(lightestFileIndex, pageIndex);

                try (RandomAccessFile raf = new RandomAccessFile(path, "rw")) {
                    raf.seek(raf.length());
                    ByteBuffer randomBuffer = generateRandomByteBuffer(DBParams.SGBDPageSize);
                    raf.getChannel().write(randomBuffer);
                }
            }

            return newPageId;
        }
    }

    // Méthode pour lire le contenu d'une page dans un ByteBuffer
    public void readPage(PageId pageId, ByteBuffer buffer) throws IOException {
        String path = DBParams.DBPath + "F" + pageId.getFileIdx() + fileExtension;
        int offset = pageId.getPageIdx() * DBParams.SGBDPageSize;

        try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
            raf.seek(offset);
            FileChannel channel = raf.getChannel();
            channel.read(buffer);
            buffer.flip(); // Prépare le ByteBuffer pour la lecture
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour écrire le contenu d'un ByteBuffer dans une page
    public void writePage(PageId pageId, ByteBuffer buffer) throws IOException {
        String path = DBParams.DBPath + "F" + pageId.getFileIdx() + fileExtension;
        int offset = pageId.getPageIdx() * DBParams.SGBDPageSize;

        try (RandomAccessFile raf = new RandomAccessFile(path, "rw")) {
            raf.seek(offset);
            FileChannel channel = raf.getChannel();
            channel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour désallouer une page
    public void deallocPage(PageId pageId) {
        deallocatedPages.push(pageId);
    }

    // Méthode pour obtenir le nombre actuel de pages allouées
    public int getCurrentCountAllocPages() {
        return allocatedPagesCount;
    }

    // Méthode utilitaire pour générer un ByteBuffer aléatoire
    private ByteBuffer generateRandomByteBuffer(int size) {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        new Random().nextBytes(buffer.array());
        return buffer;
    }
}
