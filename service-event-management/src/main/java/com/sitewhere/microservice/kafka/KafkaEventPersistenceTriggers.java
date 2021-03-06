/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.microservice.kafka;

import java.util.List;
import java.util.UUID;

import com.sitewhere.event.DeviceEventManagementDecorator;
import com.sitewhere.event.processing.OutboundPayloadEnrichmentLogic;
import com.sitewhere.event.spi.microservice.IEventManagementTenantEngine;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.device.event.IDeviceAlert;
import com.sitewhere.spi.device.event.IDeviceCommandInvocation;
import com.sitewhere.spi.device.event.IDeviceCommandResponse;
import com.sitewhere.spi.device.event.IDeviceEvent;
import com.sitewhere.spi.device.event.IDeviceEventManagement;
import com.sitewhere.spi.device.event.IDeviceLocation;
import com.sitewhere.spi.device.event.IDeviceMeasurement;
import com.sitewhere.spi.device.event.IDeviceStateChange;
import com.sitewhere.spi.device.event.request.IDeviceAlertCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceCommandInvocationCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceCommandResponseCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceLocationCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceMeasurementCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceStateChangeCreateRequest;

/**
 * Adds triggers to event persistence methods to push the new events into a
 * Kafka topic.
 * 
 * @author Derek
 */
public class KafkaEventPersistenceTriggers extends DeviceEventManagementDecorator {

    public KafkaEventPersistenceTriggers(IEventManagementTenantEngine tenantEngine, IDeviceEventManagement delegate) {
	super(delegate);
    }

    /**
     * Forward the given event to the Kafka persisted events topic.
     * 
     * @param deviceAssignmentId
     * @param events
     * @return
     * @throws SiteWhereException
     */
    protected <T extends IDeviceEvent> List<T> forwardEvents(UUID deviceAssignmentId, List<T> events)
	    throws SiteWhereException {
	getLogger().debug(String.format("Forwarding %d events to outbound topic.", events.size()));
	for (T event : events) {
	    OutboundPayloadEnrichmentLogic.enrichAndDeliver(getEventManagementTenantEngine(), event);
	}
	return events;
    }

    /*
     * @see
     * com.sitewhere.event.DeviceEventManagementDecorator#addDeviceMeasurements(java
     * .util.UUID,
     * com.sitewhere.spi.device.event.request.IDeviceMeasurementCreateRequest[])
     */
    @Override
    public List<IDeviceMeasurement> addDeviceMeasurements(UUID deviceAssignmentId,
	    IDeviceMeasurementCreateRequest... measurements) throws SiteWhereException {
	return forwardEvents(deviceAssignmentId, super.addDeviceMeasurements(deviceAssignmentId, measurements));
    }

    /*
     * @see
     * com.sitewhere.event.DeviceEventManagementDecorator#addDeviceLocations(java.
     * util.UUID,
     * com.sitewhere.spi.device.event.request.IDeviceLocationCreateRequest[])
     */
    @Override
    public List<IDeviceLocation> addDeviceLocations(UUID deviceAssignmentId, IDeviceLocationCreateRequest... request)
	    throws SiteWhereException {
	return forwardEvents(deviceAssignmentId, super.addDeviceLocations(deviceAssignmentId, request));
    }

    /*
     * @see
     * com.sitewhere.event.DeviceEventManagementDecorator#addDeviceAlerts(java.util.
     * UUID, com.sitewhere.spi.device.event.request.IDeviceAlertCreateRequest[])
     */
    @Override
    public List<IDeviceAlert> addDeviceAlerts(UUID deviceAssignmentId, IDeviceAlertCreateRequest... request)
	    throws SiteWhereException {
	return forwardEvents(deviceAssignmentId, super.addDeviceAlerts(deviceAssignmentId, request));
    }

    /*
     * @see com.sitewhere.event.DeviceEventManagementDecorator#
     * addDeviceCommandInvocations(java.util.UUID,
     * com.sitewhere.spi.device.event.request.IDeviceCommandInvocationCreateRequest[
     * ])
     */
    @Override
    public List<IDeviceCommandInvocation> addDeviceCommandInvocations(UUID deviceAssignmentId,
	    IDeviceCommandInvocationCreateRequest... request) throws SiteWhereException {
	return forwardEvents(deviceAssignmentId, super.addDeviceCommandInvocations(deviceAssignmentId, request));
    }

    /*
     * @see
     * com.sitewhere.event.DeviceEventManagementDecorator#addDeviceCommandResponses(
     * java.util.UUID,
     * com.sitewhere.spi.device.event.request.IDeviceCommandResponseCreateRequest[])
     */
    @Override
    public List<IDeviceCommandResponse> addDeviceCommandResponses(UUID deviceAssignmentId,
	    IDeviceCommandResponseCreateRequest... request) throws SiteWhereException {
	return forwardEvents(deviceAssignmentId, super.addDeviceCommandResponses(deviceAssignmentId, request));
    }

    /*
     * @see
     * com.sitewhere.event.DeviceEventManagementDecorator#addDeviceStateChanges(java
     * .util.UUID,
     * com.sitewhere.spi.device.event.request.IDeviceStateChangeCreateRequest[])
     */
    @Override
    public List<IDeviceStateChange> addDeviceStateChanges(UUID deviceAssignmentId,
	    IDeviceStateChangeCreateRequest... request) throws SiteWhereException {
	return forwardEvents(deviceAssignmentId, super.addDeviceStateChanges(deviceAssignmentId, request));
    }

    protected IEventManagementTenantEngine getEventManagementTenantEngine() {
	return (IEventManagementTenantEngine) getTenantEngine();
    }
}