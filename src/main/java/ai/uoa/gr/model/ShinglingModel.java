package ai.uoa.gr.model;

import com.beust.jcommander.internal.Lists;
import org.scify.jedai.datamodel.Attribute;
import org.scify.jedai.datamodel.EntityProfile;

import java.util.*;

public class ShinglingModel {

    List<String> ngrams;
    StringBuilder sb = new StringBuilder();
    int N;

    public ShinglingModel(List<EntityProfile> entities, int n){
        this.N = n;
        HashSet<String> uniqueNGrams = new HashSet<>();
        for (EntityProfile entity: entities){
            String entityStr = entity2string(entity);
            Set<String> entityNGrams = getNGrams(entityStr, this.N);
            uniqueNGrams.addAll(entityNGrams);
        }
        this.ngrams = Lists.newArrayList(uniqueNGrams);
    }

    public String entity2string(EntityProfile entity){
        sb.setLength(0);
        for (Attribute attribute : entity.getAttributes()) {
            sb.append(attribute.getValue().trim());
            sb.append("-");
        }
        return sb.toString();
    }

    public Set<String> getNGrams(String str, int n) {
        Set<String> ngrams = new HashSet<>();
        for (int i = 0; i < str.length() - n + 1; i++) {
            ngrams.add(str.substring(i, i + n));
        }
        return ngrams;
    }

    public Map<String, Integer> getCountedNGrams(String str, int n) {
        Map<String, Integer> ngrams = new HashMap<>();
        for (int i = 0; i < str.length() - n + 1; i++) {
            String ngram = str.substring(i, i + n);
            if (ngrams.containsKey(ngram))
                ngrams.put(ngram, ngrams.get(ngram) + 1);
            else
                ngrams.put(ngram, 1);
        }
        return ngrams;
    }

    public boolean[] getBooleanVector(EntityProfile entity){
        String entityStr = entity2string(entity);
        Set<String> entityNGrams = getNGrams(entityStr, this.N);
        boolean[] vector = new boolean[this.ngrams.size()];
        for (int i=0; i<this.ngrams.size(); i++){
            String ngram = ngrams.get(i);
            vector[i] = entityNGrams.contains(ngram);
        }
        return vector;
    }

    public int[] getIntegerVector(EntityProfile entity){
        String entityStr = entity2string(entity);
        Map<String, Integer> entityNGrams = getCountedNGrams(entityStr, this.N);
        int[] vector = new int[this.ngrams.size()];
        for (int i=0; i<this.ngrams.size(); i++){
            String ngram = ngrams.get(i);
            vector[i] = entityNGrams.getOrDefault(ngram, 0);
        }
        return vector;
    }

    public boolean[][] booleanVectorization(List<EntityProfile> entities){
        boolean[][] vectors = new boolean[entities.size()][this.ngrams.size()];
        for(int i=0; i<entities.size(); i++){
            vectors[i] = getBooleanVector(entities.get(i));
        }
        return vectors;
    }

    public int[][] vectorization(List<EntityProfile> entities){
        int[][] vectors = new int[entities.size()][this.ngrams.size()];
        for(int i=0; i<entities.size(); i++){
            vectors[i] = getIntegerVector(entities.get(i));
        }
        return vectors;
    }

    public List<String> getNgrams() {
        return ngrams;
    }

    public int getVectorSize(){
        return ngrams.size();
    }
}
