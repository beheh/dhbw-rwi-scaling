package rwi.scaling.client;

import java.net.URL;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import rwi.scaling.common.interfaces.IInstanceService;

public class ScalingClientActivator implements BundleActivator {

	private BundleContext context;
	private IInstanceService service;

	public void start(BundleContext context) throws Exception {
		this.context = context;
		// Register directly with the service
		ServiceReference<?> reference = context.getServiceReference(IInstanceService.class.getName());
		if(reference != null) {
			service = (IInstanceService) context.getService(reference);
			System.out.println("Using " + service.getProviderName() + " as provider for system scaling");
			ControlThread thread = new ControlThread(service, new URL("http://localhost:8080/info"));
			thread.start();
		}
		else {
			System.out.println("Cannot scale system: No IInstanceService registered");
		}
	}

	public void stop(BundleContext context) throws Exception {
		System.out.println("Stopping scaling client");
	}

}