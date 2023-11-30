package Test;

import org.junit.Test;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;

import Main.BufferManager;
import Main.DiskManager;
import Main.PageId;

public class BufferManagerTest {
    
    private BufferManager bufferManager;
    private DiskManager diskManager;

    @Before
    public void setUp() {
        // Initialisation des objets nécessaires pour les tests
        bufferManager = BufferManager.getInstance();
        diskManager = new DiskManager();
    }
    
    @Test
    public void testReadWritePages() throws IOException {
            // Allouez deux pages avec le DiskManager
        PageId page1 = diskManager.allocPage();
        PageId page2 = diskManager.allocPage();

        // Écrivez du contenu dans les pages
        byte[] data1 = "Hello, Page 1!".getBytes();
        byte[] data2 = "Greetings, Page 2!".getBytes();

        diskManager.writePage(page1, data1);
        diskManager.writePage(page2, data2);

        // Lisez les pages du BufferManager
        byte[] readData1 = bufferManager.getPage(page1);
        byte[] readData2 = bufferManager.getPage(page2);

        // Vérifiez que le contenu lu est le même que celui écrit
        assertArrayEquals(data1, readData1);
        assertArrayEquals(data2, readData2);
    }

    @Test
    public void testReplacementPolicy() {
        // Allouez plus de pages que la taille du buffer pour déclencher la politique de remplacement
        PageId page1 = diskManager.allocPage();
        PageId page2 = diskManager.allocPage();
        PageId page3 = diskManager.allocPage();

        // Remplissez le buffer avec les pages
        bufferManager.getPage(page1);
        bufferManager.getPage(page2);

        // Écrivez et lisez pour augmenter l'accès à la page 1
        diskManager.writePage(page1, "Page 1 content".getBytes());
        bufferManager.getPage(page1);
        bufferManager.freePage(page1, 0);

        // La page 3 doit remplacer la page 2 dans le buffer
        byte[] data3 = "This is Page 3".getBytes();
        diskManager.writePage(page3, data3);
        bufferManager.getPage(page3);

        // Vérifiez que la page 2 n'est plus dans le buffer
        assertNull(bufferManager.findFrame(page2));
    }
    }

