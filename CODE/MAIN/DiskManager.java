package Main;

import java.util.Random;
import java.util.Stack;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class DiskManager {
    private static DiskManager instance = new DiskManager();

    public DiskManager() {

    }

    public static DiskManager getInstance() {
        return instance;
    }

    // une stack pour gérer les pages désallouées
    private Stack<PageId> deallocatedPages = new Stack<PageId>();

    // allocatedPagesCount devrait être dans la classe PageId en tant que static
    // attribut
    private int allocatedPagesCount = 0;

    private String fileExtension = ".data"; // à mettre dans la classe DBParams

    public PageId allocPage() throws IOException {
        
        allocatedPagesCount++; // le cpt de pages allouées est incrémenté dès qu'on fait appel à cette méthode
        PageId newPageId; // le PageId de la page nouvellement allouée qui sera retourné par la méthode
        File file; // cet objet sera utilisé plusieurs fois dans cette méthode
        // lister tous les noms des fichiers qui sont dans le répertoire DB
        file = new File(DBParams.DBPath);
        String[] fileNames = file.list();
        // création d'un tableau qui contient la taille des fichiers du répertoire DB
        long[] fileSizes = new long[DBParams.DMFileCount]; // tableau de long initialisé à 0
        for (int i = 0; i < fileNames.length; i++) {
            int j = Integer.parseInt(fileNames[i].replaceAll("[^0-9]", ""));
            file = new File(DBParams.DBPath + fileNames[i]);
            fileSizes[j] = file.length();
        }
        // implémentation de l'algorithme d'allocation de pages
        if (!deallocatedPages.isEmpty()) {
            // Si une page désallouée précédemment est disponible, l’utiliser
            newPageId = deallocatedPages.pop();
            return newPageId;
        } else {
            // trouver l'indice du fichier le plus léger
            int lightestFileIndex = 0; // variable qui contient l'indice du fichier le plus léger
            for (int i = 0; i < fileSizes.length; i++) {
                if (fileSizes[i] < fileSizes[lightestFileIndex]) {
                    lightestFileIndex = i;
                }
            }
            String path = DBParams.DBPath + "F" + lightestFileIndex + fileExtension;
            // création du fichier ou modification d'un fichier existant
            if (fileSizes[lightestFileIndex] == 0L) {
                // si la taille du fichier est 0, le fichier n'existe pas, il faut le créer puis
                // faire l'allocation de la page
                try {
                    // création d'un nouveau fichier
                    File newFile = new File(path);
                    if (newFile.createNewFile()) {
                        System.out.println("Fichier créée : " + newFile.getName());
                    } else {
                        System.out.println("Fichier existe déjà : " + newFile.getName());
                    }
                    // allocation de la page dans le nouveau fichier créée
                    // FileIdx = indice du fichier et PageIdx = 0 car le fichier vient d'être créée
                    newPageId = new PageId(lightestFileIndex, 0);
                    RandomAccessFile raf = new RandomAccessFile(path, "rw");
                    // rajouter pageSize octets, avec une valeur quelconque, à la fin du fichier
                    raf.seek(raf.length());
                    Random rd = new Random();
                    byte[] randomByteArray = new byte[DBParams.SGBDPageSize];
                    rd.nextBytes(randomByteArray);
                    raf.write(randomByteArray);
                    raf.close();
                    return newPageId;
                } catch (IOException e) {
                    System.out.println("Erreur lors de la création du fichier");
                    e.printStackTrace();
                    newPageId = new PageId(-1, -1);
                    return newPageId;
                }
            } else {
                // sinon modifier un fichier existant (le plus léger) en allounat une page
                // PageIdx = taille du fichier / taille d'une page
                int pageIndex = (int) fileSizes[lightestFileIndex] / DBParams.SGBDPageSize;
                newPageId = new PageId(lightestFileIndex, pageIndex);
                RandomAccessFile raf = new RandomAccessFile(path, "rw");
                raf.seek(raf.length());
                Random rd = new Random();
                byte[] randomByteArray = new byte[DBParams.SGBDPageSize];
                rd.nextBytes(randomByteArray);
                raf.write(randomByteArray);
                raf.close();
                return newPageId;
            }
        }
    }

    public void readPage(PageId pageId, byte[] buff) throws IOException {
        String path = DBParams.DBPath + "F" + pageId.getFileIdx() + fileExtension;
        int offset = pageId.getPageIdx() * DBParams.SGBDPageSize;
        try {
            RandomAccessFile raf = new RandomAccessFile(path, "r");
            raf.read(buff, offset, DBParams.SGBDPageSize);
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void writePage(PageId pageId, byte[] buff) throws IOException {
        String path = DBParams.DBPath + "F" + pageId.getFileIdx() + fileExtension;
        int offset = pageId.getPageIdx() * DBParams.SGBDPageSize;
        try {
            RandomAccessFile raf = new RandomAccessFile(path, "rw");
            raf.write(buff, offset, buff.length);
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void deallocPage(PageId pageId) {
        deallocatedPages.push(pageId);
    }

    public int getCurrentCountAllocPages() {
        return allocatedPagesCount;
    }

    

}