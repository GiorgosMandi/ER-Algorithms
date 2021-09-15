package ai.uoa.gr.experiments;

import ai.uoa.gr.model.MinHashLSH;
import ai.uoa.gr.model.Performance;
import ai.uoa.gr.utils.Reader;
import org.apache.commons.cli.*;
import org.scify.jedai.datamodel.EntityProfile;
import org.scify.jedai.datamodel.IdDuplicates;

import java.util.List;
import java.util.Set;


public class MinHashExperiment {

    static int MIN_R = 1;
    static int MAX_R = 5;
    static int STEP_R = 5;

    static int MIN_BUCKETS = 10;
    static int MAX_BUCKETS = 200;
    static int STEP_BUCKETS = 100;


    public static void main(String[] args) {
        try {
            Options options = new Options();
            options.addRequiredOption("s", "source", true, "path to the source dataset");
            options.addRequiredOption("t", "target",true, "path to the target dataset");
            options.addRequiredOption("gt", "groundTruth", true, "path to the Ground Truth dataset");

            options.addOption("minR", true, "minimum value of band size");
            options.addOption("maxR", true, "maximum value of band size");
            options.addOption("stepR", true, "step value of band size");

            options.addOption("minBuckets", true, "minimum value of Buckets");
            options.addOption("maxBuckets", true, "maximum value of Buckets");
            options.addOption("stepBuckets", true, "step value of Buckets");

            CommandLineParser parser = new DefaultParser();

            CommandLine cmd = parser.parse(options, args);
            String sourcePath = cmd.getOptionValue("s");
            System.out.println("Source Path: " + sourcePath);
            EntitySerializationReader sourceReader = new EntitySerializationReader(sourcePath);
            List<EntityProfile> sourceEntities = sourceReader.getEntityProfiles();
            System.out.println("Source Entities: " + sourceEntities.size());
            System.out.println();


            String targetPath = cmd.getOptionValue("t");
            System.out.println("Target Path: " + targetPath);
            EntitySerializationReader targetReader = new EntitySerializationReader(targetPath);
            List<EntityProfile> targetEntities = targetReader.getEntityProfiles();
            System.out.println("Target Entities: " + targetEntities.size());
            System.out.println();


            String groundTruthPath = cmd.getOptionValue("gt");
            System.out.println("Ground Truth Path: " + groundTruthPath);
            GtSerializationReader gtReader = new GtSerializationReader(groundTruthPath);
            Set<IdDuplicates> gtDuplicates = gtReader.getDuplicatePairs(sourceEntities, targetEntities);
            System.out.println("GT Duplicates Entities: " + gtDuplicates.size());


            int iteration = 1;
            Performance perf = new Performance();
            for (int r=MIN_R; r<=MAX_R; r+=STEP_R){
                for (int buckets=MIN_BUCKETS; buckets<=MAX_BUCKETS; buckets+=STEP_BUCKETS){
                    System.out.format("Iteration: %d r:%d buckets:%d\n", iteration, r, buckets);
                    MinHashLSH lsh = new MinHashLSH(2048, r, buckets);
                    lsh.index(sourceEntities);

                    long tp = 0;
                    long total = 0;
                    for (int j=0; j<targetEntities.size(); j++) {
                        EntityProfile entity = targetEntities.get(j);
                        Set<Integer> candidates = lsh.query(entity);
                        for (Integer c : candidates) {
                            IdDuplicates pair = new IdDuplicates(c, j);
                            if (gtDuplicates.contains(pair)) tp += 1;
                            total += 1;
                        }
                    }
                    float recall = (float) tp / (float) gtDuplicates.size();
                    float precision =  (float) tp / (float) total;
                    float f1 = 2*((precision*recall)/(precision+recall));
                    perf.conditionalUpdate(recall, precision, f1, r, buckets);
                }
            }
            perf.print();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}