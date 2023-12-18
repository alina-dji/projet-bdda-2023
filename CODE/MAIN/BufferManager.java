package Main;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BufferManager {

    private static BufferManager instance = new BufferManager();
    private Frame[] bufferPool;

    private BufferManager() {
        bufferPool = new Frame[DBParams.frameCount];
        for (int i = 0; i < DBParams.frameCount; i++) {
            bufferPool[i] = new Frame();
        }
    }

    public static BufferManager getInstance() {
        if (instance == null)
            instance = new BufferManager();
        return instance;
    }

    private Frame findFrame(PageId pageIdx) {
        for (Frame frame : bufferPool) {
            if (frame.getPageId() != null && frame.getPageId().equals(pageIdx)) {
                return frame;
            }
        }
        return null;
    }

    private Frame replaceLFU() {
        // Création d'une liste de candidats pour le remplacement
        List<Frame> candidates = new ArrayList<>();
        for (Frame frame : bufferPool) {
            if (frame.getPageId() == null || frame.getPinCount() == 0) {
                candidates.add(frame);
            }
        }

        // Déterminer le candidat LFU le moins récemment utilisé
        candidates.sort(Comparator.comparingInt(Frame::getAccessCount));

        return candidates.get(0);
    }

    public ByteBuffer getPage(PageId pageId) throws IOException {
        Frame frame = findFrame(pageId);
        if (frame == null) {
            frame = replaceLFU();
            flush(frame);
            frame.setPageId(pageId);
            DiskManager.getInstance().readPage(pageId, frame.getBuffer());
        }
        frame.incrementPinCount();
        frame.incrementAccessCount();
        return frame.getBuffer();
    }

    public void freePage(PageId pageId, boolean valDirty) throws IOException {
        Frame frame = findFrame(pageId);
        if (frame == null) {
            throw new IllegalArgumentException("Cette page n'est pas dans le frame!");
        }
        frame.decrementPinCount();
        if (valDirty)
            frame.setDirty(true);
    }

    private void flush(Frame frame) throws IOException {
        if (frame.isDirty()) {
            DiskManager.getInstance().writePage(frame.getPageId(), frame.getBuffer());
        }
        frame.reset();
    }

    public void flushAll() throws IOException {
        for (Frame frame : bufferPool)
            flush(frame);
    }

    public void printBufferPoolStatus(String status) {
        System.out.println("Buffer Pool Status " + status + ":");
        for (int i = 0; i < bufferPool.length; i++) {
            System.out.println("Frame " + i + ": " + bufferPool[i]);
        }
        System.out.println();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("===============\n");
        sb.append("BufferManager\n");
        for (Frame frame : bufferPool) {
            sb.append(frame).append("\n");
        }
        sb.append("===============\n");

        return sb.toString();
    }
}
