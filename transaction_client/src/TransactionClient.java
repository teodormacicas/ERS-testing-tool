
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.DataOutputStream;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;


import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Variable;

// this must sent random transactions to the server
public class TransactionClient
{
	private String input_filename;
        private String server_http_address;
        private String graph_name;
        private int reset_flag;
        private int snapshot_flag;
        private String read_cons;
        private String write_cons;
        private String trans_lock_gran;
        private int repl_factor;
        private int no_threads;
        private int time_run_per_th;
        // after this timeout the results are gathered 
        private int warmup_period;
        private int operation_type;
        private int no_operations_per_transaction;
        private int no_retrials;
        private String distr_flag;
        private String operation_name;
        private String working_dir;
        private String client_id;
        
        private String conflictFlag;
        private int numDiffEnt; 
        private int numDiffPropPerEnt; 
        private int numDiffValuePerProp;
   
	private ArrayList<Node[]> input_triples;
	private Thread[] client_threads;
	public int total_successful;

        final public static String SERVER_TRANSACTION_SERVLET="/transaction";
        final public static String SERVER_HANDLE_GRAPHS_SERVLET="/graph";
        final public static String SERVER_SETUP_SERVLET="/setup";

        public static final String FILENAME_SUFFIX_READY_WARMUP = "-client-ready-for-warmup";
        public static final String FILENAME_SUFFIX_START_SENDING_REQUESTS = "-start-sending-requests";
        public static final String FILENAME_SUFFIX_FINISHED = "-finished";
        
	class ClientThread extends Thread 
	{
                private int operation_type;
                private int no_op_per_t;
                private int retrials;
                private int thread_id;

                // this flag will be set to true after the warmup period
                private boolean collect_results;
                private long start_collection_res_time;
		private boolean finished;
                private long stop_time;
		private Random random_gen;
		private int size;
                private int total_run_trans;
		private int successful_trans;
		private int conflicted_trans;
		private int aborted_trans;
                private int total_trans;
		
		private HttpURLConnection connection;
                
                private final int PperE = 20;
                private final int VperP = 5;
                
                // used to create non conflicting data when conflict flag is set to 'no'
                private int counter_e;
                private int counter_p;
                private int counter_v;

		public ClientThread(int operation_type, int no_op_per_t, int retrials, 
                        int thread_id) {
                        this.operation_type = operation_type;
                        this.no_op_per_t = no_op_per_t;
                        this.retrials = retrials;
                        this.thread_id = thread_id;

                        this.collect_results = false;
			this.finished = false; 
			this.random_gen = new Random();
			this.size = input_triples.size();
                        this.total_run_trans = 0;
			this.successful_trans = 0;
			this.conflicted_trans = 0;
			this.aborted_trans = 0;
                        this.total_trans = 0;
                        
                        this.counter_e = 0;
                        this.counter_p = 0;
                        this.counter_v = 0;
		}

		public int getSuccessfulTrans() {
			return this.successful_trans;
		}
	
		public int getConflictedTrans() {
			return this.conflicted_trans;
		}
		
		public int getAbortedTrans() {
			return this.aborted_trans;
		}

                public int getTotalTrans() {
                    return this.total_trans;
                }
			
		public void stopSendingTrans() {
			this.finished = true;
                        this.stop_time = System.currentTimeMillis();
		}

                public void startCollectingResults() {
                        this.collect_results = true;
                        this.start_collection_res_time = System.currentTimeMillis();
                }

                private Node[] getRandomNode(boolean deleteFullEntity) { 
                    String randomE, randomP, randomV;
                    Node[] n = new Node[3];
                    if( conflictFlag.equals("yes") ) {
                        randomE = String.valueOf(random_gen.nextInt(numDiffEnt));
                        randomP = String.valueOf(random_gen.nextInt(numDiffPropPerEnt));
                        randomV = String.valueOf(random_gen.nextInt(numDiffValuePerProp));
                    }
                    else { 
                        if( deleteFullEntity ) 
                            ++counter_e;
                        else {
                            if( ++counter_v > VperP ) {
                                counter_v = 0; 
                                if( ++counter_p > PperE ) {
                                    counter_p = 0;
                                    counter_e++;
                                }
                            }
                        }
                        randomE = client_id + "-" + thread_id + "-" + counter_e;
                        randomP = client_id + "-" + thread_id + "-" + counter_e
                                + "-" + counter_p;
                        randomV = client_id + "-" + thread_id + "-" + counter_e
                                + "-" + counter_p + "-" + counter_v;
                    }
                    n[0] = new Variable("eeeeeeeeeeeeeeeeeeeeee"+randomE);
                    n[1] = new Variable("pppppppppppppppppppppp"+randomP);
                    n[2] = new Variable("vvvvvvvvvvvvvvvvvvvvvv"+randomV);
                    //return input_triples.get(randomInt); 
                    return n;
                }
                
                private String createAnInsert(Integer linkFlag) {
			//Node[] random_node = input_triples.get(randomInt); 
                        Node[] random_node = getRandomNode(false); 
			// create one insert ere
			StringBuilder sb = new StringBuilder(); 
        		 if( linkFlag != 0 ) 
		            sb.append("insert_link(");
		         else
                            sb.append("insert(");
			sb.append("<"+random_node[0]+">").append(",");
			sb.append("<"+random_node[1]+">").append(",");
			// new value
			sb.append("<"+random_node[2]+">").append(",");
			sb.append(graph_name).append(");");
			return sb.toString();
		}
                
		private String createAnUpdate(Integer linkFlag) { 
                        Node[] random_node = getRandomNode(false);
			// create one update here
			StringBuilder sb = new StringBuilder(); 
		         if( linkFlag != 0 ) 
		            sb.append("update_link(");
		         else
                            sb.append("update(");
			sb.append("<"+random_node[0]+">").append(",");
			sb.append("<"+random_node[1]+">").append(",");
			// new value
			//sb.append("<"+random_node[2]+">").append(",");
			sb.append("<"+random_node[2]+">").append(",");
			sb.append(graph_name);
		        sb.append(",");
			// old value of the update
			sb.append("<"+random_node[2]+">").append(");");
			return sb.toString();
		}

                private String createADelete(Integer linkFlag) {
                        Node[] random_node = getRandomNode(false);
                        // create one insert ere
                        StringBuilder sb = new StringBuilder();
                        if( linkFlag != 0 )
                            sb.append("delete_link(");
                        else
                                sb.append("delete(");
                        sb.append("<"+random_node[0]+">").append(",");
                        sb.append("<"+random_node[1]+">").append(",");
                        sb.append("<"+random_node[2]+">").append(",");
                        sb.append(graph_name).append(");");
                        return sb.toString();
                }

                private String createAnCopyShallow(String graph_src, String graph_dest) {
                        Node[] random_node = getRandomNode(false);
                        // create one shallow copy here
                        StringBuilder sb = new StringBuilder();
                        sb.append("shallow_clone(");
                        sb.append("<"+random_node[0]+">").append(",");
                        sb.append(graph_src).append(",");
                        // new entity
                        sb.append("<NEW_"+random_node[2]+">").append(",");
                        sb.append(graph_dest);
                        sb.append(");");
                        return sb.toString();
                }

                private String createAnCopyDeep(String graph_src, String graph_dest) {
			Node[] random_node = getRandomNode(false);
			// create one deep copy 
			StringBuilder sb = new StringBuilder(); 
			sb.append("deep_clone(");
			sb.append("<"+random_node[0]+">").append(",");
			sb.append(graph_src).append(",");
			// new entity
			sb.append("<NEW_"+random_node[2]+">").append(",");
			sb.append(graph_dest);
			sb.append(");");
			return sb.toString();
		}

                private String createDeleteEntity(String graph_src) {
			Node[] random_node = getRandomNode(true);
			// create one deep copy 
			StringBuilder sb = new StringBuilder(); 
			sb.append("delete_all(");
			sb.append("<"+random_node[0]+">").append(",");
			sb.append(graph_src);
			sb.append(");");
			return sb.toString();
		}

                private StringBuilder createTransaction() {
                        StringBuilder sb = new StringBuilder();
			sb.append("BEGIN;");
			for( int i=0; i<no_op_per_t; ++i ) {
                                switch( operation_type ) {
                                        case 0: //insert
                                                sb.append(createAnInsert(0));
                                                break;
                                        case 1: // insert link
                                                sb.append(createAnInsert(1));
                                                break;
                                        case 2: // update
                                                sb.append(createAnUpdate(0));
                                                break;
                                        case 3: // update link
                                                sb.append(createAnUpdate(1));
                                                break;
                                        case 4: // delete
                                                sb.append(createADelete(0));
                                                break;
                                        case 5:
                                                sb.append(createADelete(1));
                                                break;
                                        case 6: // entity shallow copy
                                                sb.append(createAnCopyShallow(graph_name, 
                                                        graph_name));
                                                break;
                                        case 7: // entity deep copy
                                                sb.append(createAnCopyDeep(graph_name, 
                                                        graph_name));
                                                break;
                                        case 8: // entity full delete
                                                sb.append(createDeleteEntity(graph_name));
                                                break;
                                        default:
                                                break;
                                }
		        }
			sb.append("COMMIT;");
                        //System.out.println("TRANSACTION: ");
         		//System.out.println(sb.toString());
                        return sb;
                }

		private void sendTransaction() { 
			StringBuilder transaction = createTransaction();
			String urlParameters = "g="+graph_name+"&retries="+this.retrials+"&t="+transaction.toString();
			try {
                                URL url =  new URL(server_http_address+SERVER_TRANSACTION_SERVLET);
				this.connection = (HttpURLConnection) url.openConnection();           
				this.connection.setDoOutput(true);
				this.connection.setDoInput(true);
				this.connection.setInstanceFollowRedirects(false); 
				this.connection.setRequestMethod("POST"); 
				this.connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
				this.connection.setRequestProperty("charset", "utf-8");
				this.connection.setUseCaches (false);

				this.connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
				DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
				wr.writeBytes(urlParameters);
				wr.flush();

				String line;
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				line = reader.readLine();
				//System.out.println(line);
                                if( collect_results ) {
                                        if( line.equals("0") )
                                                successful_trans++;
                                        else if( line.equals("-1") )
                                                aborted_trans++;
                                        else {
                                                conflicted_trans += Integer.valueOf(line);
                                                // even if there were conflicts, this T was run successfully at the end
                                                successful_trans++;
                                        }
                                        this.total_trans++;
                                }
				wr.close();
				reader.close();         
				this.connection.disconnect(); 
			} catch( MalformedURLException ex ) { 
				ex.printStackTrace();
			} catch( IOException ex ) { 
				ex.printStackTrace();
			}
		}

		public void run() { 
			while( ! this.finished ) {
				//send here a transaction to server
				sendTransaction();
			} 
		}
	}

        public TransactionClient() {
            this.input_triples = new ArrayList<Node[]>();
        }
	
	// read the input file and populate the input_triples structure
	public void init() { 
		this.client_threads = new ClientThread[this.no_threads];
		
                /*File input_f = new File(this.input_filename); 
		if( ! input_f.exists() ) { 
			System.err.println("Input file " + input_filename + " does not exist!");
			System.exit(2); 
		}
		// load the triples file in memory
		try { 
                    FileInputStream fis = new FileInputStream(input_f);
                    Iterator<Node[]> nxp = new NxParser(fis);
                    while (nxp.hasNext()) {
                            boolean skip = false;
                            Node[] nx = nxp.next();
                            // filter here the triples that contain 'white_spaces' in one of the param
                            // reason: our primitive transaction engine do not accept this 
                            for( int j=0; j<nx.length; ++j ) { 
                                    String n = nx[j].toString(); 
                                    if( n.contains(" ") ) {
                                            skip = true;
                                            break;
                                    }
                            }
                            if( skip ) 
                                    continue;
                            this.input_triples.add(nx);
                    }
		} catch( FileNotFoundException ex) { 
			ex.printStackTrace(); 
			System.exit(3); 
		}*/
	}
	
        public void dbinit() {
            // change consistency
            changeReadWriteConsistency(read_cons, write_cons);
            // change transaction locking granularity 
            changeTransactionLockingGranularity(trans_lock_gran);
            // also change replication facotr 
            changeReplicationFactor(repl_factor);
            
            // reset the graph if needed (only for insert)
            if( reset_flag != 0 && operation_type == 0 ) {
                    System.out.println("Truncate the graph " + graph_name);
                    deleteGraph(graph_name, false);
            }
            System.out.println("Create the graph " + graph_name); 
            createNewGraph(graph_name);
        }
        
        public void dbclear() { 
            // if truncate was used so far and reset flag is set 
            if( reset_flag == 2 && operation_type ==  0) {
                    System.out.println("Delete the graph " + graph_name);
                    deleteGraph(graph_name, true);
                    
                    System.out.println("Now sleep 5s to leave some time for deletion ... ");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TransactionClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
        }
        
	public void startThreads() {
		for(int i=0; i<this.no_threads; ++i)
			client_threads[i] = new ClientThread(
                                operation_type, no_operations_per_transaction, 
                                no_retrials, i);

		for(int i=0; i<this.no_threads; ++i)
			client_threads[i].start();
	}

        public int getTotalTransactionsSent() {
            int counter = 0;
            for(int i=0; i<this.no_threads; ++i) {
                    counter += ((ClientThread)client_threads[i]).getTotalTrans();
            }
            return counter;
        }

	public double getSuccessfulRate(int total_tx) { 
		for(int i=0; i<this.no_threads; ++i)
			total_successful+=((ClientThread)client_threads[i]).getSuccessfulTrans();
		return ((double)total_successful/total_tx)*100;
	}

	public int getTotalConflicts() { 
		int r = 0; 
		for(int i=0; i<this.no_threads; ++i)
			r+=((ClientThread)client_threads[i]).getConflictedTrans();
		return r;
	}

	public int getTotalAborted() { 
		int r = 0; 
		for(int i=0; i<this.no_threads; ++i)
			r+=((ClientThread)client_threads[i]).getAbortedTrans();
		return r;
	}

	public void joinThreads() { 
		for(int i=0; i<this.no_threads; ++i) {
			((ClientThread)client_threads[i]).stopSendingTrans();
                }
                try {
                        for(int i=0; i<this.no_threads; ++i)
                                client_threads[i].join();
                } catch( InterruptedException ex ) {
                        ex.printStackTrace();
                }
	}

        public void startCollectingResults() {
		for(int i=0; i<this.no_threads; ++i) {
			((ClientThread)client_threads[i]).startCollectingResults();
                }
	}

        // delete an existing graph
        public void deleteGraph(String graph, boolean cleanup) {
                HttpURLConnection connection;
                try {
                        URL url; 
                        if( cleanup )
                            url = new URL(server_http_address+SERVER_HANDLE_GRAPHS_SERVLET+"?g="+graph+"&f=y");
                        else 
                            url = new URL(server_http_address+SERVER_HANDLE_GRAPHS_SERVLET+"?g="+graph+"&f=y&truncate");
                        
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setDoOutput(true);
                        connection.setDoInput(true);
                        connection.setInstanceFollowRedirects(false);
                        connection.setRequestMethod("DELETE");
                        connection.setRequestProperty("charset", "utf-8");
                        connection.setUseCaches (false);

                        String line;
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        line = reader.readLine();
                        System.out.println(line);
                        reader.close();

                        connection.disconnect();
                } catch( MalformedURLException ex ) {
                        ex.printStackTrace();
                } catch( IOException ex ) {
                        ex.printStackTrace();
                }
        }

        public void createNewGraph(String graph) {
                HttpURLConnection connection;
                String urlParameters = "g_id="+graph+"&g_p=<createdBy>&g_v=\"transaction_client\"";
                try {
                        URL url = new URL(server_http_address+SERVER_HANDLE_GRAPHS_SERVLET);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setDoOutput(true);
                        connection.setDoInput(true);
                        connection.setInstanceFollowRedirects(false);
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        connection.setRequestProperty("charset", "utf-8");
                        connection.setUseCaches (false);
                        connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
                        
                        DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
                        wr.writeBytes(urlParameters);
                        wr.flush();
                        wr.close();

                        String line;
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        line = reader.readLine();
                        System.out.println("Create new graph: " + line);

                        connection.disconnect();
                } catch( MalformedURLException ex ) {
                        ex.printStackTrace();
                } catch( IOException ex ) {
                        ex.printStackTrace();
                }
        }
        
        public void changeTransactionLockingGranularity(String transLockGran) {
                HttpURLConnection connection;
                String urlParameters = "trans_lock_gran="+transLockGran;
                try {
                        URL url = new URL(server_http_address+SERVER_SETUP_SERVLET);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setDoOutput(true);
                        connection.setDoInput(true);
                        connection.setInstanceFollowRedirects(false);
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        connection.setRequestProperty("charset", "utf-8");
                        connection.setUseCaches (false);
                        connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
                        
                        DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
                        wr.writeBytes(urlParameters);
                        wr.flush();
                        wr.close();

                        String line;
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        line = reader.readLine();
                        System.out.println("Change read and write consistency levels: "+line);
                       
                        connection.disconnect();
                } catch( MalformedURLException ex ) {
                        ex.printStackTrace();
                } catch( IOException ex ) {
                        ex.printStackTrace();
                }
        }

        public void changeReadWriteConsistency(String readCons, String writeCons) {
                HttpURLConnection connection;
                String urlParameters = "read_cons="+readCons+"&write_cons="+writeCons;
                try {
                        URL url = new URL(server_http_address+SERVER_SETUP_SERVLET);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setDoOutput(true);
                        connection.setDoInput(true);
                        connection.setInstanceFollowRedirects(false);
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        connection.setRequestProperty("charset", "utf-8");
                        connection.setUseCaches (false);
                        connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
                        
                        DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
                        wr.writeBytes(urlParameters);
                        wr.flush();
                        wr.close();

                        String line;
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        line = reader.readLine();
                        System.out.println("Change read and write consistency levels: "+line);
                       
                        connection.disconnect();
                } catch( MalformedURLException ex ) {
                        ex.printStackTrace();
                } catch( IOException ex ) {
                        ex.printStackTrace();
                }
        }
        
        public void changeReplicationFactor(int replFactor) {
                HttpURLConnection connection;
                String urlParameters = "repl_factor="+replFactor;
                try {
                        URL url = new URL(server_http_address+SERVER_SETUP_SERVLET);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setDoOutput(true);
                        connection.setDoInput(true);
                        connection.setInstanceFollowRedirects(false);
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        connection.setRequestProperty("charset", "utf-8");
                        connection.setUseCaches (false);
                        connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
                        
                        DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
                        wr.writeBytes(urlParameters);
                        wr.flush();
                        wr.close();

                        String line;
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        line = reader.readLine();
                        System.out.println("Change read and write consistency levels: "+line);
                       
                        connection.disconnect();
                } catch( MalformedURLException ex ) {
                        ex.printStackTrace();
                } catch( IOException ex ) {
                        ex.printStackTrace();
                }
        }
        
        private static String getOperationName(Integer operation_type) {
                String operation_name;
                switch( operation_type ) {
                        case 0:
                            operation_name = "insert";
                            break;
                        case 1:
                            operation_name = "insert link";
                            break;
                        case 2:
                            operation_name = "update";
                            break;
                        case 3:
                            operation_name = "update link";
                            break;
                        case 4:
                            operation_name = "delete";
                            break;
                        case 5:
                            operation_name = "delete link";
                            break;
                        case 6:
                            operation_name = "entity shallow copy";
                            break;
                        case 7:
                            operation_name = "entity deep copy";
                            break;
                        case 8:
                            operation_name = "entity full delete";
                            break;
                        default:
                            operation_name = null;
                            break;
                }
                return operation_name;
        }

	public static void main(String[] args) throws IOException, InterruptedException { 
		if( args.length < 21 ) {
			System.err.println("Usage: \n" +
                                           "1. server http address \n"+
                                           "2. graph name \n" +
                                           "3. reset graph (0:do nothing, 1:delete&create)\n" +
                                           "4. snashot graph (0:do nothing, 1:delete&create)\n" +
                                           "5. read consistency (one, two, three, quorum, all)\n" + 
                                           "6. write consistency (one, two, three, quorum, all, any)\n" +
                                           "7. transactional locking granularity (e, ep, epv)\n" + 
                                           "8. replication factor \n"+
					   "9. no of threads \n" +
                                           "10. time to run per thread (sec) \n" +
                                           "11. warm-up period (sec) \n" +
                                           "12. operation type (0:insert, 1:insert_link, 2:update, 3:update_link" +
                                           ", 4:delete, 5:delete_link, 6:entity_shallow_copy, 7:entity_deep_copy," +
                                           " 8:entity_full_delete) \n" +
                                           "13. number of operations to run per transaction \n" +
                                           "14. number of retrials (if transaction conficts)\n" + 
                                           "15. distributed mode flag (yes/no); NOTE: if yes, the following parameters are used\n" +
                                           "16. working directory (needed to run remote ssh commands)\n" +
                                           "17. client ID (also used by reomte ssh commands)\n" +
                                           "18. conflicts flag (if no, the following params are not used)\n" +  
                                           "19. number different entities \n" +
                                           "20. number different properties per entity \n" +
                                           "21. number different values per property \n" +
                                           "22. INIT FLAG (at most 1client must use this flag; it resets consistency, graph and others)");
			System.exit(1);
		}
                //IMPORTANT FOR TESTING TOOL; DO NOT DELETE!
                String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
                System.out.println("PID: "+PID);
                System.out.flush();
                
		System.out.print("Arguments passed: ");
		for( int i=0; i<args.length; ++i )
			System.out.print(args[i] + " "); 
		System.out.println("");

                TransactionClient tc = new TransactionClient();
		tc.server_http_address = args[0]; 
                if( tc.server_http_address.startsWith("http://134.21.")) { 
                    tc.server_http_address = new String(tc.server_http_address+"/ers");
                }
                tc.graph_name = args[1];
                tc.reset_flag = Integer.valueOf(args[2]);
                tc.snapshot_flag = Integer.valueOf(args[3]);
                
                tc.read_cons = args[4];
                tc.write_cons = args[5];
                tc.trans_lock_gran = args[6];
                tc.repl_factor = Integer.valueOf(args[7]);
                
		tc.no_threads =  ( Integer.valueOf(args[8]) < 1 ) ? 1 : Integer.valueOf(args[8]);
		tc.time_run_per_th = ( Integer.valueOf(args[9]) < 1 ) ? 1 : Integer.valueOf(args[9]);
                // after this timeout the results are gathered 
                tc.warmup_period = Integer.valueOf(args[10]);
		
                tc.operation_type = Integer.valueOf(args[11]);
		tc.no_operations_per_transaction = ( Integer.valueOf(args[12]) < 1 ) ? 1 : Integer.valueOf(args[12]);
                
                tc.no_retrials = Integer.valueOf(args[13]);
                tc.distr_flag = args[14]; 
                tc.operation_name = getOperationName(tc.operation_type);
                if( tc.operation_name == null ) {
                        System.out.println("[ERROR] Please pass a correct operation type. See the 'usage'!");
                        return;
                }
		System.out.println("Create transactions with " + tc.no_operations_per_transaction 
                        + " " + tc.operation_name + " per T");
                
                tc.working_dir="";
                tc.client_id="-1";
                if( tc.distr_flag !=null && ! tc.distr_flag.isEmpty() && tc.distr_flag.equals("yes") ) {
                    System.out.println("Distributed mode is on, thus use synch mechanism ... ");
                    tc.working_dir = args[15];
                    tc.client_id = args[16];
                }
                tc.conflictFlag = args[17];
                tc.numDiffEnt = ( Integer.valueOf(args[18]) < 1 ) ? 1 : Integer.valueOf(args[18]);
                tc.numDiffPropPerEnt = ( Integer.valueOf(args[19]) < 1 ) ? 1 : Integer.valueOf(args[19]);
                tc.numDiffValuePerProp = ( Integer.valueOf(args[20]) < 1 ) ? 1 : Integer.valueOf(args[20]);
                
                tc.init();
                // does this client initialize/setup the DB?
                if( args[21] != null && args[21].equals("yes") ) { 
                    tc.dbinit();
                }

                if( tc.distr_flag !=null && ! tc.distr_flag.isEmpty() && tc.distr_flag.equals("yes") ) {
                    // initializations are done, so create the local msg
                    String ready_warmup_filename = tc.working_dir+"/"+tc.client_id+FILENAME_SUFFIX_READY_WARMUP;
                    new File(ready_warmup_filename).createNewFile();
                    System.out.println("Client ready for warmup and sending requests ...");
                
                    // now, we wait until the coordinator creates another local file
                    String read_to_start_filename = tc.working_dir+"/"+tc.client_id+
                            FILENAME_SUFFIX_START_SENDING_REQUESTS;
                    while( ! new File(read_to_start_filename).exists() ) {
                        Thread.sleep(50);
                    }
                    System.out.println("Warmup begins now and in " + tc.warmup_period 
                            + "sec statistics will start to be gathered ...");
                }
                
                // start threads to send 'operation_type' transaction for no_op.. times
		tc.startThreads();
                long start_time = 0;
                
                // wait to warmup before signaling the other threads to go ahead
                try {
                        System.out.println("Threads have been started, but wait " + tc.warmup_period
                                + " seconds to warmup ... ");
                        for( int i=0; i<tc.warmup_period; ++i ) {
                            Thread.sleep(1000);
                            System.out.print("warmup ");
                        }
                        // now its time to start collecting data
                        System.out.println("Signal threads that now they can record statistical data");
                        tc.startCollectingResults();
                        start_time = System.currentTimeMillis();
                        // now wait the given period for the threads to run transactions before joining them
                        System.out.println("Leave threads to send transactions for " 
                                + tc.time_run_per_th + " seconds ... ");
                        for( int i=0; i<tc.time_run_per_th; i=i+5) {
                            System.out.print((tc.time_run_per_th-i)+"s ");
                            if( i+5 > tc.time_run_per_th ) 
                                Thread.sleep(tc.time_run_per_th-i * 1000);
                            else
                                Thread.sleep(5000);
                        }
                } catch (InterruptedException ex) {
                        ex.printStackTrace();
                }
                // stop all the trans threads here
                System.out.println("\nStop threads and gather statistics ");
		tc.joinThreads();

                if( args[21] != null && args[21].equals("yes") ) { 
                    tc.dbclear();
                }
                
		long total_time = System.currentTimeMillis()-start_time;
                int total_trans = tc.getTotalTransactionsSent();
		double s_rate = tc.getSuccessfulRate(total_trans);
		int conflicts = tc.getTotalConflicts();
		int aborted = tc.getTotalAborted();

		System.out.println("Time time needed for sending " + total_trans + " transactions " + total_time +"ms");
		System.out.println(" ... " + tc.no_operations_per_transaction + " no of " + tc.operation_name + " ran per transaction ");
		System.out.println(" ... with a successful rate of " + s_rate + "%   =>  transaction rate of " + ((double)tc.total_successful/total_time*1000) + "tx/sec" );
		System.out.println(" ... " + conflicts + " total number of conflicts; 1 unit means one certain transaction was restarted");
		System.out.println(" ... " + aborted + " total number of aborted transactions");
		System.out.println(" ... after " + tc.no_retrials + " of retrials a transaction was aborted!");
                
                System.out.println();
                System.out.println("No threads,Total time, total trans, conflicts, aborted, successful rate/sec, no retrials");
                System.out.println(tc.no_threads + " " + total_time + " " + total_trans + " " + conflicts + " " + aborted + " " +
                        ((double)tc.total_successful/total_time*1000) + " "  + tc.no_retrials);
                
                if( tc.distr_flag !=null && ! tc.distr_flag.isEmpty() && tc.distr_flag.equals("yes") ) {
                    // as the threads are finished, signal it again with a new empty file
                    String ready_filename = tc.working_dir+"/"+tc.client_id+FILENAME_SUFFIX_FINISHED;
                    new File(ready_filename).createNewFile();
                }
	}
}
