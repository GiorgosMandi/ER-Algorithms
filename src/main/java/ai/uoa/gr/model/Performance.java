package ai.uoa.gr.model;

/**
 * Store the configuration of the best performance
 */
public class Performance {

    final float RECALL_LIMIT = 0.9f;

    float bestRecall = 0;
    float bestPrecision = 0;
    float bestF1 = 0;
    long tp = 0;
    long time = 0;
    long verifications = 0;

    int bestBands = 0;
    int bestBuckets = 0;

    public Performance(){}

    private void update(float recall, float precision, float f1, int bands, int buckets, long verifications, long tp, long time){
        this.bestPrecision = precision;
        this.bestRecall = recall;
        this.bestF1 = f1;
        this.bestBands = bands;
        this.bestBuckets = buckets;
        this.verifications = verifications;
        this.tp = tp;
        this.time = time;
    }

    public void conditionalUpdate(float recall, float precision, float f1, int bands, int buckets, long verifications, long tp, long time){
        if (recall >= RECALL_LIMIT){
            if (bestRecall >= RECALL_LIMIT) {
                if (bestPrecision <= precision) {
                    update(recall, precision, f1, bands, buckets, verifications, tp, time);
                }
            }
            else{
                // best recall is less than RECALL_LIMIT
                update(recall, precision, f1, bands, buckets, verifications, tp, time);
            }
        }
        else if(recall >= bestRecall){
            update(recall, precision, f1, bands, buckets, verifications, tp, time);
        }
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
        System.out.println("Verifications:\t"+verifications);
        System.out.println("True Positives:\t"+tp);
        System.out.println("Time:\t"+time);

        System.out.println();
    }
}