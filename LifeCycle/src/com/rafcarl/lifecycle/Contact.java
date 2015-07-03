package com.rafcarl.lifecycle;

public class Contact {
	private String id;
	private String name;
	private String number;
	private String message;
//	private String[] numbersList = {};
	
	public Contact(){
		
	}

	public Contact(String name, String number, String message) {
		this.name = name;
		this.number = number;
		this.message = message;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setNumber(String number){
		this.number = number;
	}
	
	public void setMessage(String message){
		this.message = message;
	}
	
	public String getId(){
		return this.id;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getNumber(){
		return this.number;
	}
	
	public String getMessage(){
		return this.message;
	}
	
	@Override
	public String toString(){
		return name;
	}

}
