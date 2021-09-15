package ai.uoa.gr.model.lsh;

import info.debatty.java.lsh.LSHMinHash;
import org.scify.jedai.datamodel.Attribute;
import org.scify.jedai.datamodel.EntityProfile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MinHash extends LocalitySensitiveHashing {

    LSHMinHash lsh;

    public MinHash(int vectorSize, int r, int buckets) {
        this.vectorSize = vectorSize;
        this.bands = (int) Math.ceil((float) vectorSize /(float) r);
        this.numOfBuckets = buckets;
        this.lsh = new LSHMinHash(this.bands, this.numOfBuckets, vectorSize);

        // array of buckets containing Lists of entity IDs
        this.buckets = (ArrayList<Integer>[]) new ArrayList[this.numOfBuckets];
    }

    /**
     * given an entity compute its hash, i.e., the buckets it belongs to
     * @param e entity
     * @return the indices of buckets
     */
    public int[] hash(EntityProfile e){

        // first encode the entity
        StringBuilder sb = new StringBuilder();
        for (Attribute attr : e.getAttributes()){
            sb.append(attr.getValue());
            sb.append("\t");
        }
        // WARNING: this is not Shingling, but just a byte encoding of the string - last bytes are false
        String eText = sb.toString();
        boolean[] eSignature = this.fromString(eText);

        // compute its hash
        return lsh.hash(eSignature);
    }

    /**
     * Index a list of entities into the buckets
     *
     * @param entities a list of entities
     */
    public void index(List<EntityProfile> entities){
        for (int i=0; i<entities.size(); i++){
            EntityProfile entity = entities.get(i);
            int[] hashes = hash(entity);
            for (int hash: hashes){
                if (buckets[hash] == null) {
                    ArrayList<Integer> bucketEntities = new ArrayList<>();
                    buckets[hash] = bucketEntities;
                }
                ArrayList<Integer> bucketEntities = buckets[hash];
                bucketEntities.add(i);
            }
        }
    }

    /**
     * find the candidates of an entity.
     * @param entity target entity
     * @return a set of the IDs of the candidate entities
     */
    public Set<Integer> query(EntityProfile entity){
        Set<Integer> candidates = new HashSet<>();
        int[] hashes = hash(entity);
        for (int hash: hashes){
            if(buckets[hash] != null)
                candidates.addAll(buckets[hash]);
        }
        return candidates;
    }

    // to encode string into boolean[]

    public boolean[] byteToBoolArr(byte b) {
        boolean[] boolArr = new boolean[8];
        for (int i = 0; i < 8; i++) {
            boolArr[i] = (b & (byte) (128 / Math.pow(2, i))) != 0;
        }
        return boolArr;
    }

    // WARNING: currently I use the binary encoding of the input string
    public boolean[] fromString(String str) {
        byte[] bytes = str.getBytes();
        boolean[] result = new boolean[vectorSize];
        int index = 0;
        for (int i = 0; i < str.length(); i++, index += 8) {
            boolean[] bits = byteToBoolArr(bytes[i]);
            System.arraycopy(bits, 0, result, index, bits.length);
        }
        return result;
    }

}
