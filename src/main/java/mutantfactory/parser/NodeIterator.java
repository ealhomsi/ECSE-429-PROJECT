package mutantfactory.parser;

import com.github.javaparser.ast.Node;

public class NodeIterator {
    public interface NodeHandler {
        void handle(Node node);
    }

    private NodeHandler nodeHandler;

    public NodeIterator(NodeHandler nodeHandler) {
        this.nodeHandler = nodeHandler;
    }

    public void explore(Node node) {
        nodeHandler.handle(node);
        for (Node child : node.getChildNodes()) {
            explore(child);
        }
    }
}