/*
 * Copyright 2018 dylan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.kademi.ocr.impl.pyocr;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;

/**
 *
 * @author dylan
 */
public class PositionDeserializer extends StdDeserializer<BoxPosition> {

    public PositionDeserializer() {
        this(null);
    }

    public PositionDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public BoxPosition deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.readValueAsTree();

        if (node.isArray()) {
            ArrayNode anode = (ArrayNode) node;

            ArrayNode i0 = (ArrayNode) anode.get(0);
            int i0i0 = i0.get(0).asInt();
            int i0i1 = i0.get(1).asInt();

            ArrayNode i1 = (ArrayNode) anode.get(1);
            int i1i0 = i1.get(0).asInt();
            int i1i1 = i1.get(1).asInt();

            return new BoxPosition(i0i0, i0i1, i1i0, i1i1);
        }

        return null;
    }

}
