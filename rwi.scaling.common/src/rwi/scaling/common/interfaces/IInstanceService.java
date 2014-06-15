package rwi.scaling.common.interfaces;

public interface IInstanceService {
	/**
	 * Returns the name of the provider, e.g. "Amazon".
	 * @return
	 */
	public String getProviderName();
	
	/**
	 * Request a additional RWI instances to be launched.
	 * @param instanceCount the amount of instances to launch
	 * @return
	 */
	public boolean requestInstances(int instanceCount);
}
