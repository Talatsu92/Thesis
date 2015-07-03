package com.rafcarl.lifecycle;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class ContactsData {
	public static int count = 0;
	public static List<Contact> Contacts = new ArrayList<Contact>();
	public static final String LOG = "ContactsData";

	public ContactsData() {
		
	}

	public static int ifDuplicate(Contact item){
		int result = 0;
		String s1 = item.getName(), s2;

		for (Contact contact : Contacts) {
			Log.i(LOG,"enter ifDuplicate() for-each loop");
			s2 = contact.getName();
			if(s1.equals(s2)){
				Log.i(LOG,"enter if s1.equals(s2)");
				result = 1;
				break;
			}
		}
		return result;
	}

	public static void addContact(Contact item){
		Contacts.add(item);	
	}

	public static void deleteContact(int position){
		Contacts.remove(position);
	}

	public List<Contact> getContacts(){
		return Contacts;
	}

	public void sortList(){

	}

}
