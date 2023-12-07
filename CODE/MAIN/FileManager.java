package Main;

import java.io.IOException;
import java.nio.ByteBuffer;

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

        // Écrire l'enregistrement dans la page de données
        record.writeToBuffer(dataPageData, 0); // Modifier l'offset selon votre implémentation

        // Libérer la page auprès du BufferManager avec le bon flag dirty
        BufferManager.getInstance().freePage(pageId, true);

        // TODO : Mettre à jour la liste des pages pleines si nécessaire
        // À titre d'exemple, supposons que vous ayez une méthode pour gérer la liste des pages pleines
        updateFullPagesList(pageId);

        // Retourner l'identifiant du record
        return new RecordId(pageId, /* Indice du record, à déterminer selon votre implémentation */);
    }




}
