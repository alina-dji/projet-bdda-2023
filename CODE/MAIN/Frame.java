package Main;

public class Frame {

    public byte[] buffer;
    public PageId pageId;
    public int pinCount;
    public boolean dirty;
    public int accessCount;

    public Frame() {
        buffer = new byte[DBParams.SGBDPageSize];
        pageId = null;
        pinCount = 0;
        dirty = false;
        accessCount = 0;
    }
    
    public void incrementAccessCount() {
        accessCount++;
    }

    public void incrementPinCount() {
        pinCount++;
    }

    public void decrementPinCount() {
        pinCount--;
    }

    public void reset() {
        this.buffer = null;
        this.pageId = null;
        this.pinCount = 0;
        this.dirty = false;
        this.accessCount = 0;
    }

}
