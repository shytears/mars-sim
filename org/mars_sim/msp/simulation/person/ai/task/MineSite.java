/**
 * Mars Simulation Project
 * MineSite.java
 * @version 2.84 2008-05-01
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.mars.SurfaceFeatures;
import org.mars_sim.msp.simulation.person.NaturalAttributeManager;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.SkillManager;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.vehicle.Rover;

/**
 * Task for mining minerals at a site.
 */
public class MineSite extends EVAOperation implements Serializable {

	// Task phases
	private static final String MINING = "Mining";
	
	// Excavation rates (kg/millisol)
	private static final double HAND_EXCAVATION_RATE = .1D;
	private static final double LUV_EXCAVATION_RATE = 1D;
	
	// Time limit for mining (millisol)
	private static final double MINING_TIME_LIMIT = 100D;
	
	// Data members
	private Coordinates site;
	private Rover rover;
	private boolean drivingLUV;
	private Map<AmountResource, Double> excavationPile;
	private double miningTime;
	
	/**
	 * Constructor
	 * @param person the person performing the task.
	 * @param site the explored site to mine.
	 * @param rover the rover used for the EVA operation.
	 * @param excavationPile a map representing the mineral resources 
	 * excavated so far and their amounts (kg).
	 * @throws Exception if error creating task.
	 */
	public MineSite(Person person, Coordinates site, Rover rover, 
			Map<AmountResource, Double> excavationPile) throws Exception {
		
		// Use EVAOperation parent constructor.
		super("Mine Site", person);
		
		// Initialize data members.
		this.site = site;
		this.rover = rover;
		this.excavationPile = excavationPile;
		
		// TODO Add light utility vehicle
		
		// Add task phase
		addPhase(MINING);
	}
	
	/**
	 * Checks if a person can mine a site.
	 * @param person the person
	 * @param rover the rover
	 * @return true if person can mine a site.
	 */
	public static boolean canMineSite(Person person, Rover rover) {
		// Check if person can exit the rover.
		boolean exitable = ExitAirlock.canExitAirlock(person, rover.getAirlock());

		SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();

		// Check if it is night time outside.
		boolean sunlight = surface.getSurfaceSunlight(rover.getCoordinates()) > 0;
		
		// Check if in dark polar region.
		boolean darkRegion = surface.inDarkPolarRegion(rover.getCoordinates());

		// Check if person's medical condition will not allow task.
		boolean medical = person.getPerformanceRating() < .5D;
	
		return (exitable && (sunlight || darkRegion) && !medical);
	}
	
	/**
	 * Perform the exit rover phase of the task.
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 * @throws Exception if error exiting rover.
	 */
	private double exitRover(double time) throws Exception {
		
		try {
			time = exitAirlock(time, rover.getAirlock());
		
			// Add experience points
			addExperience(time);
		}
		catch (Exception e) {
			// Person unable to exit airlock.
			endTask();
		}
		
		if (exitedAirlock) setPhase(MINING);

		return time;
	}
	
	/**
	 * Perform the enter rover phase of the task.
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 * @throws Exception if error entering rover.
	 */
	private double enterRover(double time) throws Exception {

		time = enterAirlock(time, rover.getAirlock());

        // Add experience points
        addExperience(time);
        
        if (enteredAirlock) {
        	endTask();
        	return time;
        }
        
		return 0D;
	}
	
	/**
	 * Perform the mining phase of the task.
	 * @param time the time available (millisols).
	 * @return remaining time after performing phase (millisols).
	 * @throws Exception if error performing phase.
	 */
	private double miningPhase(double time) throws Exception {
		
		// Check for an accident during the EVA operation.
		// TODO add accidents for driving LUV?
		checkForAccident(time);
		
		// Add mining time.
		miningTime += time;
		
		// Check if there is reason to cut the mining phase short and return
		// to the rover.
		if (shouldEndEVAOperation() || (miningTime >= MINING_TIME_LIMIT)) {
			// TODO End driving light utility vehicle.
			drivingLUV = false;
			setPhase(EVAOperation.ENTER_AIRLOCK);
			return time;
		}
		
		// TODO Drive light utility vehicle if no one else driving it.
		
		// Excavate minerals.
		excavateMinerals(time);
		
		// Add experience points
        addExperience(time);
        
        return 0D;
	}
	
	private void excavateMinerals(double time) throws Exception {
		
		Map<String, Double> minerals = Simulation.instance().getMars().getSurfaceFeatures()
				.getMineralMap().getAllMineralConcentrations(site);
		Iterator<String> i = minerals.keySet().iterator();
		while (i.hasNext()) {
			String mineralName = i.next();
			double amountExcavated = 0D;
			if (drivingLUV) amountExcavated = LUV_EXCAVATION_RATE * time;
			else amountExcavated = HAND_EXCAVATION_RATE * time;
			double mineralConcentration = minerals.get(mineralName);
			amountExcavated *= mineralConcentration / 100D;
			amountExcavated *= getEffectiveSkillLevel();
			
			AmountResource mineralResource = AmountResource.findAmountResource(mineralName);
			double currentExcavated = 0D;
			if (excavationPile.containsKey(mineralResource)) currentExcavated = excavationPile.get(mineralResource);
			excavationPile.put(mineralResource, currentExcavated + amountExcavated);
		}
	}
	
	@Override
	protected void addExperience(double time) {
		SkillManager manager = person.getMind().getSkillManager();
		
		// Add experience to "EVA Operations" skill.
		// (1 base experience point per 100 millisols of time spent)
		double evaExperience = time / 100D;
		
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		NaturalAttributeManager nManager = person.getNaturalAttributeManager();
		int experienceAptitude = nManager.getAttribute(NaturalAttributeManager.EXPERIENCE_APTITUDE);
		double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
		evaExperience += evaExperience * experienceAptitudeModifier;
		evaExperience *= getTeachingExperienceModifier();
		manager.addExperience(Skill.EVA_OPERATIONS, evaExperience);
		
		// If phase is mining, add experience to areology skill.
		if (MINING.equals(getPhase())) {
			// 1 base experience point per 10 millisols of mining time spent.
			// Experience points adjusted by person's "Experience Aptitude" attribute.
			double areologyExperience = time / 10D;
			areologyExperience += areologyExperience * experienceAptitudeModifier;
			manager.addExperience(Skill.AREOLOGY, areologyExperience);
			
			// If person is driving the light utility vehicle, add experience to driving skill.
			// 1 base experience point per 10 millisols of mining time spent.
			// Experience points adjusted by person's "Experience Aptitude" attribute.
			double drivingExperience = time / 10D;
			drivingExperience += drivingExperience * experienceAptitudeModifier;
			manager.addExperience(Skill.DRIVING, drivingExperience);
		}
	}

	@Override
	public List<String> getAssociatedSkills() {
		List<String> results = new ArrayList<String>(3);
		results.add(Skill.EVA_OPERATIONS);
		results.add(Skill.AREOLOGY);
		if (drivingLUV) results.add(Skill.DRIVING);
		return results;
	}

	@Override
	public int getEffectiveSkillLevel() {
		int result = 0;
		
		SkillManager manager = person.getMind().getSkillManager();
		int EVAOperationsSkill = manager.getEffectiveSkillLevel(Skill.EVA_OPERATIONS);
		int areologySkill = manager.getEffectiveSkillLevel(Skill.AREOLOGY);
		if (drivingLUV) {
			int drivingSkill = manager.getEffectiveSkillLevel(Skill.DRIVING);
			result = (int) Math.round((double)(EVAOperationsSkill + areologySkill + drivingSkill) / 3D); 
		}
		else result = (int) Math.round((double)(EVAOperationsSkill + areologySkill) / 2D);
		
		return result;
	}

	@Override
	protected double performMappedPhase(double time) throws Exception {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (EVAOperation.EXIT_AIRLOCK.equals(getPhase())) return exitRover(time);
    	if (MINING.equals(getPhase())) return miningPhase(time);
    	if (EVAOperation.ENTER_AIRLOCK.equals(getPhase())) return enterRover(time);
    	else return time;
	}
}