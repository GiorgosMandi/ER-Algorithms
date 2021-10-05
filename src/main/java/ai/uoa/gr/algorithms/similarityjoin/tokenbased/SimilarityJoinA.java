package ai.uoa.gr.algorithms.similarityjoin.tokenbased;

import java.util.List;
import java.util.Set;

/**
 * @author George Mandilaras (NKUA)
 */
public abstract class SimilarityJoinA {
    // Jaccard Similarity Threshold
    float Tj;
    List<String> source;

    public SimilarityJoinA(List<String> source, float t) {
        this.Tj = t;
        this.source = source;
    }

    abstract public Set<Integer> query(String t);

    abstract public void index(List<String> source);
}
