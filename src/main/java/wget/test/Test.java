package wget.test;

import wget.DownloadTask;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Andrey Panov, 1/30/15.
 */
public class Test {
    public static void main(String[] args) {
        new Test();
    }

    public Test() {


        Set<DownloadTask> queue = Collections.synchronizedSet(new TreeSet<>());


        DownloadTask task1 = new DownloadTask();
        task1.setSrc("http");
        task1.getDest().add("file1");

        queue.add(task1);

        DownloadTask task2 = new DownloadTask();
        task2.setSrc("http");
        task2.getDest().add("file1");
        queue.add(task2);

        System.out.println(queue.size());

    }
}
