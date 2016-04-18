package com.groupenstein.groupenstein;

import java.util.List;

/**
 * Created by Brett on 12/12/2014.
 */
public class GroupDetailModel {
    public int Id;
    public String Name;
    public int OrganizationId;
    public String Description;
    public List<GroupMessageModel> GroupMessages;
    public List<GroupEventModel> GroupEvents;

}
