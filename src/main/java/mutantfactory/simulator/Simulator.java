package mutantfactory.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Simulator {
    private String outputDir;
    private String originalFile;
    private int threadCount;
    private int mutantCount;

    public int getMutantCount() {
        return this.mutantCount;
    }

    public void setMutantCount(int mutantCount) {
        this.mutantCount = mutantCount;
    }

    public int getThreadsCount() {
        return this.threadCount;
    }

    public void setThreadsCount(int threadsCount) {
        this.threadCount = threadsCount;
    }

    public String getOutputDir() {
        return this.outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getOriginalFile() {
        return this.originalFile;
    }

    public void setOriginalFile(String originalFile) {
        this.originalFile = originalFile;
    }

    public Simulator(String outputDir, String originalFile, int threadCount, int mutantCount) {
        super();
        this.outputDir = outputDir;
        this.originalFile = originalFile;
        this.threadCount = threadCount;
        this.mutantCount = mutantCount;
    }

    public  List<MutantTestResult> runSimulation() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        Set<Future<List<MutantTestResult>>> set = new HashSet<Future<List<MutantTestResult>>>();
        List<MutantTestResult> total = new ArrayList<>();

        // Portion per thread
        int portion = (mutantCount + (threadCount - 1)) / threadCount;
        for (int i = 0; i < threadCount; i++) {
            Callable<List<MutantTestResult>> callable = new MutantTestCallable(i, portion, outputDir, originalFile);
            Future<List<MutantTestResult>> future = pool.submit(callable);
            set.add(future);

        }
        for (Future<List<MutantTestResult>> future : set) {
            while(!future.isDone()) {
                try {
                    Thread.sleep(300);
                }
                catch(Exception e) {
                }
            }
            total.addAll(future.get());
        }

        Collections.sort(total);
        pool.shutdown();
        return total;
    }
}