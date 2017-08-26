package com.checkmarx.engine.manager;

import java.util.List;

import com.checkmarx.engine.domain.DynamicEngine;
import com.checkmarx.engine.domain.ScanSize;

/**
 * Manages provisioning of dynamic engines utilizing an underlying IaaS provider, e.g. AWS.
 * @see AwsEngines
 * 
 * @author randy@checkmarx.com
 */
public interface EngineProvisioner {
	
	public static final String CX_ROLE_TAG = "cx-role";
	public static final String CX_VERSION_TAG = "cx-version";
	public static final String CX_SIZE_TAG = "cx-engine-size";
	
	/**
	 * Checkmarx server roles, used for compute instance tagging.
	 */
	//FIXME: move to better location
	public enum CxServerRole {
		ENGINE,
		MANAGER
	}
	
	/**
	 * Queries the underlying IaaS for provisioned engines.
	 * @return list of provisioned engines.
	 */
	List<DynamicEngine> listEngines();

	/**
	 * Launches a dynamic engine.
	 * 
	 * Provisions an engine if not already provisioned, or starts an already
	 * provisioned engine.
	 * <br/> <br/>
	 * Tags the provisioned engine with the supplied name, size and role 
	 * (CxRole.ENGINE).
	 * <br/> <br/>
	 * Launch will wait/blocks until the engine enters a running state
	 * as reported by the underlying IaaS provider.  This may not mean the
	 * engine is responding yet.  This make take several minutes.
	 * 
	 * @param engine to launch
	 * @param size of the engine to launch
	 * @param waitForSpinup if true, blocks until the engine process responds to requests.
	 * 						This may take several more minutes.
	 */
	void launch(DynamicEngine engine, ScanSize size, boolean waitForSpinup);
	

	/**
	 * Stops the supplied dynamic engine.  Underlying implementation may terminate the engine.
	 * 
	 * @param engine to stop
	 */
	void stop(DynamicEngine engine);

	/**
	 * Stops the supplied dynamic engine. Underlying implementation may terminate the engine.
	 * 
	 * @param engine to stop
	 * @param forceTerminate if true, always terminates the engine
	 */
	void stop(DynamicEngine engine, boolean forceTerminate);

}
