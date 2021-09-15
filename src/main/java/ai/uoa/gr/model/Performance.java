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
        System.out.println("Recall:\t"+bestRecall);
        System.out.println("Precision:\t"+bestPrecision);
        System.out.println("F1-score:\t"+bestF1);

        System.out.println("Best Band Size (r):\t"+bestR);
        System.out.println("Best #Buckets:\t"+ bestBuckets);

        System.out.println();
    }
}
