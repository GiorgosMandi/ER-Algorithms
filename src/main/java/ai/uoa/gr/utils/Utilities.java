package ai.uoa.gr.utils;

import org.scify.jedai.datamodel.Attribute;
import org.scify.jedai.datamodel.EntityProfile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author George Mandilaras (NKUA)
 */
public class Utilities {

    public static String entity2String(EntityProfile e, StringBuilder sb){
        sb.setLength(0);
        for(Attribute attr: e.getAttributes()){
            sb.append(attr.getValue());
            sb.append("-");
        }
        return sb.toString();
    }


    public static List<String> entities2String(List<EntityProfile> entities){
        StringBuilder sb = new StringBuilder();
        return entities.stream().map(e -> entity2String(e, sb)).collect(Collectors.toList());
    }
}
