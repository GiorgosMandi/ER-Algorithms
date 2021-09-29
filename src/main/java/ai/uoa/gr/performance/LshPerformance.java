package ai.uoa.gr.performance;

/**
 * Store the configuration of the best performance
 */
public class LshPerformance extends Performance {

    int bestBands = 0;
    int bestBuckets = 0;

    private void update(float recall, float precision, float f1, int bands, int buckets, long verifications, long tp, long time){
        this.bestBands = bands;
        this.bestBuckets = buckets;
        update(recall, precision, f1, verifications, tp, time);
    }

    public void conditionalUpdate(float recall, float precision, float f1, int bands, int buckets, long verifications, long tp, long time){
        if (satisfy(recall, precision))
            update(recall, precision, f1, bands, buckets, verifications, tp, time);
    }

    public void print(){
        System.out.println("\n======= BEST PERFORMANCE =======");
        System.out.println("#Bands:\t"+ bestBands);
        System.out.println("#Buckets:\t"+ bestBuckets);
        print(bestRecall, bestPrecision, bestF1, verifications, tp, time);
    }
}