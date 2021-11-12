package ai.uoa.gr.structures.comparators;

import org.javatuples.Pair;

import java.util.Comparator;
import java.util.Objects;

/**
 * @author George Mandilaras (NKUA)
 */
public class TopKComparator implements Comparator<Pair<Integer, Float>> {

    public int compare(Pair<Integer, Float> o1, Pair<Integer, Float> o2) {
        if(Objects.equals(o1.getValue0(), o2.getValue0())) return 0;
        else if (o1.getValue1() <= o2.getValue1()) return 1;
        else return -1;
    }
}
