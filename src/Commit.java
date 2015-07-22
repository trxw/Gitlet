 import java.util.*; 
 import java.io.*;
 
	public class Commit implements Serializable{
		// commit must track its parents files not copy them...
		// if the file is 
		

		HashMap<String, String> fileToLocation; 
		boolean isSplitPoint;   // true if it is a split point else false
		String ID;            // contains the Id of the commit object
		String Message;
		String Time;           // string that keeps track of the commit time for this node
		Commit prevCommit;
		
		
		public Commit(String s, Commit C ){
			fileToLocation =new HashMap<String, String>();
			 Date time = new Date();
		       // display time and date using toString()
		      Time= time.toString();	
		      ID = UUID.randomUUID().toString();
		      Message=s;
		      prevCommit=C;
		     		      
		}
		
		public void updateHashMap(String fileName){
			fileToLocation.remove(fileName);
			
		}
		
		
	}

