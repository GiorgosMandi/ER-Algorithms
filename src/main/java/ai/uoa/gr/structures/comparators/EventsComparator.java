package ai.uoa.gr.structures.comparators;

import org.javatuples.Pair;

import java.util.Comparator;

/**
 * @author George Mandilaras (NKUA)
 */
public class EventsComparator implements Comparator<Pair<Integer, Float>> {

    public int compare(Pair<Integer, Float> o1, Pair<Integer, Float> o2) {
        return o2.getValue1().compareTo(o1.getValue1());
    }
}