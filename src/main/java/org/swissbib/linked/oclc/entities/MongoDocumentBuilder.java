package org.swissbib.linked.oclc.entities;

import org.bson.Document;


/**
 * Created by swissbib on 1/5/16.
 */
public class MongoDocumentBuilder {


    public Document jsonToDocument (String jsonStructure) {

        return  Document.parse(jsonStructure);

    }

}
