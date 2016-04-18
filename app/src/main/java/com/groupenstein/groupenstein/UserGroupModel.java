package com.groupenstein.groupenstein;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brett on 11/30/2014.
 */
public class UserGroupModel {
    public int OrganizationId;
    public String OrganizationName;
    public String OrganizationEmail;
    public String OrganizationPhone;
    public String OrganizationAddress1;
    public String OrganizationAddress2;
    public String OrganizationCity;
    public String OrganizationState;
    public String OrganizationZip;
    public String OrganizationLogoFile;

    public int GroupId;
    public String GroupName;
    public String GroupDescription;
    public boolean IsGroupMessageAdmin;
    public List<GroupMessageModel> GroupMessages;
    public List<GroupEventModel> GroupEvents;

    public UserGroupModel()
    {
    }
}
