package Main;

import java.nio.ByteBuffer;

public class DataPage {
    public static final int PAGE_ID_SIZE = 8;
    public static final int SLOT_DIR_HEADER_SIZE = 8;
    public static final int SLOT_DIR_ENTRY_SIZE = 8;

    // Data Page structure
    public static final int DATA_PAGE_NEXT_PAGE_POS = 0;
    public static final int DATA_PAGE_RECORDS_START = PAGE_ID_SIZE;

    // Slot Directory structure
    public static final int SLOT_DIR_START_POS = DBParams.SGBDPageSize - SLOT_DIR_HEADER_SIZE;
    public static final int SLOT_DIR_SPACE_START_POS = SLOT_DIR_START_POS;
    public static final int SLOT_DIR_NUM_ENTRIES_POS = SLOT_DIR_START_POS + 4;
    public static final int SLOT_DIR_ENTRIES_START = SLOT_DIR_START_POS + SLOT_DIR_HEADER_SIZE;

    public static void writeNextPageId(ByteBuffer buffer, PageId nextPageId) {
        buffer.putInt(DATA_PAGE_NEXT_PAGE_POS, nextPageId.getFileIdx());
        buffer.putInt(DATA_PAGE_NEXT_PAGE_POS + Integer.BYTES, nextPageId.getPageIdx());
    }

    public static PageId readNextPageId(ByteBuffer buffer) {
        int fileIdx = buffer.getInt(DATA_PAGE_NEXT_PAGE_POS);
        int pageIdx = buffer.getInt(DATA_PAGE_NEXT_PAGE_POS + Integer.BYTES);
        return new PageId(fileIdx, pageIdx);
    }

    public static void writeRecord(ByteBuffer buffer, int recordStart, byte[] record) {
        buffer.position(recordStart);
        buffer.put(record);
    }

    public static void writeSpaceStart(ByteBuffer buffer, int spaceStart) {
        buffer.putInt(SLOT_DIR_SPACE_START_POS, spaceStart);
    }

    public static int readSpaceStart(ByteBuffer buffer) {
        return buffer.getInt(SLOT_DIR_SPACE_START_POS);
    }

    public static void writeNumEntries(ByteBuffer buffer, int numEntries) {
        buffer.putInt(SLOT_DIR_NUM_ENTRIES_POS, numEntries);
    }

    public static int readNumEntries(ByteBuffer buffer) {
        return buffer.getInt(SLOT_DIR_NUM_ENTRIES_POS);
    }

    public static void writeSlotEntry(ByteBuffer buffer, int entryStart, int recordStart, int recordSize) {
        buffer.putInt(entryStart, recordStart);
        buffer.putInt(entryStart + Integer.BYTES, recordSize);
    }

    public static int readSlotEntryStart(ByteBuffer buffer, int entryStart) {
        return buffer.getInt(entryStart);
    }

    public static int readSlotEntrySize(ByteBuffer buffer, int entryStart) {
        return buffer.getInt(entryStart + Integer.BYTES);
    }
}
