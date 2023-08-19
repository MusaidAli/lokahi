package org.opennms.miniongateway.grpc.server;

import org.apache.ignite.Ignite;
import org.opennms.horizon.shared.grpc.common.LocationServerInterceptor;
import org.opennms.horizon.shared.grpc.common.TenantIDGrpcServerInterceptor;
import org.opennms.miniongateway.grpc.twin.GrpcTwinPublisher;
import org.opennms.miniongateway.grpc.twin.TwinRpcHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcTwinPublisherConfig {

    @Value("${debug.span.full.message:false}")
    private boolean debugSpanFullMessage;

    @Value("${debug.span.content:false}")
    private boolean debugSpanContent;

    @Bean
    public ServerHandler serverHandler(
        GrpcTwinPublisher grpcTwinPublisher,
        TenantIDGrpcServerInterceptor tenantIDGrpcServerInterceptor,
        LocationServerInterceptor locationServerInterceptor
    ) {
        return new TwinRpcHandler(grpcTwinPublisher, tenantIDGrpcServerInterceptor, locationServerInterceptor);
    }

    @Bean(initMethod = "start", destroyMethod = "close")
    public GrpcTwinPublisher grpcTwinPublisher(Ignite ignite) {
        return new GrpcTwinPublisher(ignite, debugSpanFullMessage, debugSpanContent);
    }

}
