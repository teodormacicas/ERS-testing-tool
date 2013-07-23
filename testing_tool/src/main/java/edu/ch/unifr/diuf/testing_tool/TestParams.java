package edu.ch.unifr.diuf.testing_tool;


public class TestParams 
{
    public String testId;
    
    private String testServerGraphName;
    private int testServerGraphReset;
    private String testServerReadCons; 
    private String testServerWriteCons; 
    private String testServerTransLockingGran; 
    private int testServerReplicationFactor;
    
    private String testInputFilename;
    private int testNum;
    private int testThreadNumber;
    private int testWarmupPer;
    private int testRunningPer;
    private int testOperationType;
    private int testOperationNum;
    private int testTransRetrials;

    public TestParams(String graphName, int graphReset, String readCons, 
            String writeCons, String transLockGran, int replFactor,
            String inputFilename, int num, int threadNum, int warmupPer, 
            int runningPer, int operType, int operNum, int transRetrials, String testId) {
        // server related params
        this.testServerGraphName = graphName;
        this.testServerGraphReset = graphReset;
        this.testServerReadCons = readCons; 
        this.testServerWriteCons = writeCons;
        this.testServerTransLockingGran = transLockGran;
        this.testServerReplicationFactor = replFactor;
        
        // client related params 
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
    
    public String getTestReadCons() { 
        return this.testServerReadCons;
    }
    
    public String getTestWriteCons() { 
        return this.testServerWriteCons;
    }
    
    public String getTransLockGran() { 
        return this.testServerTransLockingGran;
    }
    
    public int getReplicationFactor() {
        return this.testServerReplicationFactor;
    }
    
    public int getTransRetrials() { 
        return this.testTransRetrials;
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
        sb.append("\tSERVER PARAMS: ").append(testServerGraphName).append(" ");
        sb.append(testServerGraphReset).append(" ").append(testServerReadCons);
        sb.append(" ").append(testServerWriteCons).append(" ").append(testServerTransLockingGran);
        sb.append(" ").append(testServerReplicationFactor).append("\n");
        
        sb.append("\tTEST PARAMS: \n");
        sb.append("\t\tinput filename: ").append(testInputFilename).append("\n");
        sb.append("\t\trun steps: ").append(testNum).append("\n");
        sb.append("\t\tthread num per client: ").append(testThreadNumber).append("\n");
        sb.append("\t\twarmup period sec: ").append(testWarmupPer).append("\n");
        sb.append("\t\trunning period sec: ").append(testRunningPer).append("\n");
        sb.append("\t\toperation type: ").append(testOperationType).append("\n");
        sb.append("\t\tnum oper per trans: ").append(testOperationNum).append("\n");
        sb.append("\t\ttrans num of retrials: ").append(testRunningPer).append("\n");
        return sb.toString();
    }
    
}
