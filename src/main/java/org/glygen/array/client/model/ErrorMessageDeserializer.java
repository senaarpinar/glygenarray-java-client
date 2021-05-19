package org.glygen.array.client.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.ObjectError;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class ErrorMessageDeserializer extends StdDeserializer<ErrorMessage> { 
    private static final long serialVersionUID = 1L;

    public ErrorMessageDeserializer() { 
        this(null); 
    } 

    public ErrorMessageDeserializer(Class<?> vc) { 
        super(vc); 
    }

    @Override
    public ErrorMessage deserialize(JsonParser jp, DeserializationContext ctxt) 
      throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        String message = null;
        if (node.get("detailMessage") != null) 
            message = node.get("detailMessage").asText();
        
        ErrorMessage errorMessage = new ErrorMessage(message);
        JsonNode errorsNode = node.get("errors");

        if (errorsNode.isArray()) {
            for (JsonNode jsonNode : errorsNode) {
                String nameField = jsonNode.get("objectName").asText();   
                String defaultMessage = jsonNode.get("defaultMessage").asText();   
                JsonNode codesNode = jsonNode.get("code");
                List<String>  codes = new ArrayList<String>();
                if (codesNode.isArray()) {
                    for (JsonNode code : codesNode) {
                        codes.add(code.asText());
                    }
                }
                ObjectError error = new ObjectError(nameField, codes.toArray(new String[0]), null, defaultMessage);
                errorMessage.addError(error);
            }
        }
        
        return errorMessage;
        
    }
}
