package rwi.scaling.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.Vpc;

import rwi.scaling.common.interfaces.IInstanceService;

public class AmazonInstanceService implements IInstanceService {

	private AmazonEC2 ec2 = null;
	private String subnetId = "123";
	private Collection<String> securityGroups = new ArrayList<>();

	public AmazonInstanceService() {
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider().getCredentials();
		}
		catch(Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. " + "Please make sure that your credentials file is at the correct " + "location (~/.aws/credentials), and is in valid format.", e);
		}
		ec2 = new AmazonEC2Client(credentials);
		ec2.setRegion(Region.getRegion(Regions.EU_WEST_1));
		securityGroups.add("rwi");
		System.out.println("Initialized AmazonInstanceService");
	}

	@Override
	public String getProviderName() {
		return "Amazon AWS";
	}

	@Override
	public boolean requestInstances(int instanceCount) {
		RunInstancesRequest request = new RunInstancesRequest("id", instanceCount, instanceCount);
		
		// setup vpc
		request.setSubnetId(subnetId);
		
		// setup firewall
		request.setSecurityGroups(securityGroups);
		
		// setup additional info (server ip)
		request.setAdditionalInfo(null);
		
		// launch instance
		System.out.println("Sending RunInstanceRequest to Amazon... ");
		RunInstancesResult result = ec2.runInstances(request);

		// tag instances
		List<Instance> instances = result.getReservation().getInstances();
		int i = 0;
		for(Instance instance : instances) {
			i++;
			CreateTagsRequest createTagsRequest = new CreateTagsRequest();
			createTagsRequest.withResources(instance.getInstanceId()).withTags(new Tag("Name", "rwi-instance-" + i));
			ec2.createTags(createTagsRequest);
		}

		return true;
	}
}
