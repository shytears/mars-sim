/**
 * Mars Simulation Project
 * ConstructionSitesPanel.java
 * @version 2.85 2008-10-13
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.standard.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.Part;
import org.mars_sim.msp.simulation.structure.construction.ConstructionManager;
import org.mars_sim.msp.simulation.structure.construction.ConstructionSite;
import org.mars_sim.msp.simulation.structure.construction.ConstructionStage;
import org.mars_sim.msp.simulation.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.simulation.structure.construction.ConstructionVehicleType;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

public class ConstructionSitesPanel extends JPanel {

    // Data members
    private ConstructionManager manager;
    private List<ConstructionSite> sitesCache;
    private JPanel sitesListPane;
    private JScrollPane sitesScrollPane;
    
    public ConstructionSitesPanel(ConstructionManager manager) {
        // Use JPanel constructor.
        super();
        
        this.manager = manager;
        
        setLayout(new BorderLayout());
        setBorder(new MarsPanelBorder());
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        add(titlePanel, BorderLayout.NORTH);
        
        JLabel titleLabel = new JLabel("Construction Sites");
        titlePanel.add(titleLabel);
        
        // Create scroll panel for sites list pane.
        sitesScrollPane = new JScrollPane();
        add(sitesScrollPane, BorderLayout.CENTER);  
        
        // Prepare sites outer list pane.
        JPanel sitesOuterListPane = new JPanel(new BorderLayout(0, 0));
        sitesScrollPane.setViewportView(sitesOuterListPane);
        
        // Prepare sites list pane.
        sitesListPane = new JPanel();
        sitesListPane.setLayout(new BoxLayout(sitesListPane, BoxLayout.Y_AXIS));
        sitesOuterListPane.add(sitesListPane, BorderLayout.NORTH);
        
        // Create the site panels.
        sitesCache = manager.getConstructionSites();
        Iterator<ConstructionSite> i = sitesCache.iterator();
        while (i.hasNext()) sitesListPane.add(new ConstructionSitePanel(i.next()));
    }
    
    /**
     * Update the information on this panel.
     */
    public void update() {
        // Update sites is necessary.
        List<ConstructionSite> sites = manager.getConstructionSites();
        if (!sitesCache.equals(sites)) {
            
            // Add site panels for new sites.
            Iterator<ConstructionSite> i = sites.iterator();
            while (i.hasNext()) {
                ConstructionSite site = i.next();
                if (!sitesCache.contains(site)) 
                    sitesListPane.add(new ConstructionSitePanel(site));
            }
            
            // Remove site panels for old sites.
            Iterator<ConstructionSite> j = sitesCache.iterator();
            while (j.hasNext()) {
                ConstructionSite site = j.next();
                if (!sites.contains(site)) {
                    ConstructionSitePanel panel = getConstructionSitePanel(site);
                    if (panel != null) sitesListPane.remove(panel);
                }
            }
            
            sitesScrollPane.validate();
            
            // Update sitesCache
            sitesCache.clear();
            sitesCache.addAll(sites);
        }
        
        // Update all site panels.
        Iterator<ConstructionSite> i = sites.iterator();
        while (i.hasNext()) {
            ConstructionSitePanel panel = getConstructionSitePanel(i.next());
            if (panel != null) panel.update();
        }
    }
    
    private ConstructionSitePanel getConstructionSitePanel(ConstructionSite site) {
        ConstructionSitePanel result = null;
        
        for (int x = 0; x < sitesListPane.getComponentCount(); x++) {
            Component component = sitesListPane.getComponent(x);
            if (component instanceof ConstructionSitePanel) {
                ConstructionSitePanel panel = (ConstructionSitePanel) component;
                if (panel.getConstructionSite().equals(site)) result = panel;
            }
        }
        
        return result;
    }
    
    private class ConstructionSitePanel extends JPanel {
        
        // Data members
        private ConstructionSite site;
        private JLabel statusLabel;
        private BoundedRangeModel workBarModel;
        
        private ConstructionSitePanel(ConstructionSite site) {
            // Use JPanel constructor
            super();
            
            this.site = site;
            
            setLayout(new BorderLayout(5, 5));
            
            // Set border
            setBorder(new MarsPanelBorder());
            
            statusLabel = new JLabel("Status: ", JLabel.LEFT);
            add(statusLabel, BorderLayout.NORTH);
            
            JPanel progressBarPanel = new JPanel();
            add(progressBarPanel, BorderLayout.CENTER);
            
            // Prepare work progress bar.
            JProgressBar workBar = new JProgressBar();
            workBarModel = workBar.getModel();
            workBar.setStringPainted(true);
            progressBarPanel.add(workBar);
            
            // Update progress bar.
            update();
            
            // Add tooltip.
            setToolTipText(getToolTipString());
        }
        
        private ConstructionSite getConstructionSite() {
            return site;
        }
        
        private void update() {
            
            // Update status label.
            String statusString = getStatusString();
            
            // Make sure status label isn't too long.
            if (statusString.length() > 31) statusString = statusString.substring(0, 31) + "...";
            
            statusLabel.setText(statusString);
            
            // Update work progress bar.
            int workProgress = 0;
            ConstructionStage stage = site.getCurrentConstructionStage();
            if (stage != null) {
                double completedWork = stage.getCompletedWorkTime();
                double requiredWork = stage.getInfo().getWorkTime();
                if (requiredWork > 0D) workProgress = (int) (100D * completedWork / requiredWork);
            }
            workBarModel.setValue(workProgress);
            
            // Update the tool tip string.
            setToolTipText(getToolTipString());
        }
        
        /**
         * Gets the status label string.
         * @return status string.
         */
        private String getStatusString() {
            String statusString = "";
            ConstructionStage stage = site.getCurrentConstructionStage();
            if (stage != null) {
                if (site.isUndergoingConstruction()) statusString = "Status: constructing " + 
                        stage.getInfo().getName();
                else if (site.hasUnfinishedStage()) statusString = "Status: " + 
                        stage.getInfo().getName() + " unfinished";
                else statusString = "Status: " + stage.getInfo().getName() + " completed";
            }
            else statusString = "No construction";
            
            return statusString;
        }
        
        /**
         * Gets a tool tip string for the panel.
         */
        private String getToolTipString() {
            StringBuffer result = new StringBuffer("<html>");
            result.append(getStatusString() + "<br>");
            
            ConstructionStage stage = site.getCurrentConstructionStage();
            if (stage != null) {
                ConstructionStageInfo info = stage.getInfo();
                result.append("Stage Type: " + info.getType() + "<br>");
                DecimalFormat formatter = new DecimalFormat("0.0");
                String requiredWorkTime = formatter.format(info.getWorkTime() / 1000D);
                result.append("Work Time Required: " + requiredWorkTime + " Sols<br>");
                String completedWorkTime = formatter.format(stage.getCompletedWorkTime() / 1000D);
                result.append("Work Time Completed: " + completedWorkTime + " Sols<br>");
                result.append("Architect Construction Skill Required: " + 
                        info.getArchitectConstructionSkill() + "<br>");
                
                // Add construction resources.
                if (info.getResources().size() > 0) {
                    result.append("<br>Construction Resources:<br>");
                    Iterator<AmountResource> i = info.getResources().keySet().iterator();
                    while (i.hasNext()) {
                        AmountResource resource = i.next();
                        double amount = info.getResources().get(resource);
                        result.append("&nbsp;&nbsp;" + resource.getName() + ": " + amount + " kg<br>");
                    }
                }
                
                // Add construction parts.
                if (info.getParts().size() > 0) {
                    result.append("<br>Construction Parts:<br>");
                    Iterator<Part> j = info.getParts().keySet().iterator();
                    while (j.hasNext()) {
                        Part part = j.next();
                        int number = info.getParts().get(part);
                        result.append("&nbsp;&nbsp;" + part.getName() + ": " + number + "<br>");
                    }
                }
                
                // Add construction vehicles.
                if (info.getVehicles().size() > 0) {
                    result.append("<br>Construction Vehicles:<br>");
                    Iterator<ConstructionVehicleType> k = info.getVehicles().iterator();
                    while (k.hasNext()) {
                        ConstructionVehicleType vehicle = k.next();
                        result.append("&nbsp;&nbsp;Vehicle Type: " + vehicle.getVehicleType() + "<br>");
                        result.append("&nbsp;&nbsp;Attachment Parts:<br>");
                        Iterator<Part> l = vehicle.getAttachmentParts().iterator();
                        while (l.hasNext()) {
                            result.append("&nbsp;&nbsp;&nbsp;&nbsp;" + l.next().getName() + "<br>");
                        }
                    }
                }
            }
            
            result.append("</html>");
            
            return result.toString();
        }
    }   
}