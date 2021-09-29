package ai.uoa.gr.algorithms.similarityjoin.tokenbased;

import ai.uoa.gr.utils.PairsComparator;
import org.javatuples.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author George Mandilaras (NKUA)
 */

public class AllPairs {
    // Jaccard Similarity Threshold
    float Tj;
    List<String> source;
    Map<Character, Integer> frequenciesMap;
    Map<Character, List<Integer>> prefixInvertedIndex;

    public AllPairs(List<String> source, float t) {
        this.Tj = t;
        this.source = source;
        this.frequenciesMap = buildFrequenciesMap(source);
        this.prefixInvertedIndex = buildPrefixInvertedIndex(source);
    }

    /**
     * Find the Frequencies of each character by parsing the source dataset
     * @param source    source dataset
     * @return          a hashmap char -> frequencies
     */
    private Map<Character, Integer> buildFrequenciesMap(List<String> source) {
        Map<Character, Integer> frequencies = new HashMap<>();
        for (String s : source) {
            for (char c : s.toCharArray()) {
                if (frequencies.containsKey(c))
                    frequencies.put(c, frequencies.get(c) + 1);
                else
                    frequencies.put(c, 1);
            }
        }
        return frequencies;
    }

    /**
     * Build the Prefix Inverted Index.
     * The structure is a Map where Chars point to a list of indices which point to entities that
     * contain them in their prefix.
     * @param source    dataset based on which we will build the Inverted Index
     * @return          an Inverted Index
     */
    private Map<Character, List<Integer>> buildPrefixInvertedIndex(List<String> source) {
        Map<Character, List<Integer>> invertedIndex = new HashMap<>();
        for (int i =0; i<source.size(); i++) {
            String s = source.get(i);
            Set<Character> prefix = getPrefix(s, this.Tj);
            for (char p: prefix){
                if (!invertedIndex.containsKey(p))
                    invertedIndex.put(p, new LinkedList<>());
                invertedIndex.get(p).add(i);
            }
        }
        return invertedIndex;
    }

    /**
     * Given a String find its prefix
     *      Compute it by using a TreeSet of Pairs containing chars and their frequencies.
     *      The inequality of the pairs is defined by their values but the equality by their keys.
     *      Only different pairs (i.e., different keys) are added to the TreeSet.
     * @param s a string
     * @param tj a Jaccard threshold used to compute the size of the prefix
     * @return  string's prefix
     */
    public Set<Character> getPrefix(String s, float tj) {
        int prefixSize = getPrefixSize(s, tj);
        char[] characters = s.toCharArray();
        TreeSet<Pair<Character, Integer>> prefixWithFrequencies = new TreeSet<>(new PairsComparator());
        for (char c : characters) {
            Pair<Character, Integer> pair = new Pair<>(c, this.frequenciesMap.getOrDefault(c, Integer.MAX_VALUE));
            prefixWithFrequencies.add(pair);
            if (prefixWithFrequencies.size() > prefixSize)
                prefixWithFrequencies.pollLast();
        }
        return prefixWithFrequencies.stream().map(Pair::getValue0).collect(Collectors.toSet());
    }

    /**
     *  Given a String and a Jaccard threshold get the size of its prefix.
     * @param s a string
     * @param tj a Jaccard threshold
     * @return the size of its prefix
     */
    public int getPrefixSize(String s, float tj) {
        int size = s.length();
        return (int) (size - Math.ceil(tj * size) + 1);
    }

    /**
     * check if the input strings have similar sizes based on a Jaccard threshold
     * @param s a string
     * @param t a string
     * @param tj a Jaccard threshold
     * @return true if the strings have similar sizes
     */
    public boolean lengthFilter(String s, String t, float tj){
        return tj *t.length() <= s.length() || s.length() <= t.length()/tj;
    }

    /**
     *  For a target entity, get its candidates.
     *      Find its prefix and use it to query the Prefix Inverted Index.
     *      Filter out candidates using the Length Filter.
     * @param t a target string
     * @return a list of candidates' indices
     */
    public List<Integer> query(String t){
        List<Integer> candidates = new LinkedList<>();
        Set<Character> prefix = getPrefix(t, this.Tj);
        for (char p: prefix) {
            List<Integer> prefixCandidates = this.prefixInvertedIndex
                    .getOrDefault(p, Collections.emptyList())
                    .stream()
                    .filter(i -> this.lengthFilter(this.source.get(i), t, this.Tj))
                    .collect(Collectors.toList());
            candidates.addAll(prefixCandidates);
        }
        return candidates;
    }

    /**
     * return candidates, not their indices
     * @param t a target string
     * @return candidates
     */
    public List<String> getCandidates(String t){
        return query(t).stream().map(i -> source.get(i)).collect(Collectors.toList());
    }
}
