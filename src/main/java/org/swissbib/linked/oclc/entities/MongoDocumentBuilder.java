package org.swissbib.linked.oclc.entities;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.bson.BasicBSONCallback;
import org.bson.BasicBSONObject;
import org.bson.Document;

import java.util.Collection;


/**
 * Created by swissbib on 1/5/16.
 */
public class MongoDocumentBuilder {


    public Document jsonToDocument (String jsonStructure) {

        return  Document.parse(jsonStructure);

    }

}
