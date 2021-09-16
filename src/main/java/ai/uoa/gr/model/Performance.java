package ai.uoa.gr.model;

/**
 * Store the configuration of the best performance
 */
public class Performance {
    float bestRecall = 0;
    float bestPrecision = 0;
    float bestF1 = 0;

    int bestBands = 0;
    int bestBuckets = 0;

    public Performance(){}

    private void update(float recall, float precision, float f1, int bands, int buckets){
        bestPrecision = precision;
        bestRecall = recall;
        bestF1 = f1;
        bestBands = bands;
        bestBuckets = buckets;
    }

    public void conditionalUpdate(float recall, float precision, float f1, int bands, int buckets){
        if (recall > 0.9 && precision > bestPrecision)
            update(recall, precision, f1, bands, buckets);
        else if (bestRecall < 0.9 && bestRecall < recall )
            update(recall, precision, f1, bands, buckets);
    }

    public void print(){
        System.out.println("\n======= BEST PERFORMANCE =======");
        System.out.println("#Bands:\t"+ bestBands);
        System.out.println("#Buckets:\t"+ bestBuckets);
        print(bestRecall, bestPrecision, bestF1);

    }

    public void print(float recall, float precision, float f1){
        System.out.println("Recall:\t"+recall);
        System.out.println("Precision:\t"+precision);
        System.out.println("F1-score:\t"+f1);

        System.out.println();
    }
}
