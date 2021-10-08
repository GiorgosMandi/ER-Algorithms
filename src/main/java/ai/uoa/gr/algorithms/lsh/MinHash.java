package ai.uoa.gr.algorithms.lsh;

import info.debatty.java.lsh.LSHMinHash;

import java.util.*;

public class MinHash extends LocalitySensitiveHashing {

    public MinHash(double[][] vectors, int bands, int buckets, int vectorSize) {
        this.vectorSize = vectorSize;
        this.bands = bands;
        this.numOfBuckets = buckets;
        this.r = vectorSize/bands;
        this.lsh = new LSHMinHash(this.bands, this.numOfBuckets, this.vectorSize, Calendar.getInstance().getTimeInMillis());
        index(vectors);
    }

    /**
     * given an entity compute its hash, i.e., the buckets it belongs to
     * @param vector model
     * @return the indices of buckets
     */
    public int[] hash(double[] vector){
        boolean[] boolArray = toBooleanVector(vector);
        return ((LSHMinHash) lsh).hash(boolArray);
    }

    public boolean[] toBooleanVector(double[] vector){
        boolean[] boolArray = new boolean[vector.length];
        for (int i=0; i<vector.length;i++)
            boolArray[i] = vector[i] > 0;
        return boolArray;
    }
}
