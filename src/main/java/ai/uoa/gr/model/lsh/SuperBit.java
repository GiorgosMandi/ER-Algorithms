package ai.uoa.gr.model.lsh;

import info.debatty.java.lsh.LSHSuperBit;
import org.scify.jedai.textmodels.SuperBitUnigrams;

import java.util.HashSet;
import java.util.Set;

public class SuperBit extends LocalitySensitiveHashing{

    LSHSuperBit lsh;

    public SuperBit(SuperBitUnigrams[] models, int bands, int buckets) {
        vectorSize = models[0].getVector().length;
        this.bands = bands;
        this.numOfBuckets = buckets;
        r = vectorSize/bands;
        this.lsh = new LSHSuperBit(this.bands, this.numOfBuckets, vectorSize);

        System.out.format("SuperBit: Bands %d, r: %d, Buckets: %d, Vector Size: %d\n", this.bands, r, numOfBuckets, vectorSize);
        index(models);
    }

    /**
     * given an entity compute its hash, i.e., the buckets it belongs to
     * @param model model
     * @return the indices of buckets
     */
    public int[] hash(SuperBitUnigrams model){
        double[] eSignature = model.getVector();
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


//    public int[] fromString(String str) {
//        int[] encoding = new int[str.length()];
//        Arrays.setAll(encoding, i -> Character.getNumericValue(str.charAt(i)));
//
//        int[] result = new int[vectorSize];
//        Arrays.fill(result, 0);
//
//        int length = Math.min(encoding.length, vectorSize);
//        System.arraycopy(encoding, 0, result, 0, length);
//        return result;
//    }
}
