package ai.uoa.gr.model;

import org.scify.jedai.datamodel.Attribute;
import org.scify.jedai.datamodel.EntityProfile;
import org.scify.jedai.textmodels.SuperBitUnigrams;

import java.util.List;

public class TextModel {

    SuperBitUnigrams[] models;
    int vectorSize;
    public TextModel(List<EntityProfile> entities){
        int counter = 0;
        models = new SuperBitUnigrams[entities.size()];
        for (EntityProfile profile : entities) {
            models[counter] = new SuperBitUnigrams(profile.getEntityUrl());
            for (Attribute attribute : profile.getAttributes()) {
                models[counter].updateModel(attribute.getValue());
            }
            models[counter].finalizeModel();
            counter++;
        }
        vectorSize = models[0].getVector().length;
    }


    public SuperBitUnigrams[] computeModels(List<EntityProfile> entities){
        int counter = 0;
        final SuperBitUnigrams[] models = new SuperBitUnigrams[entities.size()];
        for (EntityProfile profile : entities) {
            SuperBitUnigrams model = new SuperBitUnigrams(profile.getEntityUrl());
            for (Attribute attribute : profile.getAttributes()) {
                model.updateModel(attribute.getValue());
            }
            models[counter++] = model;
        }
        return models;
    }

    public double[][] getVectors() {
        return getVectors(this.models);
    }

    public double[][] getVectors(List<EntityProfile> entities) {
       return getVectors(this.computeModels(entities));
    }

    public double[][] getVectors(SuperBitUnigrams[] models) {
        double[][] vectors = new double[models.length][vectorSize];
        for (int i=0; i<models.length; i++)
            vectors[i] = models[i].getVector();
        return vectors;
    }

    public int getVectorSize() {
        return vectorSize;
    }
}
