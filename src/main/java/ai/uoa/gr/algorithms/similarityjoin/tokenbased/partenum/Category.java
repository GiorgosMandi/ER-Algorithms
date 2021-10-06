package ai.uoa.gr.algorithms.similarityjoin.tokenbased.partenum;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.sun.javaws.exceptions.InvalidArgumentException;
import gnu.trove.list.TIntList;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;

/**
 * @author George Mandilaras (NKUA)
 */
public class Category {
    public static class Indices extends ArrayList<Integer> {
        Indices(List<Integer> l){
            super(l);
        }

        Indices(Integer l){
            super();
            add(l);
        }
    }
    // vector size range - application field
    int r, l;

    // vector size
    int N;

    // N1 total 1st level partitions - N2 total 2nd level partitions
    int N1, N2;
    // K is Hamming distance threshold - derives from JS threshold
    // K2 is a fixed number derived from K - (N2-K2) is the size of the second level partitions
    int K, K2;


    public List<List<Indices>> partitions;
    public long totalSignatures;

    public int[][] partitionRangeStarts;
    public int[][] partitionRangeEnds;

    // TODO see what this is
    public HashMap<Integer, TIntList>[] sig_map;


    public Category(int l, int r, float tj, int vectorSize) throws InvalidArgumentException {
        this.r = r;
        this.l = l;
        this.N = vectorSize;
        this.K = (int) floor(2f * ((1f-tj)/(1f+tj))*r);

        N1 = (int) ceil((K + 1f)/2f);
        //N1 = K + 1;

        // pick N2 so N2 > K && N2 < 2K
        N2 = (2*(K+1))/2 + 1;
        // N2 = 2;

        K2 = (int) ceil(((float)K + 1) / N1) - 1;
        if ((K + 1) % N1 != 0) {
            K2++;
        }

        if (N1 > K + 1 || N1 * N2 <= K + 1) {
            String error1 = String.format("Invalid Arguments K:%d, N1:%d, N2:%d", K, N1, N2);
            String error2 = "Input arguments must comply to `N1 > K + 1 || N1 * N2 <= K + 1`";
            throw new InvalidArgumentException(new String[]{error1, error2});
        }

        // TODO this must be a random permutation
        // the indices of the input vectors
        List<Integer> indicesVector = IntStream.range(0, N).boxed().collect(Collectors.toList());

        partitions = new ArrayList<>();

        // create the projections of the enumerations for each (1st level) partition
        // i.e. N2=4 create N2 vectors of (N2-K2) size: [ [1,2,3], [0,2,3], [0,1,3], [0,1,2] ]
        Set<Set<Integer>> enumerationsOrder = getEnumerationOrder(IntStream.range(0, N2).boxed().collect(Collectors.toList()),  N2-K2);

        // create the first level partition projection. It will create at least N1 partitions
        List<Indices> firstLeveLPartitions = partition(indicesVector, N1);

        // for each 1st level partition apply enumeration
        // each partition is partitioned into N2 partitions, which are then enumerated based on the enumeration order
        for (List<Integer> firstLeveLPartition:  firstLeveLPartitions){
            List<List<Indices>> enumeratedPartitions = enumerate(firstLeveLPartition, N2, enumerationsOrder);
            partitions.addAll(enumeratedPartitions);
        }
        totalSignatures = N1 * nCr(N2,K2);

        partitionRangeStarts = new int[N1][N2];
        partitionRangeEnds = new int[N1][N2];
        for (int i = 0; i < N1; i++) {
            for (int j = 0; j < N2; j++) {
                // In the definition it subtracts i, j with -1, because the index starts from 1 to n
                // we add +1 to avoid negative values
                partitionRangeStarts[i][j] = (N *(N2*i + j)/(N1*N2));
                partitionRangeEnds[i][j] = (N *(N2*i + j+1)/(N1*N2));
            }
        }
    }



    public List<Boolean> sign(boolean[] vector){
        List<Boolean> signature = new ArrayList<>();
        for(int i=0; i<N1; i++){
            for (int j = 0; j < partitions.get(i).size(); j++) {
                int start = Math.max(0, partitionRangeStarts[i][j]);
                int end = Math.min(vector.length, partitionRangeEnds[i][j]);
                for (int pos=start; pos<end; pos++)
                    signature.add(vector[pos]);
            }
        }
        return signature;
    }

    // creates more partitions than expected
    public List<Indices> partition(List<Integer> list, int partitionsNumber){
        List<Indices> newPartitions = new LinkedList<>();

        // in case input vector is bigger than the partition
        int vectorSize = list.size();
        if (vectorSize <= partitionsNumber){
            int rest = partitionsNumber - vectorSize;
            for(Integer v: list) {
                newPartitions.add(new Indices(v));
            }
            for(int i=0; i< rest; i++){
                newPartitions.add(new Indices(Collections.emptyList()));
            }
            return newPartitions;
        }
        else {
            int size = (int) Math.floor((float) vectorSize / (float) partitionsNumber);
            Iterables.partition(list, size).forEach(e -> newPartitions.add(new Indices(e)));
            if (newPartitions.size() < partitionsNumber) {
                // when rest == 0 it inserts an empty list
                int rest = vectorSize % size;
                newPartitions.add(new Indices(list.subList(Math.max(vectorSize - rest, 0), vectorSize)));
            }
            return newPartitions;
        }
    }

    public List<List<Indices>> enumerate(List<Integer> vector, int totalPartitions, Set<Set<Integer>> enumerationsOrder){
        List<List<Indices>> enumeratedPartitions = new ArrayList<>();
        List<Indices> secondLeveLPartitions = partition(vector, totalPartitions);
        for(Set<Integer> enumeration: enumerationsOrder){
            List<Indices> enumeratedPartition = new LinkedList<>();
            enumeration.forEach(e -> enumeratedPartition.add(secondLeveLPartitions.get(e)));
            enumeratedPartitions.add(enumeratedPartition);
        }
        return enumeratedPartitions;
    }

    public Set<Set<Integer>> getEnumerationOrder(List<Integer> list, int size){
        Set<Integer> set = new HashSet<>(list);
        Set<Set<Integer>> combinations;
        if (set.size() >= size)
            combinations = Sets.combinations(set, size);
        else {
            combinations = new HashSet<>();
            combinations.add(set);
        }
        return combinations;
    }


    long nCr(int n, int r) {
        return fact(n) / (fact(r) * fact(n - r));
    }

    long fact(int n){
        long res = 1;
        for (int i = 2; i <= n; i++)
            res = res * i;
        return res;
    }
}
