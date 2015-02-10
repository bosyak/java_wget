package wget;

import com.beust.jcommander.JCommander;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
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
    private AtomicLong downloadTotal = new AtomicLong(0);
    private ExecutorService executorService;
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
        openFile();
        startBandwidthFiller();
        startDownloadThreads();
        printStat();
    }

    private void printStat() {
        while (!executorService.isTerminated()) {
        }

        System.out.println("Всего скачано " + downloadTotal);
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

    private void openFile() {

        HashMap<String, Set<String>> map = new HashMap<>();

        try {
            Files
                    .lines(Paths.get(parameters.getInputPath()))
                    .forEach(s -> {
                        String[] split = s.trim().split("[ ]+");
                        if (split.length == 2) {
                            String srcUrl = split[0].toLowerCase();
                            String dest = split[1];

                            Set<String> dests = map.get(srcUrl);
                            if (dests == null) {
                                dests = new TreeSet<String>();
                                map.put(srcUrl, dests);
                            }
                            dests.add(dest);
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

                int timeout = 200;
                int fillAmount = parameters.getMaxBandwidth() / (1000 / timeout);

                while (true) {

                    if (bandwidth.availablePermits() < parameters.getMaxBandwidth()) {
                        bandwidth.release(fillAmount);
                        System.out.println("Положили " + fillAmount + ", осталось еще " + bandwidth.availablePermits());
                    }

                    try {
                        Thread.sleep(timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };
        filler.setDaemon(true);
        filler.start();

    }

    private void startDownloadThreads() {
        executorService = Executors.newFixedThreadPool(parameters.getThreadsCount());
        executorService.execute(() -> {
            DownloadTask downloadTask = queue.poll();
            if (d)
        });
    }


    private void startJob() {

        new Thread() {
            @Override
            public void run() {

                while (true) {
                    try {
                        System.out.println("Осталось " + bandwidth.availablePermits());
                        bandwidth.acquire(5);
//                        Thread.sleep(200);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();
    }
}
