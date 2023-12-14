package Main;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;


public class RecordIterator implements Iterator<Record>, AutoCloseable {
    private TableInfo tabInfo;
    private PageId pageId;
    private ByteBuffer dataPageBuffer;
    private int numSlots;
    private int currentSlotIdx;

     // Constructeur
     public RecordIterator(TableInfo tabInfo, PageId pageId) throws IOException {
        this.tabInfo = tabInfo;
        this.pageId = pageId;
        
        // Récupérer le buffer de la page de données via le BufferManager
        byte[] dataPageData = BufferManager.getInstance().getPage(pageId);
        this.dataPageBuffer = ByteBuffer.wrap(dataPageData);

        // Lire le nombre de slots dans la page
        this.numSlots = dataPageBuffer.getInt(DBParams.SGBDPageSize - 8);

        // Initialiser l'indice du slot actuel
        this.currentSlotIdx = 0;
    }


        // Méthode pour obtenir le prochain enregistrement
        public Record GetNextRecord() {
            if (currentSlotIdx < numSlots) {
                // Lire la position du début du record depuis le slot directory
                int slotIdx = currentSlotIdx * 8;
                int recordStartPos = dataPageBuffer.getInt(DBParams.SGBDPageSize - 8 - slotIdx);
    
                // Créer un nouveau record et le lire depuis la page de données
                Record record = new Record(tabInfo);
                record.readFromBuffer(dataPageBuffer, recordStartPos);
    
                // Mettre à jour l'indice du slot actuel
                currentSlotIdx++;
    
                return record;
            } else {
                // Aucun record restant
                return null;
            }
        }

         // Méthode pour signaler la fin d'utilisation de l'itérateur
    public void Close() {
        // Libérer la page auprès du BufferManager
        BufferManager.getInstance().freePage(pageId, false);
    }

    // Méthode pour remettre le curseur au début de la page
    public void Reset() {
        // Réinitialiser l'indice du slot actuel
        currentSlotIdx = 0;
    }

    // Méthode de l'interface Iterator
    @Override
    public boolean hasNext() {
        return currentSlotIdx < numSlots;
    }

    // Méthode de l'interface Iterator
    @Override
    public Record next() {
        return GetNextRecord();
    }

    // Méthode de l'interface AutoCloseable
    @Override
    public void close() {
        Close();
    }

    
}
