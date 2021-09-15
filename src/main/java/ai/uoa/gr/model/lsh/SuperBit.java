package ai.uoa.gr.model.lsh;

import info.debatty.java.lsh.LSHSuperBit;
import org.scify.jedai.datamodel.Attribute;
import org.scify.jedai.datamodel.EntityProfile;

import java.util.*;

public class SuperBit extends LocalitySensitiveHashing{

    LSHSuperBit lsh;

    public SuperBit(int vectorSize, int r, int buckets) {
        this.vectorSize = vectorSize;
        this.bands = (int) Math.ceil((float) vectorSize /(float) r);
        this.numOfBuckets = buckets;
        this.lsh = new LSHSuperBit(this.bands, this.numOfBuckets, vectorSize);

        // array of buckets containing Lists of entity IDs
        this.buckets = (ArrayList<Integer>[]) new ArrayList[this.numOfBuckets];
    }

    /**
     * given an entity compute its hash, i.e., the buckets it belongs to
     * @param e entity
     * @return the indices of buckets
     */
    public int[] hash(EntityProfile e){
        StringBuilder sb = new StringBuilder();
        for (Attribute attr : e.getAttributes()){
            sb.append(attr.getValue());
            sb.append("\t");
        }
        // WARNING: this is not Shingling, but just a byte encoding of the string - last bytes are false
        String eText = sb.toString();
        int[] eSignature = this.fromString(eText);
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


    // to encode string into int[]

    // WARNING: currently I use the ascii encoding of the input string
    public int[] fromString(String str) {
        int[] encoding = new int[str.length()];
        Arrays.setAll(encoding, i -> Character.getNumericValue(str.charAt(i)));

        int[] result = new int[vectorSize];
        Arrays.fill(result, 0);

        //WARNING enforce size
        int length = Math.min(encoding.length, vectorSize);
        System.arraycopy(encoding, 0, result, 0, length);
        return result;
    }
}
