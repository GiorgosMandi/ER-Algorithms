package ai.uoa.gr.model.lsh;

import info.debatty.java.lsh.LSHSuperBit;
import org.scify.jedai.datamodel.Attribute;
import org.scify.jedai.datamodel.EntityProfile;

import java.util.*;

public class SuperBit extends LocalitySensitiveHashing{

    LSHSuperBit lsh;

    public SuperBit(){
        this.lsh = new LSHSuperBit(this.bands, this.buckets, vectorSize, timeSeed);
        entitiesInBuckets = (ArrayList<Integer>[]) new ArrayList[this.buckets];
    }

    public SuperBit(int vectorSize, int r, int buckets) {
        this.vectorSize = vectorSize;
        this.bands = (int) Math.ceil((float) vectorSize /(float) r);
        this.buckets = buckets;
        this.lsh = new LSHSuperBit(this.bands, this.buckets, vectorSize);
        entitiesInBuckets = (ArrayList<Integer>[]) new ArrayList[this.buckets];
    }

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

    public void index(List<EntityProfile> entities){
        for (int i=0; i<entities.size(); i++){
            EntityProfile entity = entities.get(i);
            int[] hashes = hash(entity);
            for (int hash: hashes){
                if (entitiesInBuckets[hash] == null) {
                    ArrayList<Integer> bucketEntities = new ArrayList<>();
                    entitiesInBuckets[hash] = bucketEntities;
                }
                ArrayList<Integer> bucketEntities = entitiesInBuckets[hash];
                bucketEntities.add(i);
            }
        }
    }

    public Set<Integer> query(EntityProfile entity){
        Set<Integer> candidates = new HashSet<>();
        int[] hashes = hash(entity);
        for (int hash: hashes){
            if(entitiesInBuckets[hash] != null)
                candidates.addAll(entitiesInBuckets[hash]);
        }
        return candidates;
    }


    public boolean[] byteToBoolArr(byte b) {
        boolean[] boolArr = new boolean[8];
        for (int i = 0; i < 8; i++) {
            boolArr[i] = (b & (byte) (128 / Math.pow(2, i))) != 0;
        }
        return boolArr;
    }
    //todo Change
    public int[] fromString(String str) {
        int[] encoding = new int[str.length()];
        Arrays.setAll(encoding, i -> Character.getNumericValue(str.charAt(i)));

        int[] result = new int[vectorSize];
        Arrays.fill(result, 0);

        //todo enforce size
        int length = Math.min(encoding.length, vectorSize);
        System.arraycopy(encoding, 0, result, 0, length);
        return result;
    }
}
