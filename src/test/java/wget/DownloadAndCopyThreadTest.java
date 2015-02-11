package wget;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Queue;

import static org.testng.Assert.*;

public class DownloadAndCopyThreadTest {

    @Test
    public void testRun() throws Exception {
        String file1 = "/home/aware/Desktop/LOGO1.png";
        String file2 = "/home/aware/Desktop/LOGO2.png";

        new File(file1).delete();
        new File(file2).delete();

        DownloadTask task = new DownloadTask();
        task.setSrc("https://www.google.ru/images/srpr/logo11w.png");

        task.getDest().add(file1);
        task.getDest().add(file2);

        Queue<DownloadTask> queue = new ArrayDeque<>();
        queue.add(task);

        DownloadAndCopyThread thread = new DownloadAndCopyThread() {
            @Override
            public DownloadTask getNextDownloadTask() {
                return queue.poll();
            }

            @Override
            public void summarizeDownloadSize(long downloadSize) {
                assertEquals(downloadSize, 14022L);
            }

            @Override
            public void getBandwidthQuota(int quota) throws InterruptedException {
                return;
            }
        };
        thread.start();
        thread.join();

        File f1 = new File(file1);
        File f2 = new File(file2);
        assertTrue(f2.exists());
        assertTrue(f2.exists());
        assertEquals(f1.length(), 14022L);
        assertEquals(f2.length(), 14022L);
    }
}