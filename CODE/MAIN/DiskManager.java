package CODE.MAIN;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Stack;
import java.io.File;


public class DiskManager {
    private static DiskManager instance;
    private final String fileName;
    private final int pageSize;
    private int CompteurPageAlloue;
    private Stack<PageId> PagesDesallouees; // Pile pour gérer les pages désallouées
    private File[] dataFiles; // Tableau de fichiers de données


    public DiskManager(String fileName, int pageSize) {
        this.fileName = fileName;
        this.pageSize = DBParams.SGBDPageSize;
        this.CompteurPageAlloue = 0;
    }
    public static DiskManager getInstance(String fileName, int pageSize) {
        if (instance == null) {
            instance = new DiskManager(fileName, pageSize);
        }
        return instance;
    }
    /**
     * @return
     * @throws IOException
     */
    
    public PageId AllocPage() throws IOException {
        if (!PagesDesallouees.empty()) {
            // S'il y a des pages désallouées disponibles, utilisez-en une
            PageId PageDesallouee = PagesDesallouees.pop();
            CompteurPageAlloue++;
            return PageDesallouee;
        }

        // trouver l'indice du ficher le plus leger
        int legerFileId = -1; // Initialisez-le à -1 pour gérer le cas où aucun fichier n'est encore créé
        long tailleMin = Long.MAX_VALUE; // Initialisation avec une valeur maximale
    
        for (int i = 0; i < DBParams.DMFileCount; i++) {
            File file = dataFiles[i];
    
            // Si le fichier n'existe pas encore, c'est le plus léger
            if (!file.exists()) {
                legerFileId = i;
                break;
            }
    
            long tailleFichier = file.length();
            if (tailleFichier < tailleMin) {
                legerFileId = i;
                tailleMin = tailleFichier;
            }
        }
    
        if (legerFileId == -1) {
            // Aucun fichier n'est encore créé, utilisez le premier
            legerFileId = 0;
        }
        File newFile = dataFiles[legerFileId];
    
        // Créez un nouveau fichier et augmentez la taille du fichier pour ajouter une page
        RandomAccessFile raf = new RandomAccessFile(newFile, "rw");
        raf.seek(newFile.length());
        raf.write(new byte[pageSize]); // Remplissez avec des octets nuls
        raf.close();
    
        CompteurPageAlloue++; // est ce qu'il serait preferable de l'ajouter dans la classPageId ?? pour savoir le nb de fois qu'on a eu acces a une page 
    
        // Créez un PageId pour la nouvelle page
        return new PageId(legerFileId, (int) (newFile.length() / DBParams.SGBDPageSize) - 1);
    }
    
    /**
     * @param pageId
     * @param buff
     * @param exception
     * @throws IOException
     */
    
    public void ReadPage(PageId pageId, byte[] buff, IOException exception) throws IOException {
        long offset = (long) pageId.getPageIdx() * pageSize;
        RandomAccessFile file = new RandomAccessFile(fileName, "r");
        file.seek(offset);
        int bytesRead = file.read(buff);
        if (bytesRead < pageSize) {
            file.close();
            throw new IOException("Lecture incomplète de la page");

         }
        file.close();
    }

        
    public void WritePage(PageId pageId, byte[] buff) throws IOException {
        long offset = (long) pageId.getPageIdx() * pageSize;
        RandomAccessFile file = null;
    
        try {
            file = new RandomAccessFile(fileName, "rw");
            file.seek(offset);
            file.write(buff);
        } finally {
            if (file != null) {
                file.close();
            }
        }
    }

    public void DeallocPage(PageId pageId) throws IOException {
        if (pageId == null) {
            throw new IllegalArgumentException("PageId ne peut pas être nul.");
        }
    
        if (!isPageAllocated(pageId)) {
            throw new IllegalArgumentException("La page n'est pas allouée.");
        }
    
        // Générer le nom du fichier à partir de l'index du fichier
        String fileName = DBParams.DBPath + "/file" + pageId.toString();
    
        // Marquez la page comme désallouée
        File pageFile = new File(fileName);
        if (pageFile.exists() && pageFile.delete()) {
            PagesDesallouees.push(pageId);
            CompteurPageAlloue--; // Décrémentez le compteur après la désallocation
        } else {
            throw new IOException("Erreur lors de la désallocation de la page.");
        }
    }
    
    private boolean isPageAllocated(PageId pageId) {
        // Générer le nom du fichier à partir de l'index du fichier
        String fileName = DBParams.DBPath + "/file" + pageId.getFileIdx();
    
        File pageFile = new File(fileName);
        return pageFile.exists() && pageFile.length() == pageSize;
    }
    
    
    public int GetCurrentCountAllocPages(){
        return CompteurPageAlloue;
    }

}
