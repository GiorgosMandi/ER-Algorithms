package ai.uoa.gr.experiments;

import ai.uoa.gr.model.Performance;
import ai.uoa.gr.model.ShinglingModel;
import ai.uoa.gr.model.lsh.LocalitySensitiveHashing;
import ai.uoa.gr.model.lsh.MinHash;
import ai.uoa.gr.model.lsh.SuperBit;
import ai.uoa.gr.utils.Reader;
import org.apache.commons.cli.*;
import org.scify.jedai.datamodel.EntityProfile;
import org.scify.jedai.datamodel.IdDuplicates;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

public class ShinglingExperiment {
    static int MIN_BANDS = 10;
    static int MAX_BANDS = 200;
    static int STEP_BANDS = 50;

    static int MIN_BUCKETS = 20;
    static int MAX_BUCKETS = 400;
    static int STEP_BUCKETS = 50;


    public static void main(String[] args) {
        try {

            // define CLI arguments
            Options options = new Options();
            options.addRequiredOption("s", "source", true, "path to the source dataset");
            options.addRequiredOption("t", "target",true, "path to the target dataset");
            options.addRequiredOption("gt", "groundTruth", true, "path to the Ground Truth dataset");

            // band-related arguments
            // band defines the number of rows per band
            options.addOption("minBand", true, "minimum number of band");
            options.addOption("maxBand", true, "maximum number of band");
            options.addOption("stepBand", true, "step value of number of bands");

            // buckets-related arguments
            options.addOption("minBuckets", true, "minimum value of Buckets");
            options.addOption("maxBuckets", true, "maximum value of Buckets");
            options.addOption("stepBuckets", true, "step value of Buckets");

            options.addOption("ngrams", true, "size of ngrams");

            // SuperBit argument
            options.addOption("superBit", false, "if specified use SuperBit, otherwise use MinHash");

            // parse CLI arguments
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            // use SuperBit or MinHash
            boolean useSuperBit = cmd.hasOption("superBit");
            if (useSuperBit) System.out.println("Using LSH SuperBit");
            else System.out.println("Using LSH MinHash");

            int n = Integer.parseInt(cmd.getOptionValue("ngrams", "2"));
            System.out.println("Using Shingling with N: "+n);

            // read source entities
            String sourcePath = cmd.getOptionValue("s");
            List<EntityProfile> sourceEntities = Reader.readSerialized(sourcePath);
            System.out.println("Source Entities: " + sourceEntities.size());

            // read target entities
            String targetPath = cmd.getOptionValue("t");
            List<EntityProfile> targetEntities = Reader.readSerialized(targetPath);
            System.out.println("Target Entities: " + targetEntities.size());

            // read ground-truth file
            String groundTruthPath = cmd.getOptionValue("gt");
            Set<IdDuplicates> gtDuplicates = Reader.readSerializedGT(groundTruthPath, sourceEntities, targetEntities);
            System.out.println("GT Duplicates Entities: " + gtDuplicates.size());
            System.out.println();

            // create models
            System.out.println("Starts Vectorization");
            ShinglingModel model = new ShinglingModel(sourceEntities, n);
            int[][] sourceVectorsInt = model.vectorization(sourceEntities);
            double[][] sVectors = new double[sourceVectorsInt.length][];
            for (int row = 0; row < sourceVectorsInt.length; row++) {
                sVectors[row] = Arrays.stream(sourceVectorsInt[row]).asDoubleStream().toArray();
            }


            int[][] targetVectorsInt = model.vectorization(targetEntities);
            double[][] tVectors = new double[targetVectorsInt.length][];
            for (int row = 0; row < targetVectorsInt.length; row++) {
                tVectors[row] = Arrays.stream(targetVectorsInt[row]).asDoubleStream().toArray();
            }
            System.out.println("Vectorization Completed\nVector Size:\t"+ model.getVectorSize());



            // start Grid Search
            System.out.println("== Grid Search ==");
            System.out.format("Bands: [%d, %d] with step %d\n", MIN_BANDS, MAX_BANDS, STEP_BANDS);
            System.out.format("Buckets: [%d, %d] with step %d\n", MIN_BUCKETS, MAX_BUCKETS, STEP_BUCKETS);
            System.out.println("Grid Search Starts\n");

            Performance perf = new Performance();
            for (int buckets=MIN_BUCKETS; buckets<=MAX_BUCKETS; buckets+=STEP_BUCKETS){
                for (int bands=MIN_BANDS; bands<= MAX_BANDS; bands+= STEP_BANDS){

                    long time = Calendar.getInstance().getTimeInMillis();
                    // initialize LSH
                    LocalitySensitiveHashing lsh;
                    if (useSuperBit)
                        lsh = new SuperBit(sVectors, bands, buckets, model.getVectorSize());
                    else
                        lsh = new MinHash(sVectors, bands, buckets, model.getVectorSize());

                    // true positive
                    long tp = 0;

                    // total verifications
                    long verifications = 0;

                    // for each target entity, find its candidates (query)
                    // find TP by searching the pairs in GT
                    for (int j=0; j<targetEntities.size(); j++) {
                        double[] vector = tVectors[j];
                        Set<Integer> candidates = lsh.query(vector);
                        for (Integer c : candidates) {
                            IdDuplicates pair = new IdDuplicates(c, j);
                            if (gtDuplicates.contains(pair)) tp += 1;
                            verifications += 1;
                        }
                    }

                    // evaluate performance
                    float recall = (float) tp / (float) gtDuplicates.size();
                    float precision =  (float) tp / (float) verifications;
                    float f1 = 2*((precision*recall)/(precision+recall));

                    // store best performance
                    time = Calendar.getInstance().getTimeInMillis() - time;
                    perf.conditionalUpdate(recall, precision, f1, bands, buckets, verifications, tp, time);
                    perf.print(recall, precision, f1, verifications, tp, time);
                }
            }
            perf.print();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
