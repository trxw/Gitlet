package proj2;

import java.util.Arrays;




public class Gitlet {

	public void Gitlet(){
		Commit Head;
		
		
	}
	
	static void commit(String sArr[]){
		
	}
	
	static void add(String sArr[]){
		
	}

	static void find(String sArr[]){
		
	}
	
	static void init(){
		
	}
	
	static void rm(String sArr[]){
		
	}	

	
	static void log(){
		
	}
	
	static void global_log(){
		
	}
	

	static void status(){
		
	}
	
	
	static void branch(String sArr[]){
		
	}
	
	static void checkout(String sArr[]){
		
	}
	static void merge(String sArr[]){
		
	}

	static void rebase(String sArr[]){
		
	}
	
	/*
	 * Checks whether gitlet is initiated before.
	 */
	boolean gitletExist(){
		return false;
	}

	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Gitlet g = new Gitlet();
		
		int length = args.length;
		
		if(length==0){
			System.out.println("Please enter a command.");
		}
		else if(length==1){
			if(args[0].equals("init")){
				init();
			}	
			else if(args[0].equals("log")){
				log();
			}
			
			else if(args[0].equals("global-log")){
				global_log();
			}
			else if(args[0].equals("status")){
				status();
			}

		} 
		
		else if(length>1){
			if(args[0].equals("add")){
				add(Arrays.copyOfRange(args, 1, length));
			
			}
			
			else if(args[0].equals("commit")){
				commit(Arrays.copyOfRange(args, 1, length));
			}
			
			else if(args[0].equals("rm")){
				rm(Arrays.copyOfRange(args, 1, length));
			}
			
			
			else if(args[0].equals("find")){
				find(Arrays.copyOfRange(args, 1, length));
			}
			

			else if(args[0].equals("checkout")){
				checkout(Arrays.copyOfRange(args, 1, length));
			}
			
			else if(args[0].equals("branch")){
				branch(Arrays.copyOfRange(args, 1, length));
			}
			
			else if(args[0].equals("merge")){
				merge(Arrays.copyOfRange(args, 1, length));
			}
			
			else if(args[0].equals("rebase")){
				rebase(Arrays.copyOfRange(args, 1, length));
			}
			
			
		}
		
		else
			System.out.println("No command with that name exists.");
		}
		
		
}


