package mutantfactory.simulator;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class MutantTestCallable implements Callable<List<MutantTestResult>> {
    public static final int[] INPUT_VECTORS = { 2, 4, 8, 16, 32, 64, 128, 512, 1024 };
    public static final String METHOD_NAME = "methodUnderTest";

    private int index;
    private int portion;
    private Path[] list;
    private Method originalMethod;

    public Method getOriginalMethod() {
        return this.originalMethod;
    }

    public void setOriginalMethod(Method originalMethod) {
        this.originalMethod = originalMethod;
    }

    public Path[] getList() {
        return this.list;
    }

    public void setList(Path[] list) {
        this.list = list;
    }

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

    public MutantTestCallable(int index, int portion, Path[] list, Method originalMethod) {
        super();
        this.index = index;
        this.portion = portion;
        this.list = list;
        this.originalMethod = originalMethod;
    }

    @Override
    public List<MutantTestResult> call() throws Exception {
        int startI = index * portion;
        int endI = startI + portion;

        List<MutantTestResult> result = new ArrayList<>();

        for (int i = startI; i < list.length && i < endI; i++) {
            Path path = list[i];
            Method mutantMethod = null;

            try {
                if (compileFile(path.toFile()) != 0) {
                    result.add(new MutantTestResult(i, MutantType.StillBornMutnat));
                    continue;
                }
                mutantMethod = getClassFromFile(new File(getClassName(path.toString()))).getMethod(METHOD_NAME,
                        int.class);
            } catch (Exception e) {
                result.add(new MutantTestResult(i, MutantType.StillBornMutnat));
                continue;
            }

            boolean mutantKilled = false;
            for (int input : INPUT_VECTORS) {
                String originalOutput = null;
                String mutantOutput = null;
                Throwable originalException = null;
                Throwable mutantException = null;
                try {
                    originalOutput = (String) originalMethod.invoke(null, input);
                } catch (InvocationTargetException e) {
                    originalException = e.getCause();
                } catch (Exception e) {
                    originalException = e;
                }

                try {
                    mutantOutput = (String) mutantMethod.invoke(null, input);
                } catch (InvocationTargetException e) {
                    mutantException = e.getCause();
                } catch (Exception e) {
                    mutantException = e;
                }

                if (originalException == null && mutantException == null
                        && originalOutput.compareTo(mutantOutput) != 0) {
                    result.add(new MutantTestResult(i, MutantType.KilledMutant, input, originalOutput, mutantOutput));
                    mutantKilled = true;
                    break;
                } else if (originalException != null || mutantException != null) {
                    result.add(new MutantTestResult(i, MutantType.KilledMutant, input,
                            originalException == null ? "No Exception"
                                    : "Exception: " + originalException.getClass() + " "
                                            + originalException.getMessage(),
                            mutantException == null ? "No Exception"
                                    : "Exception: " + mutantException.getClass() + " " + mutantException.getMessage()));
                    mutantKilled = true;
                    break;
                }
            }
            if (!mutantKilled) {
                result.add(new MutantTestResult(i, MutantType.EquivalentMutant));
            }
        }
        return result;
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