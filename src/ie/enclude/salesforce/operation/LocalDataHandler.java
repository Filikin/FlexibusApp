package ie.enclude.salesforce.operation;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ie.enclude.flexibus.CONSTANTS;
import ie.enclude.flexibus.FlexibusActivity;
import ie.enclude.flexibus.FlexibusApp;
import ie.enclude.flexibus.SalesforceResponseInterface;
import ie.enclude.flexibus.database.DBAdapter;
import ie.enclude.flexibus.util.BusTrip;
import ie.enclude.flexibus.util.Passenger;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class LocalDataHandler 
{
	public static final String DEBUG_TAG="FlexibusLogging";

	private static ApexApiCaller bind = new ApexApiCaller();
	
	private String[] busNames;
	private String[] busOdoReadings;
	private JSONObject m_selectedBus;
	private JSONObject[] buses;
	private Map<String, List<String>> listofPickLists;
	private Map<String, String> pickListResults;
	private List<String> startupCheckLabels;
	private Map<String, String> startupCheckNames;
	private String startupCheckListFaultReport;
	
	public LocalDataHandler(FlexibusActivity mainActivity) 
	{
		m_selectedBus = null;
		listofPickLists = new HashMap<String, List<String>>(30);
		pickListResults = new HashMap<String, String>(30);
		startupCheckNames = new HashMap<String, String>(30);
		startupCheckLabels = null;
	}
	
	// login caller
	public String loginWithApi()
	{
		return bind.login();
	}
	
	/** refresh local data */
	public void dataRefresh() {
	}
	
	protected boolean getBuses(SalesforceResponseInterface sfrp) 
	{
		return bind.getBuses(this, sfrp);
	}
	
	protected String addOdometerReadingToSelectedBus (String odoReading, SalesforceResponseInterface sfrp)
	{
		return bind.addOdometerReadingToSelectedBus(this, odoReading, sfrp);
	}
	

	public void setBusNames(String[] theBuses) {
		busNames = theBuses;
	}

	public void setBuses(JSONObject[] jsonBusObjects) {
		buses = jsonBusObjects;
	}

	public String[] getBusNames() {
		return busNames;
	}

	public JSONObject[] getBusesObjects() {
		return buses;
	}

	public void setBusOdoReading(String[] theBuses) {
		busOdoReadings = theBuses;
	}

	public String[] getBusOdoReading() {
		return busOdoReadings;
	}

	public void setSelectedBus(CharSequence busName) {
		for (int i=0;i<buses.length;i++) 
		{
			try 
			{
				if (buses[i].getString("Name") == busName)
				{
					m_selectedBus = buses[i];
				}
			} 
			catch (JSONException e) 
			{
				Log.v(DEBUG_TAG, "setSelectedBus " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public String getCurrentBusName() 
	{
		if (m_selectedBus != null)
		{
			try 
			{
				return m_selectedBus.getString("Name");
			}
			catch (JSONException e) 
			{
				Log.v(DEBUG_TAG, "getCurrentBusName " + e.getMessage());
				e.printStackTrace();
				return CONSTANTS.UNDEFINED_BUS_NAME;
			}
		}
		else
		{
			return CONSTANTS.UNDEFINED_BUS_NAME;
		}
	}

	public String getCurrentBusID()
	{
		if (m_selectedBus != null)
		{
			try
			{
				return m_selectedBus.getString("Id");
			}
			catch (JSONException e) 
			{
				Log.v(DEBUG_TAG, "getCurrentBusID " + e.getMessage());
				e.printStackTrace();
				return "0";
			}
		}
		else
		{
			return "0";
		}
	}
	
	public String getCurrentBusOdoReading() 
	{
		if (m_selectedBus != null)
		{
			try 
			{
				String odoReading = m_selectedBus.getString("ODO_Reading__c");
				int decimalPoint = odoReading.indexOf('.');
				if (decimalPoint >= 0)
				{
					return odoReading.substring(0, decimalPoint);
				}
				else
				{
					return odoReading;
				}
			}
			catch (JSONException e) 
			{
				Log.v(DEBUG_TAG, "getCurrentBusOdoReading " + e.getMessage());
				e.printStackTrace();
				return "";
			}
		}
		else
		{
			return "";
		}
	}

	public void setCurrentBusOdoReading(String odoReading) 
	{
		try 
		{
			m_selectedBus.put ("ODO_Reading__c", odoReading);
		} 
		catch (JSONException e) 
		{
			Log.v(DEBUG_TAG, "setCurrentBusOdoReading " + e.getMessage());
			e.printStackTrace();
		}
	}

	// on startup this is called just in case the odo reading has been updated by a different phone
	public boolean initialiseSelectedBus(String busName, String busOdoReading, SalesforceResponseInterface sfrp) 
	{
		if (m_selectedBus == null)
		{
			m_selectedBus = bind.getOneBus(busName, sfrp);
		}
		else
		{
			try 
			{
				m_selectedBus.put ("Name", busName);
				m_selectedBus.put ("ODO_Reading__c", busOdoReading);
			} 
			catch (JSONException e) {
				Log.v(DEBUG_TAG, "initialiseSelectedBus " + e.getMessage());
				e.printStackTrace();
				return false;
			}
		}
		return (m_selectedBus != null);
	}

	public String getLastError() 
	{
		return bind.getLastError();
	}

	public List<String> getFieldList(String objectName) 
	{
		if (startupCheckLabels == null)
		{
			startupCheckLabels = bind.getFieldList(this, objectName);
		}
		return startupCheckLabels;
	}
	
	public void addPickListToDriversStartupCheckList (String checkListItem, List<String>pickList)
	{
		listofPickLists.put(checkListItem, pickList);
	}

	public String[] getPickList(String pickListName) 
	{
		List<String> pickList = listofPickLists.get(pickListName);
		String[] arrayPickList = new String[pickList.size()];
		for (int i=0; i<pickList.size(); i++)
		{
			arrayPickList[i] = pickList.get(i);
		}
		return arrayPickList;
	}

	public void addFaultReportTextToCheckList(String faultReport) {
		startupCheckListFaultReport = faultReport;
	}

	public String sendStartupCheckListToSalesforce() {
		return bind.sendStartupCheckListToSalesforce (this, listofPickLists, startupCheckLabels, startupCheckListFaultReport);
	}

	public void setDriversStartupCheckListName(String label, String name) 
	{
		startupCheckNames.put(label, name);
	}

	public String getStartupCheckListFieldName(String label)
	{
		return startupCheckNames.get(label);
	}

	public void setPickListResult(String itemLabel, String itemPicked) 
	{
		pickListResults.put(itemLabel, itemPicked);
	}
	
	public String getPickListResults (String label)
	{
		return pickListResults.get(label);
	}

	public void wipePreviousStartupListAnswers() 
	{
		pickListResults.clear();
		startupCheckListFaultReport = "";
	}

	public String recordFuelPurchased(FlexibusApp gs) 
	{
		return bind.recordFuelPurchased(this, gs);
	}

	public List<BusTrip> getTodaysBusTrips() {
		return bind.getTodaysBusTrips(this);
	}

	public List<Passenger> getPassengerList(String busTripID) {
		return bind.getPassengerList(this, busTripID);
	}

	public String sendTripReports(DBAdapter database) {
		return bind.sendTripReportsUsingSoap(this, database);
	}

}