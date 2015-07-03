package com.rafcarl.lifecycle;



public class Flags {
//	public static int FIRST_RUN = 1;
	private static int deleteToggle = 0;
	private static int MIN = 3;
	private static int MAX = 10;
	public static int emergencyContacts;
	public static int pos = 1;
	
	public static final String FIRST_RUN= "FIRST_RUN";
	public static final String EMERGENCY_CONTACTS= "EMERGENCY_CONTACTS";
	public static final String POS = "POS";
	public static final String CONTACT_COUNT = "CONTACT_COUNT";
	
	public Flags() {
	
	}
	
//	public static void setFirstRun(int x){
//		Flags.FIRST_RUN = x;
//	}
//	
//	public static int getFirstRun(){
//		return Flags.FIRST_RUN;
//	}
	
	public static void toggleDeleteState(){
		if(getToggleState() == 0){
			Flags.deleteToggle = 1;
		}
		else{
			Flags.deleteToggle = 0;
		}
	}
	
	public static int getToggleState(){
		return Flags.deleteToggle;
	}
	
	public static void setMin(int x){
		Flags.MIN = x;
	}
	
	public static int getMin(){
		return Flags.MIN;
	}
	
	public static void setMax(int x){
		Flags.MAX = x;
	}
	
	public static int getMax(){
		return Flags.MAX;
	}
	
	public static void setPos(int pos) {
		Flags.pos = pos;
	}

	public static int getPos() {
		return Flags.pos;
	}

	
}
