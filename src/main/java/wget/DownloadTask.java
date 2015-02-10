package wget;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author Andrey Panov, 1/30/15.
 */
public class DownloadTask {
    String src;
    Set<String> dest;

    public DownloadTask() {
    }

    public DownloadTask(String src, Set<String> dest) {
        this.src = src;
        this.dest = dest;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src.toLowerCase();
    }

    public Set<String> getDest() {
        if (dest == null) dest = new TreeSet<>();
        return dest;
    }

    public void setDest(Set<String> dest) {
        this.dest = dest;
    }
}
