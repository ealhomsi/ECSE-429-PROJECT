package mutantfactory.simulator;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

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

    public List<MutantTestResult> runSimulation() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        Set<Future<List<MutantTestResult>>> set = new HashSet<Future<List<MutantTestResult>>>();
        List<MutantTestResult> total = new ArrayList<>();

        // Fetch all mutant files
        Path[] list = Files.list(new File(outputDir).toPath()).filter(path -> path.toString().endsWith(".java"))
                .toArray(Path[]::new);

        // Initialize the original file
        File originalFilePath = new File(originalFile);

        if (compileFile(originalFilePath) != 0) {
            System.err.println("The original file did not compile");
            System.exit(1);
        }

        Method originalMethod = null;
        try {
            originalMethod = getClassFromFile(new File(getClassName(originalFile))).getMethod(MutantTestCallable.METHOD_NAME, int.class);
        } catch (Exception e) {
            System.err.println("Could not load the SUT " + e.getMessage());
            System.exit(1);
        }

        // Portion per thread
        int portion = (mutantCount + (threadCount - 1)) / threadCount;
        for (int i = 0; i < threadCount; i++) {
            Callable<List<MutantTestResult>> callable = new MutantTestCallable(i, portion, list, originalMethod);
            Future<List<MutantTestResult>> future = pool.submit(callable);
            set.add(future);
        }

        for (Future<List<MutantTestResult>> future : set) {
            total.addAll(future.get());
        }

        Collections.sort(total);
        pool.shutdown();
        return total;
    }

    private String getClassName(String file) {
        return file.substring(0, file.lastIndexOf(".java")) + ".class";
    }

    private Class<?> getClassFromFile(File javaClass) throws Exception {
        URL classUrl = new File(javaClass.getParent()).toURI().toURL();
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { classUrl });
        Class<?> clazz = Class.forName(javaClass.getName().substring(0, javaClass.getName().lastIndexOf(".class")),
                true, classLoader);
        return clazz;
    }

    private int compileFile(File path) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        return compiler.run(null, null, null, path.getAbsolutePath());
    }
}