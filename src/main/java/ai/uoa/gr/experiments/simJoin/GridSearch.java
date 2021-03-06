package ai.uoa.gr.experiments.simJoin;

import ai.uoa.gr.algorithms.similarityjoin.tokenbased.AllPairs;
import ai.uoa.gr.algorithms.similarityjoin.tokenbased.PPJoin;
import ai.uoa.gr.performance.SimJoinPerformance;
import ai.uoa.gr.utils.Reader;
import ai.uoa.gr.utils.Utilities;
import org.apache.commons.cli.*;
import org.scify.jedai.datamodel.EntityProfile;
import org.scify.jedai.datamodel.IdDuplicates;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * @author George Mandilaras (NKUA)
 */
public class GridSearch {

    static final float MIN_THRESHOLD = 0.7f;
    static final float MAX_THRESHOLD = 0.9f;
    static final float STEP_THRESHOLD = 0.03f;

    public static void main(String[] args) {
        try {
            // define CLI arguments
            Options options = new Options();
            options.addRequiredOption("s", "source", true, "path to the source dataset");
            options.addRequiredOption("t", "target", true, "path to the target dataset");
            options.addRequiredOption("gt", "groundTruth", true, "path to the Ground Truth dataset");
            options.addOption("sj", "sjAlgorithm", true, "algorithm to run");

            // parse CLI arguments
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String joinAlgorithm = cmd.hasOption("sj") ? cmd.getOptionValue("sj") : "ppjoin";

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

            SimJoinPerformance performance = new SimJoinPerformance();
            for (float t=MIN_THRESHOLD; t<=MAX_THRESHOLD; t+=STEP_THRESHOLD){
                long tp = 0;
                long verifications = 0;
                long time = Calendar.getInstance().getTimeInMillis();

                AllPairs similarityJoin;
                if (joinAlgorithm.toLowerCase(Locale.ROOT).equals("ppjoin"))
                    similarityJoin = new PPJoin(sourceSTR, t);
                else
                    similarityJoin = new AllPairs(sourceSTR, t);

                for (int j=0; j<targetSTR.size(); j++) {
                    String target = targetSTR.get(j);
                    Set<Integer> candidates = similarityJoin.query(target);
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
                time = Calendar.getInstance().getTimeInMillis() - time;

                performance.conditionalUpdate(recall, precision, f1, t, verifications, tp, time);
                System.out.println("Threshold: " + t);
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
