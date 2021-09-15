package ai.uoa.gr.model.lsh;

import info.debatty.java.lsh.LSHMinHash;
import info.debatty.java.lsh.LSHSuperBit;
import org.scify.jedai.datamodel.Attribute;
import org.scify.jedai.datamodel.EntityProfile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MinHash extends LocalitySensitiveHashing {

    LSHMinHash lsh;
    public MinHash(){
        this.lsh = new LSHMinHash(this.bands, this.buckets, vectorSize, timeSeed);
        entitiesInBuckets = (ArrayList<Integer>[]) new ArrayList[this.buckets];
    }

    public MinHash(int vectorSize, int r, int buckets) {
        this.vectorSize = vectorSize;
        this.bands = (int) Math.ceil((float) vectorSize /(float) r);
        this.buckets = buckets;
        this.lsh = new LSHMinHash(this.bands, this.buckets, vectorSize);
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
        boolean[] eSignature = this.fromString(eText);
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
