/**
 * This service exports (makes publicly available) the ttl.jdk9.demo.service package.
 *
 * Note that modules 'require' other modules but 'export' packages.  It is just a coincidence
 * that our module name is the same as the exported package name.
 *
 * We also say that we 'provide' the ttl.jdk9.demo.service.CCService, which we implement
 * with the ttl.jdk9.demo.service.impl.CCServiceImpl class.
 *
 * The 'provides' declaration allows the client to say that it is
 * 'uses' this particular service.  The magic is happening using the
 * JVM ServiceLoader mechanism
 *
 */
module ttl.jdk9.demo.service {
	exports ttl.jdk9.demo.service;
	
	provides ttl.jdk9.demo.service.CCService with ttl.jdk9.demo.service.impl.CCServiceImpl;
}