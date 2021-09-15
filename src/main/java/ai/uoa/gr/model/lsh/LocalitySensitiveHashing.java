package ai.uoa.gr.model.lsh;

import org.scify.jedai.datamodel.EntityProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class LocalitySensitiveHashing {
    long timeSeed = System.currentTimeMillis();

    // rows per band
    int r = 5;

    // size of vector
    int vectorSize = 2048;

    // number of bands
    int bands = (int) Math.ceil((float) vectorSize /(float) r);

    int buckets = 50;


    ArrayList<Integer>[] entitiesInBuckets;

    public abstract void index(List<EntityProfile> entities);

    public abstract Set<Integer> query(EntityProfile entity);
}
