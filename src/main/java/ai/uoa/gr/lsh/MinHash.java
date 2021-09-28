package ai.uoa.gr.lsh;

import info.debatty.java.lsh.LSHMinHash;

import java.util.Arrays;

public class MinHash extends LocalitySensitiveHashing {

    public MinHash(double[][] vectors, int bands, int buckets, int vectorSize) {
        this.vectorSize = vectorSize;
        this.bands = bands;
        this.numOfBuckets = buckets;
        r = vectorSize/bands;
        this.lsh = new LSHMinHash(this.bands, this.numOfBuckets, this.vectorSize);

        System.out.format("MINHASH: Bands %d, r: %d, Buckets: %d, Vector Size: %d\n", bands, r, numOfBuckets, vectorSize);
        index(vectors);
    }

    /**
     * given an entity compute its hash, i.e., the buckets it belongs to
     * @param vector model
     * @return the indices of buckets
     */
    public int[] hash(double[] vector){
        int[] integers = Arrays.stream(vector).mapToInt(i -> (int)i).toArray();
        return lsh.hashSignature(integers);
    }
}
