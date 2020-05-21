/**
 * We 'require' modules and 'export' packages. (In this case we are exporting nothing)
 *
 * We need ('require') the ttl.jdk9.demo.service module.
 *
 * We also need to load up the ttl.jdk9.demo.service.CCService.  This will only work
 * if the ttl.jdk9.demo.service module 'provides' that service
 */
module ttl.jdk9.demo.client {
	requires ttl.jdk9.demo.service;
	
	uses ttl.jdk9.demo.service.CCService;
}