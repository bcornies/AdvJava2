package ttl.jdk9.demo.client;


import ttl.jdk9.demo.service.CCService;

import java.util.ServiceLoader;

/**
 * To run from command line from 'target' directory:
 * java --module-path classes:lib/SimpleModuleService-0.0.1-SNAPSHOT.jar --module ttl.jdk9.demo.client/ttl.jdk9.demo.client.CCClient
 * or
 * java --module-path SimpleModuleClient-0.0.1-SNAPSHOT.jar:lib/SimpleModuleService-0.0.1-SNAPSHOT.jar --module ttl.jdk9.demo.client/ttl.jdk9.demo.client.CCClient
 */

public class CCClient {

    public static void main(String[] args) {
            ServiceLoader<CCService> services = ServiceLoader.load(CCService.class);
            CCService fc = services
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No service of type CCService was found"));
            System.out.println("id 1 has credit:" + fc.hasCredit("1"));
    }
}
