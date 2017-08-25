package com.checkmarx.engine.manager;

import com.checkmarx.engine.domain.DynamicEngine;
import com.checkmarx.engine.domain.ScanSize;

public interface EngineProvisioner {
	
	public static final String CX_ROLE_TAG = "cx-role";
	public static final String CX_VERSION_TAG = "cx-version";
	
	public enum CxRoles {
		ENGINE,
		MANAGER
	}

	void launch(DynamicEngine engine, ScanSize size, boolean waitForSpinup);
	
	void stop(DynamicEngine engine);

}
