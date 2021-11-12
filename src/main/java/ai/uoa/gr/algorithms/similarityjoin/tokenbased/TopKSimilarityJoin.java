package ai.uoa.gr.algorithms.similarityjoin.tokenbased;

import ai.uoa.gr.structures.TopK;
import org.javatuples.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author George Mandilaras (NKUA)
 */


//     TODO:
//      (1): During the query procedure, the algorithm populates the inverted index. There is an optimization in the algorithms
//          which prevents indexing entities that its ubound is less than the min similarity of top-k. This may have an impact
//          in the precision since it will probably result to fewer verifications.
//      (2): The paper proposes a verification optimization, but there is no need for us to do this, as the way we structured
//           the algorithm we can use a hash map, as mentioned in the paper.
//      (3):

public class TopKSimilarityJoin extends AllPairs {
    public int K;

    public TopKSimilarityJoin(List<String> source, int k) {
        super(source, 0f);
        K = k;
    }

    @Override
    public Set<Integer> query(String t) {

        // use 0f for threshold to compute as prefix the whole vector (sorts vector based DF)
        List<Pair<Character, Integer>> prefix = getPrefix(t, 0f);
        TopK topK = initializeTemporaryResults(t, prefix);
        int tSize = prefix.size();

        int prefixIndex = 0;
        float similarityUpperBound = 1f;
        float minSimilarity = topK.getMinSimilarity();
        while(similarityUpperBound > minSimilarity){
            char prefixChar = prefix.get(prefixIndex).getValue0();
            final int prefixCharIndex = prefixIndex;
            List<Pair<Integer, Integer>> candidates =  this.prefixInvertedIndex
                    .getOrDefault(prefixChar, Collections.emptyList())
                    .stream()
                    .filter(p -> p.getValue1() <= prefixCharIndex) // after that index prefixes has not yet been discovered
                    .collect(Collectors.toList());
            for (Pair<Integer, Integer> c: candidates){
                int entityIndex = c.getValue0();
                int entityPrefixIndex = c.getValue1();
                String s = source.get(entityIndex);
                float ubound = getSimilarityUpperBoundAccess(1-((float) (prefixIndex-1)/ (float) t.length()),
                                                        1-((float) (entityPrefixIndex-1)/ (float) s.length()));
                if (lengthFilter(t, s, minSimilarity) && ubound >= minSimilarity){
                    topK.insert(entityIndex, s);
                    minSimilarity = topK.getMinSimilarity();
                }
            }
            prefixIndex+=1;
            similarityUpperBound = getSimilarityUpperBoundProbe(tSize, prefixIndex+1); // to avoid the 0 case (not mentioned in the paper)
            minSimilarity = topK.getMinSimilarity();
        }
        return topK.flatten();
    }


    public TopK initializeTemporaryResults(String target, List<Pair<Character, Integer>> prefix){
        TopK topK = new TopK(target, this.K);
        for (Pair<Character, Integer> p: prefix) {
            char prefixChar = p.getValue0();
            List<Pair<Integer, Integer>> candidates = this.prefixInvertedIndex.getOrDefault(prefixChar, Collections.emptyList());
            for (Pair<Integer, Integer> c: candidates){
                if (topK.isFull())
                    return topK;
                int sourceIndex = c.getValue0();
                topK.insert(sourceIndex, source.get(sourceIndex));
            }
        }
        return topK;
    }

    public float getSimilarityUpperBoundProbe(int tSize, int prefixIndex){
        return 1 - ((float) (prefixIndex-1)/ (float) tSize);
    }

    public float getSimilarityUpperBoundAccess(float spx, float spy){
        float product = spx*spy;
        return product/(spx + spy - product);
    }
}
