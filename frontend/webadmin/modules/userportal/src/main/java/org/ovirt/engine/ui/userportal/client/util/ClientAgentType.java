package org.ovirt.engine.ui.userportal.client.util;

public class ClientAgentType {
	public String os;
	public String browser;
	public Float version;
	private String versionSearchString;
	public String platform;

	public ClientAgentType() {
		browser = getBrowser();
		version = Float.parseFloat(getVersion());
		os = getOS();
		platform = getPlatform();
	}

	public native String getBrowser() /*-{
		var data = this.@org.ovirt.engine.ui.userportal.client.util.ClientAgentType::getDataBrowser()();
		var z = this.@org.ovirt.engine.ui.userportal.client.util.ClientAgentType::searchString(Ljava/lang/Object;)(data);

		return z;
	}-*/;

	public native Object searchString(Object data) /*-{
		for (var i=0;i<data.length;i++)	{
			var dataString = data[i].string;
			var dataProp = data[i].prop;
			this.@org.ovirt.engine.ui.userportal.client.util.ClientAgentType::versionSearchString = data[i].versionSearch || data[i].identity; 
			if (dataString) {
				if (dataString.indexOf(data[i].subString) != -1)
					return data[i].identity;
			}
			else if (dataProp)
				return data[i].identity;
		}
	}-*/;

	public native String getOS() /*-{
		var dos = this.@org.ovirt.engine.ui.userportal.client.util.ClientAgentType::getdataOS()();
		var os = this.@org.ovirt.engine.ui.userportal.client.util.ClientAgentType::searchString(Ljava/lang/Object;)(dos);

		return os || "an unknown OS";
	}-*/;

	public native Object getDataBrowser() /*-{
		dataBrowser = [
		{
			string: navigator.userAgent,
			subString: "Chrome",
			identity: "Chrome"
		},
		{ 	string: navigator.userAgent,
			subString: "OmniWeb",
			versionSearch: "OmniWeb/",
			identity: "OmniWeb"
		},
		{
			string: navigator.vendor,
			subString: "Apple",
			identity: "Safari",
			versionSearch: "Version"
		},
		{
			prop: window.opera,
			identity: "Opera"
		},
		{
			string: navigator.vendor,
			subString: "iCab",
			identity: "iCab"
		},
		{
			string: navigator.vendor,
			subString: "KDE",
			identity: "Konqueror"
		},
		{
			string: navigator.userAgent,
			subString: "Firefox",
			identity: "Firefox"
		},
		{
			string: navigator.vendor,
			subString: "Camino",
			identity: "Camino"
		},
		{		// for newer Netscapes (6+)
			string: navigator.userAgent,
			subString: "Netscape",
			identity: "Netscape"
		},
		{
			string: navigator.userAgent,
			subString: "MSIE",
			identity: "Explorer",
			versionSearch: "MSIE"
		},
		{
			string: navigator.userAgent,
			subString: "Gecko",
			identity: "Mozilla",
			versionSearch: "rv"
		},
		{ 		// for older Netscapes (4-)
			string: navigator.userAgent,
			subString: "Mozilla",
			identity: "Netscape",
			versionSearch: "Mozilla"
		}
		]

		return dataBrowser;
	}-*/;

	public native Object getdataOS() /*-{
		dataOS = [
		{
			string: navigator.platform,
			subString: "Win",
			identity: "Windows"
		},
		{
			string: navigator.platform,
			subString: "Mac",
			identity: "Mac"
		},
		{
			   string: navigator.userAgent,
			   subString: "iPhone",
			   identity: "iPhone/iPod"
		},
		{
			string: navigator.platform,
			subString: "Linux",
			identity: "Linux"
		}
		]

		return dataOS;
	}-*/;

	public native String getVersion() /*-{
		var z = this.@org.ovirt.engine.ui.userportal.client.util.ClientAgentType::searchVersion(Ljava/lang/Object;)(navigator.userAgent) ||
		this.@org.ovirt.engine.ui.userportal.client.util.ClientAgentType::searchVersion(Ljava/lang/Object;)(navigator.userAgent) ||
		this.@org.ovirt.engine.ui.userportal.client.util.ClientAgentType::searchVersion(Ljava/lang/Object;)(navigator.appVersion) ||
		"0";

		return z;
	}-*/;
	
	//cpuClass only works for Opera/Safari/Explorer
	public native String getCpuClass() /*-{
		return navigator.cpuClass;
	}-*/;
	
	public native String getPlatform() /*-{
		return navigator.platform;
	}-*/;

	public native String searchVersion(Object data) /*-{
		var vss = this.@org.ovirt.engine.ui.userportal.client.util.ClientAgentType::versionSearchString;
		var index = data.indexOf(vss);
		if (index == -1) return;
		var result = parseFloat(data.substring(index+vss.length+1));

		//GWT fails to return float, returning string instead.
		return result.toString();
	}-*/;
}
