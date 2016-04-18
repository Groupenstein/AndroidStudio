package com.groupenstein.groupenstein;


import java.util.ArrayList;
import java.util.List;

public class SearchGroupModel {
    public String SearchText;
    public String LocationText;
    public List<SearchResultsDetailModel> Organizations;

    public SearchGroupModel()
    {
        Organizations = new ArrayList<SearchResultsDetailModel>();
    }

}
