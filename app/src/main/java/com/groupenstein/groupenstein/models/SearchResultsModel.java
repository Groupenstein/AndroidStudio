package com.groupenstein.groupenstein.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brett on 12/12/2014.
 */
public class SearchResultsModel {
    public List<SearchResultsDetailModel> Organizations;

    public SearchResultsModel()
    {
        Organizations = new ArrayList<SearchResultsDetailModel>();
    }
}
