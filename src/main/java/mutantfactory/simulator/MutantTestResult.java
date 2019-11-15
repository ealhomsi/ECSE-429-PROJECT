package mutantfactory.simulator;

public class MutantTestResult implements Comparable<MutantTestResult> {
    private MutantType type;
    private int testVector;
    private String originalOutput;
    private String mutantOutput;
    private int index;

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getOriginalOutput() {
        return this.originalOutput;
    }

    public void setOriginalOutput(String originalOutput) {
        this.originalOutput = originalOutput;
    }

    public String getMutantOutput() {
        return this.mutantOutput;
    }

    public void setMutantOutput(String mutantOutput) {
        this.mutantOutput = mutantOutput;
    }

    public MutantType getType() {
        return this.type;
    }

    public void setType(MutantType type) {
        this.type = type;
    }

    public int getTestVector() {
        return this.testVector;
    }

    public void setTestVector(int testVector) {
        this.testVector = testVector;
    }

    public MutantTestResult(int index, MutantType type) {
        super();
        this.index = index;
        this.type = type;
    }

    public MutantTestResult(int index, MutantType type, int test, String orginalOutput, String mutantOutput) {
        this(index, type);
        this.testVector = test;
        this.originalOutput = orginalOutput;
        this.mutantOutput = mutantOutput;
    }

    @Override
    public int compareTo(MutantTestResult o) {
        return this.index - o.index;
    }

    @Override
    public String toString() {
        if (type != MutantType.KilledMutant) {
            return String.format("%d: Mutant type: %s", index, type.toString());
        } else {
            return String.format("%d: Mutant type: %s @ testInput %d with original output %s and mutant output %s",
                    index, type.toString(), testVector, originalOutput, mutantOutput);
        }
    }
}