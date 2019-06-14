package com.dinghmcn.android.wificonnectclient;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.net.wifi.ScanResult;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.sim.cit.file.ResultWriter;
// add by dgy for 20170223
//import com.readboy.nv.NvJniItems;
// add end
//add for mark test results by songguangyu 20140220 start
//add for mark test results by songguangyu 20140220 end

public class CITTestHelper extends Application {
    private static final String TAG = "CITTestHelper";
    // add PA568list item by xiasiping 20140811 start
    private static final String CONFIG_FILE_PATH = "cit_config_item.xml";
    // add PA568list item by xiasiping 20140811 end
    private static final int CONFIG_DEFAULT_SHARP_PATH = R.xml.sharp_list_item;
    private static final int CONFIG_DEFAULT_HAIER_PATH = R.xml.haier_list_item;
    // Add xp7700 list item by Lvhongshan 20131212
    // add PA568 list item by xiasiping 20140811 start
    private static final int CONFIG_DEFAULT_KK_PATH = R.xml.kk_list_item;

    private static boolean isFromCIT = true;
    // add PA568 list item by xiasiping 20140811 end
    private static final int CONFIG_DEFAULT_XP7700_PATH = R.xml.xp7700_list_item;
    private static final int CONFIG_DEFAULT_XP7700_IS_PATH = R.xml.xp7700_is_list_item;
    private static final int CONFIG_DEFAULT_XP6700_IS_PATH = R.xml.xp6700_is_list_item;
    public static final String EXTRA_KEY_TEST_TYPE = "to_firstlist";
    public static final String EXTRA_KEY_TO_TESTLIST = "to_testlist";
    public static final String EXTRA_KEY_AUTO_TEST_INDEX = "auto_test_index";
    public static final String EXTRA_VALUE_TEST_TYPE_PCB = "pcb";
    public static final String EXTRA_VALUE_TEST_TYPE_SUBPCB = "subpcb";
    public static final String EXTRA_VALUE_TEST_TYPE_COMPLETE = "complete";
    public static final String EXTRA_VALUE_TO_FIRSTLIST_CIT1 = "cit1";
    public static final String EXTRA_VALUE_TO_FIRSTLIST_CIT2 = "cit2";
    public static final String EXTRA_VALUE_TO_FIRSTLIST_CIT3 = "cit3";
    //add for mark test results by songguangyu 20140220 start
    public static final String EXTRA_TEST_TYPE_VERSION = "version";
    public static final String EXTRA_TEST_TYPE_SPEAKER_PINKNOISE_TEST = "speaker_pinknoise_test";
    public static final int TEST_RESULT_NOT = 0;
    public static final int TEST_RESULT_PASS = 1;
    public static final int TEST_RESULT_FAIL = -1;
    private Map<Integer, List<TestItem>> testResultMaps
            = new HashMap<Integer, List<TestItem>>();
    //add for mark test results by songguangyu 20140220 end
    //add for mark version test results by songguangyu 20140220 start
    private int versionTestResult = TEST_RESULT_NOT;
    //add for mark version test results by songguangyu 20140220 end
    private int speakerPinknoiseTestResult = TEST_RESULT_NOT;
    public static final int NOT_TEST = 0;
    public static final int PCB_CIT1 = 1;
    public static final int PCB_CIT2 = 2;
    public static final int PCB_CIT3 = 3;
    public static final int COMPLETE_CIT1 = 4;
    public static final int COMPLETE_CIT2 = 5;
    public static final int COMPLETE_CIT3 = 6;
    public static final int SUBPCB_CIT1 = 7;
    public static final int SUBPCB_CIT2 = 8;

    public static final int MAIN_CIT = 11;

    private int testMode;
    private static boolean isAutoTest;
    private boolean isStartedBySdCard;
    private boolean isWifiOpened;   //记录进入CIT前wifi的状态，退出CIT恢复
    private boolean isBlueToothOpened;  //记录进入CIT前蓝牙的状态，退出CIT恢复
    private boolean isCitRunning;
    private boolean hasPreInstallTest;
    private boolean isDownloading;

    private List<TestItem> pcbcit1list = new ArrayList<TestItem>();
    private List<TestItem> pcbcit2list = new ArrayList<TestItem>();
    private List<TestItem> pcbcit3list = new ArrayList<TestItem>();
    private List<TestItem> completecit1list = new ArrayList<TestItem>();
    private List<TestItem> completecit2list = new ArrayList<TestItem>();
    private List<TestItem> completecit3list = new ArrayList<TestItem>();
    private List<TestItem> subpcbcit1list = new ArrayList<TestItem>();
    private List<TestItem> subpcbcit2list = new ArrayList<TestItem>();
    private List<String> keypadList = new ArrayList<String>();
    private List<TestItem> mainList = new ArrayList<TestItem>();
    private List<TestItem> testList;
    private List<String> titleList;
    private List<String> classNameList;
    private int autoTestListSize;
    private static final String XML_NODE_PCB = "pcb";
    private static final String XML_NODE_SUBPCB = "subpcb";
    private static final String XML_NODE_COMPLETE = "complete";
    private static final String XML_NODE_CIT1 = "cit1";
    private static final String XML_NODE_CIT2 = "cit2";
    private static final String XML_NODE_CIT3 = "cit3";
    private static final String XML_NODE_TESTITEM = "testitem";
    private static final String XML_NODE_KEYPAD = "keypad";
    private static final String XML_NODE_KEYITEM = "keyitem";
    public static final String COLLIGATE_SOUND_PATH = "/mnt/sdcard/test.mp3";
    public static final boolean HAS_LED = true;
    public static final boolean HAS_FLASH_LIGHT = false;
    private static Context context = null;

    // add by dgy 20170223 for nv 2499 data
    private byte[] nv2499 = new byte[30];
    private String nv2499Str = "UUUUUUUUUUUUUUUUUUUUUUUUUUUUUU";
    public static int mExtraItem = 0;
    String mMaiTag = "mainnormal";
    //add end
    private List<ScanResult> scanResults = new ArrayList<ScanResult>();
    private String bluetoothList;

    //    static {
//        System.loadLibrary("qcomfm_jni");
//    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "CITTestHelper onCreate");
        testMode = NOT_TEST;
        getNeedTouchForTongdao();
        mMaiTag = mExtraItem == 0 ? "mainnormal" : "maintongdao";
        fillAllList();
        context = this;
//        generateResult();
        // add by dgy read 2499 nv
//        byte[] nvdata = NvJniItems.getInstance().getNv2499();
//        System.arraycopy(nvdata, 0, nv2499, 0, 30);
        checkNv2499Data();
        // add end
    }

    public void initMainList() {
        getNeedTouchForTongdao();
        mMaiTag = mExtraItem == 0 ? "mainnormal" : "maintongdao";
        fillMainList();
    }

    public void fillMainList() {
        Log.i(TAG, "fillAllList start");
        XmlPullParser xrp = null;
        XmlPullParserFactory xmlPullParserFactory = null;
        InputStream is = getConfigXmlFromSDcard();
        if (is == null) {
            Log.i(TAG, "the cit_config_item.xml is not found ");
            xrp = getResources().getXml(CONFIG_DEFAULT_KK_PATH);
            Log.i(TAG, "error choose default config");
            // add PA568 list item by xiasiping 20140811 end
        } else {
            try {
                xmlPullParserFactory = XmlPullParserFactory.newInstance();
                xrp = xmlPullParserFactory.newPullParser();
                xrp.setInput(is, "UTF-8");
            } catch (XmlPullParserException e1) {
            }
        }
        try {
            while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlResourceParser.START_TAG && mMaiTag.equals(xrp.getName())) {
                    mainList.clear();
                    if (mExtraItem == 0) {
                        fillTestListByListType(xrp, "mainnormal", mainList);
                    } else {
                        fillTestListByListType(xrp, "maintongdao", mainList);
                    }
                }
                xrp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            Log.e(TAG, "May be the config file has error!");
//            Toast.makeText(this, "May be the config file has error!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "May be the config file has error!");
//            Toast.makeText(this, "May be the config file has error!", Toast.LENGTH_SHORT).show();
        }
    }

    private void getNeedTouchForTongdao() {
        String procCurrentStr;

        try {
            Log.v("getNeedTouchForTongdao", "getNeedTouchForTongdao1");
//			BufferedReader reader = new BufferedReader(
//					new FileReader("/sys/hideep/version"), 256);
//			try {
//				procCurrentStr = reader.readLine();
//			} finally {
//				reader.close();
//			}
            File file = new File("/sys/hideep/version");
            if (file.exists()) {
                mExtraItem = 1;
                Log.v("getNeedTouchForTongdao", "getNeedTouchForTongdao__file exists");
            } else {
                mExtraItem = 0;
                Log.e("getNeedTouchForTongdao", "getNeedTouchForTongdao__file not exists");
            }
        } catch (Exception e) {
            Log.e("getNeedTouchForTongdao", "getNeedTouchForTongdao3");
            e.printStackTrace();
            mExtraItem = 0;
        }
    }

    private InputStream getConfigXmlFromSDcard() {
        //Modify for CIT optimization by xiasiping 20140730 start
        InputStream is = null;
        File f1 = new File("/storage/sdcard0/Download", CONFIG_FILE_PATH);
        /*File f2 = new File(this.getFilesDir(),CONFIG_FILE_PATH);*/
        Log.i(TAG, "  " + this.getFilesDir());

        Log.e(TAG, "xsp cit_config_item.xmlis exists " + f1.exists());

        if (f1.exists()) {//SD config file exists
            try {
                /*Log.i(TAG, "f1="+f1.getAbsolutePath()+"\n"+"f2="+f2.getAbsolutePath());
                CommonDrive.copyFile(f1.getAbsolutePath(), f2.getAbsolutePath());*/
                // add PA568 list item by xiasiping 20140811 start
                is = new BufferedInputStream(new FileInputStream(f1.getAbsoluteFile()));
                Log.i(TAG, "the cit_config_item.xml is found ");
                isFromCIT = false;
            } catch (FileNotFoundException e) {
                Log.i(TAG, "the cit_config_item.xml is not found ");
                isFromCIT = true;
                // add PA568 list item by xiasiping 20140811 end
                return null;
            }
        }
        //Modify for CIT optimization by xiasiping 20140730 end
        return is;
    }

    // add PA568 list item by xiasiping 20140811 start
    public static boolean getIsFromCIT() {
        return isFromCIT;
    }
    // add PA568 list item by xiasiping 20140811 end

    //Modify for CIT optimization by xiasiping 20140730 start
    public static Context getContext_x() {
        return context;
    }
    //Modify for CIT optimization by xiasiping 20140730 end

    /*public XmlPullParser getXrp(){
        Log.i(TAG,"getXrp start");
        XmlPullParser xrp = null;

        xrp = getResources().getXml(R.xml.cit_conf);
        if (xrp != null){
             Log.i(TAG,"xrp is not null  ");
        }
        return xrp;
    }*/

    public void fillAllList() {
        pcbcit1list.clear();
        pcbcit2list.clear();
        pcbcit3list.clear();
        subpcbcit1list.clear();
        subpcbcit2list.clear();
        completecit1list.clear();
        completecit2list.clear();
        completecit3list.clear();
        mainList.clear();
        Log.i(TAG, "fillAllList start");
        XmlPullParser xrp = null;
        XmlPullParserFactory xmlPullParserFactory = null;
        InputStream is = getConfigXmlFromSDcard();
        if (is == null) {
            Log.i(TAG, "the cit_config_item.xml is not found ");
            // add PA568 list item by xiasiping 20140811 start
            /*String softwareTitle = SystemProperties.get("ro.product.model","unknown");
            //SD config file not exists,read defaule config file
            if (softwareTitle.contains("SH330T")||softwareTitle.contains("SH330U")) {
                xrp = getResources().getXml(CONFIG_DEFAULT_SHARP_PATH);
                Log.i(TAG, "choose sharp default config");
            }else if (softwareTitle.contains("N610E")||softwareTitle.contains("N86E")) {
                xrp = getResources().getXml(CONFIG_DEFAULT_HAIER_PATH);
                Log.i(TAG, "choose haier default config");
            } else if (softwareTitle.contains("XP7700")) {
                if (CommonDrive.getHWSubType().replace('\n',' ').contains("IS")){
                    xrp = getResources().getXml(CONFIG_DEFAULT_XP7700_IS_PATH);
                    Log.i(TAG, "choose 7700 IS config");
                } else{
                    xrp = getResources().getXml(CONFIG_DEFAULT_XP7700_PATH);
                    Log.i(TAG, "choose 7700 GEN config");
                }
            }else if (softwareTitle.contains("XP6700")) {
                if (CommonDrive.getHWSubType().replace('\n',' ').contains("IS")){
                    xrp = getResources().getXml(CONFIG_DEFAULT_XP6700_IS_PATH);
                    Log.i(TAG, "choose 6700 IS config");
                } else{
                    xrp = getResources().getXml(CONFIG_DEFAULT_SHARP_PATH);
                    Log.i(TAG, "choose 6700 config");
                }
            }else if (CommonDrive.getHWSubType().replace('\n',' ').contains("7700")) {
                xrp = getResources().getXml(CONFIG_DEFAULT_XP7700_PATH);
                Log.i(TAG, "choose HWSubType 7700 config");
            }else {
                //xrp = getResources().getXml(CONFIG_DEFAULT_XP7700_PATH);
                xrp = getResources().getXml(CONFIG_DEFAULT_XP7700_PATH);
                Log.i(TAG, "error choose default config");
            }*/
            xrp = getResources().getXml(CONFIG_DEFAULT_KK_PATH);
            Log.i(TAG, "error choose default config");
            // add PA568 list item by xiasiping 20140811 end
        } else {
            try {
                xmlPullParserFactory = XmlPullParserFactory.newInstance();
                xrp = xmlPullParserFactory.newPullParser();
                xrp.setInput(is, "UTF-8");
            } catch (XmlPullParserException e1) {
            }
        }
        try {
            while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlResourceParser.START_TAG && XML_NODE_PCB.equals(xrp.getName())) {
                    fillTestListByTestType(xrp, XML_NODE_PCB, pcbcit1list, pcbcit2list, pcbcit3list);
                } else if (xrp.getEventType() == XmlResourceParser.START_TAG && XML_NODE_SUBPCB.equals(xrp.getName())) {
                    fillTestListByTestType(xrp, XML_NODE_SUBPCB, subpcbcit1list, subpcbcit2list);
                } else if (xrp.getEventType() == XmlResourceParser.START_TAG && XML_NODE_COMPLETE.equals(xrp.getName())) {
                    fillTestListByTestType(xrp, XML_NODE_COMPLETE, completecit1list, completecit2list, completecit3list);
                } else if (xrp.getEventType() == XmlResourceParser.START_TAG && XML_NODE_KEYPAD.equals(xrp.getName())) {
                    while (!(xrp.getEventType() == XmlResourceParser.END_TAG && XML_NODE_KEYPAD.equals(xrp.getName()))) {
                        if (xrp.getEventType() == XmlResourceParser.START_TAG && XML_NODE_KEYITEM.equals(xrp.getName())) {
                            String name = xrp.getAttributeValue(0);
                            boolean isSet = false;
                            try {
                                isSet = Integer.parseInt(xrp.getAttributeValue(1)) == 1 ? true : false;
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "May be the config file has error!");
//                                Toast.makeText(this, "May be the config file has error!", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                            if (isSet) {
                                keypadList.add(name);
                            }
                        }
                        xrp.next();
                    }
                } else if (xrp.getEventType() == XmlResourceParser.START_TAG && mMaiTag.equals(xrp.getName())) {
                    mainList.clear();
                    if (mExtraItem == 0) {
                        fillTestListByListType(xrp, "mainnormal", mainList);
                    } else {
                        fillTestListByListType(xrp, "maintongdao", mainList);
                    }
                }
                xrp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            Log.e(TAG, "May be the config file has error!");
//            Toast.makeText(this, "May be the config file has error!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "May be the config file has error!");
//            Toast.makeText(this, "May be the config file has error!", Toast.LENGTH_SHORT).show();
        }
    }

    private void fillTestListByTestType(XmlPullParser xrp, String testType, List<TestItem> list1, List<TestItem> list2, List<TestItem> list3) {
        try {
            while (!(xrp.getEventType() == XmlResourceParser.END_TAG && testType.equals(xrp.getName()))) {
                if (XML_NODE_CIT1.equals(xrp.getName())) {
                    fillTestListByListType(xrp, XML_NODE_CIT1, list1);
                } else if (XML_NODE_CIT2.equals(xrp.getName())) {
                    fillTestListByListType(xrp, XML_NODE_CIT2, list2);
                } else if (XML_NODE_CIT3.equals(xrp.getName())) {
                    fillTestListByListType(xrp, XML_NODE_CIT3, list3);
                }
                xrp.next();
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG, "May be the config file has error!");
//            Toast.makeText(this, "May be the config file has error!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "May be the config file has error!");
//            Toast.makeText(this, "May be the config file has error!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void fillTestListByTestType(XmlPullParser xrp, String testType, List<TestItem> list1, List<TestItem> list2) {
        try {
            while (!(xrp.getEventType() == XmlResourceParser.END_TAG && testType.equals(xrp.getName()))) {
                if (XML_NODE_CIT1.equals(xrp.getName())) {
                    fillTestListByListType(xrp, XML_NODE_CIT1, list1);
                } else if (XML_NODE_CIT2.equals(xrp.getName())) {
                    fillTestListByListType(xrp, XML_NODE_CIT2, list2);
                }
                xrp.next();
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG, "May be the config file has error!");
//            Toast.makeText(this, "May be the config file has error!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "May be the config file has error!");
//            Toast.makeText(this, "May be the config file has error!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void fillTestListByListType(XmlPullParser xrp, String testType, List<TestItem> list) {
        try {
            while (!(xrp.getEventType() == XmlResourceParser.END_TAG && testType.equals(xrp.getName()))) {
                if (xrp.getEventType() == XmlResourceParser.START_TAG && XML_NODE_TESTITEM.equals(xrp.getName())) {
                    String title = xrp.getAttributeValue(0);
                    String className = xrp.getAttributeValue(1);
                    boolean isSet = false;
                    try {
                        isSet = Integer.parseInt(xrp.getAttributeValue(2)) == 1 ? true : false;
                    } catch (NumberFormatException e1) {
                        Log.e(TAG, "May be the config file has error!");
//                        Log.v("hqb", "hqb__1xrp.getAttributeValue(2) = " + xrp.getAttributeValue(2));
//                        Toast.makeText(this, "May be the config file has error!", Toast.LENGTH_SHORT).show();
                        e1.printStackTrace();
                    }catch (Exception e){
						Log.v("hqb", "hqb__2xrp.getAttributeValue(2) = " + xrp.getAttributeValue(2));
                    	e.printStackTrace();
					}
                    if (isSet) {
                        if (isStartedBySdCard && className.equals("TFCard")) {
                            xrp.next();
                            continue;
                        }
                        if (isStartedBySdCard && className.equals("PreInstallTest")) {
                            xrp.next();
                            continue;
                        }
                        list.add(new TestItem(title, className, TEST_RESULT_NOT));
                    }
                }
                xrp.next();
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG, "May be the config file has error!");
//            Toast.makeText(this, "May be the config file has error!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "May be the config file has error!");
//            Toast.makeText(this, "May be the config file has error!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void initTestList(boolean hasAutoTest) {
        testList = null;
        titleList = null;
        classNameList = null;
        switch (testMode) {
            case PCB_CIT1:
                testList = pcbcit1list;
                break;
            case PCB_CIT2:
                testList = pcbcit2list;
                break;
            case PCB_CIT3:
                testList = pcbcit3list;
                break;
            case COMPLETE_CIT1:
                testList = completecit1list;
                break;
            case COMPLETE_CIT2:
                testList = completecit2list;
                break;
            case COMPLETE_CIT3:
                testList = completecit3list;
                break;
            case SUBPCB_CIT1:
                testList = subpcbcit1list;
                break;
            case SUBPCB_CIT2:
                testList = subpcbcit2list;
                break;
            case MAIN_CIT:
                testList = mainList;
                break;
            default:
                break;
        }
        autoTestListSize = testList.size();
        getTitleAndclassNameList(hasAutoTest);
        //add for mark test results by songguangyu 20140220 start
        testResultMaps.put(testMode, testList);
        //add for mark test results by songguangyu 20140220 end
    }

    private void getTitleAndclassNameList(boolean hasAutoTest) {
        titleList = new ArrayList<String>();
        classNameList = new ArrayList<String>();
        if (hasAutoTest) {
//            titleList.add("Auto Test");
            titleList.add("自动测试");
        }
        for (int i = 0; i < testList.size(); i++) {
            titleList.add(testList.get(i).getTitle());
            classNameList.add(testList.get(i).getClassName());
        }
    }

    public void startTestActivity(Activity activity, int index) {
        Class<?> testItemClass = null;
        try {
            testItemClass = Class.forName("com.sim.cit.testitem." + classNameList.get(index));
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "The test item is error!");
            Toast.makeText(activity, "The test item is error!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            //Modify for PA568 EVT version by xiasiping 20140807 start
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "The test item null!");
            Toast.makeText(activity, "The test item is null!", Toast.LENGTH_SHORT).show();
        }
        //Modify for PA568 EVT version by xiasiping 20140807 end
        if (testItemClass != null) {
            Intent i = new Intent(activity, testItemClass);
            //add for mark test results by songguangyu 20140220 start
            i.putExtra("cutTestIndex", index);
            //add for mark test results by songguangyu 20140220 end
            if (isAutoTest) {
                i.putExtra(CITTestHelper.EXTRA_KEY_AUTO_TEST_INDEX, index);
            }
            activity.startActivity(i);
        }
    }

    //add for mark test results by songguangyu 20140220 start
    public Map<Integer, List<TestItem>> getTestResultMaps() {
        return testResultMaps;
    }

    //add for mark test results by songguangyu 20140220 end
    //add for mark version test results by songguangyu 20140220 start
    public int getVersionTestResult() {
        return versionTestResult;
    }

    public void setVersionTestResult(int versionTestResult) {
        this.versionTestResult = versionTestResult;
    }

    //add for mark version test results by songguangyu 20140220 end
    //add by lxx 20180725
    public int getSpeakerPinknoiseTestResult() {
        return speakerPinknoiseTestResult;
    }

    public void setSpeakerPinknoiseTestResult(int speakerPinknoiseTestResult) {
        this.speakerPinknoiseTestResult = speakerPinknoiseTestResult;
    }


    public List<String> getKeypadList() {
        return keypadList;
    }

    public int getAutoTestListSize() {
        return autoTestListSize;
    }

    public int getTestMode() {
        return testMode;
    }

    public void setTestMode(int testMode) {
        this.testMode = testMode;
    }

    public boolean isAutoTest() {
        return isAutoTest;
    }

    public void setAutoTest(boolean isAutoTest) {
        this.isAutoTest = isAutoTest;
    }

    public List<String> getTitleList() {
        return titleList;
    }

    //	private List<int[]> resultList=new ArrayList<int[]>();
//	private void generateResult(){
//		resultList.add(new int[pcbcit1list.size()]);
//		resultList.add(new int[pcbcit2list.size()]);
//		resultList.add(new int[pcbcit3list.size()]);
//		resultList.add(new int[completecit1list.size()]);
//		resultList.add(new int[completecit2list.size()]);
//		resultList.add(new int[completecit3list.size()]);
//		resultList.add(new int[subpcbcit1list.size()]);
//		resultList.add(new int[subpcbcit2list.size()]);
//	}
//	private String resultString="";
//	public void buildResult(){
//		for(int i:testResultMaps.keySet()){
//			List<TestItem> list = testResultMaps.get(i);
//			for(int j=0;j<list.size();j++){
//				resultList.get(i-1)[j]=list.get(j).getTestResult();
//			}
//		}
//		StringBuilder sb =new StringBuilder();
//		for(int[] array:resultList){
//			for(int r:array){
//				if(CITTestHelper.TEST_RESULT_FAIL==r){
//					sb.append("F ");
//				}
//				else if(CITTestHelper.TEST_RESULT_PASS==r){
//					sb.append("P ");
//				}
//				else{
//					sb.append("U ");
//				}
//			}
//			sb.append("\n");
//		}
//		resultString=sb.toString();
//		ResultWriter.writeResult(resultString);
//	}
//	public String getResultString(){
//		return resultString;
//	}
    public List<String> getClassNameList() {
        return classNameList;
    }


    // add by dgy 20170223
    public byte[] getNv2499Data() {
        checkNv2499Data();
        return nv2499;
    }

    public void setNv2499String(String nv2499) {
        nv2499Str = nv2499;
    }

    public String getNv2499String() {
        return nv2499Str;
    }

    private void checkNv2499Data() {
        if (nv2499 != null) {
            for (int i = 0; i < nv2499.length; i++) {
                if (nv2499[i] == 85 || nv2499[i] == 80 || nv2499[i] == 70 || nv2499[i] == 66 || nv2499[i] == 0) {
                    //nv2499[i] = 70;
                } else {
                    nv2499[i] = 70;
                }
            }
        }
    }

    public List<ScanResult> getScanResults() {
        return scanResults;
    }

    public void setScanResults(List<ScanResult> scanResults) {
        this.scanResults = scanResults;
    }

    public boolean isStartedBySdCard() {
        return isStartedBySdCard;
    }

    public void setStartedBySdCard(boolean startedBySdCard) {
        isStartedBySdCard = startedBySdCard;
    }

    public String getBluetoothList() {
        return bluetoothList;
    }

    public void setBluetoothList(String bluetoothList) {
        this.bluetoothList = bluetoothList;
    }

    public boolean isWifiOpened() {
        return isWifiOpened;
    }

    public void setWifiOpened(boolean wifiOpened) {
        isWifiOpened = wifiOpened;
    }

    public boolean isBlueToothOpened() {
        return isBlueToothOpened;
    }

    public void setBlueToothOpened(boolean blueToothOpened) {
        isBlueToothOpened = blueToothOpened;
    }

    public boolean isCitRunning() {
        return isCitRunning;
    }

    public void setCitRunning(boolean citRunning) {
        isCitRunning = citRunning;
    }

    public void removeSdCardTest() {
        for (int i = 0; i < pcbcit1list.size(); i++) {
            if (pcbcit1list.get(i).getClassName().equals("TFCard"))
                pcbcit1list.remove(i);
        }
        for (int i = 0; i < completecit1list.size(); i++) {
            if (completecit1list.get(i).getClassName().equals("TFCard"))
                completecit1list.remove(i);
        }
        for (int i = 0; i < mainList.size(); i++) {
            if (mainList.get(i).getClassName().equals("TFCard"))
                mainList.remove(i);
        }
    }

    public void removePreInstallTest() {
        for (int i = 0; i < pcbcit1list.size(); i++) {
            if (pcbcit1list.get(i).getClassName().equals("PreInstallTest"))
                pcbcit1list.remove(i);
        }
        for (int i = 0; i < completecit1list.size(); i++) {
            if (completecit1list.get(i).getClassName().equals("PreInstallTest"))
                completecit1list.remove(i);
        }
        for (int i = 0; i < mainList.size(); i++) {
            if (mainList.get(i).getClassName().equals("PreInstallTest"))
                mainList.remove(i);
        }
    }

    public boolean hasPreInstallTest() {
        return hasPreInstallTest;
    }

    public void setHasPreInstallTest(boolean hasPreInstallTest) {
        this.hasPreInstallTest = hasPreInstallTest;
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    public void setDownloading(boolean downloading) {
        isDownloading = downloading;
    }

    public static boolean testPreInstall() {
        int preInstallNum = Settings.System.getInt(context.getContentResolver(), "first_install_preapps_num_total", 0);
        List<String> preInstallList = new ArrayList<String>();
        List<PackageInfo> installedList = new ArrayList<PackageInfo>();
        for (int i = 0; i < preInstallNum; i++) {
            preInstallList.add(Settings.System.getString(context.getContentResolver(), "first_install_preapps_num_need_" + i));
        }
        PackageManager pm = context.getPackageManager();
        installedList = pm.getInstalledPackages(0);
        for (int i = 0; i < preInstallList.size(); i++) {
            if (preInstallList.get(i) == null) {
                continue;
            }
            boolean installed = false;
            for (int j = 0; j < installedList.size(); j++) {
                if (preInstallList.get(i).equals(installedList.get(j).packageName)) {
                    installedList.remove(j);
                    installed = true;
                    break;
                }
            }
            if (!installed) {
                return false;
            }
        }
        return true;
    }
}
