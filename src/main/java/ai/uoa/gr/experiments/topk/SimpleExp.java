package ai.uoa.gr.experiments.topk;

import ai.uoa.gr.algorithms.similarityjoin.tokenbased.SimilarityJoinA;
import ai.uoa.gr.algorithms.similarityjoin.tokenbased.TopKSimilarityJoin;
import ai.uoa.gr.performance.SimJoinPerformance;
import ai.uoa.gr.performance.TopKSimJoinPerformance;
import ai.uoa.gr.utils.Reader;
import ai.uoa.gr.utils.Utilities;
import org.apache.commons.cli.*;
import org.scify.jedai.datamodel.EntityProfile;
import org.scify.jedai.datamodel.IdDuplicates;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

public class SimpleExp {

    public static void main(String[] args) {
        try {
            // define CLI arguments
            Options options = new Options();
            options.addRequiredOption("s", "source", true, "path to the source dataset");
            options.addRequiredOption("t", "target", true, "path to the target dataset");
            options.addRequiredOption("gt", "groundTruth", true, "path to the Ground Truth dataset");
            options.addOption("k", "top-K", true, "Top-K K");

            // parse CLI arguments
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            int K = cmd.hasOption("k") ? Integer.parseInt(cmd.getOptionValue("k")) : 339;

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
            System.out.println("Brute Force Verifications: "+ sourceEntities.size()*targetEntities.size());
            System.out.println();


            TopKSimJoinPerformance performance = new TopKSimJoinPerformance();
            long tp = 0;
            long verifications = 0;
            long time = Calendar.getInstance().getTimeInMillis();

            SimilarityJoinA similarityJoin = new TopKSimilarityJoin(sourceSTR, K);

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

            performance.conditionalUpdate(recall, precision, f1, K, verifications, tp, time);
            System.out.println("K: " + K);
            performance.print(recall, precision, f1, verifications, tp, time);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        catch (NumberFormatException e){
            System.out.println("ERROR: Not valid threshold");
            e.printStackTrace();
        }
    }

}
