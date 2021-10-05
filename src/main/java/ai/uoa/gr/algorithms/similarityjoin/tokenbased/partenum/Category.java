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

    // vector size range - application field
    int r, l;

    // vector size
    int N;

    // N1 total 1st level partitions - N2 total 2nd level partitions
    int N1, N2;
    // K is Hamming distance threshold - derives from JS threshold
    // K2 is a fixed number derived from K - (N2-K2) is the size of the second level partitions
    int K, K2;

    // A List of N1 lists, where each one contains N2 lists containing a set of all possible (N2-K2) enumerations
    //i.e., TODO write example
    // [0, 9] -> [ [ Set( Set(1,2), Set(0,1), Set(0,2))  ]
    public List<List<Set<Set<Integer>>>> partitions;
    public int signatureSize;


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
        // N1 = K + 1;
        // pick N2 so N2 > K && N2 < 2K
        N2 = (2*(K+1))/2 + 1;
        //N2 = 2;

        K2 = (int) ceil(((float)K + 1) / N1) - 1;
        if ((K + 1) % N1 != 0) {
            K2++;
        }

        if (N1 > K + 1 || N1 * N2 <= K + 1) {
            String error1 = String.format("Invalid Arguments K:%d, N1:%d, N2:%d", K, N1, N2);
            String error2 = "Input arguments must comply to `N1 > K + 1 || N1 * N2 <= K + 1`";
            throw new InvalidArgumentException(new String[]{error1, error2});
        }

        int secondPartitionSize = N2 - K2;
        partitions = new ArrayList<>();
        int firstLevelSize = (int) ceil((float)N/(float)N1);
        int secondLevelSize = (int) ceil((float)firstLevelSize/(float)N2);
        List<Integer> indexVector = IntStream.range(0, N).boxed().collect(Collectors.toList());

        //create first partition level
        List<List<Integer>> firstLeveLPartitions = partition(indexVector, N1);

        for (List<Integer> firstLeveLPartition:  firstLeveLPartitions){

            List<Set<Set<Integer>>> subPartitions = new ArrayList<>();

            List<List<Integer>> secondLeveLPartitions = partition(firstLeveLPartition, N2);
            for(List<Integer> secondLeveLPartition: secondLeveLPartitions){
                Set<Integer> secondLeveLPartitionSet = new HashSet<>(secondLeveLPartition);
                Set<Set<Integer>> combinations;
                if (secondLeveLPartitionSet.size() >= secondPartitionSize)
                    combinations = Sets.combinations(secondLeveLPartitionSet, secondPartitionSize);
                else {
                    combinations = new HashSet<>();
                    combinations.add(secondLeveLPartitionSet);
                }
                subPartitions.add(combinations);
            }
            partitions.add(subPartitions);
        }
        signatureSize = N1 * N2;

        partitionRangeStarts = new int[N1][N2];
        partitionRangeEnds = new int[N1][N2];
        for (int i = 0; i < N1; i++) {
            for (int j = 0; j < N2; j++) {
                // In the definition it subtracts i, j with -1
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

    public List<List<Integer>> partition(List<Integer> list, int partitionsNumber){
        int vectorSize = list.size();
        int size = (int) Math.ceil((float)vectorSize/(float)partitionsNumber);
        List<List<Integer>> partitions = new LinkedList<>();
        Iterables.partition(list, size).forEach(partitions::add);
        if(partitions.size() < partitionsNumber ){
            // there are cases it adds and empty list
            int rest = vectorSize % size;
            partitions.add(list.subList(Math.max(vectorSize - rest, 0), vectorSize));

        }
        return partitions;
    }


}
