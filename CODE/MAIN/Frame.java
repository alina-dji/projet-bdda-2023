package Main;

import java.nio.ByteBuffer;

public class Frame {
    private PageId pageId;
    private ByteBuffer buffer;
    private int pinCount;
    private boolean dirty;
    public int accessCount;

    public Frame() {
        this.pageId = new PageId(-1, 0);
        this.buffer = ByteBuffer.allocate(DBParams.SGBDPageSize);
        this.pinCount = 0;
        this.dirty = false;
        this.accessCount = 0;
    }

    public PageId getPageId() {
        return pageId;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public int getPinCount() {
        return pinCount;
    }

    public boolean isDirty() {
        return dirty;
    }

    public int getAccessCount() {
        return accessCount;
    }

    public void setPageId(PageId pageId) {
        this.pageId = pageId;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public void setPinCount(int pinCount) {
        this.pinCount = pinCount;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void incrementPinCount() {
        pinCount++;
    }

    public void decrementPinCount() {
        pinCount--;
    }

    public void incrementAccessCount() {
        accessCount++;
    }

    public void reset() {
        this.pageId = new PageId(-1, 0);
        this.buffer.clear();
        this.pinCount = 0;
        this.dirty = false;
        this.accessCount = 0;
    }

    @Override
    public String toString() {
        return "Frame{" +
                "pageId=" + pageId +
                ", pinCount=" + pinCount +
                ", dirty=" + dirty +
                '}';
    }
}
