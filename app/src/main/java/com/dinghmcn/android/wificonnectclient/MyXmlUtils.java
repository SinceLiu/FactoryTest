package com.dinghmcn.android.wificonnectclient;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MyXmlUtils {

    private MyXml mxml = new MyXml();
    private List<XProperty> xProperties = mxml.getXProperties();
    private List<XMethod> xMethods = mxml.getXMethods();
    private List<XNode> xNodes = mxml.getXNodes();
    private static final String TAG = "xsp_mxmlUtils";
    private static boolean node_isFromStorage = false;

    private InputStream getConfigXmlFromSDcard(){
        InputStream is = null;
        File f1 = new File("/storage/sdcard0/Download","cit_conf_node.xml");
        /*File f2 = new File(this.getFilesDir(),CONFIG_FILE_PATH);*/

        //Log.e(TAG,"xsp_cit_conf_node.xml is exists = "+ f1.exists());

        if (f1.exists()) {//SD config file exists
            try {
                /*Log.i(TAG, "f1="+f1.getAbsolutePath()+"\n"+"f2="+f2.getAbsolutePath());
                CommonDrive.copyFile(f1.getAbsolutePath(), f2.getAbsolutePath());*/
                is = new BufferedInputStream(new FileInputStream(f1.getAbsoluteFile()));
                Log.i(TAG, "xsp_cit_conf_node is found! in storage");
                node_isFromStorage = true;
            } catch (FileNotFoundException e) {
                Log.e(TAG, "xsp_cit_conf_node is not found in storage!");
                return null;
            }
        }
        return is;
    }

    public static boolean getIsFromStorage(){
        return node_isFromStorage;
    }


    public MyXml getMxml () {

        Context context = CITTestHelper.getContext_x();
        XmlPullParser xrp = null;
        InputStream is = getConfigXmlFromSDcard();

        if (is == null) {
            try {
                //File f2 = new File(this.getFilesDir(),CONFIG_FILE_PATH);
                is = context.getAssets().open("cit_conf_node.xml");
                Log.i(TAG, "xsp_cit_conf_node is found! in CIT");
                node_isFromStorage = false;
            } catch (IOException e) {
                e.printStackTrace();
                Log.i (TAG,"error is "+e.toString());
                Log.e(TAG, "xsp_cit_conf_node.xml has IO error in MyXmlUtils");
            }
        }
        try {
            xrp = Xml.newPullParser();
            xrp.setInput(is, "utf-8");

            while(xrp.getEventType() != XmlResourceParser.END_DOCUMENT){
                if (xrp.getEventType() == XmlResourceParser.START_TAG && "properties".equals(xrp.getName())) {
                    getProperties(xrp, "properties", xProperties);
                } else if (xrp.getEventType() == XmlResourceParser.START_TAG && "methods".equals(xrp.getName())) {
                    getMethods(xrp, "methods", xMethods);
                } else if (xrp.getEventType() == XmlResourceParser.START_TAG && "nodes".equals(xrp.getName())) {
                    getNodes(xrp, "nodes", xNodes);
                }
                xrp.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
            Log.e(TAG, "xsp_May be the config file has XmlPullParser error!_all");
        } catch (IOException e){
            e.printStackTrace();
            Log.e(TAG, "xsp_May be the config file has IO error!_all");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    android.util.Log.i("config", e.getMessage());
                    e.printStackTrace();
                } finally {
                    is = null;
                }
            }
        }
        return mxml;
    }
    private void getProperties(XmlPullParser xrp, String pNode, List<XProperty> list){
        try {
            while(!(xrp.getEventType() == XmlResourceParser.END_TAG && pNode.equals(xrp.getName()))){
                if (xrp.getEventType() == XmlResourceParser.START_TAG && "property".equals(xrp.getName())) {
                    String name = xrp.getAttributeValue(0);
                    String value = xrp.getAttributeValue(1);
                    XProperty xproperty = new XProperty();
                    xproperty.setName(name);
                    xproperty.setValue(value);
                    list.add(xproperty);
                }
                xrp.next();
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG, "xsp_May be the config file has xmlpullparser error!_property");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "xsp_May be the config file has IO error!_property");
            e.printStackTrace();
        }
    }
    private void getMethods(XmlPullParser xrp, String pNode, List<XMethod> list){
        try {
            while(!(xrp.getEventType() == XmlResourceParser.END_TAG && pNode.equals(xrp.getName()))){
                if (xrp.getEventType() == XmlResourceParser.START_TAG && "method".equals(xrp.getName())) {
                    String name = xrp.getAttributeValue(0);
                    String value = xrp.getAttributeValue(1);
                    XMethod xmethod = new XMethod();
                    xmethod.setName(name);
                    xmethod.setValue(value);
                    list.add(xmethod);
                }
                xrp.next();
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG, "xsp_May be the config file has xmlpullparser error!_xmethod");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "xsp_May be the config file has IO error!_xmethod");
            e.printStackTrace();
        }
    }
    private void getNodes(XmlPullParser xrp, String pNode, List<XNode> list){
        try {
            while(!(xrp.getEventType() == XmlResourceParser.END_TAG && pNode.equals(xrp.getName()))){
                if (xrp.getEventType() == XmlResourceParser.START_TAG && "node".equals(xrp.getName())) {
                    String name = xrp.getAttributeValue(0);
                    String value = xrp.getAttributeValue(1);
                    String permission = xrp.getAttributeValue(2);
                    String operation = xrp.getAttributeValue(3);
                    XNode xnode = new XNode();
                    xnode.setName(name);
                    xnode.setValue(value);
                    xnode.setPermission(permission);
                    xnode.setOperation(operation);
                    list.add(xnode);
                }
                xrp.next();
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG, "xsp_May be the config file xmlpullparser has error!_xnode");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "xsp_May be the config file has  IO error!_xnode");
            e.printStackTrace();
        }
    }

}
