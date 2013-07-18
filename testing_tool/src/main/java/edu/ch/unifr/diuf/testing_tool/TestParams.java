package edu.ch.unifr.diuf.testing_tool;


public class TestParams 
{
    public String testId;
    
    private String testServerGraphName;
    private int testServerGraphReset;
    
    private String testInputFilename;
    private int testNum;
    private int testThreadNumber;
    private int testWarmupPer;
    private int testRunningPer;
    private int testOperationType;
    private int testOperationNum;
    private int testTransRetrials;

    public TestParams(String graphName, int graphReset, String inputFilename, 
            int num, int threadNum, int warmupPer, int runningPer, int operType, 
            int operNum, int transRetrials, String testId) {
        this.testServerGraphName = graphName;
        this.testServerGraphReset = graphReset;
        this.testInputFilename = inputFilename;
        this.testNum = num;
        this.testThreadNumber = threadNum;
        this.testWarmupPer = warmupPer;
        this.testRunningPer = runningPer;
        this.testOperationType = operType;
        this.testOperationNum = operNum;
        this.testTransRetrials = transRetrials;
        this.testId = testId;
    }

    public String getTestServerGraphName() { 
        return this.testServerGraphName;
    }
    
    public void setTestServerGraphName(String graphName) { 
        this.testServerGraphName = graphName;
    }
    
    public int getTestServerGraphReset() { 
        return this.testServerGraphReset;
    }
    
    public void setTestServerGraphReset(int reset) { 
        this.testServerGraphReset = reset;
    }
    
    public String getTestInputFilename() {
        return this.testInputFilename;
    }

    public int getTestNum() {
        return testNum;
    }
    
    public int getTestThreadNum() {
        return this.testThreadNumber;
    }
    
    public int getTestWarmupPer() { 
        return this.testWarmupPer;
    }
    
    public int getTestRunningPer() { 
        return this.testRunningPer;
    }
    
    public int getTestOperationType() { 
        return this.testOperationType;
    }
    
    public int getTestOperationNum() { 
        return this.testOperationNum;
    }
    
    public int getTestTransRetrials() { 
        return this.testThreadNumber;
    }

    public String toString() { 
        StringBuilder sb = new StringBuilder();
        sb.append("\tserver graph: ").append(testServerGraphName).append("\n");
        sb.append("\tserver reset graph flag: ").append(testServerGraphReset).append("\n");
        sb.append("\ttest input filename: ").append(testInputFilename).append("\n");
        sb.append("\ttest run steps: ").append(testNum).append("\n");
        sb.append("\ttest thread num per client: ").append(testThreadNumber).append("\n");
        sb.append("\ttest warmup period sec: ").append(testWarmupPer).append("\n");
        sb.append("\ttest running period sec: ").append(testRunningPer).append("\n");
        sb.append("\ttest operation type: ").append(testOperationType).append("\n");
        sb.append("\ttest num oper per trans: ").append(testOperationNum).append("\n");
        sb.append("\ttest trans num of retrials: ").append(testRunningPer).append("\n");
        return sb.toString();
    }
    
}
