package wget;

import com.beust.jcommander.JCommander;

import javax.xml.bind.SchemaOutputResolver;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

/**
 * @author Andrey Panov, 1/26/15.
 */
public class Wget {

    private Args parameters;

    private Semaphore bandwidth;
    private String destDir;
    private ConcurrentLinkedQueue<DownloadTask> queue;
    private long totalDownload;
    private List<Thread> workingThreads;

    public static void main(String[] args) {
        Args arz = new Args();
        JCommander jc = new JCommander(arz);
        try {
            jc.parse(args);
            new Wget(arz);

        } catch (Exception e) {
            jc.usage();
            String message = e.getMessage();
            System.out.println("Не указаны обязательные параметры: " + message.substring(message.indexOf(":") + 2));
        }
    }

    public Wget(Args parameters) {
        this.parameters = parameters;

        openOutputDir();
        readSourceFile();
        startBandwidthFillerThread();
        startDownloadAndCopyThreads();
        printStat();

        System.out.println("Done.");
    }

    private void printStat() {
        long startTime = System.currentTimeMillis();

        for (Thread thread : workingThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }

        long duractionSec = (System.currentTimeMillis() - startTime) / 1000;
        long kb = totalDownload / 1024;

        System.out.println("Время работы: " + duractionSec + ", секунд");
        System.out.println("Всего скачано: " + kb + ", килобайт");
        System.out.println("Средняя скорость: " + (kb / duractionSec) + " килобайт в секунду");
    }

    private void openOutputDir() {
        destDir = parameters.getOutputPath();
        if (!destDir.endsWith("/")) {
            destDir += "/";
        }
        File file = new File(destDir);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new RuntimeException("Не удалось создать директорию для загружаемых файлов");
            }
        }
    }

    private void readSourceFile() {

        HashMap<String, TreeSet<String>> map = new HashMap<>();

        try {
            Files.lines(Paths.get(parameters.getInputPath()))
                    .forEach(line -> {
                        String[] split = line.trim().split("[ \\t]+");
                        if (split.length == 2) {
                            String srcUrl = split[0].toLowerCase();
                            String dest = destDir + split[1];

                            TreeSet<String> destinations = map.get(srcUrl);
                            if (destinations == null) {
                                destinations = new TreeSet<>();
                                map.put(srcUrl, destinations);
                            }
                            destinations.add(dest);
                        }

                    });

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        queue = new ConcurrentLinkedQueue<>();
        queue.addAll(map
                .entrySet()
                .stream()
                .map(entry -> new DownloadTask(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList()));

    }

    private void startBandwidthFillerThread() {
        bandwidth = new Semaphore(parameters.getMaxBandwidth());

        Thread filler = new Thread() {
            @Override
            public void run() {

                int sleepTime = 200;
                int fillAmount = parameters.getMaxBandwidth() / (1000 / sleepTime);

                try {
                    while (true) {
                        if (bandwidth.availablePermits() < parameters.getMaxBandwidth()) {
                            bandwidth.release(fillAmount);
                        }
                        Thread.sleep(sleepTime);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
        filler.setDaemon(true);
        filler.start();

    }

    private void startDownloadAndCopyThreads() {
        workingThreads = new ArrayList<>();

        for (int i = 0; i < parameters.getThreadsCount(); i++) {
            DownloadAndCopyThread thread = new DownloadAndCopyThread() {
                @Override
                public DownloadTask getNextDownloadTask() {
                    return queue.poll();
                }

                @Override
                public void summarizeDownloadSize(long downloadSize) {
                    synchronized (Wget.this) {
                        totalDownload += downloadSize;
                    }
                }

                @Override
                public void getBandwidthQuota(int quota) throws InterruptedException {
                    bandwidth.acquire(quota);
                }
            };
            workingThreads.add(thread);
            thread.start();
        }
    }


}
