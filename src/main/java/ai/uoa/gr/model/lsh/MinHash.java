package ai.uoa.gr.model.lsh;

import info.debatty.java.lsh.LSHMinHash;
import org.scify.jedai.textmodels.SuperBitUnigrams;

import java.util.HashSet;
import java.util.Set;


public class MinHash extends LocalitySensitiveHashing {

    LSHMinHash lsh;

    public MinHash(SuperBitUnigrams[] models, int bands, int buckets) {
        vectorSize = models[0].getVector().length;
        this.bands = bands;
        this.numOfBuckets = buckets;
        r = vectorSize/bands;
        this.lsh = new LSHMinHash(this.bands, this.numOfBuckets, vectorSize);

        System.out.format("MINHASH: Bands %d, r: %d, Buckets: %d, Vector Size: %d\n", bands, r, numOfBuckets, vectorSize);
        index(models);
    }

    /**
     * given an entity compute its hash, i.e., the buckets it belongs to
     * @param model model
     * @return the indices of buckets
     */
    public int[] hash(SuperBitUnigrams model){
        // compute its hash
        boolean[] eSignature = getBooleanVector(model);
        return lsh.hash(eSignature);
    }

    /**
     * Index a list of entities into the buckets
     *
     * @param models a list of models
     */
    public void index(SuperBitUnigrams[] models){
        this.buckets = (HashSet<Integer>[]) new HashSet[this.numOfBuckets];
        for (int i=0; i<models.length; i++){
            int[] hashes = hash(models[i]);
            for (int hash: hashes){
                if (buckets[hash] == null) {
                    HashSet<Integer> bucketEntities = new HashSet<>();
                    buckets[hash] = bucketEntities;
                }
                Set<Integer> bucketEntities = buckets[hash];
                bucketEntities.add(i);
            }
        }
    }

    /**
     * find the candidates of an entity.
     * @param model target model
     * @return a set of the IDs of the candidate entities
     */
    public Set<Integer> query(SuperBitUnigrams model){
        Set<Integer> candidates = new HashSet<>();
        int[] hashes = hash(model);
        for (int hash: hashes){
            if(buckets[hash] != null)
                candidates.addAll(buckets[hash]);
        }
        return candidates;
    }


    public boolean[] getBooleanVector(SuperBitUnigrams model) {
        double[] vector = model.getVector();
        boolean[] booleanVector = new boolean[vector.length];
        for (int i=0; i<vector.length; i++)
            booleanVector[i] = vector[i] > 0;
        return booleanVector;
    }

//    public boolean[] byteToBoolArr(byte b) {
//        boolean[] boolArr = new boolean[8];
//        for (int i = 0; i < 8; i++) {
//            boolArr[i] = (b & (byte) (128 / Math.pow(2, i))) != 0;
//        }
//        return boolArr;
//    }
//
//    // binary encoding of the input string
//    public boolean[] fromString(String str) {
//        byte[] bytes = str.getBytes();
//        boolean[] result = new boolean[vectorSize];
//        int index = 0;
//        for (int i = 0; i < str.length(); i++, index += 8) {
//            boolean[] bits = byteToBoolArr(bytes[i]);
//            System.arraycopy(bits, 0, result, index, bits.length);
//        }
//        return result;
//    }

}
