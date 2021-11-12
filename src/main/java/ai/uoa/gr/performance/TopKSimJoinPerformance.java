package ai.uoa.gr.performance;

/**
 * @author George Mandilaras (NKUA)
 */
public class TopKSimJoinPerformance extends Performance {
    public int k;

    private void update(float recall, float precision, float f1, int k, long verifications, long tp, long time){
        this.k = k;
        update(recall, precision, f1, verifications, tp, time);
    }

    public void conditionalUpdate(float recall, float precision, float f1, int k, long verifications, long tp, long time){
        if (satisfy(recall, precision))
            update(recall, precision, f1, k, verifications, tp, time);
    }

    public void print(){
        System.out.println("\n======= BEST PERFORMANCE =======");
        System.out.println("K:\t"+ k);
        print(bestRecall, bestPrecision, bestF1, verifications, tp, time);
    }
}
