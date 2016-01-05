package org.swissbib.linked.oclc.entities;

/**
 * Created by swissbib on 1/4/16.
 */
public interface APISearch {

    public String executeSearch (String query, OCLCQueryType type);

}
