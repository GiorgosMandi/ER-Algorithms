package ai.uoa.gr.utils;

import org.javatuples.Pair;

import java.util.Comparator;

/**
 * @author George Mandilaras (NKUA)
 */
public class PairsComparator implements Comparator<Pair<Pair<Character, Integer>, Integer>> {

    public int compare(Pair<Pair<Character, Integer>, Integer> o1, Pair<Pair<Character, Integer>, Integer> o2) {
        if(o1.getValue0().getValue0() == o2.getValue0().getValue0()) return 0;
        else if (o1.getValue1() >= o2.getValue1()) return 1;
        else return -1;
    }
}
