/*
 * Copyright (c) 2011, salesforce.com, inc.
 * All rights reserved.
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of salesforce.com, inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission of salesforce.com, inc.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package ie.enclude.flexibus;

import ie.enclude.flexibus.database.DBAdapter;
import ie.enclude.flexibus.util.BusTrip;
import ie.enclude.flexibus.util.Passenger;
import ie.enclude.salesforce.operation.DataHandleFactory;

import java.util.List;

import android.app.Application;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.RestClient;

/**
 * Application class for our application.
 */
public class FlexibusApp extends Application 
{
    static public RestClient client;
	private DBAdapter db=null;
	private DataHandleFactory daf=null;
	private String m_AlertDialogMessage="";
	private String m_savedBusName=CONSTANTS.UNDEFINED_BUS_NAME;
	private String m_savedOdoReading="0";
	private String m_savedFuelPurchased = "0";
	private List<Passenger>currentTripPassengers=null;
	private String currentBusTripID="";
	private boolean passengerListIsDirty=false;
	private int currentFarePayingPassengers;
	private int currentFreePassengers;
	
	List<BusTrip>todaysBusTrips=null;

	@Override
	public void onCreate() {
		super.onCreate();
		SalesforceSDKManager.initNative(getApplicationContext(), new KeyImpl(), FlexibusActivity.class);

		/*
		 * Un-comment the line below to enable push notifications in this app.
		 * Replace 'pnInterface' with your implementation of 'PushNotificationInterface'.
		 * Add your Google package ID in 'bootonfig.xml', as the value
		 * for the key 'androidPushNotificationClientId'.
		 */
		// SalesforceSDKManager.getInstance().setPushNotificationReceiver(pnInterface);
	}
	
	public DBAdapter getDatabase()
	{
		if (db == null)
		{
			db = new DBAdapter (this);
		}
		return db;
	}
	
	public DataHandleFactory getDataHandler() {
		return daf;
	}

	public void setDataHandler(DataHandleFactory newdaf) {
		daf = newdaf;
	}

	public boolean LoggedIn() {
		return daf.IsLoggedIn();
	}

	public String getCurrentBusName() {
		return daf.getCurrentBusName();
	}

	public Object getCurrentBusOdoReading() {
		return daf.getCurrentBusOdoReading();
	}

	public CharSequence getCurrentAlertDialogMessage() {
		return m_AlertDialogMessage;
	}
	
	public void setAlertDialogMessage(String msg)
	{
		m_AlertDialogMessage = msg;
	}

	public void setSavedBusState(String busName, String savedOdoReading) 
	{
		m_savedBusName = busName;
		m_savedOdoReading = savedOdoReading;
	}
	
	public String getSavedBusName()
	{
		return m_savedBusName;
	}
	
	public String getSavedBusOdoReading()
	{
		return m_savedOdoReading;
	}

	public String getSavedFuelPurchased()
	{
		return m_savedFuelPurchased;
	}

	public void setSavedBusState(String busName, String odoReading, String fuelPurchased) 
	{
		m_savedBusName = busName;
		m_savedOdoReading = odoReading;
		m_savedFuelPurchased  = fuelPurchased;
	}

	public List<BusTrip> getCurrentServiceList()
	{
		return todaysBusTrips;
	}
	
	public List<Passenger> getCurrentPassengerList() 
	{
		if (currentBusTripID != null && currentTripPassengers!= null && db.checkBusTripIDIsCurrent(currentBusTripID))
		{
			return currentTripPassengers;
		}
		else
		{
			return null;
		}
	}

	public void setCurrentBusTripID (String tripID)
	{
		if (tripID == currentBusTripID) return;
		currentBusTripID = tripID;
		currentTripPassengers = null;
		setPassengerListDirty();
	}
	
	public String getCurrentBusTripID ()
	{
		return currentBusTripID;
	}
	
	public void saveCurrentPassengerList(String tripID, List<Passenger> passengers, int freePassengers, int farePayingPassengers) 
	{
		currentBusTripID = tripID;
		currentTripPassengers = passengers;
		currentFarePayingPassengers = farePayingPassengers;
		currentFreePassengers = freePassengers;
		if (currentTripPassengers != null)
		{
			db.saveNumberOfPassengersOnTrip(currentFreePassengers, currentFarePayingPassengers);
		}
	}

	public void setPassengerListDirty() 
	{
		passengerListIsDirty = true;
	}
	
	public boolean isPassengerListDirty()
	{
		return passengerListIsDirty;
	}
	
	public void clearPassengerListFlag()
	{
		passengerListIsDirty = false;
	}

	public void clearPassengerList() 
	{
		currentTripPassengers = null;
		currentBusTripID = "";
		currentFarePayingPassengers = 0;
		currentFreePassengers = 0;
	}
	
	public int getFarePayingPassengers()
	{
		return currentFarePayingPassengers;
	}
	
	public int getFreePassengers()
	{
		return currentFreePassengers;
	}
}