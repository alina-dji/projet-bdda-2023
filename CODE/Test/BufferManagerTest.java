package Test;

import java.io.IOException;

import Main.BufferManager;
import Main.DBParams;
import Main.PageId;

public class BufferManagerTest {

    private BufferManager bufferManager;

    public BufferManagerTest() {
        // Initialisation des objets nécessaires pour les tests
        bufferManager = BufferManager.getInstance();
    }

    public void runTests() throws IOException {
        testGetPageWhenAllFrameEmpty();
        testFreePage();
        testFlushAll();
        testGetPageWhenPageAlreadyExists();
        testGetPageWhenAllFramesAreFull();
    }

    private void testGetPageWhenAllFrameEmpty() throws IOException {
        bufferManager.flushAll(); // Assure que le buffer est vide
        PageId p = new PageId(0, 0);
        bufferManager.getPage(p);
        System.out.println("Test de buffPoolVide");
        // Affiche le contenu du buffer après l'opération
        bufferManager.toString();
    }

    private void testFreePage() throws IOException {
        PageId p = new PageId(0, 0);
        bufferManager.freePage(p, false); // 0 pour indiquer que la page n'est pas dirty
        System.out.println("Test de freePage");
        // Affiche le contenu du buffer après l'opération
        bufferManager.toString();
    }

    private void testFlushAll() throws IOException {
        bufferManager.flushAll();
        // Affiche le contenu du buffer après l'opération
        bufferManager.toString();
    }

    private void testGetPageWhenPageAlreadyExists() throws IOException {
        bufferManager.flushAll(); // Assure que le buffer est vide
        PageId p = new PageId(0, 0);
        bufferManager.getPage(p);
        bufferManager.getPage(p);
        // Affiche le contenu du buffer après l'opération
        bufferManager.toString();
    }

    private void testGetPageWhenAllFramesAreFull() throws IOException {
        bufferManager.flushAll(); // Assure que le buffer est vide
        PageId p = new PageId(0, 0);
        PageId p1 = new PageId(0, 1);
        PageId p2 = new PageId(0, 2);
        bufferManager.getPage(p);
        bufferManager.getPage(p1);
        bufferManager.freePage(p, false);
        bufferManager.getPage(p2);
        // Affiche le contenu du buffer après l'opération
        bufferManager.toString();
    }

    public static void main(String[] args) throws IOException {
        // Assurez-vous d'ajuster ces paramètres en fonction de votre implémentation
        // réelle
        DBParams.SGBDPageSize = 4;
        DBParams.frameCount = 2;

        BufferManagerTest test = new BufferManagerTest();
        test.runTests();
    }

}
/*
 * private BufferManager bufferManager;
 * private DiskManager diskManager;
 * 
 * public BufferManagerTest() {
 * // Initialisation des objets nécessaires pour les tests
 * bufferManager = BufferManager.getInstance();
 * diskManager = new DiskManager();
 * }
 * 
 * public void runTests() throws IOException {
 * testReadWritePages();
 * testReplacementPolicy();
 * }
 * 
 * private void testReadWritePages() throws IOException {
 * // Allouez deux pages avec le DiskManager
 * PageId page1 = diskManager.allocPage();
 * PageId page2 = diskManager.allocPage();
 * 
 * // Écrivez du contenu dans les pages
 * byte[] data1 = "Hello, Page 1!".getBytes();
 * byte[] data2 = "Greetings, Page 2!".getBytes();
 * 
 * diskManager.writePage(page1, data1);
 * diskManager.writePage(page2, data2);
 * 
 * // Lisez les pages du BufferManager
 * byte[] readData1 = bufferManager.getPage(page1);
 * byte[] readData2 = bufferManager.getPage(page2);
 * 
 * // Vérifiez que le contenu lu est le même que celui écrit
 * if (arrayEquals(data1, readData1) && arrayEquals(data2, readData2)) {
 * System.out.println("testReadWritePages: PASSED");
 * } else {
 * System.out.println("testReadWritePages: FAILED");
 * }
 * }
 * 
 * private void testReplacementPolicy() throws IOException {
 * // Allouez plus de pages que la taille du buffer pour déclencher la politique
 * de remplacement
 * PageId page1 = diskManager.allocPage();
 * PageId page2 = diskManager.allocPage();
 * PageId page3 = diskManager.allocPage();
 * 
 * // Remplissez le buffer avec les pages
 * bufferManager.getPage(page1);
 * bufferManager.getPage(page2);
 * 
 * // Écrivez et lisez pour augmenter l'accès à la page 1
 * diskManager.writePage(page1, "Page 1 content".getBytes());
 * bufferManager.getPage(page1);
 * bufferManager.freePage(page1, 0);
 * 
 * // La page 3 doit remplacer la page 2 dans le buffer
 * byte[] data3 = "This is Page 3".getBytes();
 * diskManager.writePage(page3, data3);
 * bufferManager.getPage(page3);
 * 
 * // Vérifiez que la page 2 n'est plus dans le buffer
 * if (bufferManager.findFrame(page2) == null) {
 * System.out.println("testReplacementPolicy: PASSED");
 * } else {
 * System.out.println("testReplacementPolicy: FAILED");
 * }
 * }
 * 
 * private boolean arrayEquals(byte[] arr1, byte[] arr2) {
 * if (arr1.length != arr2.length) {
 * return false;
 * }
 * for (int i = 0; i < arr1.length; i++) {
 * if (arr1[i] != arr2[i]) {
 * return false;
 * }
 * }
 * return true;
 * }
 * 
 * public static void main(String[] args) throws IOException {
 * BufferManagerTest test = new BufferManagerTest();
 * test.runTests();
 * }
 * 
 * }
 */

/*
 * public void setUp() {
 * // Initialisation des objets nécessaires pour les tests
 * bufferManager = BufferManager.getInstance();
 * diskManager = new DiskManager();
 * }
 * 
 * 
 * public void testReadWritePages() throws IOException {
 * // Allouez deux pages avec le DiskManager
 * PageId page1 = diskManager.allocPage();
 * PageId page2 = diskManager.allocPage();
 * 
 * // Écrivez du contenu dans les pages
 * byte[] data1 = "Hello, Page 1!".getBytes();
 * byte[] data2 = "Greetings, Page 2!".getBytes();
 * 
 * diskManager.writePage(page1, data1);
 * diskManager.writePage(page2, data2);
 * 
 * // Lisez les pages du BufferManager
 * byte[] readData1 = bufferManager.getPage(page1);
 * byte[] readData2 = bufferManager.getPage(page2);
 * 
 * // Vérifiez que le contenu lu est le même que celui écrit
 * assertArrayEquals(data1, readData1);
 * assertArrayEquals(data2, readData2);
 * }
 * 
 * @Test
 * public void testReplacementPolicy() throws IOException {
 * // Allouez plus de pages que la taille du buffer pour déclencher la politique
 * de remplacement
 * PageId page1 = diskManager.allocPage();
 * PageId page2 = diskManager.allocPage();
 * PageId page3 = diskManager.allocPage();
 * 
 * // Remplissez le buffer avec les pages
 * bufferManager.getPage(page1);
 * bufferManager.getPage(page2);
 * 
 * // Écrivez et lisez pour augmenter l'accès à la page 1
 * diskManager.writePage(page1, "Page 1 content".getBytes());
 * bufferManager.getPage(page1);
 * bufferManager.freePage(page1, 0);
 * 
 * // La page 3 doit remplacer la page 2 dans le buffer
 * byte[] data3 = "This is Page 3".getBytes();
 * diskManager.writePage(page3, data3);
 * bufferManager.getPage(page3);
 * 
 * // Vérifiez que la page 2 n'est plus dans le buffer
 * assertNull(bufferManager.findFrame(page2));
 * }
 */
