package edu.ch.unifr.diuf.testing_tool;


public class TestParams 
{
    public int numClients;
    public String testId;
    
    private String testServerGraphName;
    private int testServerGraphReset;
    private int testServerGraphSnaphshot;
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

    // number of different entities, properties per entity and values per property
    private int testDiffEnt;
    private int testDiffPropPerEnt; 
    private int testDiffValuesPerProf;
    
    public TestParams(int numClients, String graphName, int graphReset, 
            int graphSnapshot, String readCons, String writeCons, String transLockGran, int replFactor,
            int num, int threadNum, int warmupPer, int runningPer, int operType, int operNum, int transRetrials, 
            String testId, int diffEnt, int diffPropPerEnt, int diffValuesPerProp) {
        this.numClients = numClients;
        // server related params
        this.testServerGraphName = graphName;
        this.testServerGraphReset = graphReset;
        this.testServerGraphSnaphshot = graphSnapshot;
        this.testServerReadCons = readCons; 
        this.testServerWriteCons = writeCons;
        this.testServerTransLockingGran = transLockGran;
        this.testServerReplicationFactor = replFactor;
        
        // client related params 
        //this.testInputFilename = inputFilename;
        this.testNum = num;
        this.testThreadNumber = threadNum;
        this.testWarmupPer = warmupPer;
        this.testRunningPer = runningPer;
        this.testOperationType = operType;
        this.testOperationNum = operNum;
        this.testTransRetrials = transRetrials;
        this.testId = testId;
        
        this.testDiffEnt = diffEnt; 
        this.testDiffPropPerEnt = diffPropPerEnt;
        this.testDiffValuesPerProf = diffValuesPerProp;
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
    
    public int getGraphSnapshot() { 
        return this.testServerGraphSnaphshot;
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
    
    /*public String getTestInputFilename() {
        return this.testInputFilename;
    }*/

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

    public int getDiffEnt() { 
        return this.testDiffEnt;
    }
    
    public int getDiffPropPerEnt() { 
        return this.testDiffPropPerEnt;
    }
   
    public int getDiffValuesPerProp() { 
        return this.testDiffValuesPerProf;
    }
    
    private double getProbabilityOfConflicts() { 
        return ((getTestThreadNum()+0.0)*numClients)/
                (testDiffEnt*testDiffPropPerEnt*testDiffValuesPerProf)*100;
    }
    
    public String toString() { 
        StringBuilder sb = new StringBuilder();
        sb.append("\tSERVER PARAMS: ").append(testServerGraphName).append(" ");
        sb.append(testServerGraphReset).append(" ");
        sb.append(testServerGraphSnaphshot).append(" ").append(testServerReadCons);
        sb.append(" ").append(testServerWriteCons).append(" ").append(testServerTransLockingGran);
        sb.append(" ").append(testServerReplicationFactor).append("\n");
        
        sb.append("\tTEST PARAMS: \n");
        //sb.append("\t\tinput filename: ").append(testInputFilename).append("\n");
        sb.append("\t\trun steps: ").append(testNum).append("\n");
        sb.append("\t\tthread num per client: ").append(testThreadNumber).append("\n");
        sb.append("\t\twarmup period sec: ").append(testWarmupPer).append("\n");
        sb.append("\t\trunning period sec: ").append(testRunningPer).append("\n");
        sb.append("\t\toperation type: ").append(testOperationType).append("\n");
        sb.append("\t\tnum oper per trans: ").append(testOperationNum).append("\n");
        sb.append("\t\ttrans num of retrials: ").append(testRunningPer).append("\n");
        sb.append("\t\tnum of different entities: ").append(testDiffEnt).append("\n");
        sb.append("\t\tnum of different prop per ent: ").append(testDiffPropPerEnt).append("\n");
        sb.append("\t\tnum of different values per prop: ").append(testDiffValuesPerProf).append("\n");
        sb.append("\t\tPROBABILITY of conflicts (%): ").append(getProbabilityOfConflicts()).append("\n");
        return sb.toString();
    }
    
}
