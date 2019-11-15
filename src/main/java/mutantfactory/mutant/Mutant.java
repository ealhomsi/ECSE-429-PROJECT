package mutantfactory.mutant;

import java.io.*;
import java.nio.file.*;

import com.github.javaparser.Position;

/**
 * this class holds the mutant information
 */
public class Mutant {
    private String className;
    private char originalOperator;
    private char mutantOperator;
    private Position pos;
    private String contents;

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public char getOriginalOperator() {
        return this.originalOperator;
    }

    public void setOriginalOperator(char originalOperator) {
        this.originalOperator = originalOperator;
    }

    public char getMutantOperator() {
        return this.mutantOperator;
    }

    public void setMutantOperator(char mutantOperator) {
        this.mutantOperator = mutantOperator;
    }

    public Position getPos() {
        return this.pos;
    }

    public void setPos(Position pos) {
        this.pos = pos;
    }

    public String getContents() {
        return this.contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public Mutant(Position pos, char orig, char mutant, String contents, String className) {
        this.pos = pos;
        this.originalOperator = orig;
        this.mutantOperator = mutant;
        this.contents = contents;
        this.className = className;
    }

    @Override
    public String toString() {
        return String.format("Mutant: %s: orignal:(%c) -> mutant(%c) at %s", className, originalOperator,
                mutantOperator, pos.toString());
    }

    public void saveToFile(String prefix, int index) {
        String output = this.contents.toString() + "\n/**\n* " + this.toString() + '\n' + repeat("*", 80) + "\n*/";
        try {
            Files.write(Paths.get(prefix, String.format("%sMutant%d.java", className, index)), output.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }

        return sb.toString();
    }
}
