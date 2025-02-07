package org.opennms.horizon.inventory.service.taskset;

import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.azure.contract.AzureCollectorRequest;
import org.opennms.azure.contract.AzureCollectorResourcesRequest;
import org.opennms.horizon.azure.api.AzureScanItem;
import org.opennms.horizon.azure.api.AzureScanNetworkInterfaceItem;
import org.opennms.horizon.inventory.model.discovery.active.AzureActiveDiscovery;
import org.opennms.horizon.inventory.repository.IpInterfaceRepository;
import org.opennms.horizon.inventory.repository.NodeRepository;
import org.opennms.horizon.inventory.repository.SnmpInterfaceRepository;
import org.opennms.horizon.shared.azure.http.AzureHttpClient;

import static org.mockito.Mockito.mock;

public class CollectorTaskSetServiceTest {

    private final NodeRepository nodeRepository = mock(NodeRepository.class);
    private CollectorTaskSetService taskSetService = new CollectorTaskSetService(nodeRepository,
        mock(IpInterfaceRepository.class), mock(SnmpInterfaceRepository.class));

    public static final long NODE_ID = 1;
    public static final String TENANT_ID = "tenantId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String CLIENT_ID = "clientId";
    public static final String DIRECTORY_ID = "directoryId";
    public static final String SUBSCRIPTION_ID = "subscriptionId";

    private static final String AZURE_LOCATION = "eastus";


    @Test
    public void testAddAzureCollectorTask() throws InvalidProtocolBufferException {
        AzureActiveDiscovery discovery = new AzureActiveDiscovery();
        discovery.setClientSecret(CLIENT_SECRET);
        discovery.setClientId(CLIENT_ID);
        discovery.setDirectoryId(DIRECTORY_ID);
        discovery.setSubscriptionId(SUBSCRIPTION_ID);
        discovery.setTenantId(TENANT_ID);
        var scanItem = AzureScanItem.newBuilder()
            .setName("nodeName")
            .setResourceGroup("resourceGroup")
            .addNetworkInterfaceItems(AzureScanNetworkInterfaceItem.newBuilder()
                .setInterfaceName("interface1").setLocation(AZURE_LOCATION)
                .setPublicIpAddress(AzureScanNetworkInterfaceItem.newBuilder()
                    .setName("publicIp").setLocation(AZURE_LOCATION)))
            .addNetworkInterfaceItems(AzureScanNetworkInterfaceItem.newBuilder()
                .setInterfaceName("interface2").setLocation(AZURE_LOCATION)
                .setPublicIpAddress(AzureScanNetworkInterfaceItem.newBuilder()
                    .setName("publicIp2").setLocation(AZURE_LOCATION)))
            .addNetworkInterfaceItems(AzureScanNetworkInterfaceItem.newBuilder()
                .setInterfaceName("interface3").setLocation(AZURE_LOCATION))
            .build();
        var task = taskSetService.addAzureCollectorTask(discovery, scanItem, NODE_ID);
        Assert.assertEquals("AZURECollector", task.getPluginName());
        Assert.assertEquals(NODE_ID, task.getNodeId());

        var request = task.getConfiguration().unpack(AzureCollectorRequest.class);
        Assert.assertEquals(CLIENT_ID, request.getClientId());
        Assert.assertEquals(CLIENT_SECRET, request.getClientSecret());
        Assert.assertEquals(SUBSCRIPTION_ID, request.getSubscriptionId());
        Assert.assertEquals(DIRECTORY_ID, request.getDirectoryId());
        var resourceList = request.getCollectorResourcesList();
        var interfaceList = resourceList.stream().filter(r -> r.getType().equals(AzureHttpClient.ResourcesType.NETWORK_INTERFACES.getMetricName()))
            .map(AzureCollectorResourcesRequest::getResource).toArray();
        var publicIpList = resourceList.stream().filter(r -> r.getType().equals(AzureHttpClient.ResourcesType.PUBLIC_IP_ADDRESSES.getMetricName()))
            .map(AzureCollectorResourcesRequest::getResource).toArray();
        // interface3 should not be included due to no public interface
        Assert.assertArrayEquals(new String[]{"interface1", "interface2"}, interfaceList);
        Assert.assertArrayEquals(new String[]{"publicIp", "publicIp2"}, publicIpList);
    }
}
