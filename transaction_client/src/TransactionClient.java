
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Random;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.DataOutputStream;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;


import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

// this must sent random transactions to the server
public class TransactionClient
{
	private String server_hostname;
	private int no_threads; 
	private int time_run_per_thread;
	private String input_filename;
        private Integer operation_type;
	private String graph;
        private int reset_flag;

	private ArrayList<Node[]> input_triples;
	private Thread[] client_threads;
	public int total_successful;

        final public static String SERVER_TRANSACTION_SERVLET="/transaction";
        final public static String SERVER_HANDLE_GRAPHS_SERVLET="/graph";

        public static final String FILENAME_SUFFIX_READY_WARMUP = "-client-ready-for-warmup";
        public static final String FILENAME_SUFFIX_START_SENDING_REQUESTS = "-start-sending-requests";
        public static final String FILENAME_SUFFIX_FINISHED = "-finished";
        
	class ClientThread extends Thread 
	{
                private int operation_type;
                private int no_op_per_t;
                private int retrials;

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

		public ClientThread(int operation_type, int no_op_per_t, int retrials) {
                        this.operation_type = operation_type;
                        this.no_op_per_t = no_op_per_t;
                        this.retrials = retrials;

                        this.collect_results = false;
			this.finished = false; 
			this.random_gen = new Random();
			this.size = input_triples.size();
                        this.total_run_trans = 0;
			this.successful_trans = 0;
			this.conflicted_trans = 0;
			this.aborted_trans = 0;
                        this.total_trans = 0;
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

		private String createAnUpdate(Integer linkFlag) { 
			int randomInt = random_gen.nextInt(this.size);
			Node[] random_node = input_triples.get(randomInt); 
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
			sb.append("<NEW_"+random_node[2]+">").append(",");
			sb.append(graph);
		         sb.append(",");
			// old value of the update
			sb.append("<"+random_node[2]+">").append(");");
			return sb.toString();
		}

                private String createAnInsert(Integer linkFlag) {
			int randomInt = random_gen.nextInt(this.size);
			Node[] random_node = input_triples.get(randomInt); 
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
			sb.append(graph).append(");");
			return sb.toString();
		}

                private String createADelete(Integer linkFlag) {
                        int randomInt = random_gen.nextInt(this.size);
                        Node[] random_node = input_triples.get(randomInt);
                        // create one insert ere
                        StringBuilder sb = new StringBuilder();
                        if( linkFlag != 0 )
                            sb.append("delete_link(");
                        else
                                sb.append("delete(");
                                sb.append("<"+random_node[0]+">").append(",");
                                sb.append("<"+random_node[1]+">").append(",");
                                sb.append("<"+random_node[2]+">").append(",");
                                sb.append(graph).append(");");
                        return sb.toString();
                }

                private String createAnCopyShallow(String graph_src, String graph_dest) {
                        int randomInt = random_gen.nextInt(this.size);
                        Node[] random_node = input_triples.get(randomInt);
                        // create one shallow copy here
                        StringBuilder sb = new StringBuilder();
                        sb.append("shallow_clone(");
                        sb.append("<"+random_node[0]+">").append(",");
                        sb.append(graph_src).append(",");
                        // new entity
                        sb.append("<NEW_"+random_node[0]+">").append(",");
                        sb.append(graph_dest);
                        sb.append(");");
                        return sb.toString();
                }

                private String createAnCopyDeep(String graph_src, String graph_dest) {
			int randomInt = random_gen.nextInt(this.size);
			Node[] random_node = input_triples.get(randomInt); 
			// create one deep copy 
			StringBuilder sb = new StringBuilder(); 
			sb.append("deep_clone(");
			sb.append("<"+random_node[0]+">").append(",");
			sb.append(graph_src).append(",");
			// new entity
			sb.append("<NEW_"+random_node[0]+">").append(",");
			sb.append(graph_dest);
			sb.append(");");
			return sb.toString();
		}

                private String createDeleteEntity(String graph_src) {
			int randomInt = random_gen.nextInt(this.size);
			Node[] random_node = input_triples.get(randomInt); 
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
                                                sb.append(createAnCopyShallow(graph, graph));
                                                break;
                                        case 7: // entity deep copy
                                                sb.append(createAnCopyDeep(graph, graph));
                                                break;
                                        case 8: // entity full delete
                                                sb.append(createDeleteEntity(graph));
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
			String urlParameters = "g="+graph+"&retries="+this.retrials+"&t="+transaction.toString();
			try {
				URL url = new URL(server_hostname+SERVER_TRANSACTION_SERVLET);
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

	public TransactionClient(String sta, int noth, int time_per_thread,
                String input_filename, Integer operation_type, String graph, 
                int reset_flag) {
		this.server_hostname = sta;
		this.no_threads = noth;
		this.time_run_per_thread = time_per_thread;
		this.input_filename = input_filename;
                this.operation_type = operation_type;
		this.graph = graph;
		this.input_triples = new ArrayList<Node[]>();
                this.reset_flag = reset_flag;
		init();
                dbinit();
	}
	
	// read the input file and populate the input_triples structure
	private void init() { 
		this.client_threads = new ClientThread[this.no_threads];
		File input_f = new File(this.input_filename); 
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
		}
	}
	
        private void dbinit() {
          // reset the graph if needed
            if( reset_flag == 1 ) {
                    System.out.println("Delete and (re)create the graph " + graph);
                    deleteGraph(graph);
                    createNewGraph(graph);
            }
        }
        
	public void startThreads(int operation_type, int no_oper, int no_retrials) {
		for(int i=0; i<this.no_threads; ++i)
			client_threads[i] = new ClientThread(
                                operation_type, no_oper, no_retrials);

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
        public void deleteGraph(String graph) {
                HttpURLConnection connection;
                try {
                        URL url = new URL(server_hostname+SERVER_HANDLE_GRAPHS_SERVLET+"?g="+graph+"&f=y");
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
                        //System.out.println(line);

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
                        URL url = new URL(server_hostname+SERVER_HANDLE_GRAPHS_SERVLET);
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
                        //System.out.println(line);

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
		if( args.length < 11 ) {
			System.err.println("Usage: \n" +
                                           "1. server http address \n"+
					   "2. no of threads \n" +
                                           "3. time to run per thread (sec) \n" +
                                           "4. warm-up period (sec) \n" +
					   "5. input nt file \n" +
                                           "6. graph name \n" +
                                           "7. operation type (0:insert, 1:insert_link, 2:update, 3:update_link" +
                                           ", 4:delete, 5:delete_link, 6:entity_shallow_copy, 7:entity_deep_copy," +
                                           " 8:entity_full_delete) \n" +
                                           "8. number of operations to run per transaction \n" +
                                           "9. reset graph (0:do nothing, 1:delete&create)\n" +
                                           "10. number of retrials (if transaction conficts)\n" + 
                                           "11. distributed mode flag (yes/no); NOTE: if yes, the following parameters are used\n" +
                                           "12. working directory (needed to run remote ssh commands)\n" +
                                           "13. client ID (also used by reomte ssh commands)");
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

		String server_http_address = args[0]; 
		int no_threads =  ( Integer.valueOf(args[1]) < 1 ) ? 1 : Integer.valueOf(args[1]);
		int time_run_per_th = ( Integer.valueOf(args[2]) < 1 ) ? 1 : Integer.valueOf(args[2]);
                // after this timeout the results are gathered 
                int warmup_period = Integer.valueOf(args[3]);
		String input_nt_file = args[4];
		String graph_name = args[5];
                int operation_type = Integer.valueOf(args[6]);
		int no_operations_per_transaction = ( Integer.valueOf(args[7]) < 1 ) ? 1 : Integer.valueOf(args[7]);
                int reset_flag = Integer.valueOf(args[8]);
                int no_retrials = Integer.valueOf(args[9]);
                String distr_flag = args[10]; 
                String operation_name = getOperationName(operation_type);
                if( operation_name == null ) {
                        System.out.println("[ERROR] Please pass a correct operation type. See the 'usage'!");
                        return;
                }
		System.out.println("Create transactions with " + no_operations_per_transaction + " " + operation_name + " per T");
                
                String working_dir="";
                String client_id="-1";
                if( distr_flag !=null && ! distr_flag.isEmpty() && distr_flag.equals("yes") ) {
                    System.out.println("Distributed mode is on, thus use synch mechanism ... ");
                    working_dir = args[11];
                    client_id = args[12];
                }
                
                // create and init the client 
                TransactionClient tc = new TransactionClient(server_http_address, no_threads, time_run_per_th,
                                                                input_nt_file, operation_type, graph_name, reset_flag);

                if( distr_flag !=null && ! distr_flag.isEmpty() && distr_flag.equals("yes") ) {
                    // initializations are done, so create the local msg
                    String ready_warmup_filename = working_dir+"/"+client_id+FILENAME_SUFFIX_READY_WARMUP;
                    new File(ready_warmup_filename).createNewFile();
                    System.out.println("Client ready for warmup and sending requests ...");
                
                    // now, we wait until the coordinator creates another local file
                    String read_to_start_filename = working_dir+"/"+client_id+FILENAME_SUFFIX_START_SENDING_REQUESTS;
                    while( ! new File(read_to_start_filename).exists() ) {
                        Thread.sleep(50);
                    }
                    System.out.println("Warmup begins now and in " + warmup_period + "sec statistics will start to be gathered ...");
                }
                
                // start threads to send 'operation_type' transaction for no_op.. times
		tc.startThreads(operation_type, no_operations_per_transaction, no_retrials);
                long start_time = 0;
                // wait to warmup before signaling the other threads to go ahead
                try {
                        System.out.println("Threads have been started, but wait " + warmup_period
                                + " seconds to warmup ... ");
                        for( int i=0; i<warmup_period; ++i ) {
                            Thread.sleep(1000);
                            System.out.print("warmup ");
                        }
                        // now its time to start collecting data
                        System.out.println("Signal threads that now they can record statistical data");
                        tc.startCollectingResults();
                        start_time = System.currentTimeMillis();
                        // now wait the given period for the threads to run transactions before joining them
                        System.out.println("Leave threads to send transactions for " + time_run_per_th + " seconds ... ");
                        for( int i=0; i<time_run_per_th; i=i+5) {
                            System.out.print((time_run_per_th-i)+"s ");
                            if( i+5 > time_run_per_th ) 
                                Thread.sleep(time_run_per_th-i * 1000);
                            else
                                Thread.sleep(i * 1000);
                        }
                } catch (InterruptedException ex) {
                        ex.printStackTrace();
                }
                // stop all the trans threads here
                System.out.println("\nStop threads and gather statistics ");
		tc.joinThreads();

		long total_time = System.currentTimeMillis()-start_time;
                int total_trans = tc.getTotalTransactionsSent();
		double s_rate = tc.getSuccessfulRate(total_trans);
		int conflicts = tc.getTotalConflicts();
		int aborted = tc.getTotalAborted();

		System.out.println("Time time needed for sending " + total_trans + " transactions " + total_time +"ms");
		System.out.println(" ... " + no_operations_per_transaction + " no of " + operation_name + " ran per transaction ");
		System.out.println(" ... with a successful rate of " + s_rate + "%   =>  transaction rate of " + ((double)tc.total_successful/total_time*1000) + "tx/sec" );
		System.out.println(" ... " + conflicts + " total number of conflicts; 1 unit means one certain transaction was restarted");
		System.out.println(" ... " + aborted + " total number of aborted transactions");
		System.out.println(" ... after " + no_retrials + " of retrials a transaction was aborted!");
                
                if( distr_flag !=null && ! distr_flag.isEmpty() && distr_flag.equals("yes") ) {
                    // as the threads are finished, signal it again with a new empty file
                    String ready_filename = working_dir+"/"+client_id+FILENAME_SUFFIX_FINISHED;
                    new File(ready_filename).createNewFile();
                }
	}
}
