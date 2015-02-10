package wget;

import javafx.scene.input.InputMethodTextRun;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Andrey Panov, 10.02.15.
 */
public abstract class DownloadAndCopyThread extends Thread {

    private long threadDownload = 0;

    @Override
    public void run() {
        DownloadTask task = null;

        while ((task = getNextDownloadTask()) != null) {
            try {

                URL website = new URL(task.getSrc());
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                String firstDestination = task.getDest().pollFirst();
                FileOutputStream fos = new FileOutputStream(firstDestination);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

                for (String destination : task.getDest()) {
                    Files.copy(Paths.get(firstDestination), Paths.get(destination));
                }

            } catch (IOException e) {
                System.err.println("Ошибка при скачивании файла: " + task.getSrc());
            }
        }
    }

    public abstract DownloadTask getNextDownloadTask();

    public abstract void summarizeDownloadSize(long downloadSize);
}
