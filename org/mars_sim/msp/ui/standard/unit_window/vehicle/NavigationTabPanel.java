/**
 * Mars Simulation Project
 * NavigationTabPanel.java
 * @version 2.79 2006-06-01
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.person.ai.mission.NavPoint;
import org.mars_sim.msp.simulation.person.ai.mission.TravelMission;
import org.mars_sim.msp.simulation.person.ai.mission.VehicleMission;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.GroundVehicle;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.simulation.vehicle.VehicleOperator;
import org.mars_sim.msp.ui.standard.ImageLoader;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;
import org.mars_sim.msp.ui.standard.unit_window.TabPanel;

/** 
 * The NavigationTabPanel is a tab panel for a vehicle's navigation information.
 */
public class NavigationTabPanel extends TabPanel implements ActionListener {
    
    private DecimalFormat formatter = new DecimalFormat("0.0");
    private JButton driverButton;
    private JLabel statusLabel;
    private JLabel beaconLabel;
    private JLabel speedLabel;
    private JLabel elevationLabel;
    private JButton centerMapButton;
    private JButton destinationButton;
    private JLabel destinationTextLabel;
    private JPanel destinationLabelPanel;
    private JLabel destinationLatitudeLabel;
    private JLabel destinationLongitudeLabel;
    private JLabel distanceLabel;
    private JLabel etaLabel;
    private DirectionDisplayPanel directionDisplay;
    private TerrainDisplayPanel terrainDisplay;
    
    // Data cache
    private VehicleOperator driverCache;
    private String statusCache;
    private boolean beaconCache;
    private double speedCache;
    private double elevationCache;
    private Settlement destinationSettlementCache;
    private String destinationTextCache;
    private Coordinates destinationLocationCache;
    private String etaCache;
    private double distanceCache;
    
    /**
     * Constructor
     *
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public NavigationTabPanel(Unit unit, MainDesktopPane desktop) { 
        // Use the TabPanel constructor
        super("Navigation", null, "Navigation", unit, desktop);
        
        Vehicle vehicle = (Vehicle) unit;
        
        // Prepare main panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        topContentPanel.add(mainPanel);
        
        // Prepare top info panel
        JPanel topInfoPanel = new JPanel(new BorderLayout(0, 0));
        topInfoPanel.setBorder(new MarsPanelBorder());
        mainPanel.add(topInfoPanel, BorderLayout.NORTH);
        
        // Prepare driver panel
        JPanel driverPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topInfoPanel.add(driverPanel, BorderLayout.NORTH);
            
        // Prepare driver label
        JLabel driverLabel = new JLabel("Driver: ", JLabel.LEFT);
        driverPanel.add(driverLabel);
            
        // Prepare driver button and add it if vehicle has driver.
        driverCache = vehicle.getOperator();
        driverButton = new JButton();
        driverButton.addActionListener(this);
        driverButton.setVisible(false);
        if (driverCache != null) {
            driverButton.setText(driverCache.getOperatorName());
            driverButton.setVisible(true);
        }
        driverPanel.add(driverButton);
        
        // Prepare info label panel
        JPanel infoLabelPanel = new JPanel(new GridLayout(3, 1, 0, 0));
        topInfoPanel.add(infoLabelPanel, BorderLayout.CENTER);
        
        // Prepare status label
        statusCache = vehicle.getStatus();
        statusLabel = new JLabel("Status: " + statusCache, JLabel.LEFT);
        infoLabelPanel.add(statusLabel);
        
        // Prepare beacon label
        beaconCache = vehicle.isEmergencyBeacon();
        String beaconString;
        if (beaconCache) beaconString = "on";
        else beaconString = "off";
        beaconLabel = new JLabel("Emergency Beacon: " + beaconString, JLabel.LEFT);
        infoLabelPanel.add(beaconLabel);
        
        // Prepare speed label
        speedCache = vehicle.getSpeed();
        speedLabel = new JLabel("Speed: " + formatter.format(speedCache) + " kph.", JLabel.LEFT);
        infoLabelPanel.add(speedLabel);
        
        // Prepare elevation label if ground vehicle
        if (vehicle instanceof GroundVehicle) {
            GroundVehicle gVehicle = (GroundVehicle) vehicle;
            elevationCache = gVehicle.getElevation();
            elevationLabel = new JLabel("Elevation: " + formatter.format(elevationCache) + 
                " km.", JLabel.LEFT);
            topInfoPanel.add(elevationLabel, BorderLayout.SOUTH);
        }
        
        // Prepare destination info panel
        JPanel destinationInfoPanel = new JPanel(new BorderLayout(0, 0));
        destinationInfoPanel.setBorder(new MarsPanelBorder());
        mainPanel.add(destinationInfoPanel, BorderLayout.CENTER);
        
        // Prepare destination label panel
        destinationLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        destinationInfoPanel.add(destinationLabelPanel, BorderLayout.NORTH);
        
        // Prepare center map button
        centerMapButton = new JButton(ImageLoader.getIcon("CenterMap"));
        centerMapButton.setMargin(new Insets(1, 1, 1, 1));
        centerMapButton.addActionListener(this);
        destinationLabelPanel.add(centerMapButton);
        
        // Prepare destination label
        JLabel destinationLabel = new JLabel("Destination: ", JLabel.LEFT);
        destinationLabelPanel.add(destinationLabel);
        
        // Prepare destination button
        destinationButton = new JButton();
        destinationButton.addActionListener(this);
        
        // Prepare destination text label
        destinationTextLabel = new JLabel("", JLabel.LEFT);
        
        VehicleMission mission = (VehicleMission) 
				Simulation.instance().getMissionManager().getMissionForVehicle(vehicle);
        if ((mission != null) && mission.getTravelStatus().equals(TravelMission.TRAVEL_TO_NAVPOINT)) {
        	NavPoint destinationPoint = mission.getNextNavpoint();
        	if (destinationPoint.isSettlementAtNavpoint()) {
                // If destination is settlement, add destination button.
                destinationSettlementCache = destinationPoint.getSettlement();
                destinationButton.setText(destinationSettlementCache.getName());
                destinationLabelPanel.add(destinationButton);
        	}
        	else {
                // If destination is coordinates, add destination text label.
                destinationTextCache = "Coordinates";
                destinationTextLabel.setText(destinationTextCache);
                destinationLabelPanel.add(destinationTextLabel);
        	}
        }
        else {
        	// If destination is none, add destination text label.
            destinationTextCache = "None";
            destinationTextLabel.setText(destinationTextCache);
            destinationLabelPanel.add(destinationTextLabel);
        }
        
        // Prepare destination info label panel.
        JPanel destinationInfoLabelPanel = new JPanel(new GridLayout(4, 1, 0, 0));
        destinationInfoPanel.add(destinationInfoLabelPanel, BorderLayout.CENTER);
        
        if ((mission != null) && mission.getTravelStatus().equals(TravelMission.TRAVEL_TO_NAVPOINT))
        	destinationLocationCache = mission.getNextNavpoint().getLocation();
        else destinationLocationCache = null;
        
        // Prepare destination latitude label.
        String latitudeString = "";
        if (destinationLocationCache != null) latitudeString = 
            destinationLocationCache.getFormattedLatitudeString();
        destinationLatitudeLabel = new JLabel("Latitude: " + latitudeString, JLabel.LEFT);
        destinationInfoLabelPanel.add(destinationLatitudeLabel);

        // Prepare destination longitude label.
        String longitudeString = "";
        if (destinationLocationCache != null) longitudeString = 
            destinationLocationCache.getFormattedLongitudeString();
        destinationLongitudeLabel = new JLabel("Longitude: " + longitudeString, JLabel.LEFT);
        destinationInfoLabelPanel.add(destinationLongitudeLabel);
        
        // Prepare distance label.
        if ((mission != null) && mission.getTravelStatus().equals(TravelMission.TRAVEL_TO_NAVPOINT)) {
        	try {
        		distanceCache = mission.getCurrentLegRemainingDistance();
        	}
        	catch (Exception e) {
        		System.err.println("Error getting current leg remaining distance.");
    			e.printStackTrace(System.err);
        	}
        	distanceLabel = new JLabel("Distance: " + formatter.format(distanceCache) + " km.", JLabel.LEFT);
        }
        else {
        	distanceCache = 0D;
        	distanceLabel = new JLabel("Distance: ", JLabel.LEFT);
        }
        destinationInfoLabelPanel.add(distanceLabel);
        
        // Prepare ETA label.
        if ((mission != null) && (mission.getLegETA() != null)) etaCache = mission.getLegETA().toString();
        else etaCache = "";
        etaLabel = new JLabel("ETA: " + etaCache, JLabel.LEFT);
        destinationInfoLabelPanel.add(etaLabel);
        
        // Prepare graphic display panel
        JPanel graphicDisplayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        graphicDisplayPanel.setBorder(new MarsPanelBorder());
        mainPanel.add(graphicDisplayPanel, BorderLayout.SOUTH);
        
        // Prepare direction display panel
        JPanel directionDisplayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        directionDisplayPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        graphicDisplayPanel.add(directionDisplayPanel);
        
        // Prepare direction display
        directionDisplay = new DirectionDisplayPanel(vehicle); 
        directionDisplayPanel.add(directionDisplay);
        
        // If vehicle is a ground vehicle, prepare terrain display.
        if (vehicle instanceof GroundVehicle) {
            JPanel terrainDisplayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            terrainDisplayPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
            graphicDisplayPanel.add(terrainDisplayPanel);
            terrainDisplay = new TerrainDisplayPanel((GroundVehicle) vehicle);
            terrainDisplayPanel.add(terrainDisplay);
        }
    }
    
    /**
     * Updates the info on this panel.
     */
    public void update() {
        
        Vehicle vehicle = (Vehicle) unit;
        
        // Update driver button if necessary.
        boolean driverChange = false;
        if (driverCache == null) {
            if (vehicle.getOperator() != null) driverChange = true;
        }
        else if (!driverCache.equals(vehicle.getOperator())) driverChange = true;
        if (driverChange) {
            driverCache = vehicle.getOperator();
            if (driverCache == null) {
                driverButton.setVisible(false);
            }
            else {
                driverButton.setVisible(true);
                driverButton.setText(driverCache.getOperatorName());
            }
        }
        
        // Update status label
        if (!statusCache.equals(vehicle.getStatus())) {
            statusCache = vehicle.getStatus();
            statusLabel.setText("Status: " + statusCache);
        }
        
        // Update beacon label
        if (beaconCache != vehicle.isEmergencyBeacon()) {
        	beaconCache = vehicle.isEmergencyBeacon();
        	if (beaconCache) beaconLabel.setText("Emergency Beacon: on");
        	else beaconLabel.setText("Emergency Beacon: off");
        }
        
        // Update speed label
        if (speedCache != vehicle.getSpeed()) {
            speedCache = vehicle.getSpeed();
            speedLabel.setText("Speed: " + formatter.format(speedCache) + " kph.");
        }
        
        // Update elevation label if ground vehicle.
        if (vehicle instanceof GroundVehicle) {
            GroundVehicle gVehicle = (GroundVehicle) vehicle;
            if (elevationCache != gVehicle.getElevation()) {
                elevationCache = gVehicle.getElevation();
                elevationLabel.setText("Elevation: " + formatter.format(elevationCache) + " km.");
            }
        }
        
        VehicleMission mission = (VehicleMission) 
			Simulation.instance().getMissionManager().getMissionForVehicle(vehicle);
        if ((mission != null) && mission.getTravelStatus().equals(TravelMission.TRAVEL_TO_NAVPOINT)) {
        	NavPoint destinationPoint = mission.getNextNavpoint();
        	if (destinationPoint.isSettlementAtNavpoint()) {
        		// If destination is settlement, update destination button.
        		if (destinationSettlementCache != destinationPoint.getSettlement()) {
        			destinationSettlementCache = destinationPoint.getSettlement();
        			destinationButton.setText(destinationSettlementCache.getName());
        			addDestinationButton();
        			destinationTextCache = "";
        		}
        	}
        	else {
        		if (destinationTextCache != "Coordinates") {
        			// If destination is coordinates, update destination text label.
        			destinationTextCache = "Coordinates";
        			destinationTextLabel.setText(destinationTextCache);
        			addDestinationTextLabel();
                    destinationSettlementCache = null;
        		}
        	}
        }
        else {
        	// If destination is none, update destination text label.
        	if (destinationTextCache != "None") {
        		destinationTextCache = "None";
        		destinationTextLabel.setText(destinationTextCache);
        		addDestinationTextLabel();
        		destinationSettlementCache = null;
        	}
        }
        
        // Update latitude and longitude panels if necessary.
        if ((mission != null) && mission.getTravelStatus().equals(TravelMission.TRAVEL_TO_NAVPOINT)) {
        	if (destinationLocationCache == null) 
        		destinationLocationCache = new Coordinates(mission.getNextNavpoint().getLocation());
        	else destinationLocationCache.setCoords(mission.getNextNavpoint().getLocation());
            destinationLatitudeLabel.setText("Latitude: " + 
                    destinationLocationCache.getFormattedLatitudeString());
            destinationLongitudeLabel.setText("Longitude: " + 
                    destinationLocationCache.getFormattedLongitudeString());
        }
        else {
        	if (destinationLocationCache != null) {
        		destinationLocationCache = null;
                destinationLatitudeLabel.setText("Latitude: ");
                destinationLongitudeLabel.setText("Longitude: ");
        	}
        }
        
        // Update distance to destination if necessary.
        if (mission != null) {
        	try {
        		if (distanceCache != mission.getCurrentLegRemainingDistance()) {
        			distanceCache = mission.getCurrentLegRemainingDistance();
        			distanceLabel.setText("Distance: " + formatter.format(distanceCache) + " km.");
        		}
        	}
        	catch (Exception e) {
        		System.err.println("Error getting current leg remaining distance.");
    			e.printStackTrace(System.err);
        	}
        }
        else {
        	distanceCache = 0D;
        	distanceLabel.setText("Distance:");
        }
        
        // Update ETA if necessary
        if ((mission != null) && (mission.getLegETA() != null)){
        	if (!etaCache.equals(mission.getLegETA().toString())) {
        		etaCache = mission.getLegETA().toString();
                etaLabel.setText("ETA: " + etaCache);
        	}
        }
        else {
        	etaCache = "";
        	etaLabel.setText("ETA: ");
        }
        
        // Update direction display
        directionDisplay.update();
        
        // Update terrain display
        terrainDisplay.update();
    }
    
    /**
     * Adds a destination button if it isn't there and removes the destination text label.
     */
    private void addDestinationButton() {
        try {
            Component lastComponent = destinationLabelPanel.getComponent(2);
            if (lastComponent == destinationTextLabel) {
                destinationLabelPanel.remove(destinationTextLabel);
                destinationLabelPanel.add(destinationButton);
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            destinationLabelPanel.add(destinationButton);
        }
    }
    
    /**
     * Adds a destination text label if it isn't there and removes the destination button.
     */
    private void addDestinationTextLabel() {
        try {
            Component lastComponent = destinationLabelPanel.getComponent(2); 
            if (lastComponent == destinationButton) {
                destinationLabelPanel.remove(destinationButton);
                destinationLabelPanel.add(destinationTextLabel);
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            destinationLabelPanel.add(destinationTextLabel);
        }
    }
    
    /** 
     * Action event occurs.
     *
     * @param event the action event
     */
    public void actionPerformed(ActionEvent event) {
        JComponent source = (JComponent) event.getSource();
        
        // If center map button is pressed, center navigator tool
        // at destination location.
        if (source == centerMapButton) {
        	if (destinationLocationCache != null) 
        		desktop.centerMapGlobe(destinationLocationCache);
        }
        
        // If destination settlement button is pressed, open window for settlement.
        if (source == destinationButton) desktop.openUnitWindow(destinationSettlementCache);
        
        // If driver button is pressed, open window for driver.
        if (source == driverButton) desktop.openUnitWindow((Unit) driverCache);
    }
}
