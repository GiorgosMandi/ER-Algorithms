package ai.uoa.gr.algorithms.lsh;

import info.debatty.java.lsh.LSHSuperBit;

public class SuperBit extends LocalitySensitiveHashing{

    public SuperBit(double[][] vectors, int bands, int buckets, int vectorSize) {
        this.vectorSize = vectorSize;
        this.bands = bands;
        this.numOfBuckets = buckets;
        this.r = vectorSize/bands;
        this.lsh = new LSHSuperBit(this.bands, this.numOfBuckets, this.vectorSize);

        System.out.format("SuperBit: Bands %d, r: %d, Buckets: %d, Vector Size: %d\n", this.bands, r, numOfBuckets, vectorSize);
        index(vectors);
    }

    /**
     * given an entity compute its hash, i.e., the buckets it belongs to
     * @param vector model
     * @return the indices of buckets
     */
    public int[] hash(double[] vector){
        return ((LSHSuperBit)lsh).hash(vector);
    }
}
