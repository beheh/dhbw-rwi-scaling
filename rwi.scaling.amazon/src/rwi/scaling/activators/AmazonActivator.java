package rwi.scaling.activators;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import rwi.scaling.common.interfaces.IInstanceService;
import rwi.scaling.services.AmazonInstanceService;

/**
 * Registers an IInstanceSerivice with the Amazon service implementation.
 * 
 * @author Benedict Etzel
 *
 */
public class AmazonActivator implements BundleActivator {	

	private ServiceReference<?> reference;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		reference = bundleContext.registerService(IInstanceService.class.getName(), new AmazonInstanceService(), null).getReference();
		System.out.println("IInstanceService is registered");
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		bundleContext.ungetService(reference);
	}

}