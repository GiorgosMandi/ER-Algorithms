package ai.uoa.gr.algorithms.similarityjoin.tokenbased;

import org.javatuples.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.ceil;
import static java.lang.Math.min;

/**
 * @author George Mandilaras (NKUA)
 */
public class PPJoin extends AllPairs{

    public PPJoin(List<String> source, float t) {
        super(source, t);
    }


    /**
     *  For a target entity, get its candidates.
     *      Find its prefix and use it to query the Prefix Inverted Index.
     *      Filter out candidates using the Length Filter.
     * @param t a target string
     * @return a list of candidates' indices
     */
    public Set<Integer> query(String t){
        // in A[s] will accumulate the amount of overlaps that occur in the prefixes of (s, t)
        Map<Integer, Integer> A = new HashMap<>();
        List<Pair<Character, Integer>> targetPrefix = getPrefix(t, this.Tj);
        for (Pair<Character, Integer> p: targetPrefix) {
            char targetPrefixChar = p.getValue0();
            int targetPrefixCharIndex = p.getValue1();

            // query PrefixInvertedIndex to get the pre-candidates and filter them using the length filter
            List<Pair<Integer, Integer>> preCandidates = this.prefixInvertedIndex
                    .getOrDefault(targetPrefixChar, Collections.emptyList())
                    .stream().filter(pair -> lengthFilter(source.get(pair.getValue0()), t, this.Tj))
                    .collect(Collectors.toList());

            // for each pre-candidate examine the positional filter
            for(Pair<Integer, Integer> preCandidate: preCandidates){
                int preCandidateIndex = preCandidate.getValue0();
                int preCandidatePrefixIndex = preCandidate.getValue1();
                String s = source.get(preCandidateIndex);

                // from jaccard to overlap threshold
                int overlapThreshold = (int) ceil( (this.Tj/(this.Tj+1)) * (s.length() + t.length()) );

                // admit pairs as a candidate pair only if its upper bound is no less than the threshold overlapThreshold
                // upperBoundOverlap is an upper bound of the overlap between right partitions of (s, t) with respect
                // to the current prefix token targetPrefixChar
                int upperBoundOverlap = 1 + min(s.length()-preCandidatePrefixIndex, t.length()-targetPrefixCharIndex);

                // upperBoundOverlap holds the overlap between right partitions and
                // currently A holds the overlap of the left partitions between (s, t)
                if (A.getOrDefault(preCandidateIndex, 0) + upperBoundOverlap >= overlapThreshold)
                    A.put(preCandidateIndex, A.getOrDefault(preCandidateIndex, 0) + 1);
                else
                    A.put(preCandidateIndex, 0);
            }
        }
        return A.keySet().stream()
                .filter(k -> A.getOrDefault(k, 0) > 0)
                .collect(Collectors.toSet());
    }

}
