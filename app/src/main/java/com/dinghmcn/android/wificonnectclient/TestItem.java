package com.dinghmcn.android.wificonnectclient;

public class TestItem {
    private String title;
    private String className;
    //add for mark test results by songguangyu 20140220 start
    private int testResult;
    //add for mark test results by songguangyu 20140220 end
    //add for mark complete test result by lxx 20180726
    private int textColor;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

        //add for mark test results by songguangyu 20140220 start
        public int getTestResult() {
                return testResult;
        }

        public void setTestResult(int testResult) {
                this.testResult = testResult;
        }

        public TestItem(String title, String className, int testResult) {
                super();
                this.title = title;
                this.className = className;
                this.testResult = testResult;
        }
        //add for mark test results by songguangyu 20140220 end

	public TestItem() {
		super();
	}

	public TestItem(String title, String className) {
		super();
		this.title = title;
		this.className = className;
	}
    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }
}
