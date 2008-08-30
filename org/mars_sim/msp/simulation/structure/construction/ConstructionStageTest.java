/**
 * Mars Simulation Project
 * ConstructionStageTest.java
 * @version 2.85 2008-08-07
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.construction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.Part;
import org.mars_sim.msp.simulation.resource.Phase;
import org.mars_sim.msp.simulation.vehicle.LightUtilityVehicle;

import junit.framework.TestCase;

/**
 * Test case for the ConstructionStage class.
 */
public class ConstructionStageTest extends TestCase {

    // Data members
    private ConstructionStageInfo foundationInfo;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        Map<Part, Integer> parts = new HashMap<Part, Integer>(1);
        parts.put(new Part("test part", 1D), 1);
        
        Map<AmountResource, Double> resources = new HashMap<AmountResource, Double>(1);
        resources.put(new AmountResource("test resource", Phase.SOLID, false), 1D);
        
        List<ConstructionVehicleType> vehicles = 
            new ArrayList<ConstructionVehicleType>(1);
        List<Part> attachments = new ArrayList<Part>(1);
        attachments.add(new Part("attachment part", 1D));
        vehicles.add(new ConstructionVehicleType("Light Utility Vehicle", LightUtilityVehicle.class, 
                attachments));
        
        foundationInfo = new ConstructionStageInfo("test foundation info", 
                ConstructionStageInfo.FOUNDATION, 10D, 0, null, parts, resources, vehicles);
    }
    
    /*
     * Test method for 'org.mars_sim.msp.simulation.structure
     * .construction.ConstructionStage.ConstructionStage(ConstructionStageInfo)'
     */
    public void testConstructionStage() {
        ConstructionStage stage = new ConstructionStage(foundationInfo);
        assertNotNull(stage);
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure
     * .construction.ConstructionStage.getInfo()'
     */
    public void testGetInfo() {
        ConstructionStage stage = new ConstructionStage(foundationInfo);
        assertEquals(foundationInfo, stage.getInfo());
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure
     * .construction.ConstructionStage.getCompletedWorkTime()'
     */
    public void testGetCompletedWorkTime() {
        ConstructionStage stage = new ConstructionStage(foundationInfo);
        assertEquals(0D, stage.getCompletedWorkTime());
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure
     * .construction.ConstructionStage.addWorkTime(double)'
     */
    public void testAddWorkTime() {
        ConstructionStage stage = new ConstructionStage(foundationInfo);
        stage.addWorkTime(5D);
        assertEquals(5D, stage.getCompletedWorkTime());
        stage.addWorkTime(5D);
        assertEquals(10D, stage.getCompletedWorkTime());
        stage.addWorkTime(5D);
        assertEquals(10D, stage.getCompletedWorkTime());
    }

    /*
     * Test method for 'org.mars_sim.msp.simulation.structure
     * .construction.ConstructionStage.isComplete()'
     */
    public void testIsComplete() {
        ConstructionStage stage = new ConstructionStage(foundationInfo);
        stage.addWorkTime(5D);
        assertFalse(stage.isComplete());
        stage.addWorkTime(10D);
        assertTrue(stage.isComplete());
    }
}