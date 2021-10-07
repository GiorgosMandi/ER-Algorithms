package ai.uoa.gr.experiments.lsh;

import ai.uoa.gr.algorithms.lsh.LocalitySensitiveHashing;
import ai.uoa.gr.algorithms.lsh.MinHash;
import ai.uoa.gr.algorithms.lsh.SuperBit;
import ai.uoa.gr.model.ShinglingModel;
import ai.uoa.gr.performance.LshPerformance;
import ai.uoa.gr.utils.Reader;
import ai.uoa.gr.utils.Utilities;
import org.apache.commons.cli.*;
import org.scify.jedai.datamodel.EntityProfile;
import org.scify.jedai.datamodel.IdDuplicates;

import java.util.*;

/**
 * @author George Mandilaras (NKUA)
 */
public class SimpleExp {

    public static void main(String[] args) {
        try {

            // define CLI arguments
            Options options = new Options();
            options.addRequiredOption("s", "source", true, "path to the source dataset");
            options.addRequiredOption("t", "target",true, "path to the target dataset");
            options.addRequiredOption("gt", "groundTruth", true, "path to the Ground Truth dataset");

            options.addOption("bands", true, "number of band");
            options.addOption("buckets", true, "minimum value of Buckets");
            options.addOption("ngrams", true, "size of ngrams");

            options.addOption("sourceField", true, "Use specific field of source");
            options.addOption("targetField", true, "Use specific field of target");

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
            int bands = Integer.parseInt(cmd.getOptionValue("bands", "9"));
            int buckets = Integer.parseInt(cmd.getOptionValue("buckets", "25"));
            System.out.println("N: "+n);
            System.out.println("Bands: "+bands);
            System.out.println("Buckets: "+buckets);

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

            double cartesian = sourceEntities.size()*targetEntities.size();
            System.out.println("Cartesian: " + cartesian);

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

                    // TODO check if candidate has common n-grams
                    String sourceStr = sourceSTR.get(c);
                    String targetStr = targetSTR.get(j);

                    Set<String> sourceNGrams = model.getNGrams(sourceStr, n);
                    Set<String> targetNGrams = model.getNGrams(targetStr, n);
                    Set<String> intersection = new HashSet<>(sourceNGrams);
                    intersection.removeAll(targetNGrams);
                    if (intersection.isEmpty()){
                        System.out.println("No intersection between: " + c + " and " + j);
                    }

                    IdDuplicates pair = new IdDuplicates(c, j);
                    if (gtDuplicates.contains(pair)) tp += 1;
                    verifications += 1;
                }
            }

            // evaluate performance
            LshPerformance perf = new LshPerformance();
            float recall = (float) tp / (float) gtDuplicates.size();
            float precision =  (float) tp / (float) verifications;
            float f1 = 2*((precision*recall)/(precision+recall));
            time = Calendar.getInstance().getTimeInMillis() - time;

            perf.conditionalUpdate(recall, precision, f1, bands, buckets, verifications, tp, time);
            perf.print();
            System.out.println("Reduction: " + (1- ((float) verifications/cartesian)));

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
