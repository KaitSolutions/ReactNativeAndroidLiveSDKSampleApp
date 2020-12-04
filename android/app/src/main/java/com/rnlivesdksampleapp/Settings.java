/*
Copyright (c) 2013 - 2018 Anoto AB. All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

Redistribution of source code must retain the above copyright notice, this list of 
conditions and the following disclaimer.

Redistribution in binary form must reproduce the above copyright notice, this list of 
conditions and the following disclaimer in the documentation and/or other materials provided 
with the distribution. 

Neither the name of Anoto AB nor the names of contributors may be used to endorse or promote 
products derived from this software without specific prior written permission. 

This software is provided "AS IS," without a warranty of any kind. ALL EXPRESS OR IMPLIED 
CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. 
ANOTO AB AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A 
RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT 
WILL ANOTO AB OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, 
INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND 
REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS 
SOFTWARE, EVEN IF ANOTO AB HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. 
*/

package com.rnlivesdksampleapp;

import com.anoto.live.penaccess.client.ISettings;

import java.io.Serializable;

public class Settings implements ISettings, Serializable {

	private static final long serialVersionUID = 1L;
	private boolean _autoDiscover;
	private boolean _scanForNewPen;
	private boolean _enableDP201;
	
	public Settings(boolean aScanForNewPen, boolean aEnableDP201) {
		_scanForNewPen = aScanForNewPen;
		_enableDP201 = aEnableDP201;
	}
	
	public void setAutoDiscover(boolean aAutoDiscover) {
		_autoDiscover = aAutoDiscover;
	}
	
	public void setScanForNewPen(boolean aScanForNewPen) {
		_scanForNewPen = aScanForNewPen;
	}

	public void setEnableDP201(boolean aEnableDP201) {
		_enableDP201 = aEnableDP201;
	}
	
	@Override
	public boolean autoDiscover() {
		return _autoDiscover;
	}
	
	@Override
	public boolean scanForNewPen() {
		return _scanForNewPen;
	}
	
	@Override
	public boolean enableDP201() {
		return _enableDP201;
	}
}
