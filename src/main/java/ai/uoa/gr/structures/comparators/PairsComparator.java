package ai.uoa.gr.structures.comparators;

import org.javatuples.Pair;

import java.util.Comparator;

/**
 * @author George Mandilaras (NKUA)
 */
public class PairsComparator implements Comparator<Pair<Character, Integer>> {

    public int compare(Pair<Character, Integer> o1, Pair<Character, Integer> o2) {
        if(o1.getValue0() == o2.getValue0()) return 0;
        else if (o1.getValue1() >= o2.getValue1()) return 1;
        else return -1;
    }
}
