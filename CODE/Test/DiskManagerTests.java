package Test;

import java.io.IOException;

import Main.DBParams;
import Main.DiskManager;
import Main.PageId;

public class DiskManagerTests {
    public static void main(String[] args) throws IOException {
        DBParams.DBPath = "C:\\Users\\dell\\Desktop\\projet-bdda-2023-main\\DB\\";
        DBParams.SGBDPageSize = 4096;
        DBParams.DMFileCount = 4;

        DiskManager dm = DiskManager.getInstance();
        PageId testPageId = dm.allocPage();
        System.out.println(testPageId.toString());
    }
}