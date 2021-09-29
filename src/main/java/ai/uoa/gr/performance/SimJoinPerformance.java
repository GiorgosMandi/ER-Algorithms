package ai.uoa.gr.performance;

/**
 * @author George Mandilaras (NKUA)
 */
public class SimJoinPerformance extends Performance {
    float threshold = 0;

    private void update(float recall, float precision, float f1, float threshold, long verifications, long tp, long time){
        this.threshold = threshold;
        update(recall, precision, f1, verifications, tp, time);
    }

    public void conditionalUpdate(float recall, float precision, float f1, float threshold, long verifications, long tp, long time){
        if (satisfy(recall, precision))
            update(recall, precision, f1, threshold, verifications, tp, time);
    }

    public void print(){
        System.out.println("\n======= BEST PERFORMANCE =======");
        System.out.println("Threshold:\t"+ threshold);
        print(bestRecall, bestPrecision, bestF1, verifications, tp, time);
    }
}
