package com.groupenstein.groupenstein;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brett on 12/12/2014.
 */
public class OrganizationModel {
    public int Id;
    public String Name;
    public String Description;
    public List<GroupDetailModel> Groups;
    public String Email;
    public String Phone;
    public String Address1;
    public String Address2;
    public String City;
    public String State;
    public String Zip;
    public String Contact;
    public String LogoFile;

    public OrganizationModel()
    {
        Groups = new ArrayList<GroupDetailModel>();
    }
}
