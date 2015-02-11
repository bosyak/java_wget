package wget;

import javax.xml.bind.SchemaOutputResolver;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author Andrey Panov, 10.02.15.
 */
public abstract class DownloadAndCopyThread extends Thread {

    private long threadDownload = 0;

    @Override
    public void run() {
        DownloadTask task;

        while ((task = getNextDownloadTask()) != null) {
            String sourceUrl = task.getSrc();

            try {

                File tempFile = downloadFile(sourceUrl);
                onDownloadDone(task, tempFile);

            } catch (IOException e) {
                System.err.println("Ошибка при скачивании файла: " + sourceUrl);
                e.printStackTrace();
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }

            summarizeDownloadSize(threadDownload);
        }
    }

    private File downloadFile(String sourceUrl) throws IOException, InterruptedException {
        System.out.println("Download: " + sourceUrl);

        URL url = new URL(sourceUrl);
        BufferedInputStream bis = new BufferedInputStream(url.openStream());
        File tempFile = File.createTempFile("java_wget", "");
        FileOutputStream fis = new FileOutputStream(tempFile);

        int bandwidthQuota = 1024;
        byte[] buffer = new byte[bandwidthQuota];
        int count;
        while ((count = bis.read(buffer, 0, bandwidthQuota)) != -1) {
            fis.write(buffer, 0, count);
            getBandwidthQuota(bandwidthQuota);
            threadDownload += count;
        }
        fis.close();
        bis.close();
        return tempFile;
    }

    private void onDownloadDone(DownloadTask task, File tempFile) throws IOException {
        String firstDestination = task.getDest().pollFirst();
        Files.move(tempFile.toPath(), Paths.get(firstDestination), StandardCopyOption.REPLACE_EXISTING);

        for (String destination : task.getDest()) {
            Files.copy(Paths.get(firstDestination), Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        }

        tempFile.delete();
    }

    public abstract DownloadTask getNextDownloadTask();

    public abstract void summarizeDownloadSize(long downloadSize);

    public abstract void getBandwidthQuota(int quota) throws InterruptedException;
}
