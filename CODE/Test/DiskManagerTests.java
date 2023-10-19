package Test;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;

import Main.DiskManager;
import Main.PageId;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.runner.Result;

public class DiskManagerTests {

    // Utiliser une petite taille de page pour les tests (4oct)
    private static final int PAGE_SIZE = 4;

    @Test
    public void testAllocPage() throws IOException {
        DiskManager dm = DiskManager.getInstance("testDB", PAGE_SIZE);
        PageId pageId = dm.AllocPage();

        // Vérifiez que la page a été allouée avec succès
        assertNotNull(pageId);

        // Vérifiez que le nombre de pages allouées a augmenté
        assertEquals(1, dm.GetCurrentCountAllocPages());
    }


    @Test
    public void TestEcriturePage() throws IOException {
        DiskManager dm = new DiskManager("testDB", PAGE_SIZE);
        PageId pageId = dm.AllocPage();

        // Données à écrire sur la page
        byte[] dataToWrite = "Test".getBytes();

        // Écrivez les données sur la page
        dm.WritePage(pageId, dataToWrite);

        // Préparez un tableau de bytes pour recevoir les données lues
        byte[] dataRead = new byte[PAGE_SIZE];

        // Lisez les données de la page
        try {
            dm.ReadPage(pageId, dataRead, null);
        } catch (IOException e) {
            fail("La lecture de page a échoué : " + e.getMessage());
        }

        // Assurez-vous que les données lues correspondent aux données écrites
        assertArrayEquals(dataToWrite, dataRead);
        
    }

    @Test
    public void testDeallocPage() throws IOException {
        DiskManager dm = DiskManager.getInstance("testDB", PAGE_SIZE);
        PageId pageId = dm.AllocPage();

        // Désallouez la page
        dm.DeallocPage(pageId);

        // Vérifiez que la page n'est plus allouée
        assertFalse(dm.isPageAllocated(pageId));
    }

    @Test
    public void testIsPageAllocated() throws IOException {
        DiskManager dm = DiskManager.getInstance("testDB", PAGE_SIZE);

        // Allouez une nouvelle page
        PageId pageId = dm.AllocPage();

        // Vérifiez que la page est allouée
        assertTrue(dm.isPageAllocated(pageId));
    }



    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("Main.DiskManagerTests");

        // Exécutez les tests en utilisant JUnitCore
        Result result = JUnitCore.runClasses(DiskManagerTests.class);

        // Affichez les résultats des tests
        for (Failure failure : result.getFailures()) {
            System.out.println("Échec du test : " + failure.toString());
        }

        if (result.wasSuccessful()) {
            System.out.println("Tous les tests ont réussi !");
        }
    }
}
