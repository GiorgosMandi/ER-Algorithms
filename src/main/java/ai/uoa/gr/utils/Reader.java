package ai.uoa.gr.utils;

import org.scify.jedai.datamodel.Attribute;
import org.scify.jedai.datamodel.EntityProfile;
import org.scify.jedai.datamodel.IdDuplicates;
import org.scify.jedai.datareader.entityreader.EntitySerializationReader;
import org.scify.jedai.datareader.groundtruthreader.GtSerializationReader;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Reader {

    public static List<EntityProfile> readSerialized(String path){
        EntitySerializationReader reader = new EntitySerializationReader(path);
        return reader.getEntityProfiles();
    }

    public static List<String> readSerializedAsStrings(String path){
        EntitySerializationReader reader = new EntitySerializationReader(path);
        List<EntityProfile> entities = reader.getEntityProfiles();
        List<String> entitiesSTR = new LinkedList<>();
        StringBuilder sb = new StringBuilder();
        for(EntityProfile e: entities){
            sb.setLength(0);
            for(Attribute attr: e.getAttributes()){
                sb.append(attr.getValue());
                sb.append("-");
            }
            entitiesSTR.add(sb.toString());
        }
        return entitiesSTR;
    }

    public static Set<IdDuplicates> readSerializedGT(String path, List<EntityProfile> sourceEntities, List<EntityProfile> targetEntities){
        GtSerializationReader gtReader = new GtSerializationReader(path);
        return gtReader.getDuplicatePairs(sourceEntities, targetEntities);
    }
}
