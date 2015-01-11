/**
 * Mars Simulation Project
 * TabPanelCooking.java
 * @version 3.07 2015-01-06
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building.food;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.ColumnResizer;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;


/** 
 * This is a tab panel for displaying a settlement's Food Menu.
 */
public class TabPanelCooking
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

    /** default logger. */
    //private static Logger logger = Logger.getLogger(TabPanelCooking.class.getName());

    private static final BuildingFunction FUNCTION = BuildingFunction.COOKING;

	// Data Members
private CookingTableModel cookingTableModel;

	private int numRow = 0;
	private int dayCache = 1;
	//private MarsClock expirationCache = null;
	
	private Set<String> nameSet;
	private List<String> nameList;

	/** Constructor will flip this. */
	private boolean sortAscending = true;
	/** Sort column is defined. */
	private int sortedColumn = 0;
	
	/** The number of available meals. */
	private JLabel numMealsLabel;
	private int numMealsCache = 0;
	/** The number of meals cooked today. */
	private JLabel numMealsTodayLabel;
	private int numMealsTodayCache = 0;
	
	/** The number of available Desserts. */
	private JLabel numDessertsLabel;
	private int numDessertsCache = 0;
	/** The number of Desserts cooked today. */
	private JLabel numDessertsTodayLabel;
	private int numDessertsTodayCache = 0;
	
	private Settlement settlement;
	
	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelCooking(Unit unit, MainDesktopPane desktop) { 

		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelCooking.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelCooking.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		settlement = (Settlement) unit;
		
		// Prepare cooking label panel.
		//JPanel cookingLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel cookingLabelPanel = new JPanel(new GridLayout(2,1,0,0));
		topContentPanel.add(cookingLabelPanel);

		JLabel titleLabel = new JLabel(Msg.getString("TabPanelCooking.title"), JLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
		cookingLabelPanel.add(titleLabel);
		
		// Prepare cooking label.
		//JLabel label = new JLabel(Msg.getString("TabPanelCooking.label"), JLabel.CENTER); //$NON-NLS-1$
		//cookingLabelPanel.add(label);
		
		JPanel splitPanel = new JPanel(new GridLayout(1,2,0,0));
		cookingLabelPanel.add(splitPanel);

		// 2015-01-10 Added TitledBorder
		JPanel d = new JPanel(new GridLayout(2,1,0,0));
		TitledBorder dessertBorder = BorderFactory.createTitledBorder(
				null, "Desserts", javax.swing.border.
			      TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.
			      TitledBorder.DEFAULT_POSITION, null, java.awt.Color.darkGray);
		d.setBorder(dessertBorder);
		//dessertBorder.setTitleColor(Color.orange);

		// Prepare # of available Desserts label
		numDessertsLabel = new JLabel(Msg.getString("TabPanelCooking.servingsOfAvailableDesserts", numDessertsCache), JLabel.LEFT); //$NON-NLS-1$
		d.add(numDessertsLabel);
		// Prepare # of Desserts label
		numDessertsTodayLabel = new JLabel(Msg.getString("TabPanelCooking.servingsOfDessertsToday", numDessertsTodayCache), JLabel.LEFT); //$NON-NLS-1$
		d.add(numDessertsTodayLabel);

		splitPanel.add(d);
		
		JPanel m = new JPanel(new GridLayout(2,1,0,0));
		TitledBorder mealBorder = BorderFactory.createTitledBorder(
				null, "Meal", javax.swing.border.
			      TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.
			      TitledBorder.DEFAULT_POSITION, null, java.awt.Color.darkGray);
		m.setBorder(mealBorder);
		//mealBorder.setTitleColor(Color.orange);

		// Prepare # of available meals label
		numMealsLabel = new JLabel(Msg.getString("TabPanelCooking.numberOfAvailableMeals", numMealsCache), JLabel.LEFT); //$NON-NLS-1$
		m.add(numMealsLabel);				
		// Prepare # of cooked meals label
		numMealsTodayLabel = new JLabel(Msg.getString("TabPanelCooking.numberOfMealsToday", numMealsTodayCache), JLabel.LEFT); //$NON-NLS-1$
		m.add(numMealsTodayLabel);

		splitPanel.add(m);

		// Create scroll panel for the outer table panel.
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setOpaque(false);
		scrollPane.setBackground(new Color(0,0,0,128));
		scrollPane.setForeground(Color.orange);
		scrollPane.setPreferredSize(new Dimension(257, 230));
		// increase vertical mousewheel scrolling speed for this one
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		centerContentPanel.add(scrollPane,BorderLayout.CENTER);

		// Prepare cooking table model.
		cookingTableModel = new CookingTableModel(settlement);

		// Prepare cooking table.
		JTable table = new JTable(cookingTableModel);

		scrollPane.setViewportView(table);
		table.setCellSelectionEnabled(false);
		table.setDefaultRenderer(Double.class, new NumberCellRenderer());
		table.getColumnModel().getColumn(0).setPreferredWidth(140);
		table.getColumnModel().getColumn(1).setPreferredWidth(47);
		table.getColumnModel().getColumn(2).setPreferredWidth(45);
		table.getColumnModel().getColumn(3).setPreferredWidth(45);
		// 2014-12-03 Added the two methods below to make all heatTable columns
		//resizable automatically when its Panel resizes
		table.setPreferredScrollableViewportSize(new Dimension(225, -1));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		// 2014-12-30 Added setTableStyle()
		setTableStyle(table);
		
		repaint();
	}
	
	/**
	 * Sets the style for the table
	 * @param table
	 */
	// 2014-12-30 Added setTableStyle()
	public void setTableStyle(JTable table) {

		DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
		headerRenderer.setOpaque(true); // need to be true for setBackground() to work
		headerRenderer.setBackground(new Color(205, 133, 63));//Color.ORANGE);
		headerRenderer.setForeground( Color.WHITE); 
		headerRenderer.setFont( new Font( "Dialog", Font.BOLD, 12 ) );

		for (int i = 0; i < table.getModel().getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
		}
		MatteBorder border = new MatteBorder(1, 1, 0, 0, Color.orange);
		// set cell to have a light color border
		table.setBorder(border);
		table.setShowGrid(true);
	    table.setShowVerticalLines(true);
		table.setGridColor(new Color(222, 184, 135)); // 222 184 135burlywood
		table.setBorder(BorderFactory.createLineBorder(Color.orange,1)); // HERE  
	

        final JTable ctable = table;
	    SwingUtilities.invokeLater(new Runnable(){
	        public void run()  {
	        	ColumnResizer.adjustColumnPreferredWidths(ctable);	        	
	         } });
		
	}

	
	/**
	 * Delegates the rendering of the table cell header to add
	 *  in an icon on the cells that can be sorted.
	 *
	// 2014-12-30 Added TableHeaderRenderer
	class TableHeaderRenderer implements TableCellRenderer {
		private TableCellRenderer defaultRenderer;

		public TableHeaderRenderer(TableCellRenderer theRenderer) {
			defaultRenderer = theRenderer;
		}
		
		 //Renderer the specified Table Header cell
		public Component getTableCellRendererComponent(JTable table,
				Object value,
				boolean isSelected,
				boolean hasFocus,
				int row,
				int column) {
			Component theResult = defaultRenderer.getTableCellRendererComponent(
					table, value, isSelected, hasFocus,
					row, column);
			if (theResult instanceof JLabel) {
				JLabel cell = (JLabel)theResult;
				cell.setOpaque(true); 
				MatteBorder border = new MatteBorder(1, 1, 0, 0, Color.white);
				cell.setBorder(border);
				//cell.setBackground(new Color(205, 133, 63));//Color.ORANGE);
				//cell.setForeground( Color.WHITE); 
				//cell.setFont( new Font( "Dialog", Font.BOLD, 12 ) );

			}
			return theResult;
		}
	}
*/

	/**
	 * Updates the info on this panel.
	 */
		// Called by TabPanel whenever the Cooking tab is opened
	public void update() {
		//System.out.println("TabPanelCooking.java : update()");
		// Update cooking table.
		cookingTableModel.update();
		updateMeals();
		updateDesserts();
		
	}
	
	// 2015-01-06 Added updateMeals()
	public void updateMeals() {

		int numMeals = 0;
		int numMealsToday = 0;
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
        while (i.hasNext()) { 		
        	// for each building's kitchen in the settlement
        	Building building = i.next();
    		//System.out.println("Building is " + building.getNickName());
        	if (building.hasFunction(BuildingFunction.COOKING)) {      		
				Cooking kitchen = (Cooking) building.getFunction(BuildingFunction.COOKING);			
				
				numMeals += kitchen.getNumberOfCookedMeals();				
				numMealsToday += kitchen.getNumberOfCookedMealsToday();
        	}
        }

		// Update # of available meals
		if (numMealsCache != numMeals) {
			numMealsCache = numMeals;
			numMealsLabel.setText(Msg.getString("TabPanelCooking.numberOfAvailableMeals", numMeals)); //$NON-NLS-1$
		}

		// Update # of meals cooked today
		if (numMealsTodayCache != numMealsToday) {
			numMealsTodayCache = numMealsToday;
			numMealsTodayLabel.setText(Msg.getString("TabPanelCooking.numberOfMealsToday", numMealsToday)); //$NON-NLS-1$
		}	
		
	}

	// 2015-01-06 Added updateDesserts()
	public void updateDesserts() {

		int numDesserts = 0;
		int numDessertsToday = 0;
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
        while (i.hasNext()) { 		
        	// for each building's kitchen in the settlement
        	Building building = i.next();
    		//System.out.println("Building is " + building.getNickName());
        	if (building.hasFunction(BuildingFunction.COOKING)) {      		
				PreparingDessert kitchen = (PreparingDessert) building.getFunction(BuildingFunction.PREPARING_DESSERT);			
				
				numDesserts += kitchen.getNumServingsFreshDessert();				
				numDessertsToday += kitchen.getNumberOfDessertsToday();
        	}
        }

		// Update # of available Desserts
		if (numDessertsCache != numDesserts) {
			numDessertsCache = numDesserts;
			numDessertsLabel.setText(Msg.getString("TabPanelCooking.servingsOfAvailableDesserts", numDesserts)); //$NON-NLS-1$
		}

		// Update # of Desserts cooked today
		if (numDessertsTodayCache != numDessertsToday) {
			numDessertsTodayCache = numDessertsToday;
			numDessertsTodayLabel.setText(Msg.getString("TabPanelCooking.servingsOfDessertsToday", numDessertsToday)); //$NON-NLS-1$
		}	
		
	}
	
	/** 
	 * Internal class used as model for the cooking table.
	 */
	private class CookingTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private Settlement settlement;
		//private java.util.List<Building> buildings;
		//private ImageIcon dotRed; // ingredients missing
		//private ImageIcon dotYellow; // meal not available
		//private ImageIcon dotGreen; // meal available

		private Multiset<String> allServingsSet;
		
		private Multimap<String, Integer> qualityMap;
		private Multimap<String, Integer> allQualityMap;
		
		private Multimap<String, MarsClock> timeMap;	
		private Multimap<String, MarsClock> allTimeMap;
			
		private Collection<Map.Entry<String,Integer>> allQualityMapE ;
		private Collection<Entry<String, MarsClock>> allTimeMapE;
				
		private CookingTableModel(Settlement settlement) {
			this.settlement = settlement;
	
			//dotRed = ImageLoader.getIcon(Msg.getString("img.dotRed")); //$NON-NLS-1$
			//dotYellow = ImageLoader.getIcon(Msg.getString("img.dotYellow")); //$NON-NLS-1$
			//dotGreen = ImageLoader.getIcon(Msg.getString("img.dotGreen")); //$NON-NLS-1$
		;
			allServingsSet = HashMultiset.create();
			allQualityMap = ArrayListMultimap.create();
			allTimeMap = ArrayListMultimap.create();

		}

		public int getRowCount() {
			return numRow;

		}

		public int getColumnCount() {
			return 4;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			//if (columnIndex == 0) dataType = ImageIcon.class;
			if (columnIndex == 0) dataType = String.class;
			else if (columnIndex == 1) dataType = Double.class;
			else if (columnIndex == 2) dataType = Double.class;
			else if (columnIndex == 3) dataType = Double.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			
			String[] columnNames = {
				    "<html>Meal<br>Name</html>",
				    "<html># of<br>Servings</html>",
				    "<html>Best<br>Quality</html>",
				    "<html>Worst<br>Quality</html>"
				};
			
			//if (columnIndex == 0) return Msg.getString("TabPanelCooking.column.s"); //$NON-NLS-1$
			if (columnIndex == 0) return columnNames[0];
					// Msg.getString("TabPanelCooking.column.nameOfMeal"); //$NON-NLS-1$
			else if (columnIndex == 1) return columnNames[1];
					//Msg.getString("TabPanelCooking.column.numberOfServings"); //$NON-NLS-1$
			else if (columnIndex == 2) return columnNames[2];
					//Msg.getString("TabPanelCooking.column.bestQuality"); //$NON-NLS-1$
			else if (columnIndex == 3) return columnNames[3];
					// Msg.getString("TabPanelCooking.column.worstQuality"); //$NON-NLS-1$
			else return null;
		}

		public Object getValueAt(int row, int column) {
			//System.out.println("entering getValueAt()");
			Object result = null;
			/* if (column == 0) {
				if (haveAllIngredients) 
					return dotGreen;
				else return dotRed;
			} else */

	    	
			String name = nameList.get(row);

			if (column == 0) 
				result = name;
			
			else if (column == 1) {		
			    // use Multimap.get(key) returns a view of the values associated with the specified key				
				//int numServings = servingsList.addAll(timeMap.get(name));	
		        int numServings = allServingsSet.count(name);
		        //System.out.println(" numServings is "+ numServings);
				result = numServings;
				//allServingsSet.clear();
			}
			else if (column == 2) {
				int best = 0;
				int value = 0;
				for (Map.Entry<String, Integer> entry : allQualityMapE) {
				    String key = entry.getKey();
				    if (name == key) {
				    	value = entry.getValue();
				    	if (value > best )
				    		best = value;
				    }
				    result = best; 
				    //allQualityMap.clear();
				    	//System.out.println(" best is " +best);
				}
			}
			else if (column == 3) {
				int worst = 10;
				int value = 0;
				for (Map.Entry<String, Integer> entry : allQualityMapE) {
				    String key = entry.getKey();
				    if (name == key) {
				    	value = entry.getValue();
				    	
				    	if (value < worst )
				    		worst = value;
				    }
				    	result = worst;  
				    	//allTimeMap.clear();
				    	//System.out.println(" worst is " + worst);
				}
			}
			else result = null;
			return result;
		}

		// TODO: decide in what situation it needs update and at what time ?
		// update every second or after each meal or once a day ?
		public void update() {
			//System.out.println("CookingTableModel : entering update()");
			cleanUpTable();
			getMultimap();
			fireTableDataChanged();
	
		}
		
		public void getMultimap() {
		
			Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
			
	        while (i.hasNext()) { 		// for each building's kitchen in the settlement

	        	Building building = i.next();
	    		//System.out.println("Building is " + building.getNickName());
	            
	        	if (building.hasFunction(BuildingFunction.COOKING)) {      		
					Cooking kitchen = (Cooking) building.getFunction(BuildingFunction.COOKING);			
					
					qualityMap = kitchen.getQualityMap();
					timeMap = kitchen.getTimeMap();
					
					allQualityMap.putAll(qualityMap);
					allTimeMap.putAll(timeMap);
	        	}
	        }
	
	    	allQualityMapE = allQualityMap.entries();
	    	allTimeMapE = allTimeMap.entries();
			allServingsSet = allQualityMap.keys();
	    	
	    	numRow = allTimeMap.keySet().size();
			//System.out.println(" numRow : " + numRow);
	    	nameSet = allTimeMap.keySet(); 
	        //nameSet = servingsSet.elementSet(); // or using servingsSet
	    	nameList = new ArrayList<String>(nameSet);
	    	
	    	//nameList.addAll(listOfNames);
	    	//System.out.println("nameSet's size : " + nameSet.size());
		}

		/**
		 * Removes all entries on all maps at the beginning of each new sol.
		 */
		public void cleanUpTable() {
			// 1. find any expired meals  
			// 2. remove any expired meals from all 3 maps
			// 3. call cookingTableModel.update()
		    
			// TODO: optimize it so that it doesn't have to check it on every update
			MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
			int currentDay  = currentTime.getSolOfMonth();
			//logger.info
			//System.out.println("cleanUpTable() : Today is sol " + currentDay);
		
			if (dayCache != currentDay) {
				if (!allTimeMap.isEmpty()) {
					allTimeMap.clear();
					allTimeMapE.clear();
				}
				if (!allQualityMap.isEmpty()) {
					allQualityMap.clear();
					allQualityMapE.clear();
				}
				if (!allServingsSet.isEmpty()) 
					allServingsSet.clear();
				//System.out.println("cleanUpTable() : all maps deleted");
				/*
				// TODO: is it better to use .remove() to remove entries and when?
					timeMap.remove(key, value);
					timeMapE.remove(key);
					bestQualityMap.remove(key, value);
					bestQualityMapE.remove(key);
					worstQualityMap.remove(key, value);
					worstQualityMapE.remove(key);	
					servingsSet.remove(key);
		*/
				dayCache = currentDay;
				
			}
			
		}
	}
}