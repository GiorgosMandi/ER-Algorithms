package ai.uoa.gr.algorithms.similarityjoin.tokenbased.partenum;

import ai.uoa.gr.algorithms.similarityjoin.tokenbased.SimilarityJoinA;
import ai.uoa.gr.model.ShinglingModel;
import ai.uoa.gr.utils.Reader;
import ai.uoa.gr.utils.Utilities;
import com.sun.javaws.exceptions.InvalidArgumentException;
import org.scify.jedai.datamodel.EntityProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.lang.Math.floor;

/**
 * @author George Mandilaras (NKUA)
 */
public class PartEnum extends SimilarityJoinA   {

    ShinglingModel model;
    List<Category> vectorI;
    int vectorSize;

    public PartEnum(List<String> source, float tj, int n) throws InvalidArgumentException {
        super(source, tj);
        model = new ShinglingModel(source, n);
        model.shuffle();
        vectorSize = model.getVectorSize();

        vectorI = new ArrayList<>();
        int l=1, r=1;
        vectorI.add(new Category(l, r, tj, vectorSize));
        while (l < model.getVectorSize()){
            l = r+1;
            r = (int) floor(l/tj);
            vectorI.add(new Category(l, r, tj, vectorSize));
        }
    }

    @Override
    public Set<Integer> query(String t) {
        return null;
    }

    @Override
    public void index(List<String> source) {

    }

    public static void main(String[] args) throws InvalidArgumentException {
        String sourcePath = "/home/gmandi/Documents/Extreme_Earth/Entity_Resolution/Data/jedai-Serialized/cleanCleanErDatasets/restaurant1Profiles";
        List<EntityProfile> sourceEntities = Reader.readSerialized(sourcePath);
        System.out.println("Source Entities: " + sourceEntities.size());
        List<String> sourceSTR = Utilities.entities2String(sourceEntities);

        PartEnum simJoin = new PartEnum(sourceSTR, 0.75f, 1);
        for (String str: sourceSTR) {
            for (Category c: simJoin.vectorI){
                if (str.length() > c.l && str.length() < c.r ) {
                    List<Boolean> hash = c.sign(simJoin.model.getBooleanVector(str));
                    System.out.println();
                }
            }
        }
        System.out.println();
    }
}