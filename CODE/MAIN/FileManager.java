package Main;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class FileManager {
    private static FileManager instance;

    private FileManager() {
        /*  Empêcher l'instanciation directe de la classe FileManager
        utilisée pour mettre en œuvre le modèle de conception singleton */
    }

    public static FileManager getInstance() {
        if (instance == null) {
            instance = new FileManager();
        }
        return instance;
    }

    public PageId createNewHeaderPage() throws IOException {
        // Utiliser AllocPage du DiskManager pour allouer une nouvelle page
        PageId headerPageId = DiskManager.getInstance().allocPage();

        // Récupérer la page via le BufferManager pour pouvoir l'écrire
        byte[] headerPageData = BufferManager.getInstance().getPage(headerPageId);

        // Écrire deux PageIds factices dans la page
        PageId emptyListPageId = new PageId(-1, 0);  // PageId factice pour une liste vide
        writePageIdToBuffer(headerPageData, 0, emptyListPageId);  // Premier PageId
        writePageIdToBuffer(headerPageData, PageId.getSizeInBytes(), emptyListPageId);  // Deuxième PageId

        // Libérer la page auprès du BufferManager avec le flag dirty
        BufferManager.getInstance().freePage(headerPageId, true);

        return headerPageId;
    }

    private void writePageIdToBuffer(byte[] buffer, int offset, PageId pageId) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, offset, PageId.getSizeInBytes());
        byteBuffer.putInt(pageId.getFileIdx());
        byteBuffer.putInt(pageId.getPageIdx());
    }

    public PageId addDataPage(TableInfo tabInfo) throws IOException {
        // Allocation d'une nouvelle page via AllocPage du DiskManager
        PageId newDataPageId = DiskManager.getInstance().allocPage();

        // Récupération du buffer de la nouvelle page via le BufferManager
        byte[] newDataPageData = BufferManager.getInstance().getPage(newDataPageId);

        // TODO : Chaîner la page dans la liste des pages "où il reste de la place"
        // Vous devez lire et écrire des PageIds dans la nouvelle page et éventuellement dans d'autres pages selon votre stratégie

        // Libération de la page auprès du BufferManager avec le bon flag dirty
        BufferManager.getInstance().freePage(newDataPageId, true);

        return newDataPageId;
    }

    public PageId getFreeDataPageId(TableInfo tabInfo, int sizeRecord) throws IOException {
        // Récupérer l'identifiant de la Header Page de la relation
        PageId headerPageId = tabInfo.getHeaderPageId();

        // Récupérer la page via le BufferManager
        byte[] headerPageData = BufferManager.getInstance().getPage(headerPageId);

        // Lire les PageIds des listes "où il reste de la place" et "pages pleines" depuis la Header Page
        PageId freeListPageId = readPageIdFromBuffer(headerPageData, 0);
        PageId fullListPageId = readPageIdFromBuffer(headerPageData, PageId.getSizeInBytes());

        // Parcourir la liste "où il reste de la place"
        while (!freeListPageId.isNull()) {
            // Récupérer la page via le BufferManager
            byte[] freeListPageData = BufferManager.getInstance().getPage(freeListPageId);

            // TODO : Vérifier s'il y a suffisamment d'espace sur cette page pour insérer le record
            // Vous devrez lire le slot directory de la page pour obtenir des informations sur l'espace libre

            // Si l'espace est suffisant, libérer la page auprès du BufferManager et la retourner
            if (isSpaceAvailable(freeListPageData, sizeRecord)) {
                BufferManager.getInstance().freePage(freeListPageId, false); // false car la page n'est pas modifiée
                return freeListPageId;
            }

            // Passer à la page suivante dans la liste
            freeListPageId = readPageIdFromBuffer(freeListPageData, 0);
        }

        // Si aucune page n'a suffisamment d'espace, retourner null
        return null;
    }

    private boolean isSpaceAvailable(byte[] pageData, int sizeRecord) {
        // TODO : Implémenter la logique pour vérifier si la page a suffisamment d'espace pour le record
        // Vous devrez lire le slot directory de la page pour obtenir des informations sur l'espace libre
        return true; // Placeholder, ajustez selon votre implémentation
    }

    private PageId readPageIdFromBuffer(byte[] buffer, int offset) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, offset, PageId.getSizeInBytes());
        int fileIdx = byteBuffer.getInt();
        int pageIdx = byteBuffer.getInt();
        return new PageId(fileIdx, pageIdx);
    }

    public RecordId writeRecordToDataPage(Record record, PageId pageId) throws IOException {
        // Récupérer le buffer de la page de données via le BufferManager
        byte[] dataPageData = BufferManager.getInstance().getPage(pageId);

        // Créer un ByteBuffer à partir des données de la page de données
        ByteBuffer dataPageBuffer = ByteBuffer.wrap(dataPageData);

        // Trouver l'indice du prochain slot disponible
    int nextSlotIdx = dataPageBuffer.getInt(DBParams.SGBDPageSize - 8);

        // Écrire l'enregistrement dans la page de données
        record.writeToBuffer(dataPageBuffer, 0); // A reverifier en cas d'erreur (l'offset)

        // Libérer la page auprès du BufferManager avec le bon flag dirty
        BufferManager.getInstance().freePage(pageId, true);



        // Retourner l'identifiant du record
        return new RecordId(pageId, nextSlotIdx);
    }

     public ArrayList<Record> getRecordsInDataPage(TableInfo tabInfo, PageId pageId) throws IOException {
        // Récupérer le buffer de la page de données via le BufferManager
        byte[] dataPageData = BufferManager.getInstance().getPage(pageId);

        // Créer un ByteBuffer à partir des données de la page de données
        ByteBuffer dataPageBuffer = ByteBuffer.wrap(dataPageData);

        // Trouver le nombre de slots dans la page
        int numSlots = dataPageBuffer.getInt(DBParams.SGBDPageSize - 8);

        // Liste pour stocker les records
        ArrayList<Record> recordsList = new ArrayList<>();

        // Parcourir tous les slots et lire les records
        for (int i = 0; i < numSlots; i++) {
            // Lire la position du début du record depuis le slot directory
            int slotIdx = i * 8;
            int recordStartPos = dataPageBuffer.getInt(DBParams.SGBDPageSize - 8 - slotIdx);

            // Créer un nouveau record et le lire depuis la page de données
            Record record = new Record(tabInfo);
            record.readFromBuffer(dataPageBuffer, recordStartPos);

            // Ajouter le record à la liste
            recordsList.add(record);


        }

                    // Libérer la page auprès du BufferManager
            BufferManager.getInstance().freePage(pageId, false);

            return recordsList;
        
    }

    public ArrayList<PageId> getDataPages(TableInfo tabInfo) throws IOException {
        // Récupérer le PageId de la Header Page de la relation
        PageId headerPageId = tabInfo.getHeaderPageId();

        // Récupérer le buffer de la Header Page via le BufferManager
        byte[] headerPageData = BufferManager.getInstance().getPage(headerPageId);

        // Créer un ByteBuffer à partir des données de la Header Page
        ByteBuffer headerPageBuffer = ByteBuffer.wrap(headerPageData);

        // Lire le nombre de pages de données depuis la Header Page
        int numDataPages = headerPageBuffer.getInt(0);

        // Liste pour stocker les PageIds des pages de données
        ArrayList<PageId> dataPagesList = new ArrayList<>();

        // Parcourir les entrées de la Header Page et ajouter les PageIds à la liste
        for (int i = 0; i < numDataPages; i++) {
            // Lire le PageId de la i-ème page de données
            PageId dataPageId = readPageIdFromBuffer(headerPageBuffer, 4 + i * PageId.getSizeInBytes());

            // Ajouter le PageId à la liste
            dataPagesList.add(dataPageId);
        }

        // Libérer la Header Page auprès du BufferManager
        BufferManager.getInstance().freePage(headerPageId, false);

        return dataPagesList;
    }

    // Méthode utilitaire pour lire un PageId depuis un ByteBuffer
    private PageId readPageIdFromBuffer(ByteBuffer buffer, int offset) {
        int fileIdx = buffer.getInt(offset);
        int pageIdx = buffer.getInt(offset + Integer.BYTES);
        return new PageId(fileIdx, pageIdx);
    }



    public RecordId insertRecordIntoTable(Record record) throws IOException {
        
        // Obtenez l'identifiant de la page de données libre
        PageId freeDataPageId = getFreeDataPageId(record.getTableInfo(), record.recordSizeFromValues());

        // Écrivez l'enregistrement dans la page de données
        RecordId recordId = writeRecordToDataPage(record, freeDataPageId);

        // Associez l'identifiant de l'enregistrement à l'enregistrement lui-même
        record.setRecordId(recordId);

        return recordId;
    }

    public ArrayList<Record> getAllRecords(TableInfo tabInfo) throws IOException {
        // Récupérer la liste des PageIds des pages de données pour la relation
        ArrayList<PageId> dataPagesList = getDataPages(tabInfo);

        // Liste pour stocker tous les records de la relation
        ArrayList<Record> allRecordsList = new ArrayList<>();

        // Parcourir les pages de données pour récupérer tous les records
        for (PageId dataPageId : dataPagesList) {
            // Récupérer la liste des records dans la page de données
            ArrayList<Record> recordsInDataPage = getRecordsInDataPage(tabInfo, dataPageId);

            // Ajouter les records à la liste générale
            allRecordsList.addAll(recordsInDataPage);
        }

        return allRecordsList;
    }



}
