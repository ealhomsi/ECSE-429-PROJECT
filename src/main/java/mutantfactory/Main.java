package mutantfactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Position;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import mutantfactory.parser.*;
import mutantfactory.simulator.MutantTestResult;
import mutantfactory.simulator.Simulator;
import mutantfactory.mutant.*;

public class Main {
    /**
     * The main method that is going to call the mutators
     */
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println(
                    "Please respect this formula ./mutantfacotry <intputFile> <outputDir> <numberOfMutatns> <#threads>");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputDir = args[1];
        int numberOfMutatns = Integer.parseInt(args[2]);
        int threads = Integer.parseInt(args[3]);

        // check if the file exsits and create outputDir
        File path = new File(inputFile);
        if (!path.exists()) {
            System.out.println("The file " + inputFile + " was not found!");
            System.exit(1);
        }
        String fileName = path.getName();
        new File(outputDir).mkdirs();

        // Read the file
        String contents = readFile(path);

        // Parse the file using JavaParser
        JavaParser jp = new JavaParser();
        BinaryOperatorsFetcher bof = new BinaryOperatorsFetcher(jp);
        List<Position> operatorsPositions = bof.binaryOperationPositions(contents);

        // Generate mutant list and the mutants
        MutantListGenerator mlg = new MutantListGenerator(fileName.substring(0, fileName.lastIndexOf('.')));

        Mutant[] mutants = new Mutant[numberOfMutatns];
        String mutantsList = "";
        try {
            for (int i = 0; i < numberOfMutatns; i++) {
                mutants[i] = mlg.generateMutant(contents, operatorsPositions);
                mutantsList += (i + 1) + ": " + mutants[i].toString() + System.getProperty("line.separator");
                ;
                mutants[i].saveToFile(outputDir, i + 1);
            }
            Files.write(Paths.get(outputDir, "mutantsList.txt"), mutantsList.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Run mutant simulator
        Simulator sim = new Simulator(outputDir, inputFile, threads > numberOfMutatns ? numberOfMutatns : threads,
                numberOfMutatns);

        long startTime = System.nanoTime();
        try {
            List<MutantTestResult> total = sim.runSimulation();
            for (MutantTestResult mtr : total) {
                System.out.println(mtr.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        long elapsedTime = System.nanoTime() - startTime;

        System.out.println("Total execution time in ms is: " + elapsedTime / 1000000);

    }

    /**
     * Reads the contents of a file
     * 
     * @param path : the path of the file to read contents from
     */
    private static String readFile(File path) {
        String fileAsString = null;
        InputStream is = null;
        BufferedReader buf = null;

        try {
            is = new FileInputStream(path);
            buf = new BufferedReader(new InputStreamReader(is));
            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();
            while (line != null) {
                sb.append(line).append(System.getProperty("line.separator"));
                line = buf.readLine();
            }
            fileAsString = sb.toString();
            buf.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileAsString;
    }
}