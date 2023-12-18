package Main;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class FileManager {
    private static FileManager leFileManager;

    private FileManager() {
        // Concept singleton : empêcher l'instanciation directe de la classe FileManager
    }

    public static FileManager getInstance() {
        if (leFileManager == null) {
            leFileManager = new FileManager();
        }
        return leFileManager;
    }

    public PageId createNewHeaderPage() throws IOException {
        PageId headerPageId = DiskManager.getInstance().allocPage(); // Utiliser AllocPage du DiskManager pour allouer
                                                                     // une nouvelle page
        ByteBuffer headerPageData = BufferManager.getInstance().getPage(headerPageId); // Récupérer la page via le
                                                                                       // BufferManager pour pouvoir
                                                                                       // l'écrire
        headerPageData.putInt(0, -1); // Premier PageId
        headerPageData.putInt(PageId.getSizeInBytes(), 0); // Deuxième PageId
        BufferManager.getInstance().freePage(headerPageId, true); // Libérer la page auprès du BufferManager avec le
                                                                  // flag dirty

        return headerPageId;
    }

    public PageId addDataPage(TableInfo tabInfo) throws IOException {
        PageId newDataPageId = DiskManager.getInstance().allocPage(); // Allocation d'une nouvelle page via AllocPage du
                                                                      // DiskManager
        ByteBuffer newDataPageData = BufferManager.getInstance().getPage(newDataPageId); // Récupération du buffer de la
                                                                                         // nouvelle page via le
                                                                                         // BufferManager

        /* DATA PAGE */
        DataPage.writeNextPageId(newDataPageData, newDataPageId); // Écrire l'identifiant de la page suivante au début
                                                                  // (octet 0), sinon PageId factice pour une liste vide
        BufferManager.getInstance().freePage(newDataPageId, true);// Libération de la page Data auprès du BufferManager
                                                                  // avec le bon flag dirty

        /* CHAÎNAGE DE PAGES */
        PageId headerPageId = tabInfo.getHeaderPageId();// Récupération de l'identifiant de la Header Page de la
                                                        // relation
        ByteBuffer headerPageData = BufferManager.getInstance().getPage(headerPageId); // Récupération du buffer de la
                                                                                       // Header Page via le
                                                                                       // BufferManager

        PageId freeListPageId = DataPage.readNextPageId(headerPageData); // Lire l'identifiant de la première page de la
                                                                         // liste "où il reste de la place"
        DataPage.writeNextPageId(headerPageData, newDataPageId); // Écrire l'identifiant de la nouvelle Data Page à la
                                                                 // première position de la liste
        DataPage.writeNextPageId(newDataPageData, freeListPageId); // Écrire l'ancien identifiant de la première page de
                                                                   // la liste à la suite de la nouvelle Data Page

        BufferManager.getInstance().freePage(headerPageId, true); // Libération de la page Header auprès du
                                                                  // BufferManager avec le bon flag dirty
        return newDataPageId;
    }

    public PageId getFreeDataPageId(TableInfo tabInfo, int sizeRecord) throws IOException {
        PageId headerPageId = tabInfo.getHeaderPageId();
        ByteBuffer headerPageData = BufferManager.getInstance().getPage(headerPageId);

        PageId currentPageId = DataPage.readNextPageId(headerPageData); // Récupérer l'identifiant de la première page
                                                                        // de données

        while (!currentPageId.isNull()) {
            ByteBuffer currentPageData = BufferManager.getInstance().getPage(currentPageId);

            int freeSpace = DataPage.readSpaceStart(currentPageData) + sizeRecord; // Calculer l'espace libre dans la
                                                                                   // page en ajoutant la taille de
                                                                                   // l'enregistrement
            for (int i = 0; i < DataPage.readNumEntries(currentPageData); i++) { // Parcourir le slot directory pour
                                                                                 // vérifier chaque enregistrement
                int entryStart = DataPage.SLOT_DIR_ENTRIES_START + i * DataPage.SLOT_DIR_ENTRY_SIZE;
                int recordStart = DataPage.readSlotEntryStart(currentPageData, entryStart);
                int recordSize = DataPage.readSlotEntrySize(currentPageData, entryStart);

                if (recordSize == 0) { // Si l'enregistrement a été supprimé
                    freeSpace += sizeRecord; // Alore augmenter l'espace libre
                    break;
                }

                if (i < DataPage.readNumEntries(currentPageData) - 1) {
                    int nextRecordStart = DataPage.readSlotEntryStart(currentPageData,
                            entryStart + DataPage.SLOT_DIR_ENTRY_SIZE);
                    int spaceBetweenRecords = nextRecordStart - (recordStart + recordSize);
                    freeSpace += spaceBetweenRecords; // Calculer l'espace entre les enregistrements
                }
            }
            if (freeSpace >= sizeRecord) { // Vérifier s'il y a suffisamment d'espace libre pour insérer
                                           // l'enregistrement
                return currentPageId;
            }
            currentPageId = DataPage.readNextPageId(currentPageData); // Passer à la page suivante dans la liste
        }
        return null; // Aucune page avec suffisamment d'espace libre n'a été trouvée
    }

    public RecordId writeRecordToDataPage(Record record, PageId pageId) throws IOException {
        ByteBuffer dataPageBuffer = BufferManager.getInstance().getPage(pageId); // Récupérer le buffer de la page de
                                                                                 // données
        int spaceStart = DataPage.readSpaceStart(dataPageBuffer); // Lire la position à partir de laquelle commence
                                                                  // l'espace libre

        int recordSize = record.writeToBuffer(dataPageBuffer, DataPage.DATA_PAGE_RECORDS_START + spaceStart); // Écrire
                                                                                                              // l'enregistrement
                                                                                                              // dans le
                                                                                                              // buffer
                                                                                                              // de la
                                                                                                              // page de
                                                                                                              // données
        spaceStart += recordSize; // Mettre à jour la position de début de l'espace libre dans le slot directory
        DataPage.writeSpaceStart(dataPageBuffer, spaceStart);
        int numEntries = DataPage.readNumEntries(dataPageBuffer) + 1; // Mettre à jour le nombre d'entrées dans le slot
                                                                      // directory
        DataPage.writeNumEntries(dataPageBuffer, numEntries);

        int entryStart = DataPage.SLOT_DIR_ENTRIES_START + (numEntries - 1) * DataPage.SLOT_DIR_ENTRY_SIZE; // Écrire la
                                                                                                            // nouvelle
                                                                                                            // entrée
                                                                                                            // dans le
                                                                                                            // slot
                                                                                                            // directory
        DataPage.writeSlotEntry(dataPageBuffer, entryStart, spaceStart - recordSize, recordSize);

        BufferManager.getInstance().freePage(pageId, true); // Libérer la page auprès du BufferManager avec le flag
                                                            // dirty

        return new RecordId(pageId, numEntries - 1);
    }

    public ArrayList<Record> getRecordsInDataPage(TableInfo tabInfo, PageId pageId) throws IOException {
        ByteBuffer dataPageBuffer = BufferManager.getInstance().getPage(pageId); // Récupérer le buffer de la page de
                                                                                 // données
        ArrayList<Record> records = new ArrayList<>(); // Utilisation explicite d'ArrayList

        try {
            int numEntries = DataPage.readNumEntries(dataPageBuffer);

            for (int i = 0; i < numEntries; i++) {
                int entryStart = DataPage.SLOT_DIR_ENTRIES_START + i * DataPage.SLOT_DIR_ENTRY_SIZE;
                int recordStart = DataPage.readSlotEntryStart(dataPageBuffer, entryStart);
                int recordSize = DataPage.readSlotEntrySize(dataPageBuffer, entryStart);

                ByteBuffer recordBuffer = ByteBuffer.allocate(recordSize);
                dataPageBuffer.position(recordStart);
                dataPageBuffer.get(recordBuffer.array(), 0, recordSize);

                Record record = new Record(tabInfo);
                record.readFromBuffer(recordBuffer, 0);
                records.add(record);
            }
        } finally {
            BufferManager.getInstance().freePage(pageId, false); // Libérer la page après lecture
        }

        return records;
    }

    public ArrayList<PageId> getDataPages(TableInfo tabInfo) throws IOException {
        ByteBuffer headerPageBuffer = BufferManager.getInstance().getPage(tabInfo.getHeaderPageId());
        try {
            ArrayList<PageId> dataPages = new ArrayList<>();

            // Lire le PageId de la liste des pages où il « reste de la place »
            PageId availablePageId = DataPage.readNextPageId(headerPageBuffer);
            if (!availablePageId.isNull()) {
                dataPages.add(availablePageId);
            }

            // Lire le PageId de la liste des pages pleines
            PageId fullPageId = DataPage.readNextPageId(headerPageBuffer);
            if (!fullPageId.isNull()) {
                dataPages.add(fullPageId);
            }
            return dataPages;
        } finally {
            BufferManager.getInstance().freePage(tabInfo.getHeaderPageId(), false); // Libérer la page après lecture
        }
    }

    public RecordId insertRecordIntoTable(Record record) throws IOException {
        PageId freeDataPageId = getFreeDataPageId(record.getTableInfo(), record.getSize());// Obtenez l'identifiant de
                                                                                           // la page de données libre
        RecordId recordId = writeRecordToDataPage(record, freeDataPageId); // Écrivez l'enregistrement dans la page de
                                                                           // données
        record.setRecordId(recordId);// Associez l'identifiant de l'enregistrement à l'enregistrement lui-même

        return recordId;
    }

    public ArrayList<Record> getAllRecords(TableInfo tabInfo) throws IOException {
        ArrayList<PageId> dataPagesList = getDataPages(tabInfo); // Récupérer la liste des PageIds des pages de données
                                                                 // pour la relation
        ArrayList<Record> allRecordsList = new ArrayList<>(); // Liste pour stocker tous les records de la relation

        for (PageId dataPageId : dataPagesList) { // Parcourir les pages de données pour récupérer tous les records
            // Récupérer la liste des records dans la page de données
            ArrayList<Record> recordsInDataPage = getRecordsInDataPage(tabInfo, dataPageId);
            allRecordsList.addAll(recordsInDataPage); // Ajouter les records à la liste générale
        }
        return allRecordsList;
    }

}
