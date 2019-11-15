package mutantfactory.mutant;

import java.util.List;
import java.util.Random;
import mutantfactory.parser.*;

import com.github.javaparser.Position;

public class MutantListGenerator {
    private static final Random rand = new Random(System.currentTimeMillis());
    private String className;

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public MutantListGenerator(String className) {
        super();
        this.className = className;
    }

    public Mutant generateMutant(String inputProgram, List<Position> positions) {
        // chose an operator at random
        Position position = positions.get(rand.nextInt(positions.size()));

        // figure out its index
        int operatorIndexInProgram = ordinalIndexOf(inputProgram,  System.getProperty("line.separator"), position.line - 1) + position.column + 1;

        char originalOperator = inputProgram.charAt(operatorIndexInProgram);
        // fetch a mutant operator
        int mutantOperatorIndex = 0;
        do {
            mutantOperatorIndex = rand.nextInt(4);
        } while (BinaryOperatorsFetcher.OPERATORS.get(mutantOperatorIndex) == originalOperator);

        char mutantOperator = BinaryOperatorsFetcher.OPERATORS.get(mutantOperatorIndex);

        char[] outputProgram = new String(inputProgram).toCharArray();
        outputProgram[operatorIndexInProgram] = mutantOperator;

        return new Mutant(position, originalOperator, mutantOperator, new String(outputProgram), this.className);
    }

    private int ordinalIndexOf(String str, String substr, int n) {
        n--;
        int pos = -1;
        do {
            pos = str.indexOf(substr, pos + 1);
        } while (n-- > 0 && pos != -1);
        return pos;
    }
}