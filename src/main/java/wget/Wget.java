package wget;

import com.beust.jcommander.JCommander;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
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
        } catch (Exception e) {
            jc.usage();
            String message = e.getMessage();
            System.out.println("Не указаны обязательные параметры: " + message.substring(message.indexOf(":") + 2));
            return;
        }

        try {
            new Wget(arz);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public Wget(Args parameters) {
        this.parameters = parameters;

        openOutputDir();
        readSourceFile();
        startBandwidthFiller();
        startDownloadAndCopyThreads();
        printStat();
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
        if (!file.mkdirs()) {
            throw new RuntimeException("Не удалось создать директорию для загружаемых файлов");
        }
    }

    private void readSourceFile() {

        HashMap<String, Set<String>> map = new HashMap<>();

        try {
            Files.lines(Paths.get(parameters.getInputPath()))
                    .forEach(line -> {
                        String[] split = line.trim().split("[ ]+");
                        if (split.length == 2) {
                            String srcUrl = split[0].toLowerCase();
                            String dest = split[1];

                            Set<String> destinations = map.get(srcUrl);
                            if (destinations == null) {
                                destinations = new TreeSet<String>();
                                map.put(srcUrl, destinations);
                            }
                            destinations.add(dest);
                        }

                    });

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        queue.addAll(map
                .entrySet()
                .stream()
                .map(entry -> new DownloadTask(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList()));

    }

    private void startBandwidthFiller() {
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
            };
            workingThreads.add(thread);
            thread.start();
        }
    }


}
