package CODE.TEST;

import java.io.IOException;

import CODE.MAIN.DBParams;
import CODE.MAIN.DiskManager;
import CODE.MAIN.PageId;

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