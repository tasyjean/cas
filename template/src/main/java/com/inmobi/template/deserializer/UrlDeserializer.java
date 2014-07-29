package com.inmobi.template.deserializer;

import java.lang.reflect.Type;
import java.util.Iterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class UrlDeserializer implements JsonDeserializer<String> {
	

	@Override
	public String deserialize(JsonElement json, Type typeOf,
			JsonDeserializationContext arg2) throws JsonParseException {
		final JsonArray jsonArray = json.getAsJsonArray();
		Iterator<JsonElement> jsonItr = jsonArray.iterator();
		if(jsonItr.hasNext()){
			return jsonItr.next().getAsString();
		}
		return null;
	}
	
}