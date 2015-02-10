package wget;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author Andrey Panov, 1/30/15.
 */
public class DownloadTask {
    String src;
    TreeSet<String> dest;

    public DownloadTask() {
    }

    public DownloadTask(String src, TreeSet<String> dest) {
        this.src = src;
        this.dest = dest;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src.toLowerCase();
    }

    public TreeSet<String> getDest() {
        if (dest == null) dest = new TreeSet<>();
        return dest;
    }

    public void setDest(TreeSet<String> dest) {
        this.dest = dest;
    }
}
