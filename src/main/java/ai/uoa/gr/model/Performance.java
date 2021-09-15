package ai.uoa.gr.model;

public class Performance {
    float bestRecall = 0;
    float bestPrecision = 0;
    float bestF1 = 0;

    int bestR = 0;
    int bestBuckets = 0;

    public Performance(){}

    private void update(float recall, float precision, float f1, int r, int buckets){
        bestPrecision = precision;
        bestRecall = recall;
        bestF1 = f1;
        bestR = r;
        bestBuckets = buckets;
    }

    public void conditionalUpdate(float recall, float precision, float f1, int r, int buckets){
        if (recall > 0.9 && precision > bestPrecision)
            update(recall, precision, f1, r, buckets);
        else if (bestRecall < 0.9 && bestRecall < recall )
            update(recall, precision, f1, r, buckets);
    }

    public void print(){
        System.out.println("\n======= BEST PERFORMANCE =======");
        print(bestRecall, bestPrecision, bestF1, bestR, bestBuckets);
    }

    public void print(float recall, float precision, float f1, int r, int buckets){
        System.out.println("Recall:\t"+recall);
        System.out.println("Precision:\t"+precision);
        System.out.println("F1-score:\t"+f1);

        System.out.println("Best Band Size (r):\t"+r);
        System.out.println("Best #Buckets:\t"+ buckets);
        System.out.println();
    }
}
