package ai.uoa.gr.performance;

/**
 * @author George Mandilaras (NKUA)
 */
public abstract class Performance {

    final float RECALL_LIMIT = 0.9f;
    float bestRecall = 0;
    float bestPrecision = 0;
    float bestF1 = 0;
    long tp = 0;
    long time = 0;
    long verifications = 0;


    boolean satisfy(float recall, float precision){
        if (recall >= RECALL_LIMIT){
            if (bestRecall >= RECALL_LIMIT)
                return bestPrecision <= precision;
            else
                return true;
        }
        return  recall >= bestRecall;
    }

    public void print(float recall, float precision, float f1, long verifications, long tp, long time){
        System.out.println("Recall:\t"+recall);
        System.out.println("Precision:\t"+precision);
        System.out.println("F1-score:\t"+f1);
        System.out.println("Verifications:\t"+verifications);
        System.out.println("True Positives:\t"+tp);
        System.out.println("Time:\t"+time);

        System.out.println();
    }
}
