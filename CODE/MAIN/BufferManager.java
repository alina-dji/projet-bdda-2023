package Main;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;

public class BufferManager {
    private List<Frame> buffer;
    private static BufferManager instance;
    private DiskManager diskManager;

    public BufferManager() {
        buffer = new ArrayList<>(DBParams.frameCount);
        // Initialisation liste de frames dans le buffer
        for (int i = 0; i < DBParams.frameCount; i++) {
            buffer.add(new Frame());
        }
        diskManager = new DiskManager();

    }

    //Oprimisation = 1 seule instance
    public static BufferManager getInstance() {
        if (instance == null) {
            instance = new BufferManager();
        }
        return instance;
    }

    public byte[] getPage(PageId pageId) {
        Frame frame = findFrame(pageId);

        if (frame == null) {
            // La page n'est pas dans le buffer, il faut la charger depuis le disque
            frame = loadPage(pageId);
        }

        // Incrémenter le pin_count, car la page est maintenant utilisée
        frame.incrementAccessCount();
        frame.incrementPinCount();
        
        return frame.buffer;
    }

    // Méthode pour libérer une page
    public void freePage(PageId pageId, boolean valdirty) {
        Frame frame = findFrame(pageId);

        if (frame != null) {
            frame.decrementPinCount();


            // Actualiser le flag dirty si nécessaire
            frame.dirty = valdirty;
        }
    }

    // Méthode pour vider le buffer
    public void FlushAll() {
        for (Frame frame : buffer) {
            if (frame.dirty) {
                // Écrire la page sur le disque
                writePageToDisk(frame.pageId, frame.buffer);
                // Réinitialiser le flag dirty
                frame.dirty = false;
            }
            // possibilite de Réinitialiser d'autres informations
            frame.reset();
        }
    }

    // Autres méthodes privées utiles
    public Frame findFrame(PageId pageId) {
        for (Frame frame : buffer) {
            if (frame.pageId != null && frame.pageId.equals(pageId)) {
                return frame;
            }
        }
        return null;
    }

    /* Deuxieme proposition : assure toi que la methode equals est bien implementee
    
    private Frame findFrame(PageId pageId) {
    return buffer.stream()
            .filter(frame -> frame.pageId != null && frame.pageId.equals(pageId))
            .findFirst()
            .orElse(null); */


    private Frame loadPage(PageId pageId) {
        // Utiliser les fonctions de DiskManager pour charger la page depuis le disque
        byte[] buff = new byte[DBParams.SGBDPageSize];
        
        try {
        diskManager.readPage(pageId, buff);
        } catch (IOException e) {
        System.err.println("Erreur lors de lecture de la page sur le disque : " + e.getMessage());  // Vous pouvez gérer l'exception de manière appropriée, par exemple, en journalisant ou en affichant un message d'erreur.
        return null;
        }

        // Créer une nouvelle frame avec la page chargée
        Frame frame = new Frame();
        frame.pageId = pageId;
        frame.buffer = buff;
        buffer.add(frame);

        return frame;
    }

    private void writePageToDisk(PageId pageId, byte[] buff) {
        // Utiliser les fonctions de DiskManager pour écrire la page sur le disque
        try {
            diskManager.writePage(pageId, buff);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture de la page sur le disque : " + e.getMessage());
        }
    }

    public void buffPoolContenu() {
        System.out.println("Contenu du tampon:");

        for (int i = 0; i < buffer.size(); i++) {
            Frame frame = buffer.get(i);
            PageId pageId = frame.pageId;
            boolean isDirty = frame.dirty;
            int pinCount = frame.pinCount;
            int accessCount = frame.accessCount;

            System.out.println("Frame " + i + ":");
            System.out.println("  PageId: " + (pageId != null ? pageId.toString() : "null"));
            System.out.println("  Dirty: " + isDirty);
            System.out.println("  PinCount: " + pinCount);
            System.out.println("  AccessCount: " + accessCount);
        }
    }

}
