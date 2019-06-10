package com.dinghmcn.android.wificonnectclient;

import android.content.res.XmlResourceParser;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import android.content.Context;

import java.io.Serializable;

public class MyXml implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<XProperty> xProperties = new ArrayList<XProperty>();
    private List<XMethod> xMethods = new ArrayList<XMethod>();
    private List<XNode> xNodes = new ArrayList<XNode>();

    private static final String TAG = "xsp_myXml";

    public List<XProperty> getXProperties(){
        return xProperties;
    }

    public void setXProperties(List<XProperty> list){
        this.xProperties = list;
    }

    public List<XMethod> getXMethods(){
        return xMethods;
    }

    public void setXMethods(List<XMethod> list){
        this.xMethods = list;
    }

    public List<XNode> getXNodes(){
        return xNodes;
    }

    public void setXNodes(List<XNode> list){
        this.xNodes = list;
    }
}



