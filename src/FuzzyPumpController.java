import java.util.*;

import processing.core.*;
import com.fuzzylite.*;
import com.fuzzylite.defuzzifier.*;
import com.fuzzylite.norm.s.*;
import com.fuzzylite.norm.t.*;
import com.fuzzylite.rule.*;
import com.fuzzylite.term.*;
import com.fuzzylite.variable.*;


public class FuzzyPumpController extends PApplet{
	
	// Max and Min tank levels
	private final int MAX = 70;
	private final int MIN = 40;
	
	// For check buttons
	private boolean rainOver = false;
	private boolean demandOver = false;
	
	// Toggle external disturbances
	private boolean rainOn = false;
	private boolean demandOn = false;
	
    // Step vars to control perlin noise
	private float t1 = (float) 3;
	private float t2 = (float) 3;
	
	// Current level in the tank
	private float level;
	// Level of demand from outlet (-/+)
	private float demand;
	// Level of rain water filing tank
	private float rain;
	// Pump action (-/+)
	private float pumpAction;
	
	// Setup some colours
	private int blue = color(85,145,232);
	private int white = color(255,255,255);
	private int red = color(255,0,0);
	private int green = color(0,204,102);
	private int black = color(0,0,0);
	private int grey = color(100,100,100);
	
	private Engine engine;
	private InputVariable inputVariable1;
	private InputVariable inputVariable2;
	private OutputVariable outputVariable;
	private RuleBlock ruleBlock;
	
	public void settings(){
		size(800, 360);
	}
	
	public void setup(){
		Random ran = new Random();
		level = ran.nextInt(80)+10;
		demand = 0;
		pumpAction = 0;
		rain = 0;
		
		// TODO: Create the engine
		 engine = new Engine();
		engine.setName("Assignment1 - Fuzzy Pump Controller");
				
		// TODO: Setup the level input variable
		inputVariable1 = new InputVariable();
		inputVariable1.setEnabled(true);
		inputVariable1.setName("level");
		inputVariable1.setRange(0.000, 100.000);
		inputVariable1.addTerm(new Trapezoid("tooLow", 0.000, 0.000, 40.000, 45.000));
		inputVariable1.addTerm(new Triangle("low", 35.000, 45.000, 55.000));
		inputVariable1.addTerm(new Triangle("normal", 45.000, 55.000, 65.000));
		inputVariable1.addTerm(new Triangle("high", 55.000, 65.000, 75.000));
		inputVariable1.addTerm(new Trapezoid("tooHigh", 65.000, 70.000, 100.000, 100.000));
		engine.addInputVariable(inputVariable1);
		
		// TODO: Setup the demand input variable
		 inputVariable2 = new InputVariable();
		 inputVariable2.setEnabled(true);
		 inputVariable2.setName("demand");
		 inputVariable2.setRange(-1.000, 1.500);
		 inputVariable2.addTerm(new Trapezoid("veryLow", -1.000, -1.000, -0.750, -0.250));
		 inputVariable2.addTerm(new Triangle("low", -0.750, -0.250, 0.250));
		 inputVariable2.addTerm(new Triangle("noDemand", -0.250, 0.250, 0.750));
		 inputVariable2.addTerm(new Triangle("high", 0.250, 0.750, 1.250));
		 inputVariable2.addTerm(new Trapezoid("veryHigh", 0.750, 1.250, 1.500, 1.500));
		 engine.addInputVariable(inputVariable2);
				
		// TODO: Setup the output command variable
		 outputVariable = new OutputVariable();
		 outputVariable.setEnabled(true);
		 outputVariable.setName("command");
		 outputVariable.setRange(-1.000, 1.000);
		 outputVariable.fuzzyOutput().setAccumulation(new Maximum());
		 outputVariable.setDefuzzifier(new Centroid(200));
		 outputVariable.setDefaultValue(0.000);
		 outputVariable.setLockValidOutput(false);
		 outputVariable.setLockOutputRange(false);
		 outputVariable.addTerm(new Trapezoid("pumpBack", -1.000, -1.000, -0.500, -0.250));
		 outputVariable.addTerm(new Trapezoid("doNothing", -0.500, -0.250, 0.250, 0.500));
		 outputVariable.addTerm(new Trapezoid("pumpForward", 0.250, 0.500, 1.000, 1.000));
		 engine.addOutputVariable(outputVariable);
				
		// TODO: Setup the inference rules
		 ruleBlock = new RuleBlock();
		ruleBlock.setEnabled(true);
		ruleBlock.setName("");
		ruleBlock.setConjunction(new Minimum());
		ruleBlock.setDisjunction(new Maximum());
		ruleBlock.setActivation(new Minimum());
		ruleBlock.addRule(Rule.parse("if (level is tooLow or level is low) and demand is low or demand is high then command is pumpForward", engine));
		ruleBlock.addRule(Rule.parse("if (level is tooHigh or level is high) and demand is low or demand is high then command is pumpBack", engine));
		ruleBlock.addRule(Rule.parse("if level is normal and demand is noDemand then command is doNothing", engine));
		engine.addRuleBlock(ruleBlock);
				
		// Set the background to black
		background(255);
	}
	private void drawGameOver(){
		background(red);
		fill(white);
		textSize(60);
		text("CRITICAL LEVEL",200,100);
	}
	private void drawPipes(){
		// Set the stroke color
		stroke(0);
		// Draw the inlet pipe
		line(100,290,500,290);
		line(100,300,500,300);
		// Draw the outlet pipe
		line(600,290,750,290);
		line(600,300,750,300);
		noStroke();
		// Fill the input pipe
		fill(blue);
		rect(100,291,400,9);
		// Fill the output pipe
		rect(601,291,150,9);
	}
	private void drawPump(){
		// Draw the pump
		fill(grey);
		rect(255,275,40,40);
	}
	private void drawTank(){
		// Draw the tank
		stroke(0);
		line(500,300,500,200);
		line(600,300,600,200);
		line(500,300,600,300);
	}
	private void drawWaterLevel(float lev){
		// No outline for the water
		noStroke();
		// Set the fill color
		fill(blue);
		// Draw the rect for the water
		rect(501,300-lev,99,lev);
	}
	private void drawInfo(float l, float d, float p){
		// Output the water level
	    fill(black);
	    text("Water level: " + l,50,50);
	    text("Demand level: " + d,50,65);
	    text("Pump: " + p,50,80);
		if (l > MAX || l < MIN)
			fill(red);
		else
			fill(green);
		// Draw the warning light
		ellipse(60,95,20,20);
		fill(white);
		if (p < 0)
			text("<<<", 261,298);
		else
			text(">>>", 261,298);
	}
	private float fuzzyPumpController(float l, float d){
		// TODO: Load the input variables
		inputVariable1.setInputValue(l);
		inputVariable2.setInputValue(d);
		
		// TODO: Run the engine
		engine.process();
		// TODO: Return the output -> outputVariable.defuzzify()
		return (float)outputVariable.defuzzify();
	}
	private void drawCheckboxButtons(){
		// Draw the buttons
		stroke(black);
		if (rainOn)
			fill(grey);
		else
			fill(white);
		rect(650, 50, 15, 15);
		fill(black);
		text("Rain", 670, 62);
		stroke(black);
		if (demandOn)
			fill(grey);
		else
			fill(white);
		rect(650, 70, 15, 15);
		fill(black);
		text("Demand", 670, 83);
	}
	
	// Run the system
	public void drawSystem(){
		// Clear the background
		background(255);
		
		// Update the mouse pos
		update(mouseX, mouseY);
		
		drawCheckboxButtons();
		
		// Draw all the static visual components
		drawPipes();
		drawPump();
		drawTank();
				
		// Generate a perlin level of rain
		if (rainOn)
			rain = noise(t1) * 0.05f;
		else
			rain = 0;
		// Generate a perlin outlet demand
		if (demandOn){
			demand = noise(t2);
			// Map the demand to a value between -1 and 1.5
			demand = map(demand,0f,1f,-1f,1.5f);
		}
		else
			demand = 0;
		
		// Add the rain level to the current tank level
		level = level + rain;
		// Apply the demand -/+ to the current level
		level = level - demand;
		
		// Show the water level in the tank
		drawWaterLevel(level);
		
		// Run the fuzzy engine with inputs and get controller output
	    pumpAction = fuzzyPumpController(level, demand);
	    
	    // Apply the pump action to the current level
	    level = level + pumpAction;
	    
	    // Draw the instrumentation panel
	    drawInfo(level, demand, pumpAction);
	    
	    // Increment time step for Perlin noise
	    t1 += 0.01;
	    t2 += .6;
	}
	
	// Draw each frame of animation
	public void draw(){
		if (level < 0 || level > 100)
			drawGameOver();
		else
			drawSystem();
	}
	private void update(int x, int y) {
		if ( overRainCheckbox(650, 50, 15, 15) ) {
			rainOver = true;
		    demandOver = false;
		} 
		else if ( overDemandCheckbox(650, 70, 15, 15) ) {
		    demandOver = true;
		    rainOver = false;
		}
		else {
		    rainOver = demandOver = false;
		}
	}
	public void mousePressed() {
		if (rainOver) {
			rainOn = !rainOn;
		}
		if (demandOver) {
			demandOn = !demandOn;
		}
	}
	private boolean overRainCheckbox(int x, int y, int width, int height)  {
		if (mouseX >= x && mouseX <= x+width && mouseY >= y && mouseY <= y+height)
			return true;
		else
			return false;
	}
	private boolean overDemandCheckbox(int x, int y, int width, int height)  {
		if (mouseX >= x && mouseX <= x+width && mouseY >= y && mouseY <= y+height)
			return true;
		else
			return false;
	}
	// Main method
	public static void main(String[] args) {
		PApplet.main("FuzzyPumpController");

	}

}
