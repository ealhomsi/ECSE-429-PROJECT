package mutantfactory.simulator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class Simulator {
    private String outputDir;
    private String originalFile;
    private int threadsCount;

    public int getThreadsCount() {
        return this.threadsCount;
    }

    public void setThreadsCount(int threadsCount) {
        this.threadsCount = threadsCount;
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

    public Simulator(String outputDir, String originalFile, int threadsCount) {
        super();
        this.outputDir = outputDir;
        this.originalFile = originalFile;
        this.threadsCount = threadsCount;
    }

    public void runSimulation() throws IOException, ClassNotFoundException, IllegalAccessException,
            InstantiationException, MalformedURLException {
        // Compile code
        File originalFilePath = new File(originalFile);

        if (compileFile(originalFilePath) != 0) {
            System.out.println("The original file did not compile");
            System.exit(1);
        }

        Files.list(new File(outputDir).toPath()).filter(path -> path.toString().endsWith(".java")).forEach(path -> {
            if (compileFile(path.toFile()) != 0) {
                System.out.println("One of the mutants did not compile");
                System.exit(1);
            }
        });

        // Load Code
        runClass(new File(getClassName(originalFile)));

        Files.list(new File(outputDir).toPath()).filter(path -> path.toString().endsWith("class"))
                .forEach(path -> {
                    try {
                        runClass(path.toFile());
                    } catch (MalformedURLException | ClassNotFoundException | IllegalAccessException
                            | InstantiationException e) {
                        e.printStackTrace();
                    }
                });
    }

    private String getClassName(String file) {
        return file.substring(0, file.lastIndexOf(".java")) + ".class";
    }

    private void runClass(File javaClass)
            throws MalformedURLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        URL classUrl = new File(javaClass.getParent()).toURI().toURL();
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { classUrl });
        Class<?> clazz = Class.forName(javaClass.getName().replace(".class", ""), true, classLoader);
        clazz.newInstance();
    }

    private int compileFile(File path) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        return compiler.run(null, null, null, path.getAbsolutePath());
    }
}