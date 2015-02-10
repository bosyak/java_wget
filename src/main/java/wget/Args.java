package wget;

import com.beust.jcommander.Parameter;

/**
 * @author Andrey Panov, 1/30/15.
 */
public class Args {

    @Parameter(names = {"-n"}, description = "количество одновременно качающих потоков (1,2,3,4....)")
    private int threadsCount = 1;

    @Parameter(names = {"-l"}, description = "общее ограничение на скорость скачивания, для всех потоков, размерность - байт/секунда")
    private int maxBandwidth = 50 * 1024;

    @Parameter(names = {"-f"}, description = "путь к файлу со списком ссылок", required = true)
    private String inputPath;

    @Parameter(names = {"-o"}, description = "имя папки, куда складывать скачанные файлы", required = true)
    private String outputPath;

    public int getThreadsCount() {
        return threadsCount;
    }

    public void setThreadsCount(int threadsCount) {
        this.threadsCount = threadsCount;
    }

    public int getMaxBandwidth() {
        return maxBandwidth;
    }

    public void setMaxBandwidth(int maxBandwidth) {
        this.maxBandwidth = maxBandwidth;
    }

    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public String toString() {
        return "Args{" +
                "threadsCount=" + threadsCount +
                '}';
    }
}
