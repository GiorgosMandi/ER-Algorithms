package ai.uoa.gr.experiments.topk;

import ai.uoa.gr.algorithms.similarityjoin.tokenbased.TopKSimilarityJoin;
import ai.uoa.gr.performance.TopKSimJoinPerformance;
import ai.uoa.gr.utils.Reader;
import ai.uoa.gr.utils.Utilities;
import org.apache.commons.cli.*;
import org.scify.jedai.datamodel.EntityProfile;
import org.scify.jedai.datamodel.IdDuplicates;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

/**
 * @author George Mandilaras (NKUA)
 */
public class GridSearch {

    static final int MIN_K = 1;
    static final int MAX_K = 10;
    static final int STEP_K = 1;

    public static void main(String[] args) {
        try {
            // define CLI arguments
            Options options = new Options();
            options.addRequiredOption("s", "source", true, "path to the source dataset");
            options.addRequiredOption("t", "target", true, "path to the target dataset");
            options.addRequiredOption("gt", "groundTruth", true, "path to the Ground Truth dataset");

            // parse CLI arguments
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            // read source entities
            String sourcePath = cmd.getOptionValue("s");
            List<EntityProfile> sourceEntities = Reader.readSerialized(sourcePath);
            System.out.println("Source Entities: " + sourceEntities.size());
            List<String> sourceSTR = Utilities.entities2String(sourceEntities);

            // read target entities
            String targetPath = cmd.getOptionValue("t");
            List<EntityProfile> targetEntities = Reader.readSerialized(targetPath);
            System.out.println("Target Entities: " + targetEntities.size());
            List<String> targetSTR = Utilities.entities2String(targetEntities);

            // read ground-truth file
            String groundTruthPath = cmd.getOptionValue("gt");
            Set<IdDuplicates> gtDuplicates = Reader.readSerializedGT(groundTruthPath, sourceEntities, targetEntities);
            System.out.println("GT Duplicates Entities: " + gtDuplicates.size());
            System.out.println("Exhausted Search: "+ sourceEntities.size()*targetEntities.size());
            System.out.println();

            TopKSimJoinPerformance performance = new TopKSimJoinPerformance();
            for (int k=MIN_K; k<=MAX_K; k+=STEP_K){
                long tp = 0;
                long verifications = 0;
                long time = Calendar.getInstance().getTimeInMillis();

                TopKSimilarityJoin similarityJoin = new TopKSimilarityJoin(sourceSTR, k);

                for (int j=0; j<targetSTR.size(); j++) {
                    String target = targetSTR.get(j);
                    Set<Integer> candidates = similarityJoin.query(target);
                    for (Integer c : candidates) {
                        IdDuplicates pair = new IdDuplicates(c, j);
                        if (gtDuplicates.contains(pair))
                            tp += 1;
                        verifications += 1;
                    }
                }
                // evaluate performance
                float recall = (float) tp / (float) gtDuplicates.size();
                float precision =  (float) tp / (float) verifications;
                float f1 = 2*((precision*recall)/(precision+recall));
                time = Calendar.getInstance().getTimeInMillis() - time;

                performance.conditionalUpdate(recall, precision, f1, k, verifications, tp, time);
                System.out.println("K: " + k);
                System.out.println("TP: " + tp);
                System.out.println("#Verifications: " + verifications);

                performance.print(recall, precision, f1, verifications, tp, time);
            }
            performance.print();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        catch (NumberFormatException e){
            System.out.println("ERROR: Not valid threshold");
            e.printStackTrace();
        }
    }
}
