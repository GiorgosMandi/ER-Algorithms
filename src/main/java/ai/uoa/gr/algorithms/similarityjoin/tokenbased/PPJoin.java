package ai.uoa.gr.algorithms.similarityjoin.tokenbased;

import org.javatuples.Pair;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author George Mandilaras (NKUA)
 */
public class PPJoin extends AllPairs{

    public PPJoin(List<String> source, float t) {
        super(source, t);
    }

    public boolean positionalFiltering(String s, String t, float tj){
        // TODO
        float a = (float) Math.ceil( (tj/(tj+1)) * (s.length() + t.length()) );
//        ubound ← 1 +min(|x| − i, |y| − j);
        return true;
    }


    /**
     *  For a target entity, get its candidates.
     *      Find its prefix and use it to query the Prefix Inverted Index.
     *      Filter out candidates using the Length Filter.
     * @param t a target string
     * @return a list of candidates' indices
     */
    public Set<Integer> query(String t){
        Set<Integer> candidates = new HashSet<>();
        List<Pair<Character, Integer>> prefix = getPrefix(t, this.Tj);
        for (Pair<Character, Integer> p: prefix){
            char prefixChar = p.getValue0();
            int prefixCharIndex = p.getValue1();
            List<Integer> preCandidates = this.prefixInvertedIndex
                    .getOrDefault(prefixChar, Collections.emptyList())
                    .stream()
                    .filter(i -> this.lengthFilter(this.source.get(i), t, this.Tj) &&
                            this.positionalFiltering(this.source.get(i), t, this.Tj))
                    .collect(Collectors.toList());
            candidates.addAll(preCandidates);
        }
        return candidates;
    }

}
