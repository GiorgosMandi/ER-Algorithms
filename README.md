# ER-Experiments

A Java application for experimenting with the Entity Resolution algorithms. The implemented algorithms are the following:

- LSH
  - MinHash
  - SuperBit
- Token-based Similarity Joins
  - AllPairs
  - PPJoin
  - PartEnum
- Edit-distance-based Similarity Joins

## Build 

    mvn package


## LSH

Implementations of MinHash and SuperBit, with Singling as the main vectorization technique. 
Enables the detection of the best configurations using Grid Search. The LSH implementation is based on java-LSH

### Run

    java -cp target/LSH-Experiments-1.0-SNAPSHOT.jar ai.uoa.gr.lsh.experiments.UnigramsExperiment -s /path/to/source -t /path/to/target -gt /path/to/groundTruth

By default, it will use the MinHash algorithm and [SuperBitUnigram](https://github.com/scify/JedAIToolkit/blob/9f14506d68bc3a2a81b4a83340fea48b91fa9103/src/main/java/org/scify/jedai/textmodels/SuperBitUnigrams.java#L25)
vectorization. To execute SuperBit use `-superBit` argument.

Additional arguments:

- `-minBand m -maxBand M -stepBand sb` define the minimum and maximum number of bands and the step. This is used in the Grid Search
- `-minBuckets m -maxBuckets M -stepBuckets sb` define the minimum and maximum number of buckets and the step. This is used in the Grid Search

To use Shingling run:

    java -cp target/LSH-Experiments-1.0-SNAPSHOT.jar ai.uoa.gr.lsh.experiments.ShinglingExperiment -n NGRAM_SIZE -s /path/to/source -t /path/to/target -gt /path/to/groundTruth
