package com.acme.brms.domain;

public class AcmeFactA implements SimpleFact {
	
	private static final long serialVersionUID = 2525944096949445534L;

	String name = null; 
	
	String something = null;
	
	int counter = 0;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSomething() {
		return something;
	}

	public void setSomething(String something) {
		this.something = something;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

}
