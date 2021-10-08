package ai.uoa.gr.experiments.lsh;

import ai.uoa.gr.algorithms.lsh.LocalitySensitiveHashing;
import ai.uoa.gr.algorithms.lsh.MinHash;
import ai.uoa.gr.algorithms.lsh.SuperBit;
import ai.uoa.gr.model.ShinglingModel;
import ai.uoa.gr.performance.LshPerformance;
import ai.uoa.gr.utils.Reader;
import ai.uoa.gr.utils.Utilities;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.ArrayUtils;
import org.scify.jedai.datamodel.EntityProfile;
import org.scify.jedai.datamodel.IdDuplicates;

import java.util.*;

/**
 * @author George Mandilaras (NKUA)
 */
public class SimpleExp {


    /*

No intersection between: 972 and 8
No intersection between: 1715 and 13
No intersection between: 1687 and 23
No intersection between: 839 and 26
No intersection between: 660 and 35
No intersection between: 735 and 35
No intersection between: 1237 and 35
No intersection between: 1722 and 35
No intersection between: 2447 and 35
No intersection between: 2183 and 56
No intersection between: 839 and 57
No intersection between: 839 and 59
No intersection between: 2183 and 61
No intersection between: 0 and 62
No intersection between: 88 and 62
No intersection between: 131 and 62
No intersection between: 192 and 62
No intersection between: 203 and 62
No intersection between: 276 and 62
No intersection between: 288 and 62
No intersection between: 313 and 62
No intersection between: 331 and 62
No intersection between: 333 and 62
No intersection between: 389 and 62
No intersection between: 396 and 62
No intersection between: 403 and 62
No intersection between: 453 and 62
No intersection between: 489 and 62
No intersection between: 524 and 62
No intersection between: 546 and 62
No intersection between: 601 and 62
No intersection between: 605 and 62
No intersection between: 634 and 62
No intersection between: 656 and 62
No intersection between: 670 and 62
No intersection between: 714 and 62
No intersection between: 715 and 62
No intersection between: 767 and 62
No intersection between: 794 and 62
No intersection between: 810 and 62
No intersection between: 813 and 62
No intersection between: 861 and 62
No intersection between: 878 and 62
No intersection between: 928 and 62
No intersection between: 1006 and 62
No intersection between: 1009 and 62
No intersection between: 1016 and 62
No intersection between: 1074 and 62
No intersection between: 1079 and 62
No intersection between: 1084 and 62
No intersection between: 1090 and 62
No intersection between: 1128 and 62
No intersection between: 1143 and 62
No intersection between: 1179 and 62
No intersection between: 1207 and 62
No intersection between: 1208 and 62
No intersection between: 1225 and 62
No intersection between: 1283 and 62
No intersection between: 1319 and 62
No intersection between: 1332 and 62
No intersection between: 1345 and 62
No intersection between: 1419 and 62
No intersection between: 1491 and 62
No intersection between: 1497 and 62
No intersection between: 1637 and 62
No intersection between: 1718 and 62
No intersection between: 1740 and 62
No intersection between: 1870 and 62
No intersection between: 1871 and 62
No intersection between: 1889 and 62
No intersection between: 1908 and 62
No intersection between: 1917 and 62
No intersection between: 1928 and 62
No intersection between: 1948 and 62
No intersection between: 1983 and 62
No intersection between: 2032 and 62
No intersection between: 2042 and 62
No intersection between: 2184 and 62
No intersection between: 2262 and 62
No intersection between: 2263 and 62
No intersection between: 2411 and 62
No intersection between: 2465 and 62
No intersection between: 2528 and 62
No intersection between: 972 and 76
No intersection between: 839 and 78
No intersection between: 546 and 83
No intersection between: 839 and 86
No intersection between: 35 and 92
No intersection between: 839 and 92
No intersection between: 1109 and 92
No intersection between: 839 and 95
No intersection between: 543 and 100
No intersection between: 630 and 100
No intersection between: 927 and 100
No intersection between: 1171 and 100
No intersection between: 1417 and 100
No intersection between: 2499 and 100

     */
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
            int bands = Integer.parseInt(cmd.getOptionValue("bands", "8"));
            int buckets = Integer.parseInt(cmd.getOptionValue("buckets", "20"));
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

            // false positive pairs
            long fp = 0;
            // total verifications
            long verifications = 0;

            // for each target entity, find its candidates (query)
            // find TP by searching the pairs in GT
            for (int j=0; j<tVectors.length; j++) {
                double[] vector = tVectors[j];
                String targetStr = targetSTR.get(j);
                Set<String> targetNGrams = model.getNGrams(targetStr, n);

                Set<Integer> candidates = lsh.query(vector);
                for (Integer c : candidates) {

                    // check if candidate has common n-grams
                    String sourceStr = sourceSTR.get(c);
                    Set<String> sourceNGrams = model.getNGrams(sourceStr, n);
                    Set<String> intersection = new HashSet<>(sourceNGrams);
                    intersection.retainAll(targetNGrams);
                    if (intersection.isEmpty()){
                        fp += 1;
                        // continue
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
            System.out.println("fp: " + fp);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
