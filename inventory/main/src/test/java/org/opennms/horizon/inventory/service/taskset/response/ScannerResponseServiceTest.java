/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.horizon.inventory.service.taskset.response;

import com.google.common.base.Objects;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.opennms.horizon.azure.api.AzureScanItem;
import org.opennms.horizon.azure.api.AzureScanNetworkInterfaceItem;
import org.opennms.horizon.azure.api.AzureScanResponse;
import org.opennms.horizon.inventory.dto.NodeCreateDTO;
import org.opennms.horizon.inventory.dto.NodeDTO;
import org.opennms.horizon.inventory.exception.EntityExistException;
import org.opennms.horizon.inventory.exception.LocationNotFoundException;
import org.opennms.horizon.inventory.model.AzureInterface;
import org.opennms.horizon.inventory.model.Node;
import org.opennms.horizon.inventory.model.discovery.active.AzureActiveDiscovery;
import org.opennms.horizon.inventory.repository.IpInterfaceRepository;
import org.opennms.horizon.inventory.repository.NodeRepository;
import org.opennms.horizon.inventory.repository.discovery.active.AzureActiveDiscoveryRepository;
import org.opennms.horizon.inventory.service.AzureInterfaceService;
import org.opennms.horizon.inventory.service.IpInterfaceService;
import org.opennms.horizon.inventory.service.MonitoredServiceService;
import org.opennms.horizon.inventory.service.MonitoredServiceTypeService;
import org.opennms.horizon.inventory.service.NodeService;
import org.opennms.horizon.inventory.service.SnmpConfigService;
import org.opennms.horizon.inventory.service.SnmpInterfaceService;
import org.opennms.horizon.inventory.service.TagService;
import org.opennms.horizon.inventory.service.discovery.active.IcmpActiveDiscoveryService;
import org.opennms.horizon.inventory.service.taskset.TaskSetHandler;
import org.opennms.taskset.contract.ScanType;
import org.opennms.taskset.contract.ScannerResponse;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScannerResponseServiceTest {
    private final AzureActiveDiscoveryRepository azureActiveDiscoveryRepository = mock(AzureActiveDiscoveryRepository.class);
    private final NodeRepository nodeRepository = mock(NodeRepository.class);
    private final NodeService nodeService = mock(NodeService.class);
    private final TaskSetHandler taskSetHandler = mock(TaskSetHandler.class);
    private final IpInterfaceService ipInterfaceService = mock(IpInterfaceService.class);
    private final SnmpInterfaceService snmpInterfaceService = mock(SnmpInterfaceService.class);
    private final AzureInterfaceService azureInterfaceService = mock(AzureInterfaceService.class);
    private final TagService tagService = mock(TagService.class);
    private final SnmpConfigService snmpConfigService = mock(SnmpConfigService.class);
    private final IcmpActiveDiscoveryService icmpActiveDiscoveryService = mock(IcmpActiveDiscoveryService.class);
    private final IpInterfaceRepository ipInterfaceRepository = mock(IpInterfaceRepository.class);
    private final MonitoredServiceTypeService monitoredServiceTypeService = mock(MonitoredServiceTypeService.class);
    private final MonitoredServiceService monitoredServiceService = mock(MonitoredServiceService.class);
    private ScannerResponseService scannerResponseService = new ScannerResponseService(
        azureActiveDiscoveryRepository, nodeRepository, nodeService, taskSetHandler, ipInterfaceService,
        snmpInterfaceService, azureInterfaceService, tagService, snmpConfigService,
        icmpActiveDiscoveryService, ipInterfaceRepository, monitoredServiceTypeService, monitoredServiceService
    );

    public static final long nodeId = 1L;
    public static final String nodeLabel = "nodeLabel";

    public static final long locationId = 1L;
    public static final long discoveryId = 1L;

    public static final String TENANT_ID = "tenantId";

    @Test
    void testAzureResponse() throws InvalidProtocolBufferException, EntityExistException, LocationNotFoundException {
        // prepare
        AzureActiveDiscovery discovery = new AzureActiveDiscovery();
        discovery.setId(discoveryId);
        discovery.setTenantId(TENANT_ID);
        discovery.setClientSecret("clientSecret");
        discovery.setDirectoryId("directoryId");
        discovery.setSubscriptionId("subscriptionId");
        discovery.setClientId("clientId");

        when(azureActiveDiscoveryRepository.findByTenantIdAndId(TENANT_ID, discoveryId)).thenReturn(Optional.of(discovery));
        when(nodeRepository.findByTenantLocationIdAndNodeLabel(TENANT_ID, locationId, nodeLabel)).thenReturn(Optional.empty());

        Node node = new Node();
        node.setId(nodeId);
        node.setNodeLabel(nodeLabel);
        node.setTenantId(TENANT_ID);
        when(nodeService.createNode(any(NodeCreateDTO.class), eq(ScanType.AZURE_SCAN), eq(TENANT_ID))).thenReturn(node);

        String vmName = "vmName";
        String resourceGroup = "resourceGroup";
        String osName = "osName";
        String osVersion = "osVersion";
        String location = "eastus";
        String networkName = "network";
        String interfaceName = "iface";
        String privateIp = "10.0.0.1";
        String privateIpId = "privateIpId";
        String publicIp = "8.8.8.8";
        String publicIpId = "publicIpId";
        String interfaceName2 = "iface2";
        String privateIp2 = "10.0.0.2";
        String privateIpId2 = "privateIpId2";

        var networkItem1 = AzureScanNetworkInterfaceItem.newBuilder()
            .setName(networkName).setInterfaceName(interfaceName).setLocation(location).setIsPrimary(true)
            .setIpAddress(privateIp).setName(privateIpId).setPublicIpAddress(AzureScanNetworkInterfaceItem.newBuilder()
                .setIpAddress(publicIp).setName(publicIpId).setLocation(location)
            ).build();
        var networkItem2 = AzureScanNetworkInterfaceItem.newBuilder()
            .setName(interfaceName2).setInterfaceName(interfaceName2).setLocation(location).setIsPrimary(false)
            .setIpAddress(privateIp2).setName(privateIpId2).build();
        var azureScanItem = AzureScanItem.newBuilder()
            .setActiveDiscoveryId(discoveryId).setName(vmName).setResourceGroup(resourceGroup).setLocation(location)
            .setOsName(osName).setOsVersion(osVersion)
            .addNetworkInterfaceItems(networkItem1)
            .addNetworkInterfaceItems(networkItem2).build();
        AzureScanResponse azureScanResponse = AzureScanResponse.newBuilder()
            .addResults(azureScanItem)
            .build();
        ScannerResponse response = ScannerResponse.newBuilder()
            .setResult(Any.pack(azureScanResponse))
            .build();

        AzureInterface azureInterface1 = new AzureInterface();
        azureInterface1.setId(111L);
        AzureInterface azureInterface2 = new AzureInterface();
        azureInterface2.setId(222L);

        when(azureInterfaceService.createOrUpdateFromScanResult(eq(TENANT_ID), argThat(prepareObjectMatcher(node)),
            argThat(prepareObjectMatcher(networkItem1))))
            .thenReturn(azureInterface1);
        when(azureInterfaceService.createOrUpdateFromScanResult(eq(TENANT_ID), argThat(prepareObjectMatcher(node)),
            argThat(prepareObjectMatcher(networkItem2))))
            .thenReturn(azureInterface2);

        // execute
        scannerResponseService.accept(TENANT_ID, locationId, response);

        // check
        verify(ipInterfaceService, times(1)).createFromAzureScanResult(eq(TENANT_ID), argThat(prepareObjectMatcher(node)),
            argThat(prepareObjectMatcher(azureInterface1)), argThat(prepareObjectMatcher(networkItem1)));
        verify(ipInterfaceService, times(1)).createFromAzureScanResult(eq(TENANT_ID), argThat(prepareObjectMatcher(node)),
            argThat(prepareObjectMatcher(azureInterface2)), argThat(prepareObjectMatcher(networkItem2)));
        verify(taskSetHandler, times(1)).sendAzureMonitorTasks(argThat(prepareObjectMatcher(discovery)),
            eq(azureScanItem), eq(nodeId));
        verify(taskSetHandler, times(1)).sendAzureCollectorTasks(argThat(prepareObjectMatcher(discovery)),
            eq(azureScanItem), eq(nodeId));
    }

    private <T> ArgumentMatcher<T> prepareObjectMatcher(T expected) {
        return argument -> Objects.equal(expected, argument);
    }
}
