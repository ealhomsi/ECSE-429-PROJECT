package mutantfactory.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Position;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class BinaryOperatorsFetcher {
    private JavaParser parser;
    public final static List<Character> OPERATORS = Arrays.asList('*', '+', '-', '/');

    public BinaryOperatorsFetcher(JavaParser parser) {
        super();
        this.parser = parser;
    }

    public List<Position> binaryOperationPositions(String contents) {
        List<Position> result = new ArrayList<>();

        try {
            new NodeIterator(new NodeIterator.NodeHandler() {
                @Override
                public void handle(Node node) {
                    if (node instanceof BinaryExpr) {
                        BinaryExpr be = (BinaryExpr) node;
                        String operator = be.getOperator().asString();
                        if (operator.length() == 1 && OPERATORS.contains(operator.charAt(0))) {
                            Node left = be.getChildNodes().get(0);
                            Node right = be.getChildNodes().get(1);
                            result.add(new Position(be.getBegin().get().line,
                                    (left.getEnd().get().column + right.getBegin().get().column) / 2));
                        }
                    }
                }
            }).explore(parser.parse(contents).getResult().get());
        } catch (Exception e) {
            new RuntimeException(e);
        }

        return result;
    }
}