/**
 *  BlueCove - Java library for Bluetooth
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  @version $Id$
 */
package com.intel.bluetooth.test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

import com.intel.bluetooth.DebugLog;

/**
 * This class provides a stand-alone test for Blue Cove
 * 
 */
public class SimpleDiscovery {

	public static void main(String[] args) {

		EnvSettings.setSystemProperties();

		LocalDevice l;
		try {
			l = LocalDevice.getLocalDevice();
		} catch (BluetoothStateException e) {
			System.err.println("Cannot get local device: " + e);
			return;
		}

		System.out.println("Local btaddr is " + l.getBluetoothAddress());
		System.out.println("Local name is " + l.getFriendlyName());

		String bluecoveVersion = LocalDevice.getProperty("bluecove");
		if (bluecoveVersion != null) {
			System.out.println("bluecove:" + bluecoveVersion);
			System.out.println("stack:" + LocalDevice.getProperty("bluecove.stack"));
			System.out.println("stack version:" + LocalDevice.getProperty("bluecove.stack.version"));
			System.out.println("radio manufacturer:" + LocalDevice.getProperty("bluecove.radio.manufacturer"));
			System.out.println("radio version:" + LocalDevice.getProperty("bluecove.radio.version"));
		}

		BluetoothInquirer bi = new BluetoothInquirer();

		while (true) {

			System.out.println("Starting inquiry");

			if (!bi.startInquiry()) {
				System.out.println("Cannot start inquiry, Exit ...");
				break;
			}
			while (bi.inquiring) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
			}
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}

			System.out.println("Testing getFriendlyName");
			for (Iterator i = bi.devices.iterator(); i.hasNext();) {
				RemoteDevice btDevice = (RemoteDevice) i.next();
				try {
					System.out.println("Found " + btDevice.getBluetoothAddress() + " : "
							+ btDevice.getFriendlyName(true));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static class BluetoothInquirer implements DiscoveryListener {

		boolean inquiring;

		List devices;

		public boolean startInquiry() {
			inquiring = false;
			devices = new Vector();
			try {
				inquiring = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, this);
			} catch (BluetoothStateException e) {
				System.err.println("Cannot start inquiry: " + e);
				return false;
			}
			return inquiring;
		}

		public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
			DebugLog.debug("deviceDiscovered");
			devices.add(btDevice);
			StringBuffer name;
			try {
				DebugLog.debug("call getFriendlyName");
				name = new StringBuffer(btDevice.getFriendlyName(false));
			} catch (IOException ioe) {
				ioe.printStackTrace();
				name = new StringBuffer();
			}
			while (name.length() < 20)
				name.append(' ');
			System.out.println("Found " + btDevice.getBluetoothAddress() + " : " + name + " : " + cod);
		}

		public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		}

		public void serviceSearchCompleted(int transID, int respCode) {
		}

		public void inquiryCompleted(int discType) {
			inquiring = false;
		}

	}
}