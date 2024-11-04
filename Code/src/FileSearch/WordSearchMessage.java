package FileSearch;

import java.io.Serializable;

public class WordSearchMessage implements Serializable {
    private String keyword;
    private String requesterNode;

    public WordSearchMessage(String keyword, String requesterNode) {
        this.keyword = keyword;
        this.requesterNode = requesterNode;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getRequesterNode() {
        return requesterNode;
    }
}
