package ai.uoa.gr.experiments.lsh;

import ai.uoa.gr.algorithms.lsh.LocalitySensitiveHashing;
import ai.uoa.gr.algorithms.lsh.MinHash;
import ai.uoa.gr.algorithms.lsh.SuperBit;
import ai.uoa.gr.model.ShinglingModel;
import ai.uoa.gr.performance.LshPerformance;
import ai.uoa.gr.utils.Reader;
import ai.uoa.gr.utils.Utilities;
import org.apache.commons.cli.*;
import org.scify.jedai.datamodel.Attribute;
import org.scify.jedai.datamodel.EntityProfile;
import org.scify.jedai.datamodel.IdDuplicates;

import java.util.*;
import java.util.stream.Collectors;

public class ShinglingExperiment {
    static int MIN_BANDS = 64;
    static int MAX_BANDS = 100;
    static int STEP_BANDS = 5;

    static int MIN_BUCKETS = 2;
    static int MAX_BUCKETS = 50;
    static int STEP_BUCKETS = 5;
    static int MAX_ITER = 2; // TODO
    static int N = 1;


    private final static int[] BANDS = {64, 32, 16, 8, 4, 2, 128, 64, 32, 16, 8, 4, 2, 256, 128, 64, 32, 16, 8, 4, 2};
    private final static int[] BUCKETS = {8, 2, 4, 8, 16, 32, 64, 2, 4, 8, 16, 32, 64, 128, 2, 4, 8, 16, 32, 64, 128, 256};

    static int MIN_N_GRAM = 2;
    static int MAX_N_GRAM = 5;
    static int STEP_N_GRAM = 1;

    public static void main(String[] args) {
        try {

            // define CLI arguments
            Options options = new Options();
            options.addRequiredOption("s", "source", true, "path to the source dataset");
            options.addRequiredOption("t", "target",true, "path to the target dataset");
            options.addRequiredOption("gt", "groundTruth", true, "path to the Ground Truth dataset");

            options.addOption("sourceField", true, "Use specific field of source");
            options.addOption("targetField", true, "Use specific field of target");

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

            int n = Integer.parseInt(cmd.getOptionValue("ngrams", ""+N));
            System.out.println("Using Shingling with N: "+n);

            // read source entities
            String sourcePath = cmd.getOptionValue("s");
            List<EntityProfile> sourceEntities = Reader.readSerialized(sourcePath);
            System.out.println("Source Entities: " + sourceEntities.size());
            String sField = null;
            if (cmd.hasOption("sourceField")){
                sField = cmd.getOptionValue("sourceField");
            }
            List<String> sourceSTR = Utilities.entities2String(sourceEntities, sField);

            // read target entities
            String targetPath = cmd.getOptionValue("t");
            List<EntityProfile> targetEntities = Reader.readSerialized(targetPath);
            System.out.println("Target Entities: " + targetEntities.size());
            String tField = null;
            if (cmd.hasOption("targetField")){
                tField = cmd.getOptionValue("targetField");
            }
            List<String> targetSTR = Utilities.entities2String(targetEntities, tField);

            // read ground-truth file
            String groundTruthPath = cmd.getOptionValue("gt");
            Set<IdDuplicates> gtDuplicates = Reader.readSerializedGT(groundTruthPath, sourceEntities, targetEntities);
            System.out.println("GT Duplicates Entities: " + gtDuplicates.size());
            System.out.println();

            // create models
            System.out.println("Starts Vectorization");
            ShinglingModel model = new ShinglingModel(sourceSTR, n);
            int[][] sourceVectorsInt = model.vectorization(sourceSTR);
            double[][] sVectors = new double[sourceVectorsInt.length][];
            for (int row = 0; row < sourceVectorsInt.length; row++) {
                sVectors[row] = Arrays.stream(sourceVectorsInt[row]).asDoubleStream().toArray();
            }

            int[][] targetVectorsInt = model.vectorization(targetSTR);
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

            LshPerformance perf = new LshPerformance();
//            for (int buckets=MIN_BUCKETS; buckets<=MAX_BUCKETS; buckets+=STEP_BUCKETS){
//                for (int bands=MIN_BANDS; bands<= MAX_BANDS; bands+= STEP_BANDS){
            for (int configurationId = 0; configurationId < BANDS.length; configurationId++) {
                int bands = BANDS[configurationId];
                int buckets = BUCKETS[configurationId];

                float recall = 0f;
                float precision = 0f;
                float f1 = 0f;
                long verifications = 0;
                long tp = 0;
                long time = 0;
                System.out.println("#Bands: "+bands);
                System.out.println("#Buckets: "+buckets);
                for (int iter=0; iter<MAX_ITER; iter++) {
                    long time_ = Calendar.getInstance().getTimeInMillis();
                    // initialize LSH
                    LocalitySensitiveHashing lsh;
                    if (useSuperBit)
                        lsh = new SuperBit(sVectors, bands, buckets, model.getVectorSize());
                    else
                        lsh = new MinHash(sVectors, bands, buckets, model.getVectorSize());

                    // true positive
                    long tp_ = 0;

                    // total verifications
                    long verifications_ = 0;

                    // for each target entity, find its candidates (query)
                    // find TP by searching the pairs in GT
                    for (int j = 0; j < targetEntities.size(); j++) {
                        double[] vector = tVectors[j];
                        Set<Integer> candidates = lsh.query(vector);
                        for (Integer c : candidates) {
                            IdDuplicates pair = new IdDuplicates(c, j);
                            if (gtDuplicates.contains(pair))
                                tp_ += 1;
                            verifications_ += 1;
                        }
                    }
                    // evaluate performance
                    float recall_ = (float) tp_ / (float) gtDuplicates.size();
                    float precision_ =  (float) tp_ / (float) verifications_;
                    float f1_ = 2*((precision_*recall_)/(precision_+recall_));
                    time_ = Calendar.getInstance().getTimeInMillis() - time_;

                    recall += recall_;
                    precision += precision_;
                    f1 += f1_;
                    tp += tp_;
                    verifications += verifications_;
                    time += time_;
                }

                recall = recall/MAX_ITER;
                precision = precision/MAX_ITER;
                f1 = f1/MAX_ITER;
                tp = tp/MAX_ITER;
                verifications = verifications/MAX_ITER;
                time = time/MAX_ITER;

                // store best performance
                perf.conditionalUpdate(recall, precision, f1, bands, buckets, verifications, tp, time);
                perf.print(recall, precision, f1, verifications, tp, time);
            }
            perf.print();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
