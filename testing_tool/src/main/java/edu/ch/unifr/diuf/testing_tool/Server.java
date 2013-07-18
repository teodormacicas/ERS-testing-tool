package edu.ch.unifr.diuf.testing_tool;

/**
 *
 * @author Teodor Macicas
 */
public class Server extends Machine
{    
     // this is also used by the clients to know where to connect to
     private String serverListenIp;
     private int serverPort;
     
     // each test will use one graph
     private String serverGraph;
     private int serverResetGraph;
     
     // if set to 'yes', in case of failure restartAttempts is tried 
     private String faultTolerant;
     // number of max retrials in case of failure
     private int restartAttempts;
    
     public Server() {
        this.serverListenIp = "0.0.0.0";
        this.serverPort = 8088;
     }
     
     public Server(String ipAddress, int port, String sshUsername) 
            throws WrongIpAddressException, WrongPortNumberException {
        super();
        this.setIpAddress(ipAddress);
        this.setPort(port);
        this.setSSHUsername(sshUsername);
        this.serverListenIp = "0.0.0.0";
        this.serverPort = 8088;
     }
     
     /**
      * 
      * @param ipAddress
      * @throws WrongIpAddressException 
      */
     public void setServerHTTPListenAddress(String ipAddress) 
             throws WrongIpAddressException { 
         if( Utils.validateIpAddress(ipAddress) )
               this.serverListenIp = ipAddress;
        else
            throw new WrongIpAddressException(ipAddress + " server listen IP address"
                    + " cannot be set due to validation errors");
     }
     
     /**
      * 
      * @return 
      */
     public String getServerHTTPListenAddress() { 
         return this.serverListenIp;
     }
     
     /**
      * 
      * @param httpPort
      * @throws WrongPortNumberException 
      */
    public void setServerHttpPort(int httpPort) throws WrongPortNumberException { 
        if( ! Utils.validateRemotePort(httpPort) )
            throw new WrongPortNumberException("Server http port number is "
                    + "not valid.");
        this.serverPort = httpPort;
    }
     
    /**
     * 
     * @return 
     */
    public int getServerHttpPort() { 
        return this.serverPort;
    }
    
    /**
     * 
     * @param faultTolerant 
     */
    public void setFaultTolerant(String faultTolerant) { 
        if( faultTolerant.length() == 0 || ! faultTolerant.equals("yes")) 
            this.faultTolerant = "no";
        else
            this.faultTolerant = "yes";
    }
    
    /**
     * 
     * @return 
     */
    public String getFaultTolerant() { 
        return this.faultTolerant;
    }
    
    /**
     * 
     * @param restartAttempts 
     */
    public void setRestartAttempts(int restartAttempts) { 
        this.restartAttempts = restartAttempts;
    }
    
    /**
     * 
     * @return 
     */
    public int getRestartAttempts() { 
        return this.restartAttempts;
    }
    
    /**
     * 
     * @param graph 
     */
    public void setGraph(String graph) { 
        this.serverGraph = graph;
    }
    
    /**
     * 
     * @return 
     */
    public String getGraph() { 
        return this.serverGraph;
    }
    
    /**
     * 
     * @param reset 
     */
    public void setGraphReset(int reset) { 
        this.serverResetGraph = reset;
    }
    
    /**
     * 
     * @return 
     */
    public int getGraphReset() { 
        return this.serverResetGraph;
    }
}