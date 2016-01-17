package pdelay;


import pitaru.sonia.*;
import processing.core.*;
import util.SoniaUtils;

public class InputMeter extends PApplet {

	public void settings() { 
		
	  size(400,200);
	}
	
	public void setup(){ 

	  Sonia.start(this); 
	  LiveInput.start();	  
	} 
	 
	public void draw() {
		
	  background(0,50,0);
	 	SoniaUtils.drawInputMeter(this, 10, 10, width-20, height-20);
	} 
	
	public static void main(String[] args) {
		
		PApplet.main(new String[] { InputMeter.class.getName() });
	}

}
