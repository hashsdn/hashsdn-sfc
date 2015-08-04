/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.sfc.provider.api;


import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.sfc.provider.AbstractDataStoreManager;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.ServiceFunctionsState;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.function.entry.SfDataPlaneLocator;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.functions.ServiceFunction;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.functions.ServiceFunctionBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.functions.ServiceFunctionKey;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.functions.state.ServiceFunctionState;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.functions.state.ServiceFunctionStateBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.functions.state.ServiceFunctionStateKey;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfc.rev140701.service.function.chain.grouping.ServiceFunctionChain;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfc.rev140701.service.function.chain.grouping.ServiceFunctionChainBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfc.rev140701.service.function.chain.grouping.ServiceFunctionChainKey;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfc.rev140701.service.function.chain.grouping.service.function.chain.SfcServiceFunction;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfc.rev140701.service.function.chain.grouping.service.function.chain.SfcServiceFunctionBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfc.rev140701.service.function.chain.grouping.service.function.chain.SfcServiceFunctionKey;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfp.rev140701.ServiceFunctionPaths;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfp.rev140701.ServiceFunctionPathsBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfp.rev140701.service.function.paths.ServiceFunctionPath;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfp.rev140701.service.function.paths.ServiceFunctionPathBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfp.rev140701.service.function.paths.ServiceFunctionPathKey;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfp.rev140701.service.function.paths.service.function.path.ServicePathHop;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sft.rev140701.Firewall;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sl.rev140701.VxlanGpe;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sl.rev140701.data.plane.locator.locator.type.Ip;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class SfcProviderServicePathAPITest extends AbstractDataStoreManager {

    private List<ServiceFunction> sfList = new ArrayList<>();

    @Before
    public void before() {
        setOdlSfc();

        Ip dummyIp = SimpleTestEntityBuilder.buildLocatorTypeIp(new IpAddress(new Ipv4Address("5.5.5.5")), 555);
        SfDataPlaneLocator dummyLocator = SimpleTestEntityBuilder.buildSfDataPlaneLocator("moscow-5.5.5.5:555-vxlan", dummyIp, "sff-moscow", VxlanGpe.class);

        sfList.add(SimpleTestEntityBuilder.buildServiceFunction("simple_fw_101", Firewall.class,
                new IpAddress(new Ipv4Address("192.168.100.101")), dummyLocator, Boolean.FALSE));
        sfList.add(SimpleTestEntityBuilder.buildServiceFunction("simple_fw_102", Firewall.class,
                new IpAddress(new Ipv4Address("192.168.100.102")), dummyLocator, Boolean.FALSE));
        sfList.add(SimpleTestEntityBuilder.buildServiceFunction("simple_fw_103", Firewall.class,
                new IpAddress(new Ipv4Address("192.168.100.103")), dummyLocator, Boolean.FALSE));
    }

    @Test
    public void testCreatePathWithNamesOnly() throws ExecutionException, InterruptedException {

        String sfcName = "unittest-sfp-chain-1";
        ServiceFunctionChainKey sfcKey = new ServiceFunctionChainKey(sfcName);

        String pathName = "unittest-sfp-path-1";
        ServiceFunctionPathKey pathKey = new ServiceFunctionPathKey(pathName);

        List<SfcServiceFunction> sfcServiceFunctionList = new ArrayList<>();

        for (ServiceFunction serviceFunction : sfList) {

            SfcServiceFunctionBuilder sfcServiceFunctionBuilder = new SfcServiceFunctionBuilder();
            SfcServiceFunction sfcServiceFunction =
                    sfcServiceFunctionBuilder.setName(serviceFunction.getName())
                            .setKey(new SfcServiceFunctionKey(serviceFunction.getName()))
                            .setType(serviceFunction.getType())
                            .build();
            sfcServiceFunctionList.add(sfcServiceFunction);
        }
        ServiceFunctionChainBuilder sfcBuilder = new ServiceFunctionChainBuilder();
        sfcBuilder.setName(sfcName).setKey(sfcKey)
                .setSfcServiceFunction(sfcServiceFunctionList)
                .setSymmetric(false);

        Object[] sfcParameters = {sfcBuilder.build()};
        Class[] sfcParameterTypes = {ServiceFunctionChain.class};

        executor.submit(SfcProviderServiceChainAPI
                .getPut(sfcParameters, sfcParameterTypes)).get();

        ServiceFunctionPathBuilder pathBuilder = new ServiceFunctionPathBuilder();
        pathBuilder.setName(pathName).setKey(pathKey)
                .setServiceChainName(sfcName);

        ServiceFunctionPath path = pathBuilder.build();

        Object[] pathParameters = {path};
        Class[] pathParameterTypes = {ServiceFunctionPath.class};

        Object result1 = executor.submit(SfcProviderServicePathAPI
                .getPut(pathParameters, pathParameterTypes)).get();
        boolean ret = (boolean) result1;

        Object[] pathParameters2 = {pathName};
        Class[] pathParameterTypes2 = {String.class};
        Object result2 = executor.submit(SfcProviderServicePathAPI
                .getRead(pathParameters2, pathParameterTypes2)).get();

        ServiceFunctionPath path2 = (ServiceFunctionPath) result2;

        assertNotNull("Must be not null", path2);
        assertEquals("Must be equal", path2.getServiceChainName(), sfcName);
    }

    @Test
    public void testReadAllServiceFunctionPaths() throws Exception {
        ServiceFunctionPaths serviceFunctionPaths;
        ServiceFunctionPathBuilder serviceFunctionPathBuilder = new ServiceFunctionPathBuilder();
        ServiceFunctionPathsBuilder serviceFunctionPathsBuilder = new ServiceFunctionPathsBuilder();
        String[] testValues = {"1", "Test Name", "Sfp Key"};

        List<ServiceFunctionPath> sffList = new ArrayList<>();

        //set all tested attributes
        serviceFunctionPathBuilder.setPathId(Long.valueOf(testValues[0]))
                .setServiceChainName(testValues[1])
                .setKey(new ServiceFunctionPathKey(testValues[2]))
                .setTransportType(VxlanGpe.class);
        sffList.add(serviceFunctionPathBuilder.build());

        serviceFunctionPathsBuilder.setServiceFunctionPath(sffList);
        InstanceIdentifier<ServiceFunctionPaths> sfpsIID = InstanceIdentifier.
                builder(ServiceFunctionPaths.class).build();
        SfcDataStoreAPI.writePutTransactionAPI(sfpsIID, serviceFunctionPathsBuilder.build(), LogicalDatastoreType.CONFIGURATION);
        serviceFunctionPaths = SfcProviderServicePathAPI.readAllServiceFunctionPaths();

        assertNotNull("Must not be null", serviceFunctionPaths);
        assertFalse("Must be false", serviceFunctionPaths.getServiceFunctionPath().isEmpty());
        assertEquals("Must be equal", serviceFunctionPaths.getServiceFunctionPath().get(0).getPathId(), Long.valueOf(testValues[0]));
        assertEquals("Must be equal", serviceFunctionPaths.getServiceFunctionPath().get(0).getServiceChainName(), testValues[1]);
        assertEquals("Must be equal", serviceFunctionPaths.getServiceFunctionPath().get(0).getKey().getName(), testValues[2]);
        assertEquals("Must be equal", serviceFunctionPaths.getServiceFunctionPath().get(0).getTransportType(), VxlanGpe.class);
    }

    @Test
    public void testIsDefaultServicePath() {

        //add path hop list
        ServiceFunctionPathBuilder serviceFunctionPathBuilder = new ServiceFunctionPathBuilder();
        serviceFunctionPathBuilder.setServicePathHop(new ArrayList<ServicePathHop>());
        boolean result = SfcProviderServicePathAPI.isDefaultServicePath(serviceFunctionPathBuilder.build());
        assertFalse("Must be false", result);

        //set transport type
        serviceFunctionPathBuilder = new ServiceFunctionPathBuilder();
        serviceFunctionPathBuilder.setTransportType(VxlanGpe.class);
        result = SfcProviderServicePathAPI.isDefaultServicePath(serviceFunctionPathBuilder.build());
        assertFalse("Must be false", result);

        //set starting index
        serviceFunctionPathBuilder = new ServiceFunctionPathBuilder();
        serviceFunctionPathBuilder.setStartingIndex((short) 255);
        result = SfcProviderServicePathAPI.isDefaultServicePath(serviceFunctionPathBuilder.build());
        assertFalse("Must be false", result);

        //set path id
        serviceFunctionPathBuilder = new ServiceFunctionPathBuilder();
        serviceFunctionPathBuilder.setPathId(1L);
        result = SfcProviderServicePathAPI.isDefaultServicePath(serviceFunctionPathBuilder.build());
        assertFalse("Must be false", result);

        //nothing is set
        serviceFunctionPathBuilder = new ServiceFunctionPathBuilder();
        result = SfcProviderServicePathAPI.isDefaultServicePath(serviceFunctionPathBuilder.build());
        assertTrue("Must be true", result);
    }

    @Test
    public void testDeleteServiceFunctionPath() {
        ServiceFunctionPathBuilder serviceFunctionPathBuilder = new ServiceFunctionPathBuilder();
        serviceFunctionPathBuilder.setName("SFP1").setKey(new ServiceFunctionPathKey("SFP1"));
        ServiceFunctionPath serviceFunctionPath = serviceFunctionPathBuilder.build();

        ServiceFunctionPathKey serviceFunctionPathKey = new ServiceFunctionPathKey("SFP1");
        InstanceIdentifier<ServiceFunctionPath> sfpEntryIID = InstanceIdentifier.builder(ServiceFunctionPaths.class)
                .child(ServiceFunctionPath.class, serviceFunctionPathKey).build();

        SfcDataStoreAPI.writePutTransactionAPI(sfpEntryIID, serviceFunctionPath, LogicalDatastoreType.CONFIGURATION);
        assertTrue(SfcProviderServicePathAPI.deleteServiceFunctionPath("SFP1"));
    }

    @Test
    public void testPutAllServiceFunctionPaths() {
        ServiceFunctionPathBuilder serviceFunctionPathBuilder = new ServiceFunctionPathBuilder();
        serviceFunctionPathBuilder.setName("SFP1").setKey(new ServiceFunctionPathKey("SFP1"));
        ServiceFunctionPath serviceFunctionPath = serviceFunctionPathBuilder.build();

        ServiceFunctionPathsBuilder serviceFunctionPathsBuilder = new ServiceFunctionPathsBuilder();
        List<ServiceFunctionPath> serviceFunctionPathList = new ArrayList<>();
        serviceFunctionPathList.add(serviceFunctionPath);
        serviceFunctionPathsBuilder.setServiceFunctionPath(serviceFunctionPathList);

        ServiceFunctionPaths serviceFunctionPaths = serviceFunctionPathsBuilder.build();

        Object[] params = {"hello"};
        Class[] paramsTypes = {String.class};
        SfcProviderServicePathAPILocal sfcProviderServicePathAPILocal = new SfcProviderServicePathAPILocal(params, paramsTypes);
        assertTrue(sfcProviderServicePathAPILocal.putAllServiceFunctionPaths(serviceFunctionPaths));
    }

    // method checkServiceFunctionPathExecutor runs non-existing method, probably will be removed
//    @Test
//    public void testCheckServiceFunctionPathExecutor(){
//        ServiceFunctionPathBuilder serviceFunctionPathBuilder = new ServiceFunctionPathBuilder();
//        serviceFunctionPathBuilder.setName("SFP1").setKey(new ServiceFunctionPathKey("SFP1"));
//        ServiceFunctionPath serviceFunctionPath = serviceFunctionPathBuilder.build();
//
//        ServiceFunctionPathKey serviceFunctionPathKey = new ServiceFunctionPathKey("SFP1");
//        InstanceIdentifier<ServiceFunctionPath> sfpEntryIID = InstanceIdentifier.builder(ServiceFunctionPaths.class)
//                .child(ServiceFunctionPath.class, serviceFunctionPathKey).toInstance();
//
//        SfcDataStoreAPI.writePutTransactionAPI(sfpEntryIID, serviceFunctionPath, LogicalDatastoreType.CONFIGURATION);
//
//        Class transportType = Mac.class;
//        RenderedServicePathBuilder renderedServicePathBuilder = new RenderedServicePathBuilder();
//        renderedServicePathBuilder.setName("SFP1").setKey(new RenderedServicePathKey("SFP1")).setStartingIndex((short)5).setPathId((long)236)
//                .setTransportType(transportType);
//        RenderedServicePath renderedServicePath = renderedServicePathBuilder.build();
//        RenderedServicePathKey renderedServicePathKey = new RenderedServicePathKey("SFP1");
//        InstanceIdentifier<RenderedServicePath> rspIID =
//                InstanceIdentifier.builder(RenderedServicePaths.class)
//                        .child(RenderedServicePath.class, renderedServicePathKey)
//                        .build();
//        SfcDataStoreAPI.writePutTransactionAPI(rspIID, renderedServicePath, LogicalDatastoreType.OPERATIONAL);
//
//        assertTrue(SfcProviderServicePathAPI.checkServiceFunctionPathExecutor(serviceFunctionPath, "POST"));
//    }
//    @Test
//    public void testCheckServiceFunctionPathExecutorRsp(){
//        Class transportType = Mac.class;
//        RenderedServicePathBuilder renderedServicePathBuilder = new RenderedServicePathBuilder();
//        renderedServicePathBuilder.setName("SFP1").setKey(new RenderedServicePathKey("SFP1")).setStartingIndex((short)5).setPathId((long)236)
//                .setTransportType(transportType);
//        RenderedServicePath renderedServicePath = renderedServicePathBuilder.build();
//        RenderedServicePathKey renderedServicePathKey = new RenderedServicePathKey("SFP1");
//        InstanceIdentifier<RenderedServicePath> rspIID =
//                InstanceIdentifier.builder(RenderedServicePaths.class)
//                        .child(RenderedServicePath.class, renderedServicePathKey)
//                        .build();
//        SfcDataStoreAPI.writePutTransactionAPI(rspIID, renderedServicePath, LogicalDatastoreType.OPERATIONAL);
//
//        assertTrue(SfcProviderServicePathAPI.checkServiceFunctionPathExecutor(renderedServicePath, "POST"));
//    }

    @Test
    public void testDeleteServicePathContainingFunction() {
        ServiceFunctionBuilder serviceFunctionBuilder = new ServiceFunctionBuilder();
        serviceFunctionBuilder.setName("SF1").setKey(new ServiceFunctionKey("SF1"));
        ServiceFunction serviceFunction = serviceFunctionBuilder.build();

        ServiceFunctionStateBuilder serviceFunctionStateBuilder = new ServiceFunctionStateBuilder();
        serviceFunctionStateBuilder.setName("SF1").setKey(new ServiceFunctionStateKey("SF1"));
        ServiceFunctionState serviceFunctionState = serviceFunctionStateBuilder.build();

        ServiceFunctionStateKey serviceFunctionStateKey =
                new ServiceFunctionStateKey("SF1");
        InstanceIdentifier<ServiceFunctionState> sfStateIID =
                InstanceIdentifier.builder(ServiceFunctionsState.class)
                        .child(ServiceFunctionState.class, serviceFunctionStateKey)
                        .build();
        SfcDataStoreAPI.writePutTransactionAPI(sfStateIID, serviceFunctionState, LogicalDatastoreType.OPERATIONAL);

        Object[] params = {"hello"};
        Class[] paramsTypes = {String.class};
        SfcProviderServicePathAPILocal sfcProviderServicePathAPILocal = new SfcProviderServicePathAPILocal(params, paramsTypes);
        assertTrue(sfcProviderServicePathAPILocal.deleteServicePathContainingFunction(serviceFunction));
    }

    @Test
    public void testDeleteServicePathContainingFunctionExecutor() {
        ServiceFunctionBuilder serviceFunctionBuilder = new ServiceFunctionBuilder();
        serviceFunctionBuilder.setName("SF1").setKey(new ServiceFunctionKey("SF1"));
        ServiceFunction serviceFunction = serviceFunctionBuilder.build();

        ServiceFunctionStateBuilder serviceFunctionStateBuilder = new ServiceFunctionStateBuilder();
        serviceFunctionStateBuilder.setName("SF1").setKey(new ServiceFunctionStateKey("SF1"));
        ServiceFunctionState serviceFunctionState = serviceFunctionStateBuilder.build();

        ServiceFunctionStateKey serviceFunctionStateKey =
                new ServiceFunctionStateKey("SF1");
        InstanceIdentifier<ServiceFunctionState> sfStateIID =
                InstanceIdentifier.builder(ServiceFunctionsState.class)
                        .child(ServiceFunctionState.class, serviceFunctionStateKey)
                        .build();
        SfcDataStoreAPI.writePutTransactionAPI(sfStateIID, serviceFunctionState, LogicalDatastoreType.OPERATIONAL);

        assertTrue(SfcProviderServicePathAPI.deleteServicePathContainingFunctionExecutor(serviceFunction));
    }

    private class SfcProviderServicePathAPILocal extends SfcProviderServicePathAPI {

        SfcProviderServicePathAPILocal(Object[] params, Class[] paramsTypes) {
            super(params, paramsTypes, "m");
        }
    }

}
