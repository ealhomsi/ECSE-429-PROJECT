package mutantfactory.simulator;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class MutantTestCallable implements Callable<List<MutantTestResult>> {
    public static final int[] INPUT_VECTORS = { 2, 4, 8, 16, 32, 64, 128, 512, 1024 };

    private int index;
    private int portion;
    private String outputDir;
    private String originalFile;

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getPortion() {
        return this.portion;
    }

    public void setPortion(int portion) {
        this.portion = portion;
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

    public MutantTestCallable(int index, int portion, String outputDir, String originalFile) {
        super();
        this.index = index;
        this.portion = portion;
        this.outputDir = outputDir;
        this.originalFile = originalFile;
    }

    @Override
    public List<MutantTestResult> call() throws Exception {
        int startI = index * portion;
        int endI = startI + portion;

        List<MutantTestResult> result = new ArrayList<>();

        Path[] list = Files.list(new File(outputDir).toPath()).filter(path -> path.toString().endsWith(".java"))
                .toArray(Path[]::new);
        File originalFilePath = new File(originalFile);

        for (int i = startI; i < list.length && i < endI; i++) {
            boolean found = false;
            for (int input : INPUT_VECTORS) {
                if (compileFile(originalFilePath) != 0) {
                    System.out.println("The original file did not compile");
                    System.exit(1);
                }

                Path path = list[i];
                if (compileFile(path.toFile()) != 0) {
                    result.add(new MutantTestResult(i, MutantType.StillBornMutnat));
                    found = true;
                    break;
                }

                String originalOutput = null;
                String mutantOutput = null;
                Throwable originalException = null;
                Throwable mutantException = null;
                try {
                    originalOutput = runClass(new File(getClassName(originalFile)), input);
                } catch (InvocationTargetException e) {
                    originalException = e.getCause();
                } catch (Exception e) {
                    originalException = e;
                }

                try {
                    mutantOutput = runClass(new File(getClassName(path.toString())), input);
                } catch (InvocationTargetException e) {
                    mutantException = e.getCause();
                } catch (Exception e) {
                    mutantException = e;
                }

                if (originalException == null && mutantException == null
                        && originalOutput.compareTo(mutantOutput) != 0) {
                    result.add(new MutantTestResult(i, MutantType.KilledMutant, input, originalOutput, mutantOutput));
                    found = true;
                    break;
                } else if (originalException != null || mutantException != null) {
                    result.add(new MutantTestResult(i, MutantType.KilledMutant, input,
                            originalException == null ? "No Exception" : "Exception: " + originalException.getMessage(),
                            mutantException == null ? "No Exception" : "Exception: " + mutantException.getMessage()));
                    found = true;
                    break;
                }
            }
            if (!found) {
                result.add(new MutantTestResult(i, MutantType.EquivalentMutant));
            }
        }
        return result;
    }

    private String getClassName(String file) {
        return file.substring(0, file.lastIndexOf(".java")) + ".class";
    }

    private String runClass(File javaClass, int input) throws Exception {
        URL classUrl = new File(javaClass.getParent()).toURI().toURL();
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { classUrl });
        Class<?> clazz = Class.forName(javaClass.getName().replace(".class", ""), true, classLoader);
        Method m = clazz.getMethod("methodUnderTest", int.class);
        return (String) m.invoke(null, input);
    }

    private int compileFile(File path) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        return compiler.run(null, null, null, path.getAbsolutePath());
    }
}