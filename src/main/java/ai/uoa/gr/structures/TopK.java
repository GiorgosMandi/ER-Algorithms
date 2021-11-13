package ai.uoa.gr.structures;

import ai.uoa.gr.structures.comparators.TopKComparator;
import info.debatty.java.stringsimilarity.Jaccard;
import org.javatuples.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author George Mandilaras (NKUA)
 */
public class TopK {
    public int K;
    public String target;
    public TreeSet<Pair<Integer, Float>> maxHeap;
    public Jaccard jSimilarity = new Jaccard();
    public Map<String, Float> verifications = new HashMap<>();

    public TopK(String t, int k){
        maxHeap = new TreeSet<>(new TopKComparator());
        K = k;
        target = t;
    }

    public void insert(Integer id, String s){
        float similarity = (float) getSimilarity(s);
        maxHeap.add(new Pair<>(id, similarity));
        if (maxHeap.size() > K)
            maxHeap.pollLast();
    }

    public double getSimilarity(String s){
        if (verifications.containsKey(s))
            return verifications.get(s);
        else {
            float sim = (float) jSimilarity.similarity(target, s);
            verifications.put(s, sim);
            return sim;
        }
    }

    public Pair<Integer, Float> pollFirst(){ return maxHeap.pollFirst();}

    public Pair<Integer, Float> pollLast(){ return maxHeap.pollLast();}

    public boolean isFull(){return maxHeap.size() >= K;}

    public float getMinSimilarity(){
        return maxHeap.last().getValue1();
    }

    public Set<Integer> flatten(){
        return maxHeap.stream().map(Pair::getValue0).collect(Collectors.toSet());
    }
}
