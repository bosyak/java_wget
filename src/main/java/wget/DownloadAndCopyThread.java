package wget;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

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
                String firstDestination = task.getDest().pollFirst();

                URL website = new URL(task.getSrc());
                ReadableByteChannel in = Channels.newChannel(website.openStream());
                FileOutputStream out = new FileOutputStream(firstDestination);

                ByteBuffer buff = ByteBuffer.allocate(8*1024);

                do {
                    int read = in.read(buff);

                }


                out.getChannel().transferFrom(in, 0, Long.MAX_VALUE);

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

    public abstract void getBandwidthQuota(int quota) throws InterruptedException;
}
