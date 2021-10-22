package ai.uoa.gr.utils;

import org.scify.jedai.datamodel.Attribute;
import org.scify.jedai.datamodel.EntityProfile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author George Mandilaras (NKUA)
 */
public class Utilities {

    public static String entity2String(EntityProfile e, StringBuilder sb){
        sb.setLength(0);
        for(Attribute attr: e.getAttributes()){
            String value = attr.getValue().toLowerCase(Locale.ROOT).trim();
            if (!value.equals("")){
                sb.append(value);
                sb.append("-");
            }
        }
        return sb.toString();
    }


    public static List<String> entities2String(List<EntityProfile> entities){
        StringBuilder sb = new StringBuilder();
        return entities.stream().map(e -> entity2String(e, sb)).collect(Collectors.toList());
    }

    public static List<String> entities2String(List<EntityProfile> entities, String field){
        boolean hasField = entities.get(0).getAttributes().stream().anyMatch(a -> a.getValue().equals(field));
        if (field == null || hasField)
            return entities2String(entities);
        else{
            List<String> strEntities = new ArrayList<>();
            for(EntityProfile e: entities){
                for (Attribute attr: e.getAttributes())
                    if(Objects.equals(attr.getName(), field))
                        strEntities.add(attr.getValue());
            }
            return strEntities;
        }
    }
}
